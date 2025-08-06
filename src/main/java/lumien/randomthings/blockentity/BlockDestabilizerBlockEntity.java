package lumien.randomthings.blockentity;

import lumien.randomthings.block.BlockDestabilizerBlock;
import lumien.randomthings.menu.BlockDestabilizerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockDestabilizerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_BLOCKS = 50; // Configurable limit

    public enum State {
        IDLE,
        SEARCHING,
        DROPPING
    }

    private State currentState = State.IDLE;
    private boolean lazy = false;
    private boolean fuzzy = false;
    private boolean wasPowered = false;

    private final Set<BlockPos> blocksToDestabilize = new LinkedHashSet<>();
    private final Set<BlockPos> checkedPositions = new HashSet<>();
    private final Set<BlockPos> lazyCache = new HashSet<>();
    private final Queue<BlockPos> searchQueue = new LinkedList<>();
    private final Queue<BlockPos> dropQueue = new LinkedList<>();

    private BlockState targetBlockState;
    private int operationTimer = 0;

    public BlockDestabilizerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BLOCK_DESTABILIZER.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockDestabilizerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.operationTimer++;

        switch (blockEntity.currentState) {
            case IDLE:
                break;
            case SEARCHING:
                blockEntity.tickSearching();
                break;
            case DROPPING:
                blockEntity.tickDropping();
                break;
        }
    }

    public void onRedstoneUpdate(boolean powered) {
        if (powered && !wasPowered && currentState == State.IDLE) {
            startDestabilization();
        }
        wasPowered = powered;
    }

    private void startDestabilization() {
        Direction facing = getBlockState().getValue(BlockDestabilizerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.isAir() || targetState.getDestroySpeed(level, targetPos) < 0) {
            return;
        }

        targetBlockState = targetState;
        blocksToDestabilize.clear();
        checkedPositions.clear();
        searchQueue.clear();
        dropQueue.clear();

        if (lazy && !lazyCache.isEmpty()) {
            for (BlockPos cachedPos : lazyCache) {
                BlockState cachedState = level.getBlockState(cachedPos);
                if (isMatchingBlock(cachedState, targetBlockState)) {
                    blocksToDestabilize.add(cachedPos);
                }
            }
            if (!blocksToDestabilize.isEmpty()) {
                startDropping();
                return;
            }
        }

        searchQueue.add(targetPos);
        checkedPositions.add(targetPos);
        currentState = State.SEARCHING;
        operationTimer = 0;
        setChanged();
    }

    private void tickSearching() {
        int searchesThisTick = 0;
        while (!searchQueue.isEmpty() && searchesThisTick < 10) {
            BlockPos currentPos = searchQueue.poll();
            BlockState currentState = level.getBlockState(currentPos);

            if (isMatchingBlock(currentState, targetBlockState)) {
                blocksToDestabilize.add(currentPos);

                if (blocksToDestabilize.size() >= MAX_BLOCKS && MAX_BLOCKS > 0) {
                    break;
                }

                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.relative(direction);
                    if (!checkedPositions.contains(neighborPos)) {
                        if (!lazy || lazyCache.isEmpty() || lazyCache.contains(neighborPos)) {
                            searchQueue.add(neighborPos);
                            checkedPositions.add(neighborPos);
                        }
                    }
                }
            }
            searchesThisTick++;
        }

        if (searchQueue.isEmpty() || (blocksToDestabilize.size() >= MAX_BLOCKS && MAX_BLOCKS > 0)) {
            if (!blocksToDestabilize.isEmpty()) {
                startDropping();
            } else {
                currentState = State.IDLE;
                setChanged();
            }
        }
    }

    private void startDropping() {
        dropQueue.clear();
        
        List<BlockPos> sortedBlocks = new ArrayList<>(blocksToDestabilize);
        sortedBlocks.sort((pos1, pos2) -> {
            int yCompare = Integer.compare(pos1.getY(), pos2.getY());
            if (yCompare != 0) return yCompare;
            
            double dist1 = pos1.distSqr(worldPosition);
            double dist2 = pos2.distSqr(worldPosition);
            return Double.compare(dist1, dist2);
        });

        dropQueue.addAll(sortedBlocks);
        
        if (lazy) {
            lazyCache.clear();
            lazyCache.addAll(blocksToDestabilize);
        }

        currentState = State.DROPPING;
        operationTimer = 0;
        setChanged();
    }

    private void tickDropping() {
        if (operationTimer % 2 == 0 && !dropQueue.isEmpty()) {
            BlockPos dropPos = dropQueue.poll();
            BlockState dropState = level.getBlockState(dropPos);

            if (isMatchingBlock(dropState, targetBlockState)) {
                level.removeBlock(dropPos, false);
                
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, dropPos, dropState);
                fallingBlock.dropItem = false;
            }
        }

        if (dropQueue.isEmpty()) {
            currentState = State.IDLE;
            setChanged();
        }
    }

    private boolean isMatchingBlock(BlockState state1, BlockState state2) {
        if (fuzzy) {
            return state1.getBlock() == state2.getBlock();
        } else {
            return state1.equals(state2);
        }
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
        setChanged();
    }

    public boolean isFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
        setChanged();
    }

    public void resetLazyCache() {
        lazyCache.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        tag.putString("State", currentState.name());
        tag.putBoolean("Lazy", lazy);
        tag.putBoolean("Fuzzy", fuzzy);
        tag.putBoolean("WasPowered", wasPowered);
        tag.putInt("OperationTimer", operationTimer);

        if (targetBlockState != null) {
            CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putString("Block", targetBlockState.getBlock().builtInRegistryHolder().key().location().toString());
            tag.put("TargetBlockState", blockStateTag);
        }

        ListTag lazyCacheList = new ListTag();
        for (BlockPos pos : lazyCache) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            lazyCacheList.add(posTag);
        }
        tag.put("LazyCache", lazyCacheList);

        ListTag destabilizeList = new ListTag();
        for (BlockPos pos : blocksToDestabilize) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            destabilizeList.add(posTag);
        }
        tag.put("BlocksToDestabilize", destabilizeList);

        ListTag dropList = new ListTag();
        for (BlockPos pos : dropQueue) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            dropList.add(posTag);
        }
        tag.put("DropQueue", dropList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("State")) {
            currentState = State.valueOf(tag.getString("State"));
        }
        lazy = tag.getBoolean("Lazy");
        fuzzy = tag.getBoolean("Fuzzy");
        wasPowered = tag.getBoolean("WasPowered");
        operationTimer = tag.getInt("OperationTimer");

        lazyCache.clear();
        if (tag.contains("LazyCache")) {
            ListTag lazyCacheList = tag.getList("LazyCache", Tag.TAG_COMPOUND);
            for (int i = 0; i < lazyCacheList.size(); i++) {
                CompoundTag posTag = lazyCacheList.getCompound(i);
                lazyCache.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }

        blocksToDestabilize.clear();
        if (tag.contains("BlocksToDestabilize")) {
            ListTag destabilizeList = tag.getList("BlocksToDestabilize", Tag.TAG_COMPOUND);
            for (int i = 0; i < destabilizeList.size(); i++) {
                CompoundTag posTag = destabilizeList.getCompound(i);
                blocksToDestabilize.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }

        dropQueue.clear();
        if (tag.contains("DropQueue")) {
            ListTag dropList = tag.getList("DropQueue", Tag.TAG_COMPOUND);
            for (int i = 0; i < dropList.size(); i++) {
                CompoundTag posTag = dropList.getCompound(i);
                dropQueue.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.randomthings.block_destabilizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BlockDestabilizerMenu(containerId, this);
    }
}