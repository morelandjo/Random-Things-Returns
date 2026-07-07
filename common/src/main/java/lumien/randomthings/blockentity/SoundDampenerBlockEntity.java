package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.menu.SoundDampenerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Mutes the sounds stored on its nine Sound Patterns for players within 20 blocks. The muted-sound
 * set is consulted client-side by {@code SoundMuteHandler} (via the SoundEngine mixin), so the
 * pattern inventory syncs through {@link #getUpdateTag}.
 */
public class SoundDampenerBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    public static final Set<SoundDampenerBlockEntity> DAMPENERS = Collections.newSetFromMap(new WeakHashMap<>());
    public static final int SIZE = 9;

    private final SimpleContainer patterns = new SimpleContainer(SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            SoundDampenerBlockEntity.this.onPatternsChanged();
        }
    };
    private final HashSet<ResourceLocation> mutedSounds = new HashSet<>();

    public SoundDampenerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SOUND_DAMPENER.get(), pos, blockState);
        synchronized (DAMPENERS) {
            DAMPENERS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        synchronized (DAMPENERS) {
            DAMPENERS.remove(this);
        }
    }

    public SimpleContainer getPatterns() {
        return patterns;
    }

    public HashSet<ResourceLocation> getMutedSounds() {
        return mutedSounds;
    }

    private void onPatternsChanged() {
        recomputeMutedSounds();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void recomputeMutedSounds() {
        mutedSounds.clear();
        for (int i = 0; i < patterns.getContainerSize(); i++) {
            ItemStack stack = patterns.getItem(i);
            if (stack.getItem() instanceof ItemSoundPattern) {
                ResourceLocation sound = ItemSoundPattern.getSoundLocation(stack);
                if (sound != null) {
                    mutedSounds.add(sound);
                }
            }
        }
    }

    private void savePatterns(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < patterns.getContainerSize(); i++) {
            list.add(patterns.getItem(i).save(new CompoundTag()));
        }
        tag.put("Patterns", list);
    }

    private void loadPatterns(CompoundTag tag) {
        ListTag list = tag.getList("Patterns", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(SIZE, list.size()); i++) {
            patterns.setItem(i, ItemStack.of(list.getCompound(i)));
        }
        recomputeMutedSounds();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        savePatterns(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadPatterns(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        savePatterns(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.sound_dampener");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SoundDampenerMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
