package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.block.IgniterBlock;
import lumien.randomthings.menu.IgniterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IgniterBlockEntity extends BlockEntity implements ExtendedMenuProvider {

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
        if (level != null && !level.isClientSide) {
            wasRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Mode", this.mode.ordinal());
        tag.putBoolean("WasRedstonePowered", this.wasRedstonePowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.mode = Mode.values()[Math.max(0, Math.min(tag.getInt("Mode"), Mode.values().length - 1))];
        this.wasRedstonePowered = tag.getBoolean("WasRedstonePowered");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("Mode", this.mode.ordinal());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
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
                if (mode == Mode.KEEP_IGNITED && this.getBlockState().getBlock() instanceof IgniterBlock) {
                    igniteIfPossible(this.getBlockState().getValue(IgniterBlock.FACING));
                }
            }
        }
    }

    public void rotateMode() {
        setMode(this.mode.next());
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block neighborBlock, BlockPos neighborPos) {
        if (level.isClientSide) {
            return;
        }
        boolean isRedstonePowered = level.hasNeighborSignal(pos);
        if (mode == Mode.KEEP_IGNITED) {
            Direction facing = state.getValue(IgniterBlock.FACING);
            BlockPos frontPos = pos.relative(facing);
            if (level.isEmptyBlock(frontPos) && canPlaceFire(level, frontPos)) {
                igniteIfPossible(facing);
            }
        }
        if (isRedstonePowered != wasRedstonePowered) {
            redstoneStateChanged(wasRedstonePowered, isRedstonePowered, state);
            wasRedstonePowered = isRedstonePowered;
        }
    }

    private void redstoneStateChanged(boolean oldState, boolean newState, BlockState state) {
        Direction facing = state.getValue(IgniterBlock.FACING);
        BlockPos frontPos = worldPosition.relative(facing);
        if (oldState && !newState) {
            if (level.getBlockState(frontPos).is(Blocks.FIRE) && mode == Mode.TOGGLE) {
                level.removeBlock(frontPos, false);
            }
        } else if (newState && !oldState) {
            if (level.isEmptyBlock(frontPos) && mode != Mode.KEEP_IGNITED && canPlaceFire(level, frontPos)) {
                igniteIfPossible(facing);
            }
        }
    }

    private void igniteIfPossible(Direction facing) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockPos frontPos = worldPosition.relative(facing);
        if (level.isEmptyBlock(frontPos) && canPlaceFire(level, frontPos)) {
            RandomSource random = level.getRandom();
            level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
            level.setBlock(frontPos, Blocks.FIRE.defaultBlockState(), 11);
        }
    }

    private boolean canPlaceFire(Level level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isSolid() || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.igniter");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IgniterMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
