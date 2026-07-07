package lumien.randomthings.menu;

import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.network.ChunkAnalyzerResultPacket;
import lumien.randomthings.network.RTNetwork;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Scans the player's current chunk over several ticks and reports each block type by count. */
public class ChunkAnalyzerMenu extends AbstractContainerMenu {
    private static final int SCANNING_SLOT = 0;

    private final Player player;
    private final ItemStack analyzerStack;
    private final ContainerData data;

    private boolean scanning = false;
    private Map<BlockState, Integer> countMap;
    private int chunkX;
    private int chunkZ;
    private int nextX;
    private int nextZ;

    public ChunkAnalyzerMenu(int containerId, Inventory playerInventory, ItemStack analyzerStack) {
        super(ModMenuTypes.CHUNK_ANALYZER.get(), containerId);
        this.player = playerInventory.player;
        this.analyzerStack = analyzerStack;
        this.data = new SimpleContainerData(1);
        this.addDataSlots(this.data);
        startScanning();
    }

    public ChunkAnalyzerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    private void startScanning() {
        if (!player.level().isClientSide && !scanning) {
            this.scanning = true;
            this.data.set(SCANNING_SLOT, 1);
            this.countMap = new HashMap<>();
            LevelChunk targetChunk = player.level().getChunkAt(player.blockPosition());
            this.chunkX = targetChunk.getPos().x;
            this.chunkZ = targetChunk.getPos().z;
            this.nextX = 0;
            this.nextZ = 0;
            ChunkAnalyzerItem.clearResults(analyzerStack);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!player.level().isClientSide && scanning) {
            for (int q = 0; q < 10 && scanning; q++) {
                LevelChunk chunk = player.level().getChunk(chunkX, chunkZ);
                for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
                    BlockState state = chunk.getBlockState(new BlockPos((chunkX << 4) + nextX, y, (chunkZ << 4) + nextZ));
                    if (!state.isAir()) {
                        countMap.merge(state, 1, Integer::sum);
                    }
                }
                nextX++;
                if (nextX >= 16) {
                    nextX = 0;
                    nextZ++;
                    if (nextZ >= 16) {
                        finishScanning();
                        break;
                    }
                }
            }
        }
    }

    private void finishScanning() {
        Map<String, BlockData> nameMap = new HashMap<>();
        for (Map.Entry<BlockState, Integer> entry : countMap.entrySet()) {
            Block block = entry.getKey().getBlock();
            ItemStack displayStack = new ItemStack(block.asItem());
            if (displayStack.isEmpty()) {
                continue;
            }
            String displayName = displayStack.getHoverName().getString();
            if (nameMap.containsKey(displayName)) {
                nameMap.get(displayName).count += entry.getValue();
            } else {
                nameMap.put(displayName, new BlockData(displayStack, displayName, entry.getValue()));
            }
        }
        List<BlockData> sorted = new ArrayList<>(nameMap.values());
        sorted.sort((a, b) -> Integer.compare(b.count, a.count));

        List<ItemStack> stacks = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (BlockData d : sorted) {
            stacks.add(d.stack);
            descriptions.add(d.name);
            counts.add(d.count);
        }
        ChunkAnalyzerResult result = new ChunkAnalyzerResult(stacks, descriptions, counts);
        ChunkAnalyzerItem.setResults(analyzerStack, result);
        if (player instanceof ServerPlayer serverPlayer) {
            RTNetwork.sendToPlayer(serverPlayer, new ChunkAnalyzerResultPacket(result));
        }
        this.scanning = false;
        this.data.set(SCANNING_SLOT, 0);
        this.countMap = null;
    }

    public boolean isScanning() {
        return data.get(SCANNING_SLOT) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() instanceof ChunkAnalyzerItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
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
