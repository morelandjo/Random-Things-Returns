package lumien.randomthings.blockentity;

import lumien.randomthings.recipe.ImbuingRecipe;
import lumien.randomthings.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class ImbuingStationBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != ImbuingRecipe.SLOT_OUTPUT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            ImbuingStationBlockEntity.this.setChanged();
            if (slot != ImbuingRecipe.SLOT_OUTPUT) {
                cachedRecipe = null;
            }
        }
    };

    private int imbuingProgress = 0;

    @Nullable
    private RecipeHolder<ImbuingRecipe> cachedRecipe;

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

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public int getImbuingProgress() {
        return imbuingProgress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ImbuingStationBlockEntity be) {
        if (level.isClientSide) return;
        be.tick();
    }

    private void tick() {
        if (level == null) return;

        ImbuingRecipe.Input input = new ImbuingRecipe.Input(
            itemHandler.getStackInSlot(ImbuingRecipe.SLOT_INGREDIENT_1),
            itemHandler.getStackInSlot(ImbuingRecipe.SLOT_INGREDIENT_2),
            itemHandler.getStackInSlot(ImbuingRecipe.SLOT_INGREDIENT_3),
            itemHandler.getStackInSlot(ImbuingRecipe.SLOT_CENTRE)
        );

        Optional<RecipeHolder<ImbuingRecipe>> match = findRecipe(input);

        if (match.isEmpty()) {
            imbuingProgress = 0;
            return;
        }

        ItemStack result = match.get().value().assemble(input, level.registryAccess());
        if (!canHoldOutput(result)) {
            imbuingProgress = 0;
            return;
        }

        imbuingProgress++;
        if (imbuingProgress >= ImbuingRecipe.CRAFT_TICKS) {
            imbuingProgress = 0;
            ItemStack output = itemHandler.getStackInSlot(ImbuingRecipe.SLOT_OUTPUT);
            if (output.isEmpty()) {
                itemHandler.setStackInSlot(ImbuingRecipe.SLOT_OUTPUT, result);
            } else {
                output.grow(result.getCount());
                itemHandler.setStackInSlot(ImbuingRecipe.SLOT_OUTPUT, output);
            }
            itemHandler.extractItem(ImbuingRecipe.SLOT_INGREDIENT_1, 1, false);
            itemHandler.extractItem(ImbuingRecipe.SLOT_INGREDIENT_2, 1, false);
            itemHandler.extractItem(ImbuingRecipe.SLOT_INGREDIENT_3, 1, false);
            itemHandler.extractItem(ImbuingRecipe.SLOT_CENTRE, 1, false);
        }
        setChanged();
    }

    private Optional<RecipeHolder<ImbuingRecipe>> findRecipe(ImbuingRecipe.Input input) {
        if (level == null) return Optional.empty();
        if (cachedRecipe != null && cachedRecipe.value().matches(input, level)) {
            return Optional.of(cachedRecipe);
        }
        Optional<RecipeHolder<ImbuingRecipe>> match = level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.IMBUING.get(), input, level);
        match.ifPresent(holder -> cachedRecipe = holder);
        return match;
    }

    private boolean canHoldOutput(ItemStack result) {
        if (result.isEmpty()) return false;
        ItemStack current = itemHandler.getStackInSlot(ImbuingRecipe.SLOT_OUTPUT);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(current, result)) return false;
        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("ImbuingProgress", imbuingProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
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

    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}
