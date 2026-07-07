package lumien.randomthings.item;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import lumien.randomthings.menu.ItemFilterMenu;
import lumien.randomthings.util.ItemFilterData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

    private static final String DATA_KEY = "FilterData";

    public ItemFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new ItemFilterMenuProvider(hand, stack));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("tooltip.randomthings.item_filter"));

        ItemFilterData filterData = getFilterData(stack);
        if (filterData.isWhitelist()) {
            tooltipComponents.add(Component.translatable("tooltip.randomthings.item_filter.whitelist"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.randomthings.item_filter.blacklist"));
        }
    }

    public static ItemFilterData getFilterData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(DATA_KEY)) {
            return ItemFilterData.load(stack.getTag().getCompound(DATA_KEY));
        }
        return ItemFilterData.empty();
    }

    public static void setFilterData(ItemStack stack, ItemFilterData data) {
        stack.getOrCreateTag().put(DATA_KEY, data.save());
    }

    /** Check if an item matches this filter. */
    public static boolean matchesFilter(ItemStack filterStack, ItemStack itemToCheck) {
        ItemFilterData filterData = getFilterData(filterStack);

        boolean matches = false;
        for (ItemStack filterItem : filterData.filterItems()) {
            if (filterItem.isEmpty()) {
                continue;
            }
            if (filterData.checkTags() && hasMatchingTags(filterItem, itemToCheck)) {
                matches = true;
                break;
            }
            if (filterItem.getItem() == itemToCheck.getItem()) {
                boolean metadataMatch = !filterData.checkMetadata() || filterItem.getDamageValue() == itemToCheck.getDamageValue();
                boolean nbtMatch = !filterData.checkNBT() || ItemStack.isSameItemSameTags(filterItem, itemToCheck);
                if (metadataMatch && nbtMatch) {
                    matches = true;
                    break;
                }
            }
        }

        return filterData.isWhitelist() ? matches : !matches;
    }

    private static boolean hasMatchingTags(ItemStack filterItem, ItemStack itemToCheck) {
        for (TagKey<Item> tag : filterItem.getTags().toList()) {
            if (itemToCheck.is(tag)) {
                return true;
            }
        }
        return false;
    }

    /** Opens the filter GUI bound to the held stack; the hand is sent to the client to re-resolve it. */
    private record ItemFilterMenuProvider(InteractionHand hand, ItemStack filterStack) implements ExtendedMenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable("item.randomthings.item_filter");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new ItemFilterMenu(containerId, playerInventory, filterStack);
        }

        @Override
        public void saveExtraData(FriendlyByteBuf buf) {
            buf.writeEnum(hand);
        }
    }
}
