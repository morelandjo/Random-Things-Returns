package lumien.randomthings.blockentity;

import lumien.randomthings.block.SoundBoxBlock;
import lumien.randomthings.item.ItemSoundPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!pattern.isEmpty()) {
            tag.put("Pattern", pattern.save(registries));
        }
        tag.putBoolean("WasRedstonePowered", wasRedstonePowered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.pattern = tag.contains("Pattern")
                ? ItemStack.parse(registries, tag.getCompound("Pattern")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.wasRedstonePowered = tag.getBoolean("WasRedstonePowered");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!pattern.isEmpty()) {
            tag.put("Pattern", pattern.save(registries));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.pattern = tag.contains("Pattern")
                ? ItemStack.parse(lookupProvider, tag.getCompound("Pattern")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }
}
