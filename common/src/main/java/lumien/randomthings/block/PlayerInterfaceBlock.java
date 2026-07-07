package lumien.randomthings.block;

import lumien.randomthings.blockentity.PlayerInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/** Binds to whoever places it and exposes that player's inventory to adjacent automation. */
public class PlayerInterfaceBlock extends BaseEntityBlock {

    public PlayerInterfaceBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).strength(4.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlayerInterfaceBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (placer instanceof Player player && level.getBlockEntity(pos) instanceof PlayerInterfaceBlockEntity playerInterface) {
                playerInterface.setPlayerUUID(player.getUUID());
            } else if (placer == null) {
                level.destroyBlock(pos, false);
            }
        }
    }
}
