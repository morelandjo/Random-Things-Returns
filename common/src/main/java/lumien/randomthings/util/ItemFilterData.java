package lumien.randomthings.util;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Item Filter configuration. Stored on the filter item's NBT (1.20.1 has no Data Components) via
 * {@link #save()} / {@link #load(CompoundTag)} — the 9 filter items are persisted with
 * {@link ContainerHelper}, which handles empty slots cleanly.
 */
public record ItemFilterData(
    List<ItemStack> filterItems,
    boolean checkMetadata,
    boolean checkNBT,
    boolean checkTags,
    int listType // 0 = whitelist, 1 = blacklist
) {
    public ItemFilterData {
        if (filterItems == null) filterItems = new ArrayList<>();
        while (filterItems.size() < 9) {
            filterItems.add(ItemStack.EMPTY);
        }
        if (filterItems.size() > 9) {
            filterItems = new ArrayList<>(filterItems.subList(0, 9));
        }
    }

    private static List<ItemStack> createEmptyList() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            list.add(ItemStack.EMPTY);
        }
        return list;
    }

    public static ItemFilterData empty() {
        return new ItemFilterData(createEmptyList(), true, true, false, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("checkMetadata", checkMetadata);
        tag.putBoolean("checkNBT", checkNBT);
        tag.putBoolean("checkTags", checkTags);
        tag.putInt("listType", listType);
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            items.set(i, filterItems.get(i));
        }
        ContainerHelper.saveAllItems(tag, items, true);
        return tag;
    }

    public static ItemFilterData load(CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        return new ItemFilterData(
            new ArrayList<>(items),
            !tag.contains("checkMetadata") || tag.getBoolean("checkMetadata"),
            !tag.contains("checkNBT") || tag.getBoolean("checkNBT"),
            tag.getBoolean("checkTags"),
            tag.getInt("listType")
        );
    }

    public ItemFilterData withFilterItems(List<ItemStack> filterItems) {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData toggleMetadata() {
        return new ItemFilterData(filterItems, !checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData toggleNBT() {
        return new ItemFilterData(filterItems, checkMetadata, !checkNBT, checkTags, listType);
    }

    public ItemFilterData toggleTags() {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, !checkTags, listType);
    }

    public ItemFilterData toggleListType() {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType == 0 ? 1 : 0);
    }

    public boolean isWhitelist() {
        return listType == 0;
    }

    public boolean isBlacklist() {
        return listType == 1;
    }
}
