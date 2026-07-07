package lumien.randomthings.blockentity;

import lumien.randomthings.block.BlockBreakerBlock;
import lumien.randomthings.util.RTContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Mines the block it faces while unpowered, depositing drops into the inventory behind it.
 * Reimplemented without Forge's FakePlayer: drops come from {@link Block#getDrops} with a virtual
 * iron pickaxe and are routed through {@link RTContainers} (the cross-loader vanilla-Container path).
 */
public class BlockBreakerBlockEntity extends BlockEntity {
    private UUID uuid;
    private boolean mining = false;
    private boolean canMine = false;
    private float curBlockDamage = 0f;
    private boolean firstTick = true;

    public BlockBreakerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BLOCK_BREAKER.get(), pos, blockState);
    }

    private static ItemStack pickaxe() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockBreakerBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        if (be.uuid == null) {
            be.uuid = UUID.randomUUID();
            be.setChanged();
        }
        if (be.firstTick) {
            be.firstTick = false;
            be.neighborChanged();
        }
        if (!be.mining) {
            return;
        }
        Direction facing = state.getValue(BlockBreakerBlock.FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.isAir()) {
            be.mining = false;
            be.resetProgress();
            return;
        }
        float hardness = targetState.getDestroySpeed(level, targetPos);
        if (hardness < 0) {
            be.mining = false;
            be.resetProgress();
            return;
        }
        float digSpeed = pickaxe().getDestroySpeed(targetState);
        if (digSpeed <= 1.0f) {
            // Iron pickaxe is not effective on this block — leave it.
            be.mining = false;
            be.resetProgress();
            return;
        }
        be.curBlockDamage += digSpeed / hardness / 30.0f;
        if (be.curBlockDamage >= 1.0f) {
            be.mining = false;
            be.resetProgress();
            be.breakBlock((ServerLevel) level, targetPos, targetState, facing);
        } else {
            int progress = (int) (be.curBlockDamage * 10.0F) - 1;
            level.destroyBlockProgress(be.uuid.hashCode(), targetPos, progress);
        }
    }

    private void breakBlock(ServerLevel level, BlockPos targetPos, BlockState targetState, Direction facing) {
        List<ItemStack> drops = Block.getDrops(targetState, level, targetPos, level.getBlockEntity(targetPos), null, pickaxe());
        level.levelEvent(2001, targetPos, Block.getId(targetState));
        level.removeBlock(targetPos, false);

        BlockPos backPos = worldPosition.relative(facing.getOpposite());
        Container back = RTContainers.getContainerAt(level, backPos);
        for (ItemStack drop : drops) {
            ItemStack remainder = RTContainers.insert(back, drop, facing);
            if (!remainder.isEmpty()) {
                Vec3 dropPos = Vec3.atCenterOf(worldPosition.relative(facing));
                level.addFreshEntity(new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, remainder));
            }
        }
    }

    public void neighborChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockBreakerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        // Inverted: a redstone signal disables the breaker.
        canMine = !level.hasNeighborSignal(worldPosition);
        if (canMine && !level.getBlockState(targetPos).isAir()) {
            if (!mining) {
                mining = true;
                curBlockDamage = 0;
            }
        } else {
            if (mining) {
                mining = false;
                resetProgress();
            }
        }
    }

    private void resetProgress() {
        if (uuid != null && level != null) {
            Direction facing = getBlockState().getValue(BlockBreakerBlock.FACING);
            level.destroyBlockProgress(uuid.hashCode(), worldPosition.relative(facing), -1);
            curBlockDamage = 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (uuid != null) {
            tag.putString("uuid", uuid.toString());
        }
        tag.putBoolean("mining", mining);
        tag.putBoolean("canMine", canMine);
        tag.putFloat("curBlockDamage", curBlockDamage);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("uuid")) {
            uuid = UUID.fromString(tag.getString("uuid"));
        }
        mining = tag.getBoolean("mining");
        canMine = tag.getBoolean("canMine");
        curBlockDamage = tag.getFloat("curBlockDamage");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (mining && uuid != null && level != null) {
            resetProgress();
        }
    }
}
