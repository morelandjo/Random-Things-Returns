package lumien.randomthings.handler.redstonesignal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class RedstoneSignal {
    private String dimension;
    private BlockPos position;
    private int duration;
    private int age;
    private int redstoneStrength;

    public RedstoneSignal() {
    }

    public RedstoneSignal(String dimension, BlockPos position, int duration, int redstoneStrength) {
        this.dimension = dimension;
        this.position = position;
        this.duration = duration;
        this.redstoneStrength = redstoneStrength;
        this.age = 0;
    }

    /**
     * Advances the signal by one tick
     * @return true if the signal has expired
     */
    public boolean tick() {
        this.age++;
        return this.age >= this.duration;
    }

    public void writeToNBT(CompoundTag compound) {
        compound.putString("dimension", dimension);
        compound.putInt("posX", position.getX());
        compound.putInt("posY", position.getY());
        compound.putInt("posZ", position.getZ());
        compound.putInt("redstoneStrength", redstoneStrength);
        compound.putInt("duration", duration);
        compound.putInt("age", age);
    }

    public void readFromNBT(CompoundTag compound) {
        this.dimension = compound.getString("dimension");
        int x = compound.getInt("posX");
        int y = compound.getInt("posY");
        int z = compound.getInt("posZ");
        this.position = new BlockPos(x, y, z);
        this.redstoneStrength = compound.getInt("redstoneStrength");
        this.duration = compound.getInt("duration");
        this.age = compound.getInt("age");
    }

    public String getDimension() {
        return dimension;
    }

    public BlockPos getPosition() {
        return position;
    }

    public int getRedstoneStrength() {
        return redstoneStrength;
    }
}
