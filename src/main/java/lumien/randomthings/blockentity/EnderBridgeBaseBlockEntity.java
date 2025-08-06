package lumien.randomthings.blockentity;

import lumien.randomthings.block.EnderBridgeBaseBlock;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class EnderBridgeBaseBlockEntity extends BlockEntity {

    public enum BridgeState {
        IDLE, SCANNING, WAITING
    }

    protected BridgeState state = BridgeState.IDLE;
    protected boolean redstonePowered = false;
    protected int scanningCounter = 2;

    public static final Set<Class<? extends Entity>> ENTITY_WHITELIST;

    static {
        ENTITY_WHITELIST = new HashSet<>();
        ENTITY_WHITELIST.add(ServerPlayer.class);
        ENTITY_WHITELIST.add(ItemEntity.class);
        ENTITY_WHITELIST.add(AbstractMinecart.class);
    }

    public EnderBridgeBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("state", state.ordinal());
        tag.putBoolean("redstonePowered", redstonePowered);
        tag.putInt("scanningCounter", scanningCounter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        state = BridgeState.values()[tag.getInt("state")];
        redstonePowered = tag.getBoolean("redstonePowered");
        scanningCounter = tag.getInt("scanningCounter");
    }

    protected void performScanning(Level level, BlockPos pos, BlockState state, int scanBlocks) {
        if (this.state != BridgeState.SCANNING) return;

        Direction facing = state.getValue(EnderBridgeBaseBlock.FACING);

        for (int i = 0; i < scanBlocks; i++) {
            if (this.state != BridgeState.SCANNING) break;

            BlockPos nextPos = pos.relative(facing, scanningCounter);

            if (level.isLoaded(nextPos)) {
                BlockState nextState = level.getBlockState(nextPos);
                
                // Check if we found an Ender Anchor
                if (nextState.getBlock() == ModBlocks.ENDER_ANCHOR.get()) {
                    // Look for entities to teleport in a 5x5x5 area around the bridge
                    AABB searchArea = new AABB(
                        pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2,
                        pos.getX() + 3, pos.getY() + 3, pos.getZ() + 3
                    );
                    
                    List<Entity> entityList = level.getEntitiesOfClass(Entity.class, searchArea);
                    
                    if (!entityList.isEmpty()) {
                        BlockPos target = nextPos.above(); // Teleport on top of anchor
                        
                        for (Entity entity : entityList) {
                            if (ENTITY_WHITELIST.stream().anyMatch(clazz -> clazz.isAssignableFrom(entity.getClass()))) {
                                // Teleport entity
                                entity.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                                entity.setDeltaMovement(0, 0, 0); // Stop movement
                            }
                        }
                    }
                    
                    this.state = BridgeState.WAITING;
                    level.setBlockAndUpdate(pos, state.setValue(EnderBridgeBaseBlock.ACTIVE, false));
                    break;
                }
                // Check if we hit a solid block (stop scanning)
                else if (!nextState.isAir()) {
                    this.state = BridgeState.WAITING;
                    level.setBlockAndUpdate(pos, state.setValue(EnderBridgeBaseBlock.ACTIVE, false));
                    break;
                }
            } else {
                // Chunk not loaded, stop scanning
                this.state = BridgeState.WAITING;
                level.setBlockAndUpdate(pos, state.setValue(EnderBridgeBaseBlock.ACTIVE, false));
                break;
            }

            scanningCounter++;
        }
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            
            if (powered != redstonePowered) {
                if (state == BridgeState.IDLE && powered) {
                    scanningCounter = 2; // Skip the block directly in front
                    state = BridgeState.SCANNING;
                    level.setBlockAndUpdate(pos, blockState.setValue(EnderBridgeBaseBlock.ACTIVE, true));
                } else if (state == BridgeState.SCANNING && !powered) {
                    state = BridgeState.IDLE;
                    level.setBlockAndUpdate(pos, blockState.setValue(EnderBridgeBaseBlock.ACTIVE, false));
                } else if (state == BridgeState.WAITING && !powered) {
                    state = BridgeState.IDLE;
                }
                redstonePowered = powered;
                setChanged();
            }
        }
    }
}