package lumien.randomthings.blockentity;

import lumien.randomthings.block.PotionVaporizerBlock;
import lumien.randomthings.menu.PotionVaporizerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PotionVaporizerBlockEntity extends BlockEntity implements MenuProvider, Container, net.minecraft.world.inventory.ContainerData {
    private static final int MAX_BLOCKS = 100;
    
    // Inventory slots: 0=fuel, 1=potion input, 2=bottle output
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            PotionVaporizerBlockEntity.this.setChanged();
        }
        
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel(stack); // Fuel slot
                case 1 -> stack.is(Items.POTION) && isValidPotion(stack); // Potion slot
                case 2 -> stack.is(Items.GLASS_BOTTLE); // Glass bottle output
                default -> false;
            };
        }
    };
    
    // Room detection
    private HashSet<BlockPos> affectedBlocks = new HashSet<>();
    private HashSet<BlockPos> validBlocks = new HashSet<>();
    private HashSet<BlockPos> checkedBlocks = new HashSet<>();
    private ArrayList<BlockPos> toBeChecked = new ArrayList<>();
    private int checkCounter = 0;
    private boolean firstCheck = true;
    
    // Potion effect
    private PotionContents currentPotionEffect = null;
    private int durationLeft = 1;
    
    // Fuel
    private int fuelBurnTime = 0;
    private int fuelBurn = 0;
    
    public PotionVaporizerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.POTION_VAPORIZER.get(), pos, blockState);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, PotionVaporizerBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        // Room detection steps
        int roomSteps = blockEntity.affectedBlocks.size() > 0 ? 2 : 5;
        for (int i = 0; i < roomSteps; i++) {
            blockEntity.stepRoomDetection();
        }
        
        blockEntity.stepFuel();
        blockEntity.stepPotionTank();
        
        if (blockEntity.fuelBurnTime > 0 && blockEntity.affectedBlocks.size() > 0) {
            blockEntity.stepPotionEffect(level);
            blockEntity.spawnParticles(level);
        }
    }
    
    private boolean isValidPotion(ItemStack stack) {
        PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (contents == null) return false;
        
        for (net.minecraft.world.effect.MobEffectInstance effect : contents.getAllEffects()) {
            if (!effect.getEffect().value().isInstantenous()) {
                return true;
            }
        }
        return false;
    }
    
    private void stepRoomDetection() {
        if (firstCheck) {
            Direction facing = getBlockState().getValue(PotionVaporizerBlock.FACING);
            toBeChecked.add(this.worldPosition.relative(facing));
            firstCheck = false;
        }
        
        if (checkCounter > MAX_BLOCKS) {
            affectedBlocks.clear();
            validBlocks.clear();
            reset();
        } else {
            if (!toBeChecked.isEmpty()) {
                BlockPos toCheck = toBeChecked.remove(0);
                if (!checkedBlocks.contains(toCheck)) {
                    checkedBlocks.add(toCheck);
                    if (level != null && level.isLoaded(toCheck) && level.isEmptyBlock(toCheck)) {
                        validBlocks.add(toCheck);
                        checkCounter++;
                        
                        for (Direction facing : Direction.values()) {
                            BlockPos nextPos = toCheck.relative(facing);
                            if (!checkedBlocks.contains(nextPos)) {
                                toBeChecked.add(nextPos);
                            }
                        }
                    }
                }
            } else {
                reset();
            }
        }
    }
    
    private void reset() {
        affectedBlocks.clear();
        affectedBlocks.addAll(validBlocks);
        
        checkCounter = 0;
        toBeChecked.clear();
        validBlocks.clear();
        checkedBlocks.clear();
        
        Direction facing = getBlockState().getValue(PotionVaporizerBlock.FACING);
        toBeChecked.add(this.worldPosition.relative(facing));
    }
    
    private void stepPotionTank() {
        // Only consume potions when fuel is actively burning and a room is detected
        if (currentPotionEffect == null && fuelBurnTime > 0 && affectedBlocks.size() > 0) {
            ItemStack newPotion = itemHandler.getStackInSlot(1);
            
            if (!newPotion.isEmpty()) {
                ItemStack output = itemHandler.getStackInSlot(2);
                
                if (output.isEmpty() || output.getCount() < 64) {
                    PotionContents contents = newPotion.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                    
                    boolean hasValidEffect = false;
                    int firstEffectDuration = 0;
                    for (net.minecraft.world.effect.MobEffectInstance effect : contents.getAllEffects()) {
                        if (!effect.getEffect().value().isInstantenous()) {
                            hasValidEffect = true;
                            if (firstEffectDuration == 0) {
                                firstEffectDuration = effect.getDuration();
                            }
                        }
                    }
                    
                    if (hasValidEffect) {
                        currentPotionEffect = contents;
                        durationLeft = firstEffectDuration;
                        
                        itemHandler.extractItem(1, 1, false);
                        
                        if (!output.isEmpty()) {
                            output.grow(1);
                        } else {
                            itemHandler.insertItem(2, new ItemStack(Items.GLASS_BOTTLE), false);
                        }
                    }
                }
            }
        }
    }
    
    private void stepFuel() {
        if (fuelBurnTime > 0) {
            fuelBurnTime--;
        } else {
            // Start burning fuel if there's a potion to process or currently processing
            boolean needsFuel = (currentPotionEffect != null && durationLeft > 0) || 
                              (!itemHandler.getStackInSlot(1).isEmpty() && affectedBlocks.size() > 0);
            
            if (needsFuel && !itemHandler.getStackInSlot(0).isEmpty()) {
                ItemStack fuel = itemHandler.getStackInSlot(0);
                Integer burnTime = net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel().get(fuel.getItem());
                if (burnTime != null && burnTime > 0) {
                    fuelBurnTime = fuelBurn = burnTime;
                    itemHandler.extractItem(0, 1, false);
                }
            }
        }
    }
    
    private void stepPotionEffect(Level level) {
        if (currentPotionEffect != null) {
            durationLeft--;
            
            // Apply potion effects to all living entities in affected blocks
            AABB[] bbs = new AABB[affectedBlocks.size()];
            int counter = 0;
            
            for (BlockPos pos : affectedBlocks) {
                bbs[counter] = new AABB(pos.getX(), pos.getY(), pos.getZ(), 
                                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                counter++;
            }
            
            for (AABB bb : bbs) {
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bb);
                for (LivingEntity entity : entities) {
                    // Apply effects from the potion
                    currentPotionEffect.getAllEffects().forEach(effect -> {
                        entity.addEffect(effect);
                    });
                }
            }
        }
        
        if (durationLeft <= 0) {
            currentPotionEffect = null;
        }
    }
    
    private void spawnParticles(Level level) {
        if (currentPotionEffect != null && level.getGameTime() % 5 == 0) {
            // Spawn particles in affected blocks
            for (BlockPos pos : affectedBlocks) {
                if (level.random.nextInt(4) == 0) {
                    double x = pos.getX() + level.random.nextDouble();
                    double y = pos.getY() + level.random.nextDouble();
                    double z = pos.getZ() + level.random.nextDouble();
                    
                    // Use dust particles with potion color
                    int color = currentPotionEffect.getColor();
                    float r = ((color >> 16) & 0xFF) / 255.0f;
                    float g = ((color >> 8) & 0xFF) / 255.0f;
                    float b = (color & 0xFF) / 255.0f;
                    
                    level.addParticle(new DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.0f), 
                                     x, y, z, 0, 0, 0);
                }
            }
        }
    }
    
    // NBT Serialization
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("DurationLeft", durationLeft);
        tag.putInt("FuelBurn", fuelBurn);
        tag.putInt("FuelBurnTime", fuelBurnTime);
        
        if (currentPotionEffect != null) {
            // Store potion color as a simple workaround for now
            tag.putInt("PotionColor", currentPotionEffect.getColor());
        }
        
        ListTag affectedBlocksList = new ListTag();
        for (BlockPos pos : affectedBlocks) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("posX", pos.getX());
            posTag.putInt("posY", pos.getY());
            posTag.putInt("posZ", pos.getZ());
            affectedBlocksList.add(posTag);
        }
        tag.put("AffectedBlocks", affectedBlocksList);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        durationLeft = tag.getInt("DurationLeft");
        fuelBurn = tag.getInt("FuelBurn");
        fuelBurnTime = tag.getInt("FuelBurnTime");
        
        if (tag.contains("PotionColor")) {
            // For now, we'll just reset to null and let it reload from potion items
            currentPotionEffect = null;
        }
        
        ListTag affectedBlocksList = tag.getList("AffectedBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < affectedBlocksList.size(); i++) {
            CompoundTag posTag = affectedBlocksList.getCompound(i);
            affectedBlocks.add(new BlockPos(
                posTag.getInt("posX"),
                posTag.getInt("posY"), 
                posTag.getInt("posZ")
            ));
        }
    }
    
    // Menu Provider
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new PotionVaporizerMenu(windowId, playerInventory, this);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.potion_vaporizer");
    }
    
    // Container implementation for inventory access
    @Override
    public int getContainerSize() {
        return 3;
    }
    
    @Override
    public boolean isEmpty() {
        for (int i = 0; i < 3; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }
    
    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }
    
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
    
    @Override
    public void clearContent() {
        for (int i = 0; i < 3; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
    
    // Getters for GUI
    public int getDurationLeft() {
        return durationLeft;
    }
    
    public int getDuration() {
        if (currentPotionEffect == null) return 0;
        
        for (net.minecraft.world.effect.MobEffectInstance effect : currentPotionEffect.getAllEffects()) {
            return effect.getDuration(); // Return first effect duration
        }
        return 0;
    }
    
    public int getColor() {
        return currentPotionEffect != null ? currentPotionEffect.getColor() : 0;
    }
    
    public int getFuelBurnTime() {
        return fuelBurnTime;
    }
    
    public int getFuelBurn() {
        return fuelBurn;
    }
    
    public IItemHandler getItemHandler() {
        return itemHandler;
    }
    
    // ContainerData implementation for client-server synchronization
    @Override
    public int get(int index) {
        return switch (index) {
            case 0 -> durationLeft;
            case 1 -> getDuration();
            case 2 -> getColor();
            case 3 -> 0; // potionID - not used in 1.21.1
            case 4 -> 0; // amplifier - not used in 1.21.1  
            case 5 -> fuelBurn;
            case 6 -> fuelBurnTime;
            default -> 0;
        };
    }
    
    @Override
    public void set(int index, int value) {
        // Update client-side values when received from server
        switch (index) {
            case 0 -> durationLeft = value;
            case 1 -> {} // duration is calculated, don't set directly
            case 2 -> {} // color is calculated, don't set directly
            case 3 -> {} // unused
            case 4 -> {} // unused
            case 5 -> fuelBurn = value;
            case 6 -> fuelBurnTime = value;
        }
    }
    
    @Override
    public int getCount() {
        return 7; // Number of data slots
    }
}