package lumien.randomthings.blockentity;

import lumien.randomthings.block.IgniterBlock;
import lumien.randomthings.menu.IgniterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.Packet;

public class IgniterBlockEntity extends BlockEntity implements MenuProvider {
    
    public enum Mode {
        TOGGLE("toggle"),
        IGNITE("ignite"), 
        KEEP_IGNITED("keepIgnited");
        
        private final String key;
        
        Mode(String key) {
            this.key = key;
        }
        
        public String getTranslationKey() {
            return "gui.randomthings.igniter." + key;
        }
        
        public Mode next() {
            Mode[] values = Mode.values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
    
    private Mode mode = Mode.TOGGLE;
    private boolean wasRedstonePowered = false;
    
    public IgniterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.IGNITER.get(), pos, blockState);
    }
    
    public void onBlockPlaced() {
        // Initialize redstone state when block is first placed
        if (level != null && !level.isClientSide) {
            wasRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Mode", this.mode.ordinal());
        tag.putBoolean("WasRedstonePowered", this.wasRedstonePowered);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mode = Mode.values()[Math.max(0, Math.min(tag.getInt("Mode"), Mode.values().length - 1))];
        this.wasRedstonePowered = tag.getBoolean("WasRedstonePowered");
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Mode", this.mode.ordinal());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.mode = Mode.values()[Math.max(0, Math.min(tag.getInt("Mode"), Mode.values().length - 1))];
    }
    
    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public Mode getMode() {
        return this.mode;
    }
    
    public void setMode(Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            this.setChanged();
            
            if (this.level != null && !this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                
                // If switching to KEEP_IGNITED mode while powered, immediately ignite
                if (mode == Mode.KEEP_IGNITED) {
                    BlockState state = this.getBlockState();
                    if (state.getBlock() instanceof IgniterBlock) {
                        Direction facing = state.getValue(IgniterBlock.FACING);
                        igniteIfPossible(facing);
                    }
                }
            }
        }
    }
    
    public void rotateMode() {
        setMode(this.mode.next());
    }
    
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (level.isClientSide) return;
        
        boolean isRedstonePowered = level.hasNeighborSignal(pos);
        
        // Debug output
        System.out.println("Igniter neighborChanged: powered=" + isRedstonePowered + ", was=" + wasRedstonePowered + ", mode=" + mode);
        
        if (mode == Mode.KEEP_IGNITED) {
            Direction facing = state.getValue(IgniterBlock.FACING);
            BlockPos frontPos = pos.relative(facing);
            
            if (level.isEmptyBlock(frontPos) && canPlaceFire(level, frontPos)) {
                igniteIfPossible(facing);
            }
        }
        
        // Handle redstone state changes
        if (isRedstonePowered != wasRedstonePowered) {
            System.out.println("Igniter redstone state change: " + wasRedstonePowered + " -> " + isRedstonePowered);
            redstoneStateChanged(wasRedstonePowered, isRedstonePowered, state);
            wasRedstonePowered = isRedstonePowered;
        }
    }
    
    private void redstoneStateChanged(boolean oldState, boolean newState, BlockState state) {
        Direction facing = state.getValue(IgniterBlock.FACING);
        BlockPos frontPos = worldPosition.relative(facing);
        
        System.out.println("Igniter redstoneStateChanged: facing=" + facing + ", frontPos=" + frontPos + ", mode=" + mode);
        
        if (oldState && !newState) {
            // Redstone turned off
            BlockState frontState = level.getBlockState(frontPos);
            System.out.println("Redstone OFF: frontBlock=" + frontState.getBlock());
            
            if (frontState.is(Blocks.FIRE) && mode == Mode.TOGGLE) {
                System.out.println("Extinguishing fire");
                level.removeBlock(frontPos, false);
            }
        } else if (newState && !oldState) {
            // Redstone turned on
            boolean isEmpty = level.isEmptyBlock(frontPos);
            boolean canPlace = canPlaceFire(level, frontPos);
            System.out.println("Redstone ON: isEmpty=" + isEmpty + ", canPlace=" + canPlace + ", mode=" + mode);
            
            if (isEmpty && mode != Mode.KEEP_IGNITED && canPlace) {
                System.out.println("Attempting to ignite");
                igniteIfPossible(facing);
            }
        }
    }
    
    private void igniteIfPossible(Direction facing) {
        if (level == null || level.isClientSide) return;
        
        BlockPos frontPos = worldPosition.relative(facing);
        boolean isEmpty = level.isEmptyBlock(frontPos);
        boolean canPlace = canPlaceFire(level, frontPos);
        
        System.out.println("igniteIfPossible: frontPos=" + frontPos + ", isEmpty=" + isEmpty + ", canPlace=" + canPlace);
        
        if (isEmpty && canPlace) {
            System.out.println("Placing fire at " + frontPos);
            RandomSource random = level.getRandom();
            level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 
                          1.0F, random.nextFloat() * 0.4F + 0.8F);
            level.setBlock(frontPos, Blocks.FIRE.defaultBlockState(), 11);
        } else {
            System.out.println("Cannot place fire: isEmpty=" + isEmpty + ", canPlace=" + canPlace);
        }
    }
    
    private boolean canPlaceFire(Level level, BlockPos pos) {
        // Check if the block below can support fire (similar to flint and steel logic)
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        boolean isSolid = belowState.isSolid();
        boolean isFaceSturdy = belowState.isFaceSturdy(level, belowPos, Direction.UP);
        
        System.out.println("canPlaceFire: pos=" + pos + ", belowPos=" + belowPos + ", belowBlock=" + belowState.getBlock() + ", isSolid=" + isSolid + ", isFaceSturdy=" + isFaceSturdy);
        
        return isSolid || isFaceSturdy;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.igniter");
    }
    
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IgniterMenu(containerId, playerInventory, this);
    }
}