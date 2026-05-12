package lumien.randomthings.blockentity;

import lumien.randomthings.recipe.DyeingMachineRecipe;
import lumien.randomthings.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import javax.annotation.Nullable;
import java.util.Optional;

public class DyeingMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_DYE = 1;
    public static final int SLOT_OUTPUT = 2;
    private static final int CRAFT_TICKS = 4;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_DYE -> stack.getItem() instanceof DyeItem;
                case SLOT_OUTPUT -> false;
                default -> true;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            DyeingMachineBlockEntity.this.setChanged();
            if (slot != SLOT_OUTPUT) {
                cachedRecipe = null;
            }
        }
    };

    @Nullable
    private RecipeHolder<DyeingMachineRecipe> cachedRecipe;
    private int tickCounter;

    public DyeingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.DYEING_MACHINE.get(), pos, state);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IItemHandler getSidedHandler(@Nullable Direction side) {
        if (side == null) return itemHandler;
        return switch (side) {
            case DOWN -> new RangedWrapper(itemHandler, SLOT_OUTPUT, SLOT_OUTPUT + 1);
            case UP -> new RangedWrapper(itemHandler, SLOT_INPUT, SLOT_INPUT + 1);
            default -> new RangedWrapper(itemHandler, SLOT_INPUT, SLOT_DYE + 1);
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DyeingMachineBlockEntity be) {
        if (level.isClientSide) return;
        be.tickCounter++;
        if (be.tickCounter < CRAFT_TICKS) return;
        be.tickCounter = 0;
        be.tryCraft();
    }

    private void tryCraft() {
        if (level == null) return;
        ItemStack input = itemHandler.getStackInSlot(SLOT_INPUT);
        ItemStack dye = itemHandler.getStackInSlot(SLOT_DYE);
        if (input.isEmpty() || dye.isEmpty()) return;

        DyeingMachineRecipe.Input recipeInput = new DyeingMachineRecipe.Input(input, dye);
        Optional<RecipeHolder<DyeingMachineRecipe>> match = findRecipe(recipeInput);
        if (match.isEmpty()) return;

        ItemStack result = match.get().value().assemble(recipeInput, level.registryAccess());
        if (result.isEmpty()) return;

        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(output, result)) return;
            if (output.getCount() + result.getCount() > output.getMaxStackSize()) return;
        }

        itemHandler.extractItem(SLOT_INPUT, 1, false);
        itemHandler.extractItem(SLOT_DYE, 1, false);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, result);
        } else {
            output.grow(result.getCount());
            itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        }
    }

    private Optional<RecipeHolder<DyeingMachineRecipe>> findRecipe(DyeingMachineRecipe.Input input) {
        if (level == null) return Optional.empty();
        if (cachedRecipe != null && cachedRecipe.value().matches(input, level)) {
            return Optional.of(cachedRecipe);
        }
        Optional<RecipeHolder<DyeingMachineRecipe>> match = level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.DYEING_MACHINE.get(), input, level);
        match.ifPresent(holder -> cachedRecipe = holder);
        return match;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
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

    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}
