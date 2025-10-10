package lumien.randomthings.blockentity;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityRuneBase extends BlockEntity {
    private int[][] runeData = new int[4][4];

    public BlockEntityRuneBase(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.RUNE_BASE.get(), pos, state);

        // Initialize with -1 (empty)
        for (int a = 0; a < 4; a++) {
            for (int b = 0; b < 4; b++) {
                runeData[a][b] = -1;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag runeList = new ListTag();
        for (int i = 0; i < runeData.length; i++) {
            runeList.add(new IntArrayTag(runeData[i]));
        }

        tag.put("runeData", runeList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        ListTag runeList = tag.getList("runeData", Tag.TAG_INT_ARRAY);

        for (int i = 0; i < runeList.size() && i < runeData.length; i++) {
            runeData[i] = runeList.getIntArray(i);
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

    public int[][] getRuneData() {
        return runeData;
    }

    public void setRuneData(int[][] newData) {
        this.runeData = newData;
        setChanged();
    }

    /**
     * Drops all rune dust items at their positions
     */
    public void dropRuneDust() {
        if (level == null || level.isClientSide) {
            return;
        }

        for (int x = 0; x < runeData.length; x++) {
            for (int z = 0; z < runeData[0].length; z++) {
                int rune = runeData[x][z];

                if (rune != -1 && rune < DyeColor.values().length) {
                    ItemStack dustStack = new ItemStack(ModItems.RUNE_DUST.get(), 1);
                    dustStack.set(lumien.randomthings.item.ModDataComponents.RUNE_COLOR.get(), DyeColor.byId(rune));

                    net.minecraft.world.entity.item.ItemEntity entityItem =
                        new net.minecraft.world.entity.item.ItemEntity(
                            level,
                            worldPosition.getX() + x / 4.0,
                            worldPosition.getY() + 0.1,
                            worldPosition.getZ() + z / 4.0,
                            dustStack
                        );
                    entityItem.setDefaultPickUpDelay();
                    level.addFreshEntity(entityItem);
                }
            }
        }
    }

    /**
     * Checks if all cells are empty
     */
    public boolean isEmpty() {
        for (int x = 0; x < runeData.length; x++) {
            for (int z = 0; z < runeData[0].length; z++) {
                if (runeData[x][z] != -1) {
                    return false;
                }
            }
        }
        return true;
    }
}
