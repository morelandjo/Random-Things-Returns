package lumien.randomthings.block;

import javax.annotation.Nullable;

import lumien.randomthings.blockentity.AdvancedRedstoneTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AdvancedRedstoneWallTorchBlock extends AdvancedRedstoneTorchBlock
{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<AdvancedRedstoneTorchBlock.COLOR> COLOR_PROPERTY = AdvancedRedstoneTorchBlock.COLOR_PROPERTY;

	protected AdvancedRedstoneWallTorchBlock()
	{
		super(BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel((state) -> 7).sound(SoundType.WOOD));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COLOR_PROPERTY, AdvancedRedstoneTorchBlock.COLOR.RED));
	}

	/**
	 * Returns the unlocalized name of the block with "tile." appended to the front.
	 */
	public String getDescriptionId()
	{
		return this.asItem().getDescriptionId();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return WallTorchBlock.getShape(state);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
		Direction direction = state.getValue(FACING);
		BlockPos blockpos = pos.relative(direction.getOpposite());
		BlockState blockstate = level.getBlockState(blockpos);
		return blockstate.isFaceSturdy(level, blockpos, direction);
	}

	/**
	 * Update the provided state given the provided neighbor facing and neighbor state, returning a new state. For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately returns its solidified counterpart. Note that this method should ideally consider only the specific face passed in.
	 */
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
	{
		return facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos) ? 
			Blocks.AIR.defaultBlockState() : state;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(context);
		return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
	}

	/**
	 * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless of whether the block can receive random update ticks
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if (state.getValue(COLOR_PROPERTY) == COLOR.RED)
		{
			Direction direction = state.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double) pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepX();
			double d2 = (double) pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double) pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepZ();
			level.addParticle(DustParticleOptions.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
		}
		else
		{
			Direction direction = state.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double) pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepX();
			double d2 = (double) pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double) pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepZ();
			level.addParticle(GREEN_DUST, d1, d2, d3, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
	{
		if (level.isClientSide)
		{
			return InteractionResult.SUCCESS;
		}
		else
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof AdvancedRedstoneTorchBlockEntity)
			{
				AdvancedRedstoneTorchBlockEntity art = (AdvancedRedstoneTorchBlockEntity) blockEntity;
				player.openMenu(art, pos);
			}

			return InteractionResult.CONSUME;
		}
	}

	@Override
	protected boolean shouldBeGreen(Level level, BlockPos pos, BlockState state)
	{
		return level.hasNeighborSignal(pos);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
	{
		if (!level.isClientSide && !level.getBlockTicks().hasScheduledTick(pos, this))
		{
			level.scheduleTick(pos, this, 1);
		}
		for (Direction direction : Direction.values())
		{
			level.updateNeighborsAt(pos.relative(direction), this);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		boolean shouldBeGreen = this.shouldBeGreen(level, pos, state);
		AdvancedRedstoneTorchBlock.update(state, level, pos, random, shouldBeGreen);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
	{
		if (!level.isClientSide)
		{
			boolean currentlyRed = state.getValue(COLOR_PROPERTY) == COLOR.RED;
			boolean shouldBeGreen = this.shouldBeGreen(level, pos, state);
			
			if (currentlyRed == shouldBeGreen && !level.getBlockTicks().hasScheduledTick(pos, this))
			{
				level.scheduleTick(pos, this, this.tickRate(level));
			}
		}
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		if (blockAccess.getBlockEntity(pos) instanceof AdvancedRedstoneTorchBlockEntity te)
		{
			int strength = blockState.getValue(COLOR_PROPERTY) == COLOR.RED ? te.signalStrengthRed() : te.signalStrengthGreen();
			return blockState.getValue(FACING) != side ? strength : 0;
		}
		return 0;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
	 */
	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
	 */
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, COLOR_PROPERTY);
	}
}