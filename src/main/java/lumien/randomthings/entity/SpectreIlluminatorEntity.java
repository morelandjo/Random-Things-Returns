package lumien.randomthings.entity;

import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationHelper;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpectreIlluminatorEntity extends Entity {
    // Thread-safe map to track illuminated chunks across all level instances
    // Using ConcurrentHashMap with Sets for thread-safe access from render threads
    private static final ConcurrentHashMap<Level, Set<Long>> illuminatedChunks = new ConcurrentHashMap<>();

    public SpectreIlluminatorEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SpectreIlluminatorEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.SPECTRE_ILLUMINATOR.get(), level);
        this.setPos(x, y, z);
    }

    /**
     * Check if a chunk is illuminated - called from Mixin
     */
    public static boolean isChunkIlluminated(BlockPos pos, net.minecraft.world.level.BlockAndTintGetter blockAndTintGetter) {
        // Convert BlockAndTintGetter to Level
        if (!(blockAndTintGetter instanceof Level level)) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        Set<Long> chunks = illuminatedChunks.get(level);
        return chunks != null && chunks.contains(chunkPos.toLong());
    }

    /**
     * Check if a chunk is illuminated without needing a Level reference
     * This checks ALL levels for the chunk position
     * Thread-safe for concurrent access from render threads
     */
    public static boolean isChunkIlluminatedStatic(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long chunkLong = chunkPos.toLong();

        // Check if ANY level has this chunk illuminated
        // Using values() instead of keySet() to avoid concurrent modification
        for (Set<Long> chunks : illuminatedChunks.values()) {
            if (chunks.contains(chunkLong)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synced data needed
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        // No additional data to save
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        // No additional data to load
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        ChunkPos chunkPos = new ChunkPos(this.blockPosition());

        // Add to thread-safe set (server-side tracking)
        illuminatedChunks.computeIfAbsent(level(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(chunkPos.toLong());

        // Force light updates for the chunk
        SpectreIlluminationHelper.lightUpdateChunk(level(), chunkPos);

        // Send packet to clients to sync illumination state
        if (!level().isClientSide) {
            String dimension = level().dimension().location().toString();
            lumien.randomthings.network.SpectreIlluminationPacket packet =
                    new lumien.randomthings.network.SpectreIlluminationPacket(dimension, chunkPos.toLong(), true);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(
                    (net.minecraft.server.level.ServerLevel) level(), chunkPos, packet);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        return interact(player, hand);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            // Drop the item
            this.spawnAtLocation(new ItemStack(ModItems.SPECTRE_ILLUMINATOR.get()), 0.0F);
            // kill() will trigger remove() which cleans up illumination
            this.kill();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();

        // Simple float in place - Irregular Implements has complex movement, we can keep it simple for now
        // Just float gently
        double bobbing = Math.sin(this.tickCount * 0.05) * 0.02;
        this.setDeltaMovement(0, bobbing, 0);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public void remove(RemovalReason reason) {
        ChunkPos chunkPos = new ChunkPos(this.blockPosition());

        // Remove from thread-safe set (server-side tracking)
        Set<Long> chunks = illuminatedChunks.get(level());
        if (chunks != null) {
            chunks.remove(chunkPos.toLong());
            // Clean up empty sets
            if (chunks.isEmpty()) {
                illuminatedChunks.remove(level());
            }
        }

        // Send packet to clients to sync illumination removal
        if (!level().isClientSide) {
            String dimension = level().dimension().location().toString();
            lumien.randomthings.network.SpectreIlluminationPacket packet =
                    new lumien.randomthings.network.SpectreIlluminationPacket(dimension, chunkPos.toLong(), false);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(
                    (net.minecraft.server.level.ServerLevel) level(), chunkPos, packet);
        }

        // Force light updates for the chunk to return to normal lighting
        SpectreIlluminationHelper.lightUpdateChunk(level(), chunkPos);

        super.remove(reason);
    }

    /**
     * Clean up illuminated chunks when a level is unloaded
     */
    public static void clearLevelIllumination(Level level) {
        illuminatedChunks.remove(level);
    }
}
