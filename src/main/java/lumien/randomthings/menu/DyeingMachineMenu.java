package lumien.randomthings.menu;

import lumien.randomthings.blockentity.DyeingMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DyeingMachineMenu extends AbstractContainerMenu {
    private final DyeingMachineBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public DyeingMachineMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.DYEING_MACHINE.get(), containerId);
        this.blockEntity = resolve(playerInventory, pos);
        this.access = ContainerLevelAccess.create(this.blockEntity.getLevel(), this.blockEntity.getBlockPos());

        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), DyeingMachineBlockEntity.SLOT_INPUT, 27, 22));
        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), DyeingMachineBlockEntity.SLOT_DYE, 76, 22) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }
        });
        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), DyeingMachineBlockEntity.SLOT_OUTPUT, 133, 22) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
    }

    private static DyeingMachineBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof DyeingMachineBlockEntity machine) return machine;
        throw new IllegalStateException("Block entity at " + pos + " is not a DyeingMachineBlockEntity");
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 59 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 117));
        }
    }

    public DyeingMachineBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < 3) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (stack.getItem() instanceof DyeItem) {
                if (!this.moveItemStackTo(stack, DyeingMachineBlockEntity.SLOT_DYE, DyeingMachineBlockEntity.SLOT_DYE + 1, false)) {
                    if (!this.moveItemStackTo(stack, DyeingMachineBlockEntity.SLOT_INPUT, DyeingMachineBlockEntity.SLOT_INPUT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (!this.moveItemStackTo(stack, DyeingMachineBlockEntity.SLOT_INPUT, DyeingMachineBlockEntity.SLOT_INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
