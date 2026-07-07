package lumien.randomthings.block;

import lumien.randomthings.blockentity.SpectreEnergyInjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/** Receives energy from external generators/pipes into the placing player's Spectre Energy buffer. */
public class SpectreEnergyInjectorBlock extends Block implements EntityBlock {

    public SpectreEnergyInjectorBlock() {
        super(Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(3.0f)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpectreEnergyInjectorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player
            && level.getBlockEntity(pos) instanceof SpectreEnergyInjectorBlockEntity injector) {
            injector.setOwner(player.getUUID());
        }
    }
}
