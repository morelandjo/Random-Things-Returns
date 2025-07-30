package lumien.randomthings.menu;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.AdvancedRedstoneTorchBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdvancedRedstoneTorchMenu extends AbstractContainerMenu implements ISignalContainer {
    ContainerLevelAccess access;

    public ContainerData data = new SimpleContainerData(2);

    AdvancedRedstoneTorchBlockEntity blockEntity;

    public AdvancedRedstoneTorchMenu(int windowId, FriendlyByteBuf extraData) {
        this(windowId, ContainerLevelAccess.NULL);
    }

    public AdvancedRedstoneTorchMenu(int windowId, ContainerLevelAccess access) {
        super(ModMenuTypes.ADVANCED_REDSTONE_TORCH.get(), windowId);

        this.access = access;
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ADVANCED_REDSTONE_TORCH.get()) || 
               stillValid(this.access, player, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get());
    }

    @Override
    public void broadcastChanges() {
        this.access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof AdvancedRedstoneTorchBlockEntity art) {
                this.data.set(0, art.signalStrengthGreen());
                this.data.set(1, art.signalStrengthRed());
            }
        });

        super.broadcastChanges();
    }

    @Override
    public void handle(int id, FriendlyByteBuf data) {
        if (id == 0) {
            int action = data.readInt();

            this.access.execute((level, pos) -> {
                BlockEntity be = level.getBlockEntity(pos);

                if (be instanceof AdvancedRedstoneTorchBlockEntity art) {
                    switch (action) {
                        case 0: // Decrease Green Strength
                            art.setSignalStrengthGreen(Math.max(0, art.signalStrengthGreen() - 1));
                            break;
                        case 1: // Increase Green Strength
                            art.setSignalStrengthGreen(Math.min(15, art.signalStrengthGreen() + 1));
                            break;
                        case 2: // Decrease Red Strength
                            art.setSignalStrengthRed(Math.max(0, art.signalStrengthRed() - 1));
                            break;
                        case 3: // Increase Red Strength
                            art.setSignalStrengthRed(Math.min(15, art.signalStrengthRed() + 1));
                            break;
                    }
                }
            });
        }
    }

    public int getSignalStrengthGreen() {
        return this.data.get(0);
    }

    public int getSignalStrengthRed() {
        return this.data.get(1);
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}