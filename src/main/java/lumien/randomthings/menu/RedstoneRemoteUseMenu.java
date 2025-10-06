package lumien.randomthings.menu;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for using the Redstone Remote.
 * This is a simple menu with no slots - the GUI will display buttons dynamically.
 */
public class RedstoneRemoteUseMenu extends AbstractContainerMenu {
    private final Container remoteInventory;
    private final ItemStack remoteStack;
    private final InteractionHand hand;

    public RedstoneRemoteUseMenu(int id, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.REDSTONE_REMOTE_USE.get(), id);
        this.hand = hand;
        this.remoteStack = playerInventory.player.getItemInHand(hand);

        if (!remoteStack.isEmpty() && remoteStack.is(ModItems.REDSTONE_REMOTE.get())) {
            this.remoteInventory = new ItemStackInventory(remoteStack, 18);
        } else {
            this.remoteInventory = null;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No slots to shift-click
    }

    @Override
    public boolean stillValid(Player player) {
        return !remoteStack.isEmpty() && player.getItemInHand(hand) == remoteStack;
    }

    public Container getRemoteInventory() {
        return remoteInventory;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
