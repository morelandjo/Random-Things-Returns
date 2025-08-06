package lumien.randomthings.menu;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.AdvancedRedstoneRepeaterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdvancedRedstoneRepeaterMenu extends AbstractContainerMenu implements ISignalContainer {
    ContainerLevelAccess access;

    public ContainerData data = new SimpleContainerData(2);

    AdvancedRedstoneRepeaterBlockEntity blockEntity;

    public AdvancedRedstoneRepeaterMenu(int windowId, FriendlyByteBuf extraData) {
        this(windowId, ContainerLevelAccess.NULL);
    }

    public AdvancedRedstoneRepeaterMenu(int windowId, ContainerLevelAccess access) {
        super(ModMenuTypes.ADVANCED_REDSTONE_REPEATER.get(), windowId);

        this.access = access;
        
        // Initialize the block entity reference
        this.access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdvancedRedstoneRepeaterBlockEntity arr) {
                this.blockEntity = arr;
                this.data.set(0, arr.getTurnOffDelay());
                this.data.set(1, arr.getTurnOnDelay());
            }
        });
        
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ADVANCED_REDSTONE_REPEATER.get()) || 
               stillValid(this.access, player, ModBlocks.ADVANCED_REDSTONE_REPEATER_POWERED.get());
    }

    @Override
    public void broadcastChanges() {
        this.access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof AdvancedRedstoneRepeaterBlockEntity arr) {
                this.data.set(0, arr.getTurnOffDelay());
                this.data.set(1, arr.getTurnOnDelay());
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

                if (be instanceof AdvancedRedstoneRepeaterBlockEntity arr) {
                    switch (action) {
                        case 0: // Decrease Turn Off Delay 1
                            arr.decreaseTurnOffDelay(1);
                            break;
                        case 1: // Increase Turn Off Delay 1
                            arr.increaseTurnOffDelay(1);
                            break;
                        case 2: // Decrease Turn On Delay 1
                            arr.decreaseTurnOnDelay(1);
                            break;
                        case 3: // Increase Turn On Delay 1
                            arr.increaseTurnOnDelay(1);
                            break;
                        case 4: // Decrease Turn Off Delay 10
                            arr.decreaseTurnOffDelay(10);
                            break;
                        case 5: // Increase Turn Off Delay 10
                            arr.increaseTurnOffDelay(10);
                            break;
                        case 6: // Decrease Turn On Delay 10
                            arr.decreaseTurnOnDelay(10);
                            break;
                        case 7: // Increase Turn On Delay 10
                            arr.increaseTurnOnDelay(10);
                            break;
                        case 8: // Decrease Turn Off Delay 100
                            arr.decreaseTurnOffDelay(100);
                            break;
                        case 9: // Increase Turn Off Delay 100
                            arr.increaseTurnOffDelay(100);
                            break;
                        case 10: // Decrease Turn On Delay 100
                            arr.decreaseTurnOnDelay(100);
                            break;
                        case 11: // Increase Turn On Delay 100
                            arr.increaseTurnOnDelay(100);
                            break;
                        case 12: // Decrease Turn Off Delay 1000
                            arr.decreaseTurnOffDelay(1000);
                            break;
                        case 13: // Increase Turn Off Delay 1000
                            arr.increaseTurnOffDelay(1000);
                            break;
                        case 14: // Decrease Turn On Delay 1000
                            arr.decreaseTurnOnDelay(1000);
                            break;
                        case 15: // Increase Turn On Delay 1000
                            arr.increaseTurnOnDelay(1000);
                            break;
                    }
                }
            });
        }
    }

    public int getTurnOffDelay() {
        return this.data.get(0);
    }

    public int getTurnOnDelay() {
        return this.data.get(1);
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}