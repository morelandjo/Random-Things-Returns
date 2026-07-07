package lumien.randomthings.item;

import lumien.randomthings.entity.TimeAcceleratorEntity;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/** Passively stores time, then spends it to place a Time Accelerator that speeds up a nearby block. */
public class TimeInABottleItem extends Item {
    private static final int REQUIRED_TIME = 20 * 30; // 30 seconds

    public TimeInABottleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof LivingEntity && level.getGameTime() % 20 == 0) {
            setStoredTime(stack, getStoredTime(stack) + 20);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        int storedTime = getStoredTime(stack);
        boolean creative = player.getAbilities().instabuild;
        if (storedTime < REQUIRED_TIME && !creative) {
            player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.insufficient_time").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        List<TimeAcceleratorEntity> existing = level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos).inflate(0.5));
        if (!existing.isEmpty()) {
            TimeAcceleratorEntity accelerator = existing.get(0);
            int currentRate = accelerator.getTimeRate();
            if (currentRate >= 32) {
                player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.max_level").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.FAIL;
            }
            int nextRate = currentRate * 2;
            int timeRequired = nextRate / 2 * REQUIRED_TIME;
            if (storedTime >= timeRequired || creative) {
                int usedUpTime = REQUIRED_TIME - accelerator.getRemainingTime();
                int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;
                if (!creative) {
                    setStoredTime(stack, storedTime - timeRequired);
                }
                accelerator.setTimeRate(nextRate);
                accelerator.setRemainingTime(accelerator.getRemainingTime() + timeAdded);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 0.5F, pitchForRate(nextRate));
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.insufficient_time").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!creative) {
            setStoredTime(stack, storedTime - REQUIRED_TIME);
        }
        TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(level, pos, 2);
        accelerator.setRemainingTime(REQUIRED_TIME);
        level.addFreshEntity(accelerator);
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 0.5F, 0.749154F);
        return InteractionResult.SUCCESS;
    }

    private static float pitchForRate(int rate) {
        return switch (rate) {
            case 2 -> 0.793701F;
            case 4 -> 0.890899F;
            case 8 -> 1.059463F;
            case 16 -> 0.943874F;
            case 32 -> 0.890899F;
            default -> 1.0F;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int storedSeconds = getStoredTime(stack) / 20;
        int hours = storedSeconds / 3600;
        int minutes = (storedSeconds % 3600) / 60;
        int seconds = storedSeconds % 60;
        tooltip.add(Component.translatable("tooltip.timeInABottle",
            String.format("%02d", hours), String.format("%02d", minutes), String.format("%02d", seconds)).withStyle(ChatFormatting.BLUE));
    }

    public static int getStoredTime(ItemStack stack) {
        return RTNbt.getInt(stack, RTDataKeys.STORED_TIME);
    }

    public static void setStoredTime(ItemStack stack, int time) {
        RTNbt.setInt(stack, RTDataKeys.STORED_TIME, Math.max(0, time));
    }
}
