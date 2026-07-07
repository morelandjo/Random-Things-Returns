package lumien.randomthings.menu;

import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChatDetectorMenu extends AbstractContainerMenu {
    private final ChatDetectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public ChatDetectorMenu(int containerId, Inventory playerInventory, ChatDetectorBlockEntity blockEntity) {
        super(ModMenuTypes.CHAT_DETECTOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /** Client-side constructor used by the Architectury extended menu factory. */
    public ChatDetectorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    public ChatDetectorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static ChatDetectorBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ChatDetectorBlockEntity chatDetector) {
            return chatDetector;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a ChatDetectorBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public ChatDetectorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
