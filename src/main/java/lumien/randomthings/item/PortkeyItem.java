package lumien.randomthings.item;

import lumien.randomthings.util.PortkeyTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portkey item - A teleportation device that can be set to a target location.
 * When thrown on the ground and picked up after 5 seconds (100 ticks), it teleports the player
 * to within 2 blocks of the target location.
 */
public class PortkeyItem extends Item {

    public PortkeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Set the target position when right-clicking a block
        if (!context.getLevel().isClientSide) {
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();

            // Store the target location
            PortkeyTarget target = new PortkeyTarget(level.dimension(), pos);
            stack.set(ModDataComponents.PORTKEY_TARGET.get(), target);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // When in inventory, clear the age counter
        // This ensures the portkey only ages when on the ground
        stack.remove(ModDataComponents.PORTKEY_AGE.get());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        // Handle aging when the portkey is on the ground
        Integer age = stack.getOrDefault(ModDataComponents.PORTKEY_AGE.get(), 0);

        if (age == 0) {
            // First tick on ground - set no-despawn
            entity.setUnlimitedLifetime();
        }

        // Increment age counter
        stack.set(ModDataComponents.PORTKEY_AGE.get(), age + 1);

        return false; // Return false to allow normal entity item updates
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Show enchantment glint if:
        // 1. Age is less than 100 ticks (not primed yet), OR
        // 2. No target is set
        Integer age = stack.get(ModDataComponents.PORTKEY_AGE.get());
        PortkeyTarget target = stack.get(ModDataComponents.PORTKEY_TARGET.get());

        if (age != null && age < 100) {
            return true; // Show glint while aging
        }

        if (target == null) {
            return true; // Show glint when no target set
        }

        return false; // No glint when primed and has target
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PortkeyTarget target = stack.get(ModDataComponents.PORTKEY_TARGET.get());

        if (target != null) {
            // TODO: Check if coordinates should be hidden via config
            // For now, always show coordinates
            BlockPos pos = target.pos();
            tooltip.add(Component.translatable("item.randomthings.portkey.x", pos.getX()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.randomthings.portkey.y", pos.getY()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.randomthings.portkey.z", pos.getZ()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.randomthings.portkey.notset").withStyle(ChatFormatting.GRAY));
        }

        // Debug: Show camo info
        net.minecraft.resources.ResourceLocation camoId = stack.get(ModDataComponents.PORTKEY_CAMO.get());
        if (camoId != null) {
            tooltip.add(Component.literal("Camo: " + camoId).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("Camo: NONE").withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);
        PortkeyTarget target = stack.get(ModDataComponents.PORTKEY_TARGET.get());

        if (target != null) {
            // TODO: Check if coordinates should be hidden via config
            // For now, always show coordinates in hover name
            BlockPos pos = target.pos();
            return Component.literal(baseName.getString() + String.format(" (%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()));
        }

        return baseName;
    }
}
