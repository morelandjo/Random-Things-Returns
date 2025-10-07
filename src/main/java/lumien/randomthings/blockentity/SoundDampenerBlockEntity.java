package lumien.randomthings.blockentity;

import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.menu.SoundDampenerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class SoundDampenerBlockEntity extends BlockEntity implements MenuProvider {
    // Static set of all active sound dampeners for the event handler to check
    public static Set<SoundDampenerBlockEntity> DAMPENERS = Collections.newSetFromMap(new WeakHashMap<>());

    private static final int INVENTORY_SIZE = 9;
    private final ItemStackHandler inventory;
    private HashSet<ResourceLocation> mutedSounds;

    public SoundDampenerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SOUND_DAMPENER.get(), pos, blockState);

        this.mutedSounds = new HashSet<>();
        this.inventory = new ItemStackHandler(INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                updateMutedSounds();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                // Only accept filled Sound Pattern items
                if (stack.isEmpty()) return true;
                if (stack.getItem() != ModItems.SOUND_PATTERN.get()) return false;
                return ItemSoundPattern.getSoundLocation(stack) != null;
            }
        };

        // Register this dampener
        synchronized (DAMPENERS) {
            DAMPENERS.add(this);
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public HashSet<ResourceLocation> getMutedSounds() {
        return mutedSounds;
    }

    private void updateMutedSounds() {
        mutedSounds.clear();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() == ModItems.SOUND_PATTERN.get()) {
                ResourceLocation sound = ItemSoundPattern.getSoundLocation(stack);
                if (sound != null) {
                    mutedSounds.add(sound);
                }
            }
        }

        // Sync to client if needed
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }

        // Load muted sounds
        if (tag.contains("MutedSounds", Tag.TAG_LIST)) {
            ListTag soundList = tag.getList("MutedSounds", Tag.TAG_STRING);
            mutedSounds.clear();
            for (int i = 0; i < soundList.size(); i++) {
                String soundString = soundList.getString(i);
                mutedSounds.add(ResourceLocation.parse(soundString));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Inventory", inventory.serializeNBT(registries));

        // Save muted sounds
        ListTag soundList = new ListTag();
        for (ResourceLocation sound : mutedSounds) {
            soundList.add(StringTag.valueOf(sound.toString()));
        }
        tag.put("MutedSounds", soundList);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Unregister this dampener
        synchronized (DAMPENERS) {
            DAMPENERS.remove(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        // Unregister this dampener
        synchronized (DAMPENERS) {
            DAMPENERS.remove(this);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.sound_dampener");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SoundDampenerMenu(containerId, playerInventory, this.worldPosition);
    }
}
