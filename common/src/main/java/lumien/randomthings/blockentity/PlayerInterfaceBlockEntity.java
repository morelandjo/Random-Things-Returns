package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Exposes the bound player's inventory to automation as a {@link WorldlyContainer} (the cross-loader
 * replacement for the 1.21.1 item-handler capability). Faces map to inventory sections: down=hotbar,
 * up=armor, north=offhand, sides=main inventory.
 */
public class PlayerInterfaceBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int SIZE = 41; // 0-8 hotbar, 9-35 main, 36-39 armor, 40 offhand
    private static final int[] HOTBAR = IntStream.range(0, 9).toArray();
    private static final int[] MAIN = IntStream.range(9, 36).toArray();
    private static final int[] ARMOR = IntStream.range(36, 40).toArray();
    private static final int[] OFFHAND = {40};
    private static final int[] NONE = {};

    private UUID playerUUID;

    public PlayerInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.PLAYER_INTERFACE.get(), pos, blockState);
    }

    @Nullable
    private Inventory getPlayerInventory() {
        if (this.playerUUID == null || level == null || level.isClientSide) {
            return null;
        }
        ServerPlayer player = ((ServerLevel) level).getServer().getPlayerList().getPlayer(playerUUID);
        return player != null ? player.getInventory() : null;
    }

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    // --- WorldlyContainer ---
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (getPlayerInventory() == null) {
            return NONE;
        }
        return switch (side) {
            case UP -> ARMOR;
            case DOWN -> HOTBAR;
            case NORTH -> OFFHAND;
            default -> MAIN;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        Inventory inv = getPlayerInventory();
        if (inv == null) {
            return true;
        }
        for (int i = 0; i < SIZE; i++) {
            if (!inv.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        Inventory inv = getPlayerInventory();
        return inv != null && slot >= 0 && slot < SIZE ? inv.getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        Inventory inv = getPlayerInventory();
        return inv != null ? inv.removeItem(slot, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        Inventory inv = getPlayerInventory();
        return inv != null ? inv.removeItemNoUpdate(slot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Inventory inv = getPlayerInventory();
        if (inv != null) {
            inv.setItem(slot, stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        Inventory inv = getPlayerInventory();
        if (inv != null) {
            inv.clearContent();
        }
    }

    // --- NBT / sync ---
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.playerUUID != null) {
            tag.putUUID("player_uuid", this.playerUUID);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("player_uuid")) {
            this.playerUUID = tag.getUUID("player_uuid");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (this.playerUUID != null) {
            tag.putUUID("player_uuid", this.playerUUID);
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
