package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.block.IronDropperBlock;
import lumien.randomthings.menu.IronDropperMenu;
import lumien.randomthings.util.RTContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dropper-like block: ejects one item every few ticks (or on a redstone pulse) into the inventory it
 * faces, or out into the world. Backed by a vanilla {@link Container} so it works on both loaders;
 * targets are reached through {@link RTContainers} instead of Forge capabilities.
 */
public class IronDropperBlockEntity extends BlockEntity implements Container, ExtendedMenuProvider {

    public enum RedstoneMode { PULSE, REPEAT_POWERED, REPEAT }
    public enum PickupDelay { NONE, TICKS_5, TICKS_20 }
    public enum Effects { NONE, SOUND, PARTICLE, SOUND_PARTICLE }

    private RedstoneMode redstoneMode = RedstoneMode.REPEAT_POWERED;
    private PickupDelay pickupDelay = PickupDelay.TICKS_5;
    private Effects effects = Effects.NONE;
    private boolean randomMotion = false;
    private int dropCounter = 0;
    private boolean redstonePowered = false;
    private Component customName;
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public IronDropperBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.IRON_DROPPER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IronDropperBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.dropCounter++;
        if (be.dropCounter % 4 == 0
            && (be.redstoneMode == RedstoneMode.REPEAT
                || (be.redstoneMode == RedstoneMode.REPEAT_POWERED && be.redstonePowered))) {
            be.drop();
        }
    }

    private void drop() {
        if (level == null || level.isClientSide) {
            return;
        }
        int slot = -1;
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            return;
        }
        Direction facing = getBlockState().getValue(IronDropperBlock.FACING);
        ItemStack toDrop = items.get(slot).copy();
        toDrop.setCount(1);

        Container target = RTContainers.getContainerAt(level, worldPosition.relative(facing));
        if (target != null) {
            ItemStack remainder = RTContainers.insert(target, toDrop.copy(), facing.getOpposite());
            if (remainder.isEmpty()) {
                items.get(slot).shrink(1);
                setChanged();
                playEffects(facing);
                return;
            }
        }

        items.get(slot).shrink(1);
        setChanged();
        spawnItem(facing, toDrop);
        playEffects(facing);
    }

    private void spawnItem(Direction facing, ItemStack toDrop) {
        double posX = worldPosition.getX() + 0.5 + 0.7D * facing.getStepX();
        double posY = worldPosition.getY() + 0.5 + 0.7D * facing.getStepY();
        double posZ = worldPosition.getZ() + 0.5 + 0.7D * facing.getStepZ();
        posY -= facing.getAxis() == Direction.Axis.Y ? 0.125D : 0.15625D;

        ItemEntity entity = new ItemEntity(level, posX, posY, posZ, toDrop);
        entity.setPickUpDelay(switch (pickupDelay) {
            case NONE -> 0;
            case TICKS_5 -> 5;
            case TICKS_20 -> 20;
        });
        double strength = randomMotion ? level.random.nextDouble() * 0.1D + 0.2D : 0.25D;
        double mx = facing.getStepX() * strength;
        double my = 0.2D;
        double mz = facing.getStepZ() * strength;
        if (randomMotion) {
            mx += level.random.nextGaussian() * 0.045D;
            my += level.random.nextGaussian() * 0.045D;
            mz += level.random.nextGaussian() * 0.045D;
        } else {
            mx += facing.getStepX() * 0.0225D;
            my += facing.getStepY() * 0.0225D;
            mz += facing.getStepZ() * 0.0225D;
        }
        entity.setDeltaMovement(mx, my, mz);
        level.addFreshEntity(entity);
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
        if (level == null) {
            return;
        }
        boolean newPowered = level.hasNeighborSignal(worldPosition);
        if (redstonePowered != newPowered) {
            boolean wasPowered = redstonePowered;
            this.redstonePowered = newPowered;
            if (redstoneMode == RedstoneMode.PULSE && newPowered && !wasPowered) {
                drop();
            }
            setChanged();
        }
    }

    // --- config ---
    public RedstoneMode getRedstoneMode() { return redstoneMode; }
    public PickupDelay getPickupDelay() { return pickupDelay; }
    public Effects getEffects() { return effects; }
    public boolean isRandomMotion() { return randomMotion; }

    private static <T extends Enum<T>> T rotate(T value) {
        T[] vals = value.getDeclaringClass().getEnumConstants();
        return vals[(value.ordinal() + 1) % vals.length];
    }

    public void rotateRedstoneMode() { redstoneMode = rotate(redstoneMode); syncToClient(); }
    public void rotatePickupDelay() { pickupDelay = rotate(pickupDelay); syncToClient(); }
    public void rotateEffects() { effects = rotate(effects); syncToClient(); }
    public void rotateRandomMotion() { randomMotion = !randomMotion; syncToClient(); }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    // --- Container ---
    @Override
    public int getContainerSize() { return items.size(); }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
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
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // --- NBT / sync ---
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("redstoneMode", redstoneMode.ordinal());
        tag.putInt("pickupDelay", pickupDelay.ordinal());
        tag.putInt("effects", effects.ordinal());
        tag.putBoolean("randomMotion", randomMotion);
        tag.putInt("dropCounter", dropCounter);
        tag.putBoolean("redstonePowered", redstonePowered);
        ContainerHelper.saveAllItems(tag, items);
        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        redstoneMode = RedstoneMode.values()[Math.floorMod(tag.getInt("redstoneMode"), RedstoneMode.values().length)];
        pickupDelay = PickupDelay.values()[Math.floorMod(tag.getInt("pickupDelay"), PickupDelay.values().length)];
        effects = Effects.values()[Math.floorMod(tag.getInt("effects"), Effects.values().length)];
        randomMotion = tag.getBoolean("randomMotion");
        dropCounter = tag.getInt("dropCounter");
        redstonePowered = tag.getBoolean("redstonePowered");
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        customName = tag.contains("CustomName", 8) ? Component.Serializer.fromJson(tag.getString("CustomName")) : null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("redstoneMode", redstoneMode.ordinal());
        tag.putInt("pickupDelay", pickupDelay.ordinal());
        tag.putInt("effects", effects.ordinal());
        tag.putBoolean("randomMotion", randomMotion);
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

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
