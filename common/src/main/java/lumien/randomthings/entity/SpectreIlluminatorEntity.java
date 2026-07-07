package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.SpectreIlluminationHelper;
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
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A floating orb that keeps its chunk fully lit. The bright effect is applied by
 * {@code SpectreIlluminatorLightMixin} (client), which reads {@link #isChunkIlluminatedStatic}.
 * Because the entity is tracked to clients, its add/remove maintains the illuminated-chunk set on
 * both sides — no dedicated sync packet needed.
 */
public class SpectreIlluminatorEntity extends Entity {
    private static final ConcurrentHashMap<Level, Set<Long>> illuminatedChunks = new ConcurrentHashMap<>();

    public SpectreIlluminatorEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SpectreIlluminatorEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.SPECTRE_ILLUMINATOR.get(), level);
        this.setPos(x, y, z);
    }

    public static boolean isChunkIlluminated(BlockPos pos, Level level) {
        Set<Long> chunks = illuminatedChunks.get(level);
        return chunks != null && chunks.contains(new ChunkPos(pos).toLong());
    }

    public static boolean isChunkIlluminatedStatic(BlockPos pos) {
        long chunkLong = new ChunkPos(pos).toLong();
        for (Set<Long> chunks : illuminatedChunks.values()) {
            if (chunks.contains(chunkLong)) {
                return true;
            }
        }
        return false;
    }

    public static void clearLevelIllumination(Level level) {
        illuminatedChunks.remove(level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
    }

    private boolean registered = false;

    private void registerIllumination() {
        registered = true;
        ChunkPos chunkPos = new ChunkPos(this.blockPosition());
        illuminatedChunks.computeIfAbsent(level(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
            .add(chunkPos.toLong());
        SpectreIlluminationHelper.lightUpdateChunk(level(), chunkPos);
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
            this.spawnAtLocation(new ItemStack(ModItems.SPECTRE_ILLUMINATOR.get()), 0.0F);
            this.kill();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        if (!registered) {
            registerIllumination();
        }
        double bobbing = Math.sin(this.tickCount * 0.05) * 0.02;
        this.setDeltaMovement(0, bobbing, 0);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public void remove(RemovalReason reason) {
        ChunkPos chunkPos = new ChunkPos(this.blockPosition());
        Set<Long> chunks = illuminatedChunks.get(level());
        if (chunks != null) {
            chunks.remove(chunkPos.toLong());
            if (chunks.isEmpty()) {
                illuminatedChunks.remove(level());
            }
        }
        SpectreIlluminationHelper.lightUpdateChunk(level(), chunkPos);
        super.remove(reason);
    }
}
