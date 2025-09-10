package lumien.randomthings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ChunkAnalyzerResult(
    List<ItemStack> displayStacks,
    List<String> blockDescriptions,
    List<Integer> blockCounts
) {
    // Validation constructor to ensure all lists have the same size
    public ChunkAnalyzerResult {
        // Ensure lists are not null and have consistent sizes
        if (displayStacks == null) displayStacks = new ArrayList<>();
        if (blockDescriptions == null) blockDescriptions = new ArrayList<>();
        if (blockCounts == null) blockCounts = new ArrayList<>();
        
        // Ensure all lists have the same size
        int size = displayStacks.size();
        if (blockDescriptions.size() != size || blockCounts.size() != size) {
            // If sizes don't match, create empty lists
            displayStacks = new ArrayList<>();
            blockDescriptions = new ArrayList<>();
            blockCounts = new ArrayList<>();
        }
    }
    
    public static final Codec<ChunkAnalyzerResult> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(ItemStack.CODEC).optionalFieldOf("displayStacks", new ArrayList<>()).forGetter(ChunkAnalyzerResult::displayStacks),
            Codec.list(Codec.STRING).optionalFieldOf("blockDescriptions", new ArrayList<>()).forGetter(ChunkAnalyzerResult::blockDescriptions),
            Codec.list(Codec.INT).optionalFieldOf("blockCounts", new ArrayList<>()).forGetter(ChunkAnalyzerResult::blockCounts)
        ).apply(instance, ChunkAnalyzerResult::new)
    );

    public static ChunkAnalyzerResult empty() {
        return new ChunkAnalyzerResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public static ChunkAnalyzerResult fromLists(List<ItemStack> stacks, List<String> descriptions, List<Integer> counts) {
        return new ChunkAnalyzerResult(new ArrayList<>(stacks), new ArrayList<>(descriptions), new ArrayList<>(counts));
    }

    public ChunkAnalyzerResult addBlock(ItemStack displayStack, String blockDescription, int count) {
        List<ItemStack> newStacks = new ArrayList<>(displayStacks);
        List<String> newDescriptions = new ArrayList<>(blockDescriptions);
        List<Integer> newCounts = new ArrayList<>(blockCounts);
        
        newStacks.add(displayStack);
        newDescriptions.add(blockDescription);
        newCounts.add(count);
        
        return new ChunkAnalyzerResult(newStacks, newDescriptions, newCounts);
    }

    public int size() {
        return displayStacks.size();
    }

    public boolean isEmpty() {
        return displayStacks.isEmpty();
    }
}