package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
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
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFilterItem extends Item {

    public EntityFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide) {
            ItemStack heldStack = player.getItemInHand(hand);

            EntityType<?> entityType = target.getType();
            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

            if (entityKey != null) {
                RTNbt.setResourceLocation(heldStack, RTDataKeys.ENTITY_TYPE_FILTER, entityKey);

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("tooltip.randomthings.entity_filter"));

        ResourceLocation entityKey = RTNbt.getResourceLocation(stack, RTDataKeys.ENTITY_TYPE_FILTER);
        if (entityKey != null) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityKey);
            tooltipComponents.add(Component.translatable("tooltip.randomthings.entity_filter.entity", entityType.getDescription()));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.randomthings.entity_filter.invalid"));
        }
    }

    /** Check if an entity matches this filter. */
    public static boolean matchesFilter(ItemStack filterStack, Entity entity) {
        ResourceLocation entityKey = RTNbt.getResourceLocation(filterStack, RTDataKeys.ENTITY_TYPE_FILTER);
        if (entityKey == null) {
            return false;
        }
        EntityType<?> filterType = BuiltInRegistries.ENTITY_TYPE.get(entityKey);
        return filterType.equals(entity.getType());
    }
}
