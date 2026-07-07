package lumien.randomthings.blockentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.handler.floo.FlooNetworkSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BlockEntity for {@link lumien.randomthings.block.FlooBrickBlock}. A floo fireplace consists of one
 * master brick (created by clicking a Floo Sign on a vanilla brick) and N child bricks flood-filled
 * at sign-use time.
 */
public class FlooBrickBlockEntity extends BlockEntity {
    private boolean amMaster = false;
    @Nullable
    private UUID uuid; // master: own ID; child: master's ID
    @Nullable
    private Direction facing; // master only
    private final List<BlockPos> children = new ArrayList<>(); // master only

    public FlooBrickBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.FLOO_BRICK.get(), pos, blockState);
    }

    public void initMaster(UUID uuid, Direction facing, List<BlockPos> children) {
        this.amMaster = true;
        this.uuid = uuid;
        this.facing = facing;
        this.children.clear();
        this.children.addAll(children);
        this.setChanged();
    }

    public void initChild(UUID masterUUID) {
        this.amMaster = false;
        this.uuid = masterUUID;
        this.facing = null;
        this.children.clear();
        this.setChanged();
    }

    public boolean isMaster() {
        return amMaster;
    }

    @Nullable
    public UUID getMasterUUID() {
        return uuid;
    }

    @Nullable
    public Direction getFacing() {
        return facing;
    }

    public List<BlockPos> getChildren() {
        return children;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level == null || this.level.isClientSide) return;
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        if (this.uuid == null) return;

        if (this.amMaster) {
            for (BlockPos childPos : new ArrayList<>(children)) {
                if (serverLevel.getBlockState(childPos).getBlock() == ModBlocks.FLOO_BRICK.get()) {
                    serverLevel.setBlock(childPos, Blocks.BRICKS.defaultBlockState(), 3);
                }
            }
            FlooNetworkSavedData.get(serverLevel).brokenMaster(this.uuid);
        } else {
            BlockPos masterPos = FlooNetworkSavedData.get(serverLevel).findMasterPosition(serverLevel, this.uuid);
            if (masterPos != null && serverLevel.getBlockState(masterPos).is(ModBlocks.FLOO_BRICK.get())) {
                serverLevel.setBlock(masterPos, Blocks.BRICKS.defaultBlockState(), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("amMaster", this.amMaster);
        if (this.uuid != null) {
            tag.putUUID("uuid", this.uuid);
        }
        if (this.amMaster) {
            if (this.facing != null) {
                tag.putInt("facing", this.facing.ordinal());
            }
            ListTag list = new ListTag();
            for (BlockPos child : children) {
                CompoundTag c = new CompoundTag();
                c.put("pos", NbtUtils.writeBlockPos(child));
                list.add(c);
            }
            tag.put("children", list);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.amMaster = tag.getBoolean("amMaster");
        this.uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
        this.children.clear();
        if (this.amMaster) {
            if (tag.contains("facing")) {
                int ord = tag.getInt("facing");
                Direction[] dirs = Direction.values();
                this.facing = (ord >= 0 && ord < dirs.length) ? dirs[ord] : Direction.NORTH;
            }
            ListTag list = tag.getList("children", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                this.children.add(NbtUtils.readBlockPos(list.getCompound(i).getCompound("pos")));
            }
        }
    }
}
