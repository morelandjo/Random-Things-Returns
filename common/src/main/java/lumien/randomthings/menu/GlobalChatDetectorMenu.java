package lumien.randomthings.menu;

import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import lumien.randomthings.item.IdCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GlobalChatDetectorMenu extends AbstractContainerMenu {
    private final GlobalChatDetectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public GlobalChatDetectorMenu(int containerId, Inventory playerInventory, GlobalChatDetectorBlockEntity blockEntity) {
        super(ModMenuTypes.GLOBAL_CHAT_DETECTOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // 9 ID Card slots.
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(blockEntity, i, 8 + i * 18, 49) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof IdCardItem;
                }
            });
        }

        // Player inventory (slots 9-35)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar (slots 36-44)
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    /** Client-side constructor used by the Architectury extended menu factory. */
    public GlobalChatDetectorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    public GlobalChatDetectorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static GlobalChatDetectorBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof GlobalChatDetectorBlockEntity globalChatDetector) {
            return globalChatDetector;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a GlobalChatDetectorBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public GlobalChatDetectorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotIndex >= 0 && slotIndex < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= 9 && slotIndex < 45) {
                if (itemstack1.getItem() instanceof IdCardItem) {
                    if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
