package lumien.randomthings.item;

import lumien.randomthings.menu.ModMenuTypes;
import lumien.randomthings.menu.SoundRecorderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SoundRecorderItem extends Item {

    public SoundRecorderItem() {
        super(new Item.Properties()
                .stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Toggle recording mode
                boolean recording = stack.getOrDefault(ModDataComponents.SOUND_RECORDER_RECORDING.get(), false);
                stack.set(ModDataComponents.SOUND_RECORDER_RECORDING.get(), !recording);

                // If starting recording, clear the previous sound list
                if (!recording) {
                    stack.remove(ModDataComponents.SOUND_RECORDER_SOUNDS.get());
                }

                return InteractionResultHolder.success(stack);
            } else {
                // Only open GUI if not recording
                boolean recording = stack.getOrDefault(ModDataComponents.SOUND_RECORDER_RECORDING.get(), false);
                if (!recording) {
                    // Open GUI
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.openMenu(new SimpleMenuProvider(
                            (id, playerInventory, p) -> new SoundRecorderMenu(id, playerInventory),
                            Component.translatable("container.randomthings.sound_recorder")
                        ));
                    }
                }

                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        boolean recording = stack.getOrDefault(ModDataComponents.SOUND_RECORDER_RECORDING.get(), false);

        if (recording) {
            tooltipComponents.add(Component.literal("Recording...").withStyle(net.minecraft.ChatFormatting.RED));
        }

        List<ResourceLocation> sounds = getRecordedSounds(stack);
        if (!sounds.isEmpty()) {
            tooltipComponents.add(Component.literal("Recorded sounds: " + sounds.size()).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    /**
     * Records a sound to the stack if it's in recording mode and not a duplicate
     */
    public static void recordSound(ItemStack stack, ResourceLocation soundName) {
        if (stack.getItem() instanceof SoundRecorderItem) {
            boolean recording = stack.getOrDefault(ModDataComponents.SOUND_RECORDER_RECORDING.get(), false);

            if (recording) {
                List<ResourceLocation> soundList = stack.getOrDefault(
                    ModDataComponents.SOUND_RECORDER_SOUNDS.get(),
                    new ArrayList<>()
                );

                // Make a mutable copy
                List<ResourceLocation> mutableList = new ArrayList<>(soundList);

                // Check for duplicates
                if (!mutableList.contains(soundName)) {
                    mutableList.add(soundName);

                    // Auto-stop recording if we hit 10 sounds
                    if (mutableList.size() >= 10) {
                        stack.set(ModDataComponents.SOUND_RECORDER_RECORDING.get(), false);
                    }

                    // Save the updated list
                    stack.set(ModDataComponents.SOUND_RECORDER_SOUNDS.get(), mutableList);
                }
            }
        }
    }

    /**
     * Gets the list of recorded sounds from the stack
     */
    public static List<ResourceLocation> getRecordedSounds(ItemStack stack) {
        return stack.getOrDefault(
            ModDataComponents.SOUND_RECORDER_SOUNDS.get(),
            new ArrayList<>()
        );
    }

    /**
     * Checks if the recorder is currently in recording mode
     */
    public static boolean isRecording(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SOUND_RECORDER_RECORDING.get(), false);
    }
}
