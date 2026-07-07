package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.AnalogEmitterBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.AnalogEmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Serverbound: set the Analog Emitter's output level (1-15). */
public record AnalogEmitterUpdatePacket(BlockPos pos, int level) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "analog_emitter_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(level);
    }

    public static AnalogEmitterUpdatePacket decode(FriendlyByteBuf buf) {
        return new AnalogEmitterUpdatePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof AnalogEmitterMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof AnalogEmitterBlockEntity emitter) {
                emitter.setEmitLevel(level);
            }
        });
    }
}
