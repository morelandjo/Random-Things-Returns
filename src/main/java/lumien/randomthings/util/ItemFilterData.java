package lumien.randomthings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ItemFilterData(
    List<ItemStack> filterItems,
    boolean checkMetadata,
    boolean checkNBT,
    boolean checkTags,
    int listType // 0 = whitelist, 1 = blacklist
) {
    // Validation constructor
    public ItemFilterData {
        if (filterItems == null) filterItems = new ArrayList<>();
        // Ensure we have exactly 9 slots
        while (filterItems.size() < 9) {
            filterItems.add(ItemStack.EMPTY);
        }
        if (filterItems.size() > 9) {
            filterItems = new ArrayList<>(filterItems.subList(0, 9));
        }
    }

    public static final Codec<ItemFilterData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(ItemStack.OPTIONAL_CODEC).optionalFieldOf("filterItems", createEmptyList()).forGetter(ItemFilterData::filterItems),
            Codec.BOOL.optionalFieldOf("checkMetadata", true).forGetter(ItemFilterData::checkMetadata),
            Codec.BOOL.optionalFieldOf("checkNBT", true).forGetter(ItemFilterData::checkNBT),
            Codec.BOOL.optionalFieldOf("checkTags", false).forGetter(ItemFilterData::checkTags),
            Codec.INT.optionalFieldOf("listType", 0).forGetter(ItemFilterData::listType)
        ).apply(instance, ItemFilterData::new)
    );

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

    public ItemFilterData withFilterItems(List<ItemStack> filterItems) {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData withCheckMetadata(boolean checkMetadata) {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData withCheckNBT(boolean checkNBT) {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData withCheckTags(boolean checkTags) {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData withListType(int listType) {
        return new ItemFilterData(filterItems, checkMetadata, checkNBT, checkTags, listType);
    }

    public ItemFilterData toggleMetadata() {
        return withCheckMetadata(!checkMetadata);
    }

    public ItemFilterData toggleNBT() {
        return withCheckNBT(!checkNBT);
    }

    public ItemFilterData toggleTags() {
        return withCheckTags(!checkTags);
    }

    public ItemFilterData toggleListType() {
        return withListType(listType == 0 ? 1 : 0);
    }

    public boolean isWhitelist() {
        return listType == 0;
    }

    public boolean isBlacklist() {
        return listType == 1;
    }
}
