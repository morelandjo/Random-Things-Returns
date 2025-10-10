package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.BlockEntityRuneBase;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockRuneBase extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.08, 16.0);

    public static final MapCodec<BlockRuneBase> CODEC = simpleCodec(BlockRuneBase::new);

    public BlockRuneBase(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public BlockRuneBase() {
        this(BlockBehaviour.Properties.of()
            .strength(0.2f)
            .noOcclusion()
            .noCollission());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRuneBase(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        // Check if the block below is still solid
        BlockState downState = level.getBlockState(pos.below());
        if (!downState.isFaceSturdy(level, pos.below(), Direction.UP)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockEntityRuneBase runeBase) {
                runeBase.dropRuneDust();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Right-click with paper to create rune pattern
        if (!level.isClientSide && stack.is(Items.PAPER)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockEntityRuneBase runeBase) {
                int[][] runeData = runeBase.getRuneData();

                // Flatten 4x4 array into 1D array for storage
                int[] flatData = new int[16];
                for (int x = 0; x < 4; x++) {
                    for (int z = 0; z < 4; z++) {
                        flatData[x + z * 4] = runeData[x][z];
                    }
                }

                ItemStack pattern = new ItemStack(ModItems.RUNE_PATTERN.get());
                pattern.set(ModDataComponents.RUNE_PATTERN.get(), flatData);

                stack.shrink(1);

                if (!player.getInventory().add(pattern)) {
                    ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), pattern);
                    level.addFreshEntity(itemEntity);
                }

                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockEntityRuneBase runeBase) {
                // Calculate which cell was clicked
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 end = start.add(look.scale(6.0));

                BlockHitResult hitResult = level.clip(new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.OUTLINE,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    player
                ));

                if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK &&
                    hitResult.getBlockPos().equals(pos)) {

                    Vec3 hitVec = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos));
                    int[][] runeData = runeBase.getRuneData();

                    int x = (int) Math.floor(hitVec.x * 4);
                    int z = (int) Math.floor(hitVec.z * 4);

                    if (x >= 0 && x < 4 && z >= 0 && z < 4 && runeData[x][z] != -1) {
                        // Drop the rune dust
                        ItemStack dustStack = new ItemStack(ModItems.RUNE_DUST.get(), 1);
                        dustStack.set(ModDataComponents.RUNE_COLOR.get(), DyeColor.byId(runeData[x][z]));

                        ItemEntity entityItem = new ItemEntity(level,
                            pos.getX() + hitVec.x,
                            pos.getY() + 0.1,
                            pos.getZ() + hitVec.z,
                            dustStack);
                        entityItem.setDefaultPickUpDelay();
                        level.addFreshEntity(entityItem);

                        runeData[x][z] = -1;
                        runeBase.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);

                        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);

                        // Remove block if empty
                        if (runeBase.isEmpty()) {
                            level.removeBlock(pos, false);
                        }
                    }
                }
            }
        }

        super.attack(state, level, pos, player);
    }
}
