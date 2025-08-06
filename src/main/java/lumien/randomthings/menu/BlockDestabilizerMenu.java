package lumien.randomthings.menu;

import lumien.randomthings.blockentity.BlockDestabilizerBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class BlockDestabilizerMenu extends AbstractContainerMenu {
    private final BlockDestabilizerBlockEntity blockEntity;

    public BlockDestabilizerMenu(int containerId, BlockDestabilizerBlockEntity blockEntity) {
        super(ModMenuTypes.BLOCK_DESTABILIZER.get(), containerId);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity &&
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5, 
                                   blockEntity.getBlockPos().getY() + 0.5, 
                                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case 0: // Toggle lazy mode
                blockEntity.setLazy(!blockEntity.isLazy());
                return true;
            case 1: // Toggle fuzzy mode
                blockEntity.setFuzzy(!blockEntity.isFuzzy());
                return true;
            case 2: // Reset lazy cache
                blockEntity.resetLazyCache();
                return true;
            default:
                return false;
        }
    }

    public BlockDestabilizerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}