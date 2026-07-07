package lumien.randomthings.handler.floo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nullable;
import java.util.UUID;

public class FlooFireplace {
    private final UUID masterUUID;
    @Nullable
    private final UUID creatorPlayerUUID;
    @Nullable
    private String name;
    private BlockPos lastKnownPosition;

    public FlooFireplace(UUID masterUUID, @Nullable UUID creatorPlayerUUID, @Nullable String name, BlockPos lastKnownPosition) {
        this.masterUUID = masterUUID;
        this.creatorPlayerUUID = creatorPlayerUUID;
        this.name = name;
        this.lastKnownPosition = lastKnownPosition;
    }

    public UUID getMasterUUID() {
        return masterUUID;
    }

    @Nullable
    public UUID getCreatorPlayerUUID() {
        return creatorPlayerUUID;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public BlockPos getLastKnownPosition() {
        return lastKnownPosition;
    }

    public void setLastKnownPosition(BlockPos lastKnownPosition) {
        this.lastKnownPosition = lastKnownPosition;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("masterUUID", masterUUID);
        if (creatorPlayerUUID != null) {
            tag.putUUID("creatorPlayerUUID", creatorPlayerUUID);
        }
        if (name != null) {
            tag.putString("name", name);
        }
        tag.put("position", NbtUtils.writeBlockPos(lastKnownPosition));
        return tag;
    }

    public static FlooFireplace fromNbt(CompoundTag tag) {
        UUID master = tag.getUUID("masterUUID");
        UUID creator = tag.hasUUID("creatorPlayerUUID") ? tag.getUUID("creatorPlayerUUID") : null;
        String name = tag.contains("name") ? tag.getString("name") : null;
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("position"));
        return new FlooFireplace(master, creator, name, pos);
    }
}
