package lumien.randomthings.blockentity;

import lumien.randomthings.block.IronDropperBlock;
import lumien.randomthings.menu.IronDropperMenu;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.util.BiomeColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class IronDropperBlockEntity extends BlockEntity implements MenuProvider {
    public enum RedstoneMode {
        PULSE, REPEAT_POWERED, REPEAT
    }

    public enum PickupDelay {
        NONE, TICKS_5, TICKS_20
    }

    public enum Effects {
        NONE, SOUND, PARTICLE, SOUND_PARTICLE
    }

    private RedstoneMode redstoneMode = RedstoneMode.REPEAT_POWERED;
    private PickupDelay pickupDelay = PickupDelay.TICKS_5;
    private Effects effects = Effects.NONE;
    private boolean randomMotion = false;

    private int dropCounter = 0;
    private boolean redstonePowered = false;
    private Component customName;

    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            IronDropperBlockEntity.this.setChanged();
        }
    };

    public IronDropperBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.IRON_DROPPER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IronDropperBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.dropCounter++;

        if (blockEntity.dropCounter % 4 == 0 && 
           (blockEntity.redstoneMode == RedstoneMode.REPEAT || 
            (blockEntity.redstoneMode == RedstoneMode.REPEAT_POWERED && blockEntity.redstonePowered))) {
            blockEntity.drop();
        }
    }

    private void drop() {
        if (level == null || level.isClientSide) return;

        Direction facing = getBlockState().getValue(IronDropperBlock.FACING);
        BlockPos blockPos = worldPosition.relative(facing);
        BlockEntity tileEntity = level.getBlockEntity(blockPos);

        int slot = -1;
        ItemStack stack = ItemStack.EMPTY;
        
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
                stack = slotStack;
                slot = i;
                break;
            }
        }

        if (slot == -1 || stack.isEmpty()) return;

        ItemStack toDrop = stack.copy();
        toDrop.setCount(1);

        if (tileEntity != null) {
            IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, facing.getOpposite());
            if (targetHandler != null) {
                ItemStack result = ItemHandlerHelper.insertItemStacked(targetHandler, toDrop, false);
                if (result.isEmpty()) {
                    itemHandler.extractItem(slot, 1, false);
                    playEffects(facing);
                    return;
                }
            }
        }

        itemHandler.extractItem(slot, 1, false);

        double posX = worldPosition.getX() + 0.5 + 0.7D * facing.getStepX();
        double posY = worldPosition.getY() + 0.5 + 0.7D * facing.getStepY();
        double posZ = worldPosition.getZ() + 0.5 + 0.7D * facing.getStepZ();

        if (facing.getAxis() == Direction.Axis.Y) {
            posY = posY - 0.125D;
        } else {
            posY = posY - 0.15625D;
        }

        ItemEntity entityItem = new ItemEntity(level, posX, posY, posZ, toDrop);

        int pickupDelayTicks = switch (pickupDelay) {
            case NONE -> 0;
            case TICKS_5 -> 5;
            case TICKS_20 -> 20;
        };

        entityItem.setPickUpDelay(pickupDelayTicks);

        double motionStrength;
        if (randomMotion) {
            motionStrength = level.random.nextDouble() * 0.1D + 0.2D;
        } else {
            motionStrength = 0.25D;
        }

        double motionX = facing.getStepX() * motionStrength;
        double motionY = 0.20000000298023224D;
        double motionZ = facing.getStepZ() * motionStrength;

        if (randomMotion) {
            double speed = 6.0;
            motionX += level.random.nextGaussian() * 0.007499999832361937D * speed;
            motionY += level.random.nextGaussian() * 0.007499999832361937D * speed;
            motionZ += level.random.nextGaussian() * 0.007499999832361937D * speed;
        } else {
            double speed = 6.0;
            motionX += facing.getStepX() * 0.5 * 0.007499999832361937D * speed;
            motionY += facing.getStepY() * 0.5 * 0.007499999832361937D * speed;
            motionZ += facing.getStepZ() * 0.5 * 0.007499999832361937D * speed;
        }

        entityItem.setDeltaMovement(motionX, motionY, motionZ);
        level.addFreshEntity(entityItem);

        playEffects(facing);
    }

    private void playEffects(Direction facing) {
        if (effects == Effects.SOUND || effects == Effects.SOUND_PARTICLE) {
            level.playSound(null, worldPosition, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (effects == Effects.PARTICLE || effects == Effects.SOUND_PARTICLE) {
            level.levelEvent(2000, worldPosition, facing.getStepX() + 1 + (facing.getStepZ() + 1) * 3);
        }
    }

    public void neighborChanged() {
        boolean newPowered = level.hasNeighborSignal(worldPosition);
        boolean changed = redstonePowered != newPowered;

        if (changed) {
            boolean oldState = redstonePowered;
            this.redstonePowered = newPowered;

            if (redstoneMode == RedstoneMode.PULSE && newPowered && !oldState) {
                drop();
            }

            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("redstoneMode", redstoneMode.ordinal());
        tag.putInt("pickupDelay", pickupDelay.ordinal());
        tag.putInt("effects", effects.ordinal());
        tag.putBoolean("randomMotion", randomMotion);
        tag.putInt("dropCounter", dropCounter);
        tag.putBoolean("redstonePowered", redstonePowered);
        
        tag.put("inventory", itemHandler.serializeNBT(registries));
        
        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName, registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (tag.contains("redstoneMode")) {
            redstoneMode = RedstoneMode.values()[tag.getInt("redstoneMode")];
        }
        if (tag.contains("pickupDelay")) {
            pickupDelay = PickupDelay.values()[tag.getInt("pickupDelay")];
        }
        if (tag.contains("effects")) {
            effects = Effects.values()[tag.getInt("effects")];
        }
        randomMotion = tag.getBoolean("randomMotion");
        dropCounter = tag.getInt("dropCounter");
        redstonePowered = tag.getBoolean("redstonePowered");
        
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        
        if (tag.contains("CustomName", 8)) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("container.randomthings.iron_dropper");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IronDropperMenu(containerId, playerInventory, this, worldPosition);
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode mode) {
        this.redstoneMode = mode;
        setChanged();
    }

    public PickupDelay getPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(PickupDelay delay) {
        this.pickupDelay = delay;
        setChanged();
    }

    public Effects getEffects() {
        return effects;
    }

    public void setEffects(Effects effects) {
        this.effects = effects;
        setChanged();
    }

    public boolean isRandomMotion() {
        return randomMotion;
    }

    public void setRandomMotion(boolean randomMotion) {
        this.randomMotion = randomMotion;
        setChanged();
    }

    public void rotateRedstoneMode() {
        this.redstoneMode = BiomeColorUtils.rotateEnum(redstoneMode);
        setChanged();
        syncToClient();
    }

    public void rotatePickupDelay() {
        this.pickupDelay = BiomeColorUtils.rotateEnum(pickupDelay);
        setChanged();
        syncToClient();
    }

    public void rotateRandomMotion() {
        this.randomMotion = !randomMotion;
        setChanged();
        syncToClient();
    }

    public void rotateEffects() {
        this.effects = BiomeColorUtils.rotateEnum(effects);
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}