package lumien.randomthings.client.events;

import lumien.randomthings.blockentity.SoundDampenerBlockEntity;
import lumien.randomthings.item.ItemPortableSoundDampener;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.network.SoundPlayedPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.List;

/**
 * Client-side event handler for Sound Dampener functionality.
 * Cancels sounds that match patterns stored in Sound Dampeners.
 */
@OnlyIn(Dist.CLIENT)
public class SoundDampenerClientEvents {

    private static final int DAMPENER_RADIUS = 20;
    private static final int DAMPENER_RADIUS_SQ = DAMPENER_RADIUS * DAMPENER_RADIUS;

    /**
     * Intercepts sound playback and cancels sounds matching any Sound Pattern
     * in either a Portable Sound Dampener in the player's inventory or a
     * Sound Dampener block within 20 blocks.
     *
     * Also records sounds to Sound Recorders that are in recording mode.
     */
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() == null) {
            return;
        }

        ResourceLocation soundLocation = event.getSound().getLocation();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.level == null) {
            return;
        }

        // Check for Sound Recorders in recording mode and send packet to server
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.getItem() == ModItems.SOUND_RECORDER.get()) {
                if (SoundRecorderItem.isRecording(stack)) {
                    // Send packet to server to record this sound
                    PacketDistributor.sendToServer(new SoundPlayedPacket(soundLocation, slot));
                }
            }
        }

        // Check portable dampeners in player inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemPortableSoundDampener) {
                if (shouldCancelSoundFromPortable(stack, soundLocation)) {
                    event.setSound(null);
                    return;
                }
            }
        }

        // Check sound dampener blocks within radius
        BlockPos playerPos = player.blockPosition();

        synchronized (SoundDampenerBlockEntity.DAMPENERS) {
            for (SoundDampenerBlockEntity dampener : SoundDampenerBlockEntity.DAMPENERS) {
                // Check if dampener is in same level
                if (dampener.getLevel() != mc.level) {
                    continue;
                }

                // Check if dampener is within radius
                if (dampener.getBlockPos().distSqr(playerPos) > DAMPENER_RADIUS_SQ) {
                    continue;
                }

                // Check if this dampener has the sound muted
                if (dampener.getMutedSounds().contains(soundLocation)) {
                    event.setSound(null);
                    return;
                }
            }
        }
    }

    /**
     * Checks if a sound should be canceled based on a Portable Sound Dampener's inventory
     */
    private static boolean shouldCancelSoundFromPortable(ItemStack dampenerStack, ResourceLocation soundToCheck) {
        ItemContainerContents contents = dampenerStack.get(ModDataComponents.DAMPENER_INVENTORY.get());

        if (contents == null) {
            return false;
        }

        List<ItemStack> items = contents.stream().toList();

        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.SOUND_PATTERN.get()) {
                ResourceLocation storedSound = ItemSoundPattern.getSoundLocation(stack);

                if (storedSound != null && storedSound.equals(soundToCheck)) {
                    return true;
                }
            }
        }

        return false;
    }
}
