package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.IronDropperBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.IronDropperMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Serverbound: cycle an Iron Dropper config option. 0=redstone mode, 1=pickup delay, 2=random motion, 3=effects. */
public record IronDropperUpdatePacket(BlockPos pos, int action) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "iron_dropper_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(action);
    }

    public static IronDropperUpdatePacket decode(FriendlyByteBuf buf) {
        return new IronDropperUpdatePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof IronDropperMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof IronDropperBlockEntity dropper)) return;
            switch (action) {
                case 0 -> dropper.rotateRedstoneMode();
                case 1 -> dropper.rotatePickupDelay();
                case 2 -> dropper.rotateRandomMotion();
                case 3 -> dropper.rotateEffects();
            }
        });
    }
}
