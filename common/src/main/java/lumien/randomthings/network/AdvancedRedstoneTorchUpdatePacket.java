package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.AdvancedRedstoneTorchBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.AdvancedRedstoneTorchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Serverbound: Advanced Redstone Torch GUI buttons. action 0/1 = green -/+, 2/3 = red -/+. */
public record AdvancedRedstoneTorchUpdatePacket(BlockPos pos, int action) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "advanced_redstone_torch_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(action);
    }

    public static AdvancedRedstoneTorchUpdatePacket decode(FriendlyByteBuf buf) {
        return new AdvancedRedstoneTorchUpdatePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof AdvancedRedstoneTorchMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof AdvancedRedstoneTorchBlockEntity art)) return;
            switch (action) {
                case 0 -> art.setSignalStrengthGreen(Math.max(0, art.signalStrengthGreen() - 1));
                case 1 -> art.setSignalStrengthGreen(Math.min(15, art.signalStrengthGreen() + 1));
                case 2 -> art.setSignalStrengthRed(Math.max(0, art.signalStrengthRed() - 1));
                case 3 -> art.setSignalStrengthRed(Math.min(15, art.signalStrengthRed() + 1));
            }
        });
    }
}
