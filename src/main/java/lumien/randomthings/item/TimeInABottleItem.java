package lumien.randomthings.item;

import lumien.randomthings.entity.TimeAcceleratorEntity;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class TimeInABottleItem extends Item {
    
    public TimeInABottleItem(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof LivingEntity) {
            // Passive time collection - accumulate time based on config
            int currentTime = getStoredTime(stack);
            int ticksPerSecond = 20; // 20 ticks = 1 second
            
            // Add 20 ticks (1 second) every ticksPerSecond ticks
            if (level.getGameTime() % ticksPerSecond == 0) {
                setStoredTime(stack, currentTime + 20);
            }
        }
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        System.out.println("[TimeInABottle] useOn called at pos: " + pos + ", isClientSide: " + level.isClientSide());
        
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        
        int storedTime = getStoredTime(stack);
        
        // Always requires 30 seconds (600 ticks) like original
        int requiredTime = 20 * 30; // 600 ticks (30 seconds)
        
        System.out.println("[TimeInABottle] storedTime: " + storedTime + ", requiredTime: " + requiredTime + ", creative: " + player.getAbilities().instabuild);
        
        if (storedTime < requiredTime && !player.getAbilities().instabuild) {
            // Not enough time stored
            System.out.println("[TimeInABottle] Insufficient time!");
            player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.insufficient_time")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        
        // Check if there's already a time accelerator at this position
        List<TimeAcceleratorEntity> existingAccelerators = level.getEntitiesOfClass(TimeAcceleratorEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(0.5));
        
        System.out.println("[TimeInABottle] Found " + existingAccelerators.size() + " existing accelerators");
            
        if (!existingAccelerators.isEmpty()) {
            // Upgrade existing accelerator like original implementation
            TimeAcceleratorEntity existing = existingAccelerators.get(0);
            int currentRate = existing.getTimeRate();
            
            // Can we upgrade further? Max rate is 32 (2^5)
            if (currentRate >= 32) {
                player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.max_level")
                    .withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.FAIL;
            }
            
            int nextRate = currentRate * 2;
            int timeRequired = nextRate / 2 * 20 * 30; // Time calculation from original
            
            if (storedTime >= timeRequired || player.getAbilities().instabuild) {
                int usedUpTime = 20 * 30 - existing.getRemainingTime();
                int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;
                
                if (!player.getAbilities().instabuild) {
                    setStoredTime(stack, storedTime - timeRequired);
                }
                
                existing.setTimeRate(nextRate);
                existing.setRemainingTime(existing.getRemainingTime() + timeAdded);
                
                // Play sound based on rate like original
                float pitch = switch (nextRate) {
                    case 2 -> 0.793701F;
                    case 4 -> 0.890899F;
                    case 8 -> 1.059463F;
                    case 16 -> 0.943874F;
                    case 32 -> 0.890899F;
                    default -> 1.0F;
                };
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 0.5F, pitch);
                
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.insufficient_time")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        } else {
            // Create new accelerator starting at 1x rate like original
            System.out.println("[TimeInABottle] Creating new accelerator at " + pos);
            
            if (!player.getAbilities().instabuild) {
                setStoredTime(stack, storedTime - requiredTime);
            }
            
            TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(level, pos, 2); // Start at rate 2 (first visual level)
            accelerator.setRemainingTime(20 * 30); // 30 seconds duration
            System.out.println("[TimeInABottle] Created accelerator: " + accelerator + ", rate: " + accelerator.getTimeRate());
            
            level.addFreshEntity(accelerator);
            System.out.println("[TimeInABottle] Added accelerator to world");
            
            // Play sound for first accelerator (rate 1)
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 0.5F, 0.749154F);
        }
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // Don't cause re-equip animation when only the stored time changes
        // Return false if items are the same type (only data changed)
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int storedTime = getStoredTime(stack);
        
        // Display stored time in hours:minutes:seconds format like original
        int storedSeconds = storedTime / 20;
        int hours = storedSeconds / 3600;
        int minutes = (storedSeconds % 3600) / 60;
        int seconds = storedSeconds % 60;
        
        tooltip.add(Component.translatable("tooltip.timeInABottle", 
            String.format("%02d", hours), 
            String.format("%02d", minutes), 
            String.format("%02d", seconds))
            .withStyle(ChatFormatting.BLUE));
    }
    
    public static int getStoredTime(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_TIME.get(), 0);
    }
    
    public static void setStoredTime(ItemStack stack, int time) {
        stack.set(ModDataComponents.STORED_TIME.get(), Math.max(0, time));
    }
    
    // Remove acceleration level tracking - not needed in original implementation
    
    public static float getFillLevel(ItemStack stack) {
        int storedTime = getStoredTime(stack);
        // Fill level based on stored time, max at ~8 hours like original
        return Math.min(1.0f, storedTime / (20.0f * 60 * 60 * 8)); // Max at 8 hours
    }
}