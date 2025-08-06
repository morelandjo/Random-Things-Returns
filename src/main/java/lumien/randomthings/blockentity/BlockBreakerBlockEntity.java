package lumien.randomthings.blockentity;

import com.mojang.authlib.GameProfile;
import lumien.randomthings.block.BlockBreakerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BlockBreakerBlockEntity extends BlockEntity {
    public static final GameProfile BREAKER_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("RTBlockBreaker".getBytes(StandardCharsets.UTF_8)), "RTBlockBreaker");

    private UUID uuid;
    private boolean mining = false;
    private boolean canMine = false;
    private WeakReference<FakePlayer> fakePlayer;
    private float curBlockDamage = 0f;
    private boolean firstTick = true;

    public BlockBreakerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BLOCK_BREAKER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockBreakerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockEntity.firstTick) {
            blockEntity.firstTick = false;
            blockEntity.initFakePlayer();
            blockEntity.neighborChanged();
        }

        if (blockEntity.mining) {
            Direction facing = state.getValue(BlockBreakerBlock.FACING);
            BlockPos targetPos = pos.relative(facing);
            BlockState targetState = level.getBlockState(targetPos);

            if (targetState.isAir()) {
                blockEntity.mining = false;
                blockEntity.resetProgress();
                return;
            }

            FakePlayer player = blockEntity.fakePlayer.get();
            if (player == null) {
                blockEntity.initFakePlayer();
                player = blockEntity.fakePlayer.get();
            }

            if (player != null) {
                // Calculate block breaking progress based on block hardness and tool effectiveness
                float hardness = targetState.getDestroySpeed(level, targetPos);
                if (hardness < 0) {
                    // Unbreakable block
                    blockEntity.mining = false;
                    blockEntity.resetProgress();
                    return;
                }

                // Simulate iron pickaxe breaking speed
                float digSpeed = player.getDigSpeed(targetState, targetPos);
                if (digSpeed > 1.0f) {
                    blockEntity.curBlockDamage += digSpeed / hardness / 30.0f; // Approximate breaking speed
                } else {
                    // Can't break this block with iron pickaxe
                    blockEntity.mining = false;
                    blockEntity.resetProgress();
                    return;
                }

                if (blockEntity.curBlockDamage >= 1.0f) {
                    blockEntity.mining = false;
                    blockEntity.resetProgress();

                    // Break the block
                    blockEntity.breakBlock(level, targetPos, targetState, facing);
                } else {
                    // Show breaking progress
                    int progress = (int) (blockEntity.curBlockDamage * 10.0F) - 1;
                    level.destroyBlockProgress(blockEntity.uuid.hashCode(), targetPos, progress);
                }
            }
        }
    }

    private void initFakePlayer() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
            setChanged();
        }

        if (level instanceof ServerLevel serverLevel) {
            FakePlayer player = FakePlayerFactory.get(serverLevel, BREAKER_PROFILE);
            fakePlayer = new WeakReference<>(player);

            // Give the fake player an unbreakable iron pickaxe
            ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
            ironPickaxe.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
            
            player.setItemInHand(InteractionHand.MAIN_HAND, ironPickaxe);
            player.setSilent(true);
            player.setOnGround(true);
        }
    }

    private void breakBlock(Level level, BlockPos targetPos, BlockState targetState, Direction facing) {
        FakePlayer player = fakePlayer.get();
        if (player == null) return;

        // Collect items and place them in inventory behind the block breaker
        boolean hasInventory = false;
        IItemHandler itemHandler = null;
        
        BlockPos backPos = worldPosition.relative(facing.getOpposite());
        BlockEntity backEntity = level.getBlockEntity(backPos);
        if (backEntity != null) {
            itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, backPos, facing);
            hasInventory = itemHandler != null;
        }

        // Break the block and collect drops
        player.gameMode.destroyBlock(targetPos);

        // Handle items from fake player inventory
        for (int i = 1; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            player.getInventory().setItem(i, ItemStack.EMPTY);

            if (!stack.isEmpty()) {
                ItemStack remainder = stack;
                
                if (hasInventory && itemHandler != null) {
                    remainder = ItemHandlerHelper.insertItemStacked(itemHandler, stack, false);
                }

                if (!remainder.isEmpty()) {
                    // Drop remaining items in front of the block breaker
                    Vec3 dropPos = Vec3.atCenterOf(worldPosition.relative(facing));
                    net.minecraft.world.entity.item.ItemEntity itemEntity = 
                        new net.minecraft.world.entity.item.ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, remainder);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    public void neighborChanged() {
        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        Direction facing = state.getValue(BlockBreakerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);

        // Check if powered by redstone (inverted logic - powered = disabled)
        canMine = !level.hasNeighborSignal(worldPosition);

        if (canMine) {
            BlockState targetState = level.getBlockState(targetPos);
            if (!targetState.isAir()) {
                if (!mining) {
                    mining = true;
                    curBlockDamage = 0;
                }
            } else {
                mining = false;
                resetProgress();
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
            BlockState state = getBlockState();
            Direction facing = state.getValue(BlockBreakerBlock.FACING);
            BlockPos targetPos = worldPosition.relative(facing);
            level.destroyBlockProgress(uuid.hashCode(), targetPos, -1);
            curBlockDamage = 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (uuid != null) {
            tag.putString("uuid", uuid.toString());
        }
        tag.putBoolean("mining", mining);
        tag.putBoolean("canMine", canMine);
        tag.putFloat("curBlockDamage", curBlockDamage);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
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