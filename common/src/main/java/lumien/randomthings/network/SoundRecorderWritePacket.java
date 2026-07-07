package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.SoundRecorderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Serverbound: write the recorded sound at {@code soundIndex} onto the pattern in the recorder GUI. */
public record SoundRecorderWritePacket(int soundIndex) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "sound_recorder_write");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(soundIndex);
    }

    public static SoundRecorderWritePacket decode(FriendlyByteBuf buf) {
        return new SoundRecorderWritePacket(buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player
                && player.containerMenu instanceof SoundRecorderMenu menu) {
                menu.writeSound(soundIndex);
            }
        });
    }
}
