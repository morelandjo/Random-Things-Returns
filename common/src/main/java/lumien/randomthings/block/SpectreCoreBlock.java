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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * The 2x2 core at the center of each Spectre room. Right-click with Ectoplasm to increase the room
 * height, or empty-handed to teleport back out.
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
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false)
                .noLootTable()
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

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        Block adjacentBlock = adjacentState.getBlock();
        return adjacentBlock == this || adjacentBlock == ModBlocks.SPECTRE_BLOCK.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

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
        else if (heldItem.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            handler.teleportPlayerBack(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
