package lumien.randomthings.item;

import lumien.randomthings.menu.ItemFilterMenu;
import lumien.randomthings.util.ItemFilterData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFilterItem extends Item {
    public ItemFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ItemFilterMenuProvider(stack));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("tooltip.randomthings.item_filter"));

        ItemFilterData filterData = getFilterData(stack);
        if (filterData != null) {
            if (filterData.isWhitelist()) {
                tooltipComponents.add(Component.translatable("tooltip.randomthings.item_filter.whitelist"));
            } else {
                tooltipComponents.add(Component.translatable("tooltip.randomthings.item_filter.blacklist"));
            }
        }
    }

    public static ItemFilterData getFilterData(ItemStack stack) {
        ItemFilterData data = stack.get(ModDataComponents.ITEM_FILTER_DATA.get());
        if (data == null) {
            data = ItemFilterData.empty();
            stack.set(ModDataComponents.ITEM_FILTER_DATA.get(), data);
        }
        return data;
    }

    public static void setFilterData(ItemStack stack, ItemFilterData data) {
        stack.set(ModDataComponents.ITEM_FILTER_DATA.get(), data);
    }

    /**
     * Check if an item matches this filter
     */
    public static boolean matchesFilter(ItemStack filterStack, ItemStack itemToCheck) {
        ItemFilterData filterData = getFilterData(filterStack);
        if (filterData == null) {
            return false;
        }

        boolean matches = false;

        for (ItemStack filterItem : filterData.filterItems()) {
            if (filterItem.isEmpty()) {
                continue;
            }

            // Check tags if enabled
            if (filterData.checkTags()) {
                if (hasMatchingTags(filterItem, itemToCheck)) {
                    matches = true;
                    break;
                }
            }

            // Check item type
            if (filterItem.getItem() == itemToCheck.getItem()) {
                boolean metadataMatch = true;
                boolean nbtMatch = true;

                // Check metadata (damage value) if enabled
                if (filterData.checkMetadata()) {
                    metadataMatch = filterItem.getDamageValue() == itemToCheck.getDamageValue();
                }

                // Check NBT if enabled
                if (filterData.checkNBT()) {
                    nbtMatch = ItemStack.isSameItemSameComponents(filterItem, itemToCheck);
                }

                if (metadataMatch && nbtMatch) {
                    matches = true;
                    break;
                }
            }
        }

        // Apply whitelist/blacklist logic
        if (filterData.isWhitelist()) {
            return matches; // Whitelist: allow only if matches
        } else {
            return !matches; // Blacklist: allow only if doesn't match
        }
    }

    /**
     * Check if two items share any common tags
     */
    private static boolean hasMatchingTags(ItemStack filterItem, ItemStack itemToCheck) {
        // Check all tags of the filter item against the item to check
        for (TagKey<Item> tag : filterItem.getTags().toList()) {
            if (itemToCheck.is(tag)) {
                return true;
            }
        }

        return false;
    }

    private static class ItemFilterMenuProvider implements MenuProvider {
        private final ItemStack filterStack;

        public ItemFilterMenuProvider(ItemStack filterStack) {
            this.filterStack = filterStack;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.randomthings.item_filter");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new ItemFilterMenu(containerId, playerInventory, filterStack);
        }
    }
}
