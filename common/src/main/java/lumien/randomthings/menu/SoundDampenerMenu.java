package lumien.randomthings.menu;

import lumien.randomthings.blockentity.SoundDampenerBlockEntity;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.menu.slots.FilteredSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Nine Sound Pattern slots + player inventory. */
public class SoundDampenerMenu extends AbstractContainerMenu {
    private final SoundDampenerBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public SoundDampenerMenu(int containerId, Inventory playerInventory, SoundDampenerBlockEntity blockEntity) {
        super(ModMenuTypes.SOUND_DAMPENER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        for (int col = 0; col < SoundDampenerBlockEntity.SIZE; col++) {
            this.addSlot(new FilteredSlot(blockEntity.getPatterns(), col, 8 + col * 18, 18,
                stack -> stack.getItem() instanceof ItemSoundPattern));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    public SoundDampenerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    public SoundDampenerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    private static SoundDampenerBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof SoundDampenerBlockEntity dampener) {
            return dampener;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a SoundDampenerBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < SoundDampenerBlockEntity.SIZE) {
                if (!this.moveItemStackTo(stack, SoundDampenerBlockEntity.SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!(stack.getItem() instanceof ItemSoundPattern)
                || !this.moveItemStackTo(stack, 0, SoundDampenerBlockEntity.SIZE, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
}
