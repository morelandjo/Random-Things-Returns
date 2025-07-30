package lumien.randomthings.block;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.collect.Lists;

import lumien.randomthings.blockentity.AdvancedRedstoneTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AdvancedRedstoneTorchBlock extends BaseEntityBlock
{
	public static final MapCodec<AdvancedRedstoneTorchBlock> CODEC = simpleCodec(AdvancedRedstoneTorchBlock::new);

	@Override
	public MapCodec<AdvancedRedstoneTorchBlock> codec() {
		return CODEC;
	}

	public static enum COLOR implements StringRepresentable
	{
		GREEN, RED;

		@Override
		public String getSerializedName()
		{
			return this == GREEN ? "green" : "red";
		}
	}

	public static final EnumProperty<COLOR> COLOR_PROPERTY = EnumProperty.create("color", COLOR.class);
	private static final Map<BlockGetter, List<AdvancedRedstoneTorchBlock.Toggle>> BURNED_TORCHES = new WeakHashMap<>();

	protected AdvancedRedstoneTorchBlock()
	{
		super(BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel((state) -> 7).sound(SoundType.WOOD));
		this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_PROPERTY, COLOR.RED));
	}

	protected AdvancedRedstoneTorchBlock(BlockBehaviour.Properties properties)
	{
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new AdvancedRedstoneTorchBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
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

	/**
	 * How many world ticks before ticking
	 */
	public int tickRate(LevelReader level)
	{
		return 2;
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
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
	{
		if (!movedByPiston)
		{
			for (Direction direction : Direction.values())
			{
				level.updateNeighborsAt(pos.relative(direction), this);
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		if (blockAccess.getBlockEntity(pos) instanceof AdvancedRedstoneTorchBlockEntity te)
		{
			int strength = blockState.getValue(COLOR_PROPERTY) == COLOR.RED ? te.signalStrengthRed() : te.signalStrengthGreen();
			return Direction.UP != side ? strength : 0;
		}
		return 0;
	}

	protected boolean shouldBeGreen(Level level, BlockPos pos, BlockState state)
	{
		return level.hasNeighborSignal(pos);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		boolean shouldBeGreen = this.shouldBeGreen(level, pos, state);
		update(state, level, pos, random, shouldBeGreen);
	}

	public static void update(BlockState state, Level level, BlockPos pos, RandomSource random, boolean shouldBeGreen)
	{
		List<AdvancedRedstoneTorchBlock.Toggle> list = BURNED_TORCHES.get(level);

		while (list != null && !list.isEmpty() && level.getGameTime() - (list.get(0)).time > 60L)
		{
			list.remove(0);
		}

		if (state.getValue(COLOR_PROPERTY) == COLOR.RED)
		{
			if (shouldBeGreen)
			{
				level.setBlock(pos, state.setValue(COLOR_PROPERTY, COLOR.GREEN), 3);
				for (Direction direction : Direction.values())
				{
					level.updateNeighborsAt(pos.relative(direction), level.getBlockState(pos).getBlock());
				}
				if (isBurnedOut(level, pos, true))
				{
					level.levelEvent(1502, pos, 0);
					level.scheduleTick(pos, level.getBlockState(pos).getBlock(), 160);
				}
			}
		}
		else if (!shouldBeGreen && !isBurnedOut(level, pos, false))
		{
			level.setBlock(pos, state.setValue(COLOR_PROPERTY, COLOR.RED), 3);
			for (Direction direction : Direction.values())
			{
				level.updateNeighborsAt(pos.relative(direction), level.getBlockState(pos).getBlock());
			}
		}
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
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return side == Direction.DOWN ? blockState.getSignal(blockAccess, pos, side) : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	protected static final DustParticleOptions GREEN_DUST = new DustParticleOptions(new org.joml.Vector3f(0.0F, 1.0F, 0.0F), 1.0F);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if (state.getValue(COLOR_PROPERTY) == COLOR.RED)
		{
			double d0 = (double) pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
			level.addParticle(DustParticleOptions.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
		else
		{
			double d0 = (double) pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
			level.addParticle(GREEN_DUST, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(COLOR_PROPERTY);
	}

	private static boolean isBurnedOut(Level level, BlockPos pos, boolean addToggle)
	{
		List<AdvancedRedstoneTorchBlock.Toggle> list = BURNED_TORCHES.computeIfAbsent(level, (l) -> {
			return Lists.newArrayList();
		});
		if (addToggle)
		{
			list.add(new AdvancedRedstoneTorchBlock.Toggle(pos.immutable(), level.getGameTime()));
		}

		int i = 0;

		for (int j = 0; j < list.size(); ++j)
		{
			AdvancedRedstoneTorchBlock.Toggle toggle = list.get(j);
			if (toggle.pos.equals(pos))
			{
				++i;
				if (i >= 8)
				{
					return true;
				}
			}
		}

		return false;
	}

	public static class Toggle
	{
		private final BlockPos pos;
		private final long time;

		public Toggle(BlockPos pos, long time)
		{
			this.pos = pos;
			this.time = time;
		}
	}
}