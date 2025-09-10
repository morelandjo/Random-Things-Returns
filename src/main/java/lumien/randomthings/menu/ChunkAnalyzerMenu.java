package lumien.randomthings.menu;

import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.ChunkAnalyzerResultPacket;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.BlockPos;

import java.util.*;

public class ChunkAnalyzerMenu extends AbstractContainerMenu {
    private final Player player;
    private final ItemStack analyzerStack;
    private final ContainerData data;
    
    // Scanning state
    private boolean scanning = false;
    private int chunkX, chunkZ;
    private int nextX, nextZ;
    private Map<BlockState, Integer> countMap;
    
    // Data slots for client sync
    private static final int SCANNING_SLOT = 0;

    public ChunkAnalyzerMenu(int containerId, Inventory playerInventory, ItemStack analyzerStack) {
        super(ModMenuTypes.CHUNK_ANALYZER.get(), containerId);
        this.player = playerInventory.player;
        this.analyzerStack = analyzerStack;
        this.data = new SimpleContainerData(1); // 1 slot for scanning status
        this.addDataSlots(this.data);
    }

    public void startScanning() {
        if (!player.level().isClientSide && !scanning) {
            this.scanning = true;
            this.data.set(SCANNING_SLOT, 1);
            this.countMap = new HashMap<>();
            
            LevelChunk targetChunk = player.level().getChunkAt(player.blockPosition());
            this.chunkX = targetChunk.getPos().x;
            this.chunkZ = targetChunk.getPos().z;
            this.nextX = 0;
            this.nextZ = 0;
            
            // Clear previous results
            ChunkAnalyzerItem.clearResults(analyzerStack);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        
        // Process scanning on server side
        if (!player.level().isClientSide && scanning) {
            // Process up to 10 positions per tick to avoid lag
            for (int q = 0; q < 10 && scanning; q++) {
                LevelChunk chunk = player.level().getChunk(chunkX, chunkZ);
                
                // Scan from bedrock to build height at this X,Z position
                for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos((chunkX << 4) + nextX, y, (chunkZ << 4) + nextZ);
                    BlockState state = chunk.getBlockState(pos);
                    
                    if (!state.isAir()) {
                        countMap.merge(state, 1, Integer::sum);
                    }
                }
                
                // Move to next position
                nextX++;
                if (nextX >= 16) {
                    nextX = 0;
                    nextZ++;
                    
                    if (nextZ >= 16) {
                        // Scanning finished
                        finishScanning();
                        break;
                    }
                }
            }
        }
    }

    private void finishScanning() {
        // Process results and group by display name
        Map<String, BlockData> nameMap = new HashMap<>();
        
        for (Map.Entry<BlockState, Integer> entry : countMap.entrySet()) {
            BlockState state = entry.getKey();
            Block block = state.getBlock();
            
            ItemStack displayStack = new ItemStack(block.asItem());
            
            if (displayStack.isEmpty()) {
                continue; // Skip blocks that don't have item representations
            }
            
            String displayName = displayStack.getHoverName().getString();
            
            if (nameMap.containsKey(displayName)) {
                nameMap.get(displayName).count += entry.getValue();
            } else {
                nameMap.put(displayName, new BlockData(displayStack, displayName, entry.getValue()));
            }
        }
        
        // Sort by count (descending)
        List<BlockData> sortedResults = new ArrayList<>(nameMap.values());
        sortedResults.sort((a, b) -> Integer.compare(b.count, a.count));
        
        // Create result object
        List<ItemStack> stacks = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        for (BlockData data : sortedResults) {
            stacks.add(data.stack);
            descriptions.add(data.name);
            counts.add(data.count);
        }
        
        ChunkAnalyzerResult result = ChunkAnalyzerResult.fromLists(stacks, descriptions, counts);
        
        // Store results on the item
        ChunkAnalyzerItem.setResults(analyzerStack, result);
        
        // Send results to client
        if (player instanceof ServerPlayer serverPlayer) {
            RTPacketHandler.sendToPlayer(new ChunkAnalyzerResultPacket(result), serverPlayer);
        }
        
        // Stop scanning
        this.scanning = false;
        this.data.set(SCANNING_SLOT, 0);
        this.countMap = null;
    }

    public boolean isScanning() {
        return data.get(SCANNING_SLOT) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack held = player.getMainHandItem();
        return held.getItem() instanceof ChunkAnalyzerItem && !held.isEmpty();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No slots to move items to/from
    }

    public ItemStack getAnalyzerStack() {
        return analyzerStack;
    }

    private static class BlockData {
        final ItemStack stack;
        final String name;
        int count;
        
        BlockData(ItemStack stack, String name, int count) {
            this.stack = stack;
            this.name = name;
            this.count = count;
        }
    }
}