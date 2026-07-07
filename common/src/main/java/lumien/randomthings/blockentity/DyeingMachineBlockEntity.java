package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.recipe.DyeingMachineRecipe;
import lumien.randomthings.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class DyeingMachineBlockEntity extends BlockEntity implements WorldlyContainer, ExtendedMenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_DYE = 1;
    public static final int SLOT_OUTPUT = 2;
    private static final int CRAFT_TICKS = 4;

    private static final int[] SLOTS_DOWN = {SLOT_OUTPUT};
    private static final int[] SLOTS_UP = {SLOT_INPUT};
    private static final int[] SLOTS_SIDE = {SLOT_INPUT, SLOT_DYE};

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    @Nullable
    private DyeingMachineRecipe cachedRecipe;
    private int tickCounter;

    public DyeingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.DYEING_MACHINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DyeingMachineBlockEntity be) {
        if (level.isClientSide) return;
        be.tickCounter++;
        if (be.tickCounter < CRAFT_TICKS) return;
        be.tickCounter = 0;
        be.tryCraft(level);
    }

    private void tryCraft(Level level) {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack dye = items.get(SLOT_DYE);
        if (input.isEmpty() || dye.isEmpty()) return;

        Optional<DyeingMachineRecipe> match = findRecipe(level);
        if (match.isEmpty()) return;

        ItemStack result = match.get().assemble(this, level.registryAccess());
        if (result.isEmpty()) return;

        ItemStack output = items.get(SLOT_OUTPUT);
        if (!output.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(output, result)) return;
            if (output.getCount() + result.getCount() > output.getMaxStackSize()) return;
        }

        input.shrink(1);
        dye.shrink(1);
        if (output.isEmpty()) {
            items.set(SLOT_OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }
        setChanged();
    }

    private Optional<DyeingMachineRecipe> findRecipe(Level level) {
        if (cachedRecipe != null && cachedRecipe.matches(this, level)) {
            return Optional.of(cachedRecipe);
        }
        Optional<DyeingMachineRecipe> match = level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.DYEING_MACHINE.get(), this, level);
        match.ifPresent(r -> cachedRecipe = r);
        return match;
    }

    // --- Container / WorldlyContainer ---

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            cachedRecipe = null;
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        if (slot != SLOT_OUTPUT) {
            cachedRecipe = null;
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_DYE -> stack.getItem() instanceof DyeItem;
            case SLOT_OUTPUT -> false;
            default -> true;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> SLOTS_DOWN;
            case UP -> SLOTS_UP;
            default -> SLOTS_SIDE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.dyeing_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new lumien.randomthings.menu.DyeingMachineMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
