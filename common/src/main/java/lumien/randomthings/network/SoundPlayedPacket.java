package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Serverbound: the client heard a sound while a Sound Recorder in {@code slot} was recording. */
public record SoundPlayedPacket(ResourceLocation sound, int slot) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "sound_played");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(sound);
        buf.writeVarInt(slot);
    }

    public static SoundPlayedPacket decode(FriendlyByteBuf buf) {
        return new SoundPlayedPacket(buf.readResourceLocation(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (slot < 0 || slot >= player.getInventory().getContainerSize()) return;
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof SoundRecorderItem) {
                SoundRecorderItem.recordSound(stack, sound);
            }
        });
    }
}
