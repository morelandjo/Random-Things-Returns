package lumien.randomthings.item;

import lumien.randomthings.menu.SoundRecorderMenu;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shift-click to toggle recording: while recording, every sound the player hears is captured (up to
 * ten). Right-click when not recording to open the GUI and write a captured sound onto a Sound Pattern.
 */
public class SoundRecorderItem extends Item {
    private static final String RECORDING_KEY = "sound_recorder_recording";
    private static final String SOUNDS_KEY = "SoundRecorderSounds";

    public SoundRecorderItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                boolean recording = isRecording(stack);
                RTNbt.setBoolean(stack, RECORDING_KEY, !recording);
                if (!recording && stack.getTag() != null) {
                    stack.getTag().remove(SOUNDS_KEY);
                }
            } else if (!isRecording(stack) && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new SoundRecorderMenu(id, inventory, stack),
                    Component.translatable("container.randomthings.sound_recorder")));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isRecording(stack)) {
            tooltip.add(Component.literal("Recording...").withStyle(ChatFormatting.RED));
        }
        List<ResourceLocation> sounds = getRecordedSounds(stack);
        if (!sounds.isEmpty()) {
            tooltip.add(Component.literal("Recorded sounds: " + sounds.size()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void recordSound(ItemStack stack, ResourceLocation soundName) {
        if (stack.getItem() instanceof SoundRecorderItem && isRecording(stack)) {
            List<ResourceLocation> sounds = getRecordedSounds(stack);
            if (!sounds.contains(soundName)) {
                sounds.add(soundName);
                if (sounds.size() >= 10) {
                    RTNbt.setBoolean(stack, RECORDING_KEY, false);
                }
                ListTag list = new ListTag();
                for (ResourceLocation sound : sounds) {
                    list.add(StringTag.valueOf(sound.toString()));
                }
                stack.getOrCreateTag().put(SOUNDS_KEY, list);
            }
        }
    }

    public static List<ResourceLocation> getRecordedSounds(ItemStack stack) {
        List<ResourceLocation> sounds = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(SOUNDS_KEY, Tag.TAG_LIST)) {
            ListTag list = tag.getList(SOUNDS_KEY, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation loc = ResourceLocation.tryParse(list.getString(i));
                if (loc != null) {
                    sounds.add(loc);
                }
            }
        }
        return sounds;
    }

    public static boolean isRecording(ItemStack stack) {
        return RTNbt.getBoolean(stack, RECORDING_KEY);
    }
}
