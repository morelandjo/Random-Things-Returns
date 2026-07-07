package lumien.randomthings.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChatEvent;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import lumien.randomthings.blockentity.FlooBrickBlockEntity;
import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import lumien.randomthings.handler.floo.FlooNetworkSavedData;
import lumien.randomthings.item.FlooPouchItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes incoming server chat to all active chat detectors. Detectors register themselves on
 * construction and unregister on removal.
 *
 * <p>Ported from the 1.21.1 NeoForge {@code ServerChatEvent} handler to Architectury's
 * {@link ChatEvent#RECEIVED} (returning {@link EventResult#interruptFalse()} cancels the message).</p>
 *
 * <p>After the detector pass, an unconsumed message is offered to the Floo network: standing on a
 * Floo Brick while holding Floo Powder (or carrying a charged Floo Pouch, or in creative) teleports
 * the player to the fireplace whose name best matches the typed message.</p>
 */
public final class ChatEventHandler {

    public static final Set<ChatDetectorBlockEntity> CHAT_DETECTORS = ConcurrentHashMap.newKeySet();
    public static final Set<GlobalChatDetectorBlockEntity> GLOBAL_CHAT_DETECTORS = ConcurrentHashMap.newKeySet();

    private ChatEventHandler() {
    }

    public static void register() {
        ChatEvent.RECEIVED.register(ChatEventHandler::onServerChat);
    }

    public static void registerChatDetector(ChatDetectorBlockEntity detector) {
        CHAT_DETECTORS.add(detector);
    }

    public static void unregisterChatDetector(ChatDetectorBlockEntity detector) {
        CHAT_DETECTORS.remove(detector);
    }

    public static void registerGlobalChatDetector(GlobalChatDetectorBlockEntity detector) {
        GLOBAL_CHAT_DETECTORS.add(detector);
    }

    public static void unregisterGlobalChatDetector(GlobalChatDetectorBlockEntity detector) {
        GLOBAL_CHAT_DETECTORS.remove(detector);
    }

    private static EventResult onServerChat(@Nullable ServerPlayer player, Component message) {
        if (player == null) {
            return EventResult.pass();
        }

        String text = message.getString();
        UUID uuid = player.getUUID();

        // Drop detectors that have been removed.
        CHAT_DETECTORS.removeIf(ChatDetectorBlockEntity::isRemoved);
        GLOBAL_CHAT_DETECTORS.removeIf(GlobalChatDetectorBlockEntity::isRemoved);

        List<ChatDetectorBlockEntity> chatDetectors = new ArrayList<>(CHAT_DETECTORS);
        List<GlobalChatDetectorBlockEntity> globalChatDetectors = new ArrayList<>(GLOBAL_CHAT_DETECTORS);

        boolean shouldConsume = false;
        for (ChatDetectorBlockEntity detector : chatDetectors) {
            if (detector.shouldConsumeMessage(text, uuid)) {
                shouldConsume = true;
                break;
            }
        }
        if (!shouldConsume) {
            for (GlobalChatDetectorBlockEntity detector : globalChatDetectors) {
                if (detector.shouldConsumeMessage(text, uuid)) {
                    shouldConsume = true;
                    break;
                }
            }
        }

        // Trigger all detectors regardless of whether the message is consumed.
        for (ChatDetectorBlockEntity detector : chatDetectors) {
            detector.onChatMessage(text, uuid);
        }
        for (GlobalChatDetectorBlockEntity detector : globalChatDetectors) {
            detector.onChatMessage(text, uuid);
        }

        if (shouldConsume) {
            return EventResult.interruptFalse();
        }

        // Floo network: a temporary fireplace (Floo Token) the player stands in, then a Floo Brick.
        if (player.level() instanceof ServerLevel serverLevel) {
            if (tryTemporaryFireplace(serverLevel, player, text) || tryFlooTeleport(serverLevel, player, text)) {
                return EventResult.interruptFalse();
            }
        }

        return EventResult.pass();
    }

    /** Temporary fireplace (Floo Token) path — no powder cost, origin is the temporary entity. */
    private static boolean tryTemporaryFireplace(ServerLevel level, ServerPlayer player, String message) {
        net.minecraft.world.phys.AABB area = player.getBoundingBox().inflate(0.5);
        var temps = level.getEntitiesOfClass(lumien.randomthings.entity.TemporaryFlooFireplaceEntity.class, area);
        if (temps.isEmpty()) {
            return false;
        }
        return FlooNetworkSavedData.get(level).teleport(level, null, player, message);
    }

    /**
     * Floo Brick teleport path: the player must stand on a Floo Brick and have a powder source
     * (held Floo Powder, a charged Floo Pouch in the inventory, or creative mode). On success the
     * powder is consumed and the message is suppressed.
     *
     * <p>The temporary-fireplace path (Floo Token) is not ported — see PORTING.md.</p>
     */
    private static boolean tryFlooTeleport(ServerLevel level, ServerPlayer player, String message) {
        BlockPos belowFeet = player.blockPosition().below();
        BlockState below = level.getBlockState(belowFeet);
        if (!below.is(ModBlocks.FLOO_BRICK.get())) {
            return false;
        }

        ItemStack powderStack = null;
        ItemStack pouchStack = null;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (main.is(ModItems.FLOO_POWDER.get())) powderStack = main;
        else if (off.is(ModItems.FLOO_POWDER.get())) powderStack = off;
        if (powderStack == null) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(ModItems.FLOO_POUCH.get()) && FlooPouchItem.getCharge(s) > 0) {
                    pouchStack = s;
                    break;
                }
            }
        }
        boolean creative = player.isCreative();
        if (!creative && powderStack == null && pouchStack == null) {
            return false;
        }

        if (!(level.getBlockEntity(belowFeet) instanceof FlooBrickBlockEntity flooBE)) {
            return false;
        }
        UUID masterUUID = flooBE.getMasterUUID();
        if (masterUUID == null) {
            return false;
        }
        FlooNetworkSavedData data = FlooNetworkSavedData.get(level);
        BlockPos masterPos = data.findMasterPosition(level, masterUUID);
        if (masterPos == null) {
            return false;
        }

        boolean teleported = data.teleport(level, masterPos, player, message);
        if (teleported && !creative) {
            if (powderStack != null) {
                powderStack.shrink(1);
            } else if (pouchStack != null) {
                FlooPouchItem.setCharge(pouchStack, FlooPouchItem.getCharge(pouchStack) - 1);
            }
        }
        return teleported;
    }
}
