package lumien.randomthings.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EntityFilterItem extends Item {

    public EntityFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide) {
            // Get the actual stack from the player's hand to ensure we modify the right one
            ItemStack heldStack = player.getItemInHand(hand);

            // Get the entity type and store it
            EntityType<?> entityType = target.getType();
            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

            if (entityKey != null) {
                heldStack.set(ModDataComponents.ENTITY_TYPE_FILTER.get(), entityKey);

                // Send feedback to player
                Component entityName = entityType.getDescription();
                player.displayClientMessage(
                    Component.translatable("message.randomthings.entity_filter.set", entityName),
                    true
                );
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("tooltip.randomthings.entity_filter"));

        ResourceLocation entityKey = stack.get(ModDataComponents.ENTITY_TYPE_FILTER.get());
        if (entityKey != null) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityKey);
            if (entityType != null) {
                tooltipComponents.add(Component.translatable("tooltip.randomthings.entity_filter.entity", entityType.getDescription()));
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.randomthings.entity_filter.invalid"));
        }
    }

    /**
     * Check if an entity matches this filter
     */
    public static boolean matchesFilter(ItemStack filterStack, Entity entity) {
        ResourceLocation entityKey = filterStack.get(ModDataComponents.ENTITY_TYPE_FILTER.get());
        if (entityKey == null) {
            return false;
        }

        EntityType<?> filterType = BuiltInRegistries.ENTITY_TYPE.get(entityKey);
        if (filterType == null) {
            return false;
        }

        // Check if the entity is of the same type or a subclass
        return filterType.equals(entity.getType());
    }
}
