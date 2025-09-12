package lumien.randomthings.handler;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RTWorldSavedData extends SavedData {
    private static final String DATA_NAME = "randomthings_worldinfo";
    
    private boolean enderDragonDefeated = false;
    
    public RTWorldSavedData() {
    }
    
    public RTWorldSavedData(CompoundTag compound, HolderLookup.Provider registries) {
        this.enderDragonDefeated = compound.getBoolean("enderDragonDefeated");
    }
    
    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        compound.putBoolean("enderDragonDefeated", this.enderDragonDefeated);
        return compound;
    }
    
    public boolean isDragonDefeated() {
        return this.enderDragonDefeated;
    }
    
    public void setDragonDefeated(boolean defeated) {
        if (this.enderDragonDefeated != defeated) {
            this.enderDragonDefeated = defeated;
            this.setDirty();
        }
    }
    
    public static RTWorldSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(RTWorldSavedData::new, RTWorldSavedData::new),
            DATA_NAME
        );
    }
}