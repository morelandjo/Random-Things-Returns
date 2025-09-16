package lumien.randomthings.network;

import lumien.randomthings.item.EnderLetterItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EnderLetterUpdatePacket(String receiverName) implements IRTMessage {
    public static final Type<EnderLetterUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "ender_letter_update"));

    public static final StreamCodec<FriendlyByteBuf, EnderLetterUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, EnderLetterUpdatePacket::receiverName,
        EnderLetterUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        handle(this, context);
    }

    public static void handle(EnderLetterUpdatePacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Update the receiver name on the letter in the player's main hand
                ItemStack heldItem = serverPlayer.getMainHandItem();
                if (heldItem.getItem() instanceof EnderLetterItem) {
                    EnderLetterItem.setReceiver(heldItem, msg.receiverName);
                }
            }
        });
    }
}