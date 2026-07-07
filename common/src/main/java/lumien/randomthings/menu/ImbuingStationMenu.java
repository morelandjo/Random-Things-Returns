package lumien.randomthings.menu;

import lumien.randomthings.blockentity.ImbuingStationBlockEntity;
import lumien.randomthings.recipe.ImbuingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ImbuingStationMenu extends AbstractContainerMenu {

    private final ImbuingStationBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client-side constructor used by the Architectury extended menu factory. */
    public ImbuingStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    public ImbuingStationMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.IMBUING_STATION.get(), containerId);
        this.blockEntity = resolve(playerInventory, pos);
        this.access = ContainerLevelAccess.create(this.blockEntity.getLevel(), this.blockEntity.getBlockPos());
        this.data = blockEntity.dataAccess;

        // Slot layout copied from upstream ContainerImbuingStation.
        this.addSlot(new Slot(blockEntity, ImbuingRecipe.SLOT_INGREDIENT_1, 80, 9));
        this.addSlot(new Slot(blockEntity, ImbuingRecipe.SLOT_INGREDIENT_2, 35, 54));
        this.addSlot(new Slot(blockEntity, ImbuingRecipe.SLOT_INGREDIENT_3, 80, 99));
        this.addSlot(new Slot(blockEntity, ImbuingRecipe.SLOT_CENTRE, 80, 54));
        this.addSlot(new Slot(blockEntity, ImbuingRecipe.SLOT_OUTPUT, 125, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
        this.addDataSlots(this.data);
    }

    private static ImbuingStationBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof ImbuingStationBlockEntity station) return station;
        throw new IllegalStateException("Block entity at " + pos + " is not an ImbuingStationBlockEntity");
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 126 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 184));
        }
    }

    public ImbuingStationBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getImbuingProgress() {
        return this.data.get(0);
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

        // Slots 0-4 = imbuing station; 5+ = player inventory.
        if (index < 5) {
            if (!this.moveItemStackTo(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 4, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
