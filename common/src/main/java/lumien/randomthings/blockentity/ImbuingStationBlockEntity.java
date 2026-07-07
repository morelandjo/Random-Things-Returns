package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.recipe.ImbuingRecipe;
import lumien.randomthings.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class ImbuingStationBlockEntity extends BlockEntity implements Container, ExtendedMenuProvider {

    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);

    private int imbuingProgress = 0;

    @Nullable
    private ImbuingRecipe cachedRecipe;

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? imbuingProgress : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) imbuingProgress = value;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public ImbuingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.IMBUING_STATION.get(), pos, state);
    }

    public int getImbuingProgress() {
        return imbuingProgress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ImbuingStationBlockEntity be) {
        if (level.isClientSide) return;
        be.tick(level);
    }

    private void tick(Level level) {
        Optional<ImbuingRecipe> match = findRecipe(level);
        if (match.isEmpty()) {
            imbuingProgress = 0;
            return;
        }

        ItemStack result = match.get().assemble(this, level.registryAccess());
        if (!canHoldOutput(result)) {
            imbuingProgress = 0;
            return;
        }

        imbuingProgress++;
        if (imbuingProgress >= ImbuingRecipe.CRAFT_TICKS) {
            imbuingProgress = 0;
            ItemStack output = items.get(ImbuingRecipe.SLOT_OUTPUT);
            if (output.isEmpty()) {
                items.set(ImbuingRecipe.SLOT_OUTPUT, result);
            } else {
                output.grow(result.getCount());
            }
            items.get(ImbuingRecipe.SLOT_INGREDIENT_1).shrink(1);
            items.get(ImbuingRecipe.SLOT_INGREDIENT_2).shrink(1);
            items.get(ImbuingRecipe.SLOT_INGREDIENT_3).shrink(1);
            items.get(ImbuingRecipe.SLOT_CENTRE).shrink(1);
        }
        setChanged();
    }

    private Optional<ImbuingRecipe> findRecipe(Level level) {
        if (cachedRecipe != null && cachedRecipe.matches(this, level)) {
            return Optional.of(cachedRecipe);
        }
        Optional<ImbuingRecipe> match = level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.IMBUING.get(), this, level);
        match.ifPresent(r -> cachedRecipe = r);
        return match;
    }

    private boolean canHoldOutput(ItemStack result) {
        if (result.isEmpty()) return false;
        ItemStack current = items.get(ImbuingRecipe.SLOT_OUTPUT);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameTags(current, result)) return false;
        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    // --- Container ---

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
            if (slot != ImbuingRecipe.SLOT_OUTPUT) cachedRecipe = null;
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
        if (slot != ImbuingRecipe.SLOT_OUTPUT) cachedRecipe = null;
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != ImbuingRecipe.SLOT_OUTPUT;
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
        tag.putInt("ImbuingProgress", imbuingProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        imbuingProgress = tag.getInt("ImbuingProgress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.imbuing_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new lumien.randomthings.menu.ImbuingStationMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
