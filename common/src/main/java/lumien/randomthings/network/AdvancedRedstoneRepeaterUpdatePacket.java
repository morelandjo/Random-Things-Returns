package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.AdvancedRedstoneRepeaterBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.AdvancedRedstoneRepeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Serverbound: Advanced Redstone Repeater GUI buttons. {@code action} encodes which delay and step:
 * even = off-delay, odd = on-delay; step grows ×10 every two actions (±1/±10/±100/±1000).
 */
public record AdvancedRedstoneRepeaterUpdatePacket(BlockPos pos, int action) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "advanced_redstone_repeater_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(action);
    }

    public static AdvancedRedstoneRepeaterUpdatePacket decode(FriendlyByteBuf buf) {
        return new AdvancedRedstoneRepeaterUpdatePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof AdvancedRedstoneRepeaterMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof AdvancedRedstoneRepeaterBlockEntity arr)) return;
            switch (action) {
                case 0 -> arr.decreaseTurnOffDelay(1);
                case 1 -> arr.increaseTurnOffDelay(1);
                case 2 -> arr.decreaseTurnOnDelay(1);
                case 3 -> arr.increaseTurnOnDelay(1);
                case 4 -> arr.decreaseTurnOffDelay(10);
                case 5 -> arr.increaseTurnOffDelay(10);
                case 6 -> arr.decreaseTurnOnDelay(10);
                case 7 -> arr.increaseTurnOnDelay(10);
                case 8 -> arr.decreaseTurnOffDelay(100);
                case 9 -> arr.increaseTurnOffDelay(100);
                case 10 -> arr.decreaseTurnOnDelay(100);
                case 11 -> arr.increaseTurnOnDelay(100);
                case 12 -> arr.decreaseTurnOffDelay(1000);
                case 13 -> arr.increaseTurnOffDelay(1000);
                case 14 -> arr.decreaseTurnOnDelay(1000);
                case 15 -> arr.increaseTurnOnDelay(1000);
            }
        });
    }
}
