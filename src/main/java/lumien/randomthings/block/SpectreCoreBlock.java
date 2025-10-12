package lumien.randomthings.block;

import lumien.randomthings.handler.spectre.SpectreCube;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Spectre Core - The center 2x2 multiblock of each Spectre room.
 * Players can right-click with Ectoplasm to increase room height or empty-handed to teleport back.
 */
public class SpectreCoreBlock extends Block {

    public static final EnumProperty<Orientation> ORIENTATION = EnumProperty.create("orientation", Orientation.class);

    public enum Orientation implements StringRepresentable {
        NW("nw"),
        NE("ne"),
        ES("es"),
        SW("sw");

        private final String name;

        Orientation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public SpectreCoreBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(-1.0F, 3600000.0F) // Unbreakable
                .sound(SoundType.GLASS)
                .noOcclusion() // Allow translucent rendering
                .isViewBlocking((state, level, pos) -> false)
                .noLootTable() // Don't appear in creative tab
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, Orientation.NW));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Use getStateForPlacement from block placement context for proper placement
    // But for Spectre Core, we determine orientation dynamically via getActualState
    // So we don't need to override getStateForPlacement

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        // Cannot be destroyed by any entity
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        // Cannot be destroyed by explosions
        return false;
    }

    @Override
    public float getExplosionResistance() {
        // Maximum explosion resistance
        return Float.MAX_VALUE - 1000f;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        // Don't render face if adjacent block is also a spectre block or spectre core
        Block adjacentBlock = adjacentState.getBlock();
        return adjacentBlock == this || adjacentBlock == ModBlocks.SPECTRE_BLOCK.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Must be in Spectre Dimension
        if (!SpectreHandler.isSpectreDimension(level)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        SpectreHandler handler = SpectreHandler.getInstance(level.getServer());

        if (handler == null) {
            return InteractionResult.FAIL;
        }

        // Right-click with Ectoplasm to increase height
        if (heldItem.is(ModItems.ECTOPLASM.get())) {
            SpectreCube cube = handler.getSpectreCubeFromPos(level, pos.above());

            if (cube != null) {
                int consumed = cube.increaseHeight(heldItem.getCount());
                heldItem.shrink(consumed);
            }

            return InteractionResult.SUCCESS;
        }
        // Right-click empty-handed to teleport back
        else if (heldItem.isEmpty()) {
            handler.teleportPlayerBack((ServerPlayer) player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
