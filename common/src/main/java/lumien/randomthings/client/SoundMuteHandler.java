package lumien.randomthings.client;

import lumien.randomthings.blockentity.SoundDampenerBlockEntity;
import lumien.randomthings.item.ItemPortableSoundDampener;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.network.RTNetwork;
import lumien.randomthings.network.SoundPlayedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client sound interception (invoked from {@code SoundEngineMixin}): feeds recording Sound Recorders
 * and mutes sounds matched by a Portable Sound Dampener in the inventory or a Sound Dampener block
 * within 20 blocks. Replaces the loader-specific PlaySoundEvent.
 */
@Environment(EnvType.CLIENT)
public final class SoundMuteHandler {
    private static final int DAMPENER_RADIUS_SQ = 20 * 20;

    private SoundMuteHandler() {
    }

    /** Returns true if the sound should be cancelled. */
    public static boolean onPlaySound(SoundInstance sound) {
        if (sound == null) {
            return false;
        }
        ResourceLocation soundLocation = sound.getLocation();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return false;
        }

        // Feed recording Sound Recorders (server applies the actual write).
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() == ModItems.SOUND_RECORDER.get() && SoundRecorderItem.isRecording(stack)) {
                RTNetwork.sendToServer(new SoundPlayedPacket(soundLocation, slot));
            }
        }

        // Portable dampeners anywhere in the inventory.
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemPortableSoundDampener
                && ItemPortableSoundDampener.mutes(stack, soundLocation)) {
                return true;
            }
        }

        // Dampener blocks in range.
        BlockPos playerPos = player.blockPosition();
        synchronized (SoundDampenerBlockEntity.DAMPENERS) {
            for (SoundDampenerBlockEntity dampener : SoundDampenerBlockEntity.DAMPENERS) {
                if (dampener.getLevel() == mc.level
                    && dampener.getBlockPos().distSqr(playerPos) <= DAMPENER_RADIUS_SQ
                    && dampener.getMutedSounds().contains(soundLocation)) {
                    return true;
                }
            }
        }
        return false;
    }
}
