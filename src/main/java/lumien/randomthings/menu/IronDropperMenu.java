package lumien.randomthings.menu;

import lumien.randomthings.blockentity.IronDropperBlockEntity;
import lumien.randomthings.network.RTPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class IronDropperMenu extends AbstractContainerMenu {
    private final IronDropperBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    public IronDropperMenu(int containerId, Inventory playerInventory, IronDropperBlockEntity blockEntity, BlockPos pos) {
        super(ModMenuTypes.IRON_DROPPER.get(), containerId);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(playerInventory.player.level(), pos);

        // Iron Dropper inventory (3x3 grid)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        // Player inventory
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        // Player hotbar
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(levelAccess, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < 9) {
                // From dropper to player inventory
                if (!this.moveItemStackTo(slotStack, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 9) {
                // From player inventory to dropper - only allow one item at a time
                if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();

                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack)) {
                    int j = slotStack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.setCount(0);
                        slotStack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (slotStack.getCount() < maxSize) {
                        stack.shrink(maxSize - slotStack.getCount());
                        slotStack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();

                if (slotStack.isEmpty() && slot.mayPlace(stack)) {
                    if (startIndex < 9) {
                        // Moving to dropper slots - only allow one item per slot
                        if (stack.getCount() > 1) {
                            ItemStack copy = stack.copy();
                            copy.setCount(1);
                            slot.setByPlayer(copy);
                            stack.shrink(1);
                            flag = true;
                            break;
                        } else {
                            slot.setByPlayer(stack.copy());
                            slot.setChanged();
                            stack.setCount(0);
                            flag = true;
                            break;
                        }
                    } else {
                        // Moving to player inventory - normal stacking
                        int maxStackSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                        if (stack.getCount() <= maxStackSize) {
                            slot.setByPlayer(stack.copy());
                            slot.setChanged();
                            stack.setCount(0);
                            flag = true;
                            break;
                        } else {
                            ItemStack copy = stack.copy();
                            copy.setCount(maxStackSize);
                            slot.setByPlayer(copy);
                            slot.setChanged();
                            stack.shrink(maxStackSize);
                            flag = true;
                        }
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    public IronDropperBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public void handleButtonPress(int buttonId) {
        switch (buttonId) {
            case 0:
                blockEntity.rotateRedstoneMode();
                break;
            case 1:
                blockEntity.rotatePickupDelay();
                break;
            case 2:
                blockEntity.rotateRandomMotion();
                break;
            case 3:
                blockEntity.rotateEffects();
                break;
        }
    }
}