package lumien.randomthings.network;

import io.netty.buffer.ByteBuf;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.menu.ItemStackInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent when a button is clicked in the Redstone Remote Use GUI
 */
public record RedstoneRemotePayload(InteractionHand hand, int slotId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RedstoneRemotePayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "redstone_remote"));

    public static final StreamCodec<ByteBuf, RedstoneRemotePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal),
        RedstoneRemotePayload::hand,
        ByteBufCodecs.VAR_INT,
        RedstoneRemotePayload::slotId,
        RedstoneRemotePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RedstoneRemotePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer player) {
                handleServer(payload, player);
            }
        });
    }

    private static void handleServer(RedstoneRemotePayload payload, ServerPlayer player) {
        if (payload.slotId < 0 || payload.slotId >= 9) {
            return;
        }

        ItemStack remoteStack = player.getItemInHand(payload.hand);
        if (remoteStack.isEmpty() || !remoteStack.is(ModItems.REDSTONE_REMOTE.get())) {
            return;
        }

        // Access the remote's inventory
        ItemStackInventory inventory = new ItemStackInventory(remoteStack, 18);
        ItemStack positionFilter = inventory.getItem(payload.slotId);

        if (!positionFilter.isEmpty() && positionFilter.is(ModItems.POSITION_FILTER.get())) {
            BlockPos targetPos = PositionFilterItem.getPosition(positionFilter);
            String dimension = PositionFilterItem.getDimension(positionFilter);

            if (targetPos != null && dimension != null) {
                // Get the target level
                ServerLevel targetLevel = player.getServer().getLevel(
                    player.level().dimension()); // TODO: Properly resolve dimension from string

                if (targetLevel != null) {
                    // Send 20-tick redstone pulse with strength 15
                    RedstoneSignalHandler handler = RedstoneSignalHandler.get(player.getServer());
                    handler.addSignal(targetLevel, targetPos, 20, 15);
                }
            }
        }
    }
}
