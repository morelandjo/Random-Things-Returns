package lumien.randomthings.event;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import lumien.randomthings.blockentity.FlooBrickBlockEntity;
import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import lumien.randomthings.entity.TemporaryFlooFireplaceEntity;
import lumien.randomthings.handler.floo.FlooNetworkSavedData;
import lumien.randomthings.item.FlooPouchItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class ChatEventHandler {
    
    // Static registry to track active chat detectors
    public static final Set<ChatDetectorBlockEntity> CHAT_DETECTORS = ConcurrentHashMap.newKeySet();
    public static final Set<GlobalChatDetectorBlockEntity> GLOBAL_CHAT_DETECTORS = ConcurrentHashMap.newKeySet();
    
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
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();
        
        // Use the static registry - much more efficient
        List<ChatDetectorBlockEntity> chatDetectors = new ArrayList<>(CHAT_DETECTORS);
        List<GlobalChatDetectorBlockEntity> globalChatDetectors = new ArrayList<>(GLOBAL_CHAT_DETECTORS);
        
        // Clean up invalid entries (detectors that have been removed)
        chatDetectors.removeIf(detector -> detector.isRemoved());
        globalChatDetectors.removeIf(detector -> detector.isRemoved());
        CHAT_DETECTORS.removeIf(detector -> detector.isRemoved());
        GLOBAL_CHAT_DETECTORS.removeIf(detector -> detector.isRemoved());
        
        // Check if any detectors want to consume this message
        boolean shouldConsume = false;
        
        // Check chat detectors first
        for (ChatDetectorBlockEntity detector : chatDetectors) {
            if (detector.shouldConsumeMessage(message, player.getUUID())) {
                shouldConsume = true;
                break;
            }
        }
        
        // Check global chat detectors
        if (!shouldConsume) {
            for (GlobalChatDetectorBlockEntity detector : globalChatDetectors) {
                if (detector.shouldConsumeMessage(message, player.getUUID())) {
                    shouldConsume = true;
                    break;
                }
            }
        }
        
        // Cancel the event if any detector wants to consume the message
        if (shouldConsume) {
            event.setCanceled(true);
        }

        // Trigger all detectors regardless of whether message is consumed
        for (ChatDetectorBlockEntity detector : chatDetectors) {
            detector.onChatMessage(message, player.getUUID());
        }

        for (GlobalChatDetectorBlockEntity detector : globalChatDetectors) {
            detector.onChatMessage(message, player.getUUID());
        }

        // Floo teleport (only if chat wasn't already consumed by a detector)
        if (!event.isCanceled()) {
            tryFlooTeleport(event, player, message);
        }
    }

    /**
     * Attempt floo-network teleport. Mirrors upstream RTEventHandler.chatEvent (file:line 738-806).
     *
     * Two valid origins, in priority order:
     *  (1) Inside a TemporaryFlooFireplaceEntity (bbox inflated by 0.5 like upstream) —
     *      destination resolution is the only cost, no powder/pouch needed.
     *  (2) Standing on a Floo Brick (child or master) AND carrying powder/pouch/creative —
     *      origin pos passed to teleport() is the MASTER's position (not the brick under feet),
     *      so the same-destination check works regardless of which brick the player stands on.
     *      Consumes 1 powder (held) or 1 pouch charge on success.
     */
    private static void tryFlooTeleport(ServerChatEvent event, ServerPlayer player, String message) {
        ServerLevel level = (ServerLevel) player.level();
        FlooNetworkSavedData data = FlooNetworkSavedData.get(level);

        // (1) Temporary fireplace path — no powder cost, originPos = null
        AABB tempArea = player.getBoundingBox().inflate(0.5);
        List<TemporaryFlooFireplaceEntity> temps = level.getEntitiesOfClass(TemporaryFlooFireplaceEntity.class, tempArea);
        if (!temps.isEmpty()) {
            boolean teleported = data.teleport(level, null, player, message);
            if (teleported) {
                event.setCanceled(true);
            }
            return;
        }

        // (2) Floo Brick path
        BlockPos belowFeet = player.blockPosition().below();
        BlockState below = level.getBlockState(belowFeet);
        if (!below.is(ModBlocks.FLOO_BRICK.get())) return;

        // Need powder source: held in either hand, OR pouch in inventory, OR creative.
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
        if (!creative && powderStack == null && pouchStack == null) return;

        // Resolve the master's position from the brick we're standing on. Upstream calls
        // te.getFirePlaceUid() then handler.getFirePlaceTE(world, uuid) → masterBrick.getPos().
        BlockEntity be = level.getBlockEntity(belowFeet);
        if (!(be instanceof FlooBrickBlockEntity flooBE)) return;
        UUID masterUUID = flooBE.getMasterUUID();
        if (masterUUID == null) return;
        BlockPos masterPos = data.findMasterPosition(level, masterUUID);
        if (masterPos == null) return;

        boolean teleported = data.teleport(level, masterPos, player, message);
        if (teleported) {
            event.setCanceled(true);
            if (!creative) {
                if (powderStack != null) {
                    powderStack.shrink(1);
                } else if (pouchStack != null) {
                    FlooPouchItem.setCharge(pouchStack, FlooPouchItem.getCharge(pouchStack) - 1);
                }
            }
        }
    }
}