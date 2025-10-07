package lumien.randomthings.menu;

import lumien.randomthings.blockentity.SoundDampenerBlockEntity;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SoundDampenerMenu extends AbstractContainerMenu {
    private final SoundDampenerBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public SoundDampenerMenu(int containerId, Inventory playerInventory, SoundDampenerBlockEntity blockEntity) {
        super(ModMenuTypes.SOUND_DAMPENER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // Add Sound Pattern slots (9 slots in a row)
        IItemHandler itemHandler = blockEntity.getInventory();
        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(itemHandler, col, 8 + col * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // Only accept filled Sound Pattern items
                    if (stack.isEmpty()) return true;
                    if (!stack.is(ModItems.SOUND_PATTERN.get())) return false;
                    return ItemSoundPattern.getSoundLocation(stack) != null;
                }
            });
        }

        // Add player inventory slots
        addPlayerInventory(playerInventory);
    }

    public SoundDampenerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static SoundDampenerBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SoundDampenerBlockEntity dampener) {
            return dampener;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a SoundDampenerBlockEntity!");
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Main inventory (3x9 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Hotbar (1x9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From dampener inventory to player inventory
            if (index < 9) {
                if (!moveItemStackTo(slotStack, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to dampener inventory
            else if (index >= 9 && !moveItemStackTo(slotStack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity == null) return false;
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public SoundDampenerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
