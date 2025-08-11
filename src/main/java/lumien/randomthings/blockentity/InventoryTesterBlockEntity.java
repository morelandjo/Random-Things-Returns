package lumien.randomthings.blockentity;

import lumien.randomthings.menu.InventoryTesterMenu;
import lumien.randomthings.block.InventoryTesterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.Packet;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class InventoryTesterBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
        
        @Override
        protected void onContentsChanged(int slot) {
            InventoryTesterBlockEntity.this.setChanged();
        }
    };
    
    private boolean emitRedstone = false;
    private boolean invertSignal = false;
    private int counter = 0;

    public InventoryTesterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.INVENTORY_TESTER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InventoryTesterBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        blockEntity.counter++;
        if (blockEntity.counter >= 2) { // Update every 2 ticks (10 times per second)
            blockEntity.counter = 0;
            blockEntity.updateRedstoneOutput();
        }
    }

    private void updateRedstoneOutput() {
        if (level == null) return;
        
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof InventoryTesterBlock)) return;
        
        Direction facing = state.getValue(InventoryTesterBlock.FACING);
        BlockPos adjacentPos = worldPosition.relative(facing.getOpposite());
        
        boolean canInsert = false;
        ItemStack testStack = itemHandler.getStackInSlot(0);
        
        if (!testStack.isEmpty()) {
            IItemHandler adjacentHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, adjacentPos, facing);
            
            if (adjacentHandler != null) {
                // Simulate insertion
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(adjacentHandler, testStack.copy(), true);
                canInsert = remainder.getCount() < testStack.getCount();
            }
        }
        
        boolean shouldEmit = invertSignal ? !canInsert : canInsert;
        
        if (shouldEmit != emitRedstone) {
            emitRedstone = shouldEmit;
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public void onNeighborChanged() {
        updateRedstoneOutput();
    }

    public int getRedstoneOutput() {
        return emitRedstone ? 15 : 0;
    }

    public boolean isInvertSignal() {
        return invertSignal;
    }

    public void setInvertSignal(boolean invert) {
        if (this.invertSignal != invert) {
            this.invertSignal = invert;
            setChanged();
            
            // Send update to client immediately
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            
            updateRedstoneOutput();
        }
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", itemHandler.serializeNBT(registries));
        tag.putBoolean("EmitRedstone", emitRedstone);
        tag.putBoolean("InvertSignal", invertSignal);
        tag.putInt("Counter", counter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("Items"));
        emitRedstone = tag.getBoolean("EmitRedstone");
        invertSignal = tag.getBoolean("InvertSignal");
        counter = tag.getInt("Counter");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("EmitRedstone", emitRedstone);
        tag.putBoolean("InvertSignal", invertSignal);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        emitRedstone = tag.getBoolean("EmitRedstone");
        invertSignal = tag.getBoolean("InvertSignal");
    }

    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.inventory_tester");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new InventoryTesterMenu(containerId, playerInventory, this);
    }
}