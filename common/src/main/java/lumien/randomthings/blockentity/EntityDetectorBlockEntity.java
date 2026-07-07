package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.block.EntityDetectorBlock;
import lumien.randomthings.item.EntityFilterItem;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class EntityDetectorBlockEntity extends BlockEntity implements ExtendedMenuProvider {

    public enum Filter {
        ALL("all"),
        LIVING("living"),
        ANIMAL("animal"),
        MONSTER("monster"),
        PLAYER("player"),
        ITEM("item"),
        CUSTOM("custom");

        private final String key;
        Filter(String key) { this.key = key; }
        public String translationKey() { return "gui.randomthings.entity_detector.filter." + key; }
    }

    public static final int MAX_RANGE = 10;

    // Synced state
    private Filter filter = Filter.ALL;
    private int rangeX = 5;
    private int rangeY = 5;
    private int rangeZ = 5;
    private boolean invert = false;
    private boolean strongOutput = false;
    private boolean powered = false;

    private final SimpleContainer filterSlot = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EntityDetectorBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    /**
     * Index mapping for menu ContainerData:
     *   0 → filter ordinal, 1 → rangeX, 2 → rangeY, 3 → rangeZ, 4 → invert, 5 → strongOutput
     */
    public final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> filter.ordinal();
                case 1 -> rangeX;
                case 2 -> rangeY;
                case 3 -> rangeZ;
                case 4 -> invert ? 1 : 0;
                case 5 -> strongOutput ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> filter = Filter.values()[Math.floorMod(value, Filter.values().length)];
                case 1 -> rangeX = clampRange(value);
                case 2 -> rangeY = clampRange(value);
                case 3 -> rangeZ = clampRange(value);
                case 4 -> invert = value != 0;
                case 5 -> strongOutput = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public EntityDetectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.ENTITY_DETECTOR.get(), pos, blockState);
    }

    public static int clampRange(int v) {
        return Math.max(0, Math.min(MAX_RANGE, v));
    }

    public Filter getFilter() { return filter; }
    public int getRangeX() { return rangeX; }
    public int getRangeY() { return rangeY; }
    public int getRangeZ() { return rangeZ; }
    public boolean isInvert() { return invert; }
    public boolean isStrongOutput() { return strongOutput; }
    public boolean isPowered() { return powered; }
    public SimpleContainer getFilterSlot() { return filterSlot; }

    public void cycleFilter() {
        int next = (filter.ordinal() + 1) % Filter.values().length;
        filter = Filter.values()[next];
        markChangedAndSync();
    }

    public void setRange(int axis, int value) {
        int clamped = clampRange(value);
        switch (axis) {
            case 0 -> rangeX = clamped;
            case 1 -> rangeY = clamped;
            case 2 -> rangeZ = clamped;
        }
        markChangedAndSync();
    }

    public void toggleInvert() { this.invert = !this.invert; markChangedAndSync(); }
    public void toggleStrongOutput() { this.strongOutput = !this.strongOutput; markChangedAndSync(); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EntityDetectorBlockEntity be) {
        if (level.getGameTime() % 10 != 0) return; // 2 Hz

        boolean newPowered = be.checkDetected(level, pos);
        if (newPowered != be.powered) {
            be.powered = newPowered;
            be.setChanged();
            if (state.getBlock() instanceof EntityDetectorBlock detectorBlock) {
                detectorBlock.notifyAllNeighbors(level, pos);
            }
        }
    }

    private boolean checkDetected(Level level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(rangeX, rangeY, rangeZ);
        Predicate<Entity> predicate = filterPredicate();
        List<Entity> hits = level.getEntitiesOfClass(Entity.class, box, predicate);
        boolean any = !hits.isEmpty();
        return invert != any;
    }

    private Predicate<Entity> filterPredicate() {
        return switch (filter) {
            case ALL -> e -> true;
            case LIVING -> e -> e instanceof LivingEntity;
            case ANIMAL -> e -> e instanceof Animal;
            case MONSTER -> e -> e instanceof Enemy;
            case PLAYER -> e -> e instanceof Player;
            case ITEM -> e -> e instanceof ItemEntity;
            case CUSTOM -> this::matchesCustomFilter;
        };
    }

    private boolean matchesCustomFilter(Entity entity) {
        ItemStack stack = filterSlot.getItem(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof EntityFilterItem)) {
            return false;
        }
        ResourceLocation filterKey = RTNbt.getResourceLocation(stack, RTDataKeys.ENTITY_TYPE_FILTER);
        if (filterKey == null) return false;
        EntityType<?> type = entity.getType();
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return filterKey.equals(entityKey);
    }

    private void markChangedAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private void writeState(CompoundTag tag) {
        tag.putInt("Filter", filter.ordinal());
        tag.putInt("RangeX", rangeX);
        tag.putInt("RangeY", rangeY);
        tag.putInt("RangeZ", rangeZ);
        tag.putBoolean("Invert", invert);
        tag.putBoolean("StrongOutput", strongOutput);
        ItemStack s = filterSlot.getItem(0);
        if (!s.isEmpty()) {
            tag.put("FilterItem", s.save(new CompoundTag()));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeState(tag);
        tag.putBoolean("Powered", powered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int filterOrdinal = tag.getInt("Filter");
        this.filter = filterOrdinal >= 0 && filterOrdinal < Filter.values().length ? Filter.values()[filterOrdinal] : Filter.ALL;
        this.rangeX = tag.contains("RangeX") ? clampRange(tag.getInt("RangeX")) : 5;
        this.rangeY = tag.contains("RangeY") ? clampRange(tag.getInt("RangeY")) : 5;
        this.rangeZ = tag.contains("RangeZ") ? clampRange(tag.getInt("RangeZ")) : 5;
        this.invert = tag.getBoolean("Invert");
        this.strongOutput = tag.getBoolean("StrongOutput");
        this.powered = tag.getBoolean("Powered");
        filterSlot.setItem(0, tag.contains("FilterItem") ? ItemStack.of(tag.getCompound("FilterItem")) : ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeState(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.entity_detector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new lumien.randomthings.menu.EntityDetectorMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null) {
            ItemStack stack = filterSlot.getItem(0);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack);
                filterSlot.setItem(0, ItemStack.EMPTY);
            }
        }
    }
}
