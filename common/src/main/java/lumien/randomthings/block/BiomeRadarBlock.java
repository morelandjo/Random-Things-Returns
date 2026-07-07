package lumien.randomthings.block;

import lumien.randomthings.blockentity.BiomeRadarBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** Insert a Biome Crystal and power it to search for that biome; right-click paper to extract the location. */
public class BiomeRadarBlock extends BaseEntityBlock {

    public BiomeRadarBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BiomeRadarBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BiomeRadarBlockEntity radar)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (radar.getState() == BiomeRadarBlockEntity.State.IDLE) {
            if (radar.getCurrentCrystal().isEmpty() && item == ModItems.BIOME_CRYSTAL.get()) {
                if (!level.isClientSide) {
                    ItemStack one = stack.copy();
                    one.setCount(1);
                    radar.setCrystal(one);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.levelEvent(null, 1037, pos, 0);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (stack.isEmpty() && !radar.getCurrentCrystal().isEmpty()) {
                if (!level.isClientSide) {
                    ItemStack toGive = radar.getCurrentCrystal().copy();
                    radar.setCrystal(ItemStack.EMPTY);
                    if (!player.getInventory().add(toGive)) {
                        player.drop(toGive, false);
                    }
                    level.levelEvent(null, 1036, pos, 0);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else if (radar.getState() == BiomeRadarBlockEntity.State.FINISHED && item == Items.PAPER) {
            if (!level.isClientSide) {
                ResourceKey<Biome> biomeKey = BiomeCrystalItem.getBiome(radar.getCurrentCrystal());
                if (biomeKey != null) {
                    BlockPos found = radar.getFoundPosition();
                    ItemStack filter = new ItemStack(ModItems.POSITION_FILTER.get());
                    PositionFilterItem.setPosition(filter, level.dimension().location().toString(), found.getX(), found.getY(), found.getZ());
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (!player.getInventory().add(filter)) {
                        player.drop(filter, false);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BiomeRadarBlockEntity radar && !radar.getCurrentCrystal().isEmpty()) {
                Block.popResource(level, pos, radar.getCurrentCrystal());
                radar.setCrystal(ItemStack.EMPTY);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BiomeRadarBlockEntity radar) {
            radar.onNeighborChanged(level.hasNeighborSignal(pos));
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
            ? createTickerHelper(type, ModBlockEntityTypes.BIOME_RADAR.get(), BiomeRadarBlockEntity::clientTick)
            : createTickerHelper(type, ModBlockEntityTypes.BIOME_RADAR.get(), BiomeRadarBlockEntity::serverTick);
    }
}
