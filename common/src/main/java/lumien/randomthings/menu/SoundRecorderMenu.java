package lumien.randomthings.menu;

import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.menu.slots.FilteredSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Sound Recorder GUI: one Sound Pattern slot; clicking a recorded sound in the screen writes it
 * onto the inserted pattern (via {@code SoundRecorderWritePacket}).
 */
public class SoundRecorderMenu extends AbstractContainerMenu {
    private final ItemStack recorderStack;
    private final SimpleContainer patternSlot = new SimpleContainer(1);

    public SoundRecorderMenu(int containerId, Inventory playerInventory, ItemStack recorderStack) {
        super(ModMenuTypes.SOUND_RECORDER.get(), containerId);
        this.recorderStack = recorderStack;

        this.addSlot(new FilteredSlot(patternSlot, 0, 80, 90,
            stack -> stack.getItem() instanceof ItemSoundPattern));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 120 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            int slotIndex = col;
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 178) {
                @Override
                public boolean mayPickup(Player player) {
                    return playerInventory.getItem(slotIndex) != recorderStack;
                }
            });
        }
    }

    public SoundRecorderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    public ItemStack getRecorderStack() {
        return recorderStack;
    }

    /** Server-side: write the recorded sound at {@code soundIndex} onto the inserted pattern. */
    public void writeSound(int soundIndex) {
        List<ResourceLocation> sounds = SoundRecorderItem.getRecordedSounds(recorderStack);
        ItemStack pattern = patternSlot.getItem(0);
        if (soundIndex >= 0 && soundIndex < sounds.size() && pattern.getItem() instanceof ItemSoundPattern) {
            ItemSoundPattern.setSoundLocation(pattern, sounds.get(soundIndex));
            patternSlot.setChanged();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            ItemStack pattern = patternSlot.removeItemNoUpdate(0);
            if (!pattern.isEmpty() && !player.getInventory().add(pattern)) {
                player.drop(pattern, false);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().contains(recorderStack) || player.getOffhandItem() == recorderStack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!(stack.getItem() instanceof ItemSoundPattern)
                || !this.moveItemStackTo(stack, 0, 1, false)) {
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
