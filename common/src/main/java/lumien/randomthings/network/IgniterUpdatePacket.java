package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.IgniterBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.IgniterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Serverbound: cycle the Igniter's mode (toggle → ignite → keep-ignited). */
public record IgniterUpdatePacket(BlockPos pos) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "igniter_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static IgniterUpdatePacket decode(FriendlyByteBuf buf) {
        return new IgniterUpdatePacket(buf.readBlockPos());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof IgniterMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof IgniterBlockEntity igniter) {
                igniter.rotateMode();
            }
        });
    }
}
