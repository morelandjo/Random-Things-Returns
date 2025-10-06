package lumien.randomthings.menu;

import lumien.randomthings.blockentity.InventoryTesterBlockEntity;
import lumien.randomthings.menu.slot.ItemHandlerGhostSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

public class InventoryTesterMenu extends AbstractContainerMenu {
    private final InventoryTesterBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public InventoryTesterMenu(int containerId, Inventory playerInventory, InventoryTesterBlockEntity blockEntity) {
        super(ModMenuTypes.INVENTORY_TESTER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // Add the ghost slot for the test item
        this.addSlot(new ItemHandlerGhostSlot(blockEntity.getItemHandler(), 0, 64, 18));

        // Add player inventory slots (3x9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 54 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 112));
        }
    }

    public InventoryTesterMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static InventoryTesterBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof InventoryTesterBlockEntity inventoryTester) {
            return inventoryTester;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not an InventoryTesterBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity == null || this.blockEntity.isRemoved()) {
            return false;
        }
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public InventoryTesterBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No shift-clicking for inventory tester
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0 && blockEntity != null && !blockEntity.isRemoved()) {
            // Toggle invert signal
            blockEntity.setInvertSignal(!blockEntity.isInvertSignal());
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Clean up any remaining references
        this.access.execute((level, pos) -> {
            // Nothing specific to do here, just ensure cleanup
        });
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId) instanceof ItemHandlerGhostSlot) {
            ItemHandlerGhostSlot ghostSlot = (ItemHandlerGhostSlot) this.slots.get(slotId);
            ItemStack carried = getCarried();
            
            if (clickType == net.minecraft.world.inventory.ClickType.PICKUP) {
                if (button == 0) { // Left click
                    if (!carried.isEmpty()) {
                        // Set ghost slot to carried item with count 1
                        ItemStack newStack = carried.copy();
                        newStack.setCount(1);
                        ghostSlot.set(newStack);
                    } else {
                        // Clear ghost slot
                        ghostSlot.set(ItemStack.EMPTY);
                    }
                } else if (button == 1) { // Right click
                    ItemStack current = ghostSlot.getItem();
                    if (!current.isEmpty() && current.getCount() < current.getMaxStackSize()) {
                        // Increase stack size
                        ItemStack newStack = current.copy();
                        newStack.setCount(Math.min(current.getCount() + 1, current.getMaxStackSize()));
                        ghostSlot.set(newStack);
                    }
                }
            }
            return;
        }
        
        super.clicked(slotId, button, clickType, player);
    }
}