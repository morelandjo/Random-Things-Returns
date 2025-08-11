package lumien.randomthings.blockentity;

import lumien.randomthings.block.FluidDisplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class FluidDisplayBlockEntity extends BlockEntity {
    
    public enum Rotation implements StringRepresentable {
        NONE("none"),
        CLOCKWISE_90("clockwise_90"),
        CLOCKWISE_180("clockwise_180"),
        COUNTERCLOCKWISE_90("counterclockwise_90");
        
        private final String name;
        
        Rotation(String name) {
            this.name = name;
        }
        
        @Override
        public String getSerializedName() {
            return name;
        }
        
        public Rotation next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }
    
    private FluidStack fluidStack = FluidStack.EMPTY;
    private boolean flowing = false;
    private Rotation rotation = Rotation.NONE;
    
    public FluidDisplayBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.FLUID_DISPLAY.get(), pos, blockState);
        this.fluidStack = FluidStack.EMPTY; // Ensure proper initialization
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, FluidDisplayBlockEntity blockEntity) {
        // This block entity doesn't need regular ticking, but the method is required for the ticker
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (!fluidStack.isEmpty()) {
            tag.put("fluidStack", fluidStack.save(registries));
        }
        
        tag.putBoolean("flowing", flowing);
        tag.putString("rotation", rotation.getSerializedName());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (tag.contains("fluidStack")) {
            CompoundTag fluidTag = tag.getCompound("fluidStack");
            if (!fluidTag.isEmpty()) {
                this.fluidStack = FluidStack.parse(registries, fluidTag).orElse(FluidStack.EMPTY);
            } else {
                this.fluidStack = FluidStack.EMPTY;
            }
        } else {
            this.fluidStack = FluidStack.EMPTY;
        }
        
        this.flowing = tag.getBoolean("flowing");
        
        if (tag.contains("rotation")) {
            String rotationName = tag.getString("rotation");
            for (Rotation rot : Rotation.values()) {
                if (rot.getSerializedName().equals(rotationName)) {
                    this.rotation = rot;
                    break;
                }
            }
        } else {
            this.rotation = Rotation.NONE;
        }
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }
    
    // Getters and setters
    public FluidStack getFluidStack() {
        return fluidStack != null ? fluidStack : FluidStack.EMPTY;
    }
    
    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        setChanged();
    }
    
    public boolean isFlowing() {
        return flowing;
    }
    
    public void toggleFlowing() {
        this.flowing = !flowing;
        setChanged();
        syncToClients();
    }
    
    public Rotation getRotation() {
        return rotation;
    }
    
    public void cycleRotation() {
        this.rotation = rotation.next();
        setChanged();
        syncToClients();
    }
}