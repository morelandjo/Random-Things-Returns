package lumien.randomthings.menu;

import lumien.randomthings.blockentity.PotionVaporizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PotionVaporizerMenu extends AbstractContainerMenu {
    private final PotionVaporizerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public PotionVaporizerMenu(int containerId, Inventory playerInventory, PotionVaporizerBlockEntity blockEntity) {
        super(ModMenuTypes.POTION_VAPORIZER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        
        // Use the BlockEntity as ContainerData for automatic synchronization
        this.data = blockEntity;
        this.addDataSlots(this.data);
        
        // Add slots for the PotionVaporizer
        IItemHandler itemHandler = blockEntity.getItemHandler();
        
        // Fuel slot (slot 0) - center bottom
        this.addSlot(new SlotItemHandler(itemHandler, 0, 80, 53));
        
        // Potion input slot (slot 1) - left side
        this.addSlot(new SlotItemHandler(itemHandler, 1, 29, 17));
        
        // Bottle output slot (slot 2) - right side
        this.addSlot(new SlotItemHandler(itemHandler, 2, 131, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Output slot - no manual placing
            }
        });
        
        // Add player inventory slots
        addPlayerInventory(playerInventory);
    }


    public PotionVaporizerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static PotionVaporizerBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PotionVaporizerBlockEntity potionVaporizer) {
            return potionVaporizer;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a PotionVaporizerBlockEntity!");
    }
    
    private void addPlayerInventory(Inventory playerInventory) {
        // Main inventory (3x9 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        
        // Hotbar (1x9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity == null) return false;
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public PotionVaporizerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
    
    // Getters for synchronized data
    public int getDurationLeft() {
        return this.data.get(0);
    }
    
    public int getDuration() {
        return this.data.get(1);
    }
    
    public int getColor() {
        return this.data.get(2);
    }
    
    public int getFuelBurn() {
        return this.data.get(5);
    }
    
    public int getFuelBurnTime() {
        return this.data.get(6);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            
            // If clicking on machine slots (0-2)
            if (index < 3) {
                // Try to move to player inventory
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // If clicking on player inventory, try to move to machine slots
                boolean moved = false;
                
                // Try fuel slot first (for fuel items)
                if (net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel(slotStack)) {
                    moved = this.moveItemStackTo(slotStack, 0, 1, false);
                }
                
                // Try potion slot (for potions)
                if (!moved && slotStack.is(net.minecraft.world.item.Items.POTION)) {
                    moved = this.moveItemStackTo(slotStack, 1, 2, false);
                }
                
                if (!moved) {
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
}