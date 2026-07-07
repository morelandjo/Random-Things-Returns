package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

/** Holds the block a Diaphanous Block pretends to be, plus whether its collision is inverted. */
public class DiaphanousBlockEntity extends BlockEntity {
    private BlockState displayState = Blocks.STONE.defaultBlockState();
    private final Map<Direction, Boolean> renderMap = new EnumMap<>(Direction.class);
    private boolean inverted = false;

    public DiaphanousBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.DIAPHANOUS_BLOCK.get(), pos, state);
        for (Direction direction : Direction.values()) {
            renderMap.put(direction, true);
        }
    }

    public BlockState getDisplayState() {
        return displayState;
    }

    public void setDisplayState(BlockState displayState) {
        this.displayState = displayState;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        setChanged();
    }

    public void updateRenderMap() {
        if (level == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            boolean shouldRender = !neighborState.isSolidRender(level, neighborPos)
                && neighborState.getBlock() != lumien.randomthings.block.ModBlocks.DIAPHANOUS_BLOCK.get();
            renderMap.put(direction, shouldRender);
        }
        setChanged();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void writeState(CompoundTag tag) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(displayState.getBlock());
        tag.putString("block", blockId.toString());
        tag.putBoolean("inverted", inverted);
        for (Direction direction : Direction.values()) {
            tag.putBoolean(direction.getName(), renderMap.getOrDefault(direction, true));
        }
    }

    private void readState(CompoundTag tag) {
        if (tag.contains("block")) {
            ResourceLocation blockId = ResourceLocation.tryParse(tag.getString("block"));
            Block block = blockId != null ? BuiltInRegistries.BLOCK.get(blockId) : Blocks.STONE;
            this.displayState = block.defaultBlockState();
        }
        this.inverted = tag.getBoolean("inverted");
        for (Direction direction : Direction.values()) {
            if (tag.contains(direction.getName())) {
                renderMap.put(direction, tag.getBoolean(direction.getName()));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readState(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeState(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
