package lumien.randomthings.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The blocks found in a chunk scan (icon + name + count), serialized to NBT / network. */
public record ChunkAnalyzerResult(List<ItemStack> displayStacks, List<String> blockDescriptions, List<Integer> blockCounts) {

    public ChunkAnalyzerResult {
        if (displayStacks == null) {
            displayStacks = new ArrayList<>();
        }
        if (blockDescriptions == null) {
            blockDescriptions = new ArrayList<>();
        }
        if (blockCounts == null) {
            blockCounts = new ArrayList<>();
        }
        if (blockDescriptions.size() != displayStacks.size() || blockCounts.size() != displayStacks.size()) {
            displayStacks = new ArrayList<>();
            blockDescriptions = new ArrayList<>();
            blockCounts = new ArrayList<>();
        }
    }

    public static ChunkAnalyzerResult empty() {
        return new ChunkAnalyzerResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public int size() {
        return displayStacks.size();
    }

    public boolean isEmpty() {
        return displayStacks.isEmpty();
    }

    public CompoundTag toNbt() {
        ListTag list = new ListTag();
        for (int i = 0; i < size(); i++) {
            CompoundTag entry = new CompoundTag();
            entry.put("stack", displayStacks.get(i).save(new CompoundTag()));
            entry.putString("desc", blockDescriptions.get(i));
            entry.putInt("count", blockCounts.get(i));
            list.add(entry);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("entries", list);
        return tag;
    }

    public static ChunkAnalyzerResult fromNbt(CompoundTag tag) {
        List<ItemStack> stacks = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            stacks.add(ItemStack.of(entry.getCompound("stack")));
            descriptions.add(entry.getString("desc"));
            counts.add(entry.getInt("count"));
        }
        return new ChunkAnalyzerResult(stacks, descriptions, counts);
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(size());
        for (int i = 0; i < size(); i++) {
            buf.writeItem(displayStacks.get(i));
            buf.writeUtf(blockDescriptions.get(i));
            buf.writeVarInt(blockCounts.get(i));
        }
    }

    public static ChunkAnalyzerResult fromBuffer(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<ItemStack> stacks = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            stacks.add(buf.readItem());
            descriptions.add(buf.readUtf());
            counts.add(buf.readVarInt());
        }
        return new ChunkAnalyzerResult(stacks, descriptions, counts);
    }
}
