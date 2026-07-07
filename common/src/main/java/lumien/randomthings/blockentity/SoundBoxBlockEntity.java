package lumien.randomthings.blockentity;

import lumien.randomthings.block.SoundBoxBlock;
import lumien.randomthings.item.ItemSoundPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SoundBoxBlockEntity extends BlockEntity {
    private ItemStack pattern = ItemStack.EMPTY;
    private boolean wasRedstonePowered = false;

    public SoundBoxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SOUND_BOX.get(), pos, blockState);
    }

    public ItemStack getPattern() {
        return this.pattern;
    }

    public void setPattern(ItemStack newPattern) {
        this.pattern = newPattern == null ? ItemStack.EMPTY : newPattern;
        boolean hasPattern = !this.pattern.isEmpty();

        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof SoundBoxBlock
                    && state.getValue(SoundBoxBlock.HAS_PATTERN) != hasPattern) {
                level.setBlock(worldPosition, state.setValue(SoundBoxBlock.HAS_PATTERN, hasPattern), 3);
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        setChanged();
    }

    public void initRedstoneState() {
        if (level != null && !level.isClientSide) {
            this.wasRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }

    public void onNeighborChanged() {
        if (level == null || level.isClientSide) return;

        boolean isPowered = level.hasNeighborSignal(worldPosition);
        if (isPowered && !wasRedstonePowered) {
            playStoredSound();
        }
        if (isPowered != wasRedstonePowered) {
            wasRedstonePowered = isPowered;
            setChanged();
        }
    }

    private void playStoredSound() {
        if (pattern.isEmpty()) return;
        ResourceLocation soundLocation = ItemSoundPattern.getSoundLocation(pattern);
        if (soundLocation == null) return;
        SoundEvent event = SoundEvent.createVariableRangeEvent(soundLocation);
        level.playSound(null, worldPosition, event, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!pattern.isEmpty()) {
            tag.put("Pattern", pattern.save(new CompoundTag()));
        }
        tag.putBoolean("WasRedstonePowered", wasRedstonePowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.pattern = tag.contains("Pattern") ? ItemStack.of(tag.getCompound("Pattern")) : ItemStack.EMPTY;
        this.wasRedstonePowered = tag.getBoolean("WasRedstonePowered");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (!pattern.isEmpty()) {
            tag.put("Pattern", pattern.save(new CompoundTag()));
        }
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
