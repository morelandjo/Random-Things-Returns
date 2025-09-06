package lumien.randomthings.event;

import lumien.randomthings.blockentity.SlimeCubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Random;

@EventBusSubscriber
public class SlimeCubeSpawnHandler {
    
    // Counter for custom spawn attempts
    private static int tickCounter = 0;
    private static final Random random = new Random();
    
    /**
     * Handle spawn placement checks - this is where we override vanilla slime chunk logic
     * Uses the correct Result enum values discovered from NeoForge source
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        // Only handle slime spawning
        if (event.getEntityType() != EntityType.SLIME) {
            return;
        }
        
        ServerLevelAccessor level = event.getLevel();
        BlockPos spawnPos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(spawnPos);
        
        // Check if there's a powered slime cube in this chunk (prevents spawning)
        if (SlimeCubeBlockEntity.hasPoweredSlimeCubeInChunk(chunkPos)) {
            // Use FAIL to forcibly prevent the spawn
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
            return;
        }
        
        // Check if there's an active (unpowered) slime cube in this chunk
        if (SlimeCubeBlockEntity.hasActiveSlimeCubeInChunk(chunkPos)) {
            // Use SUCCEED to forcibly allow the spawn, bypassing vanilla slime chunk checks
            // This makes ANY chunk act like a slime chunk and allows spawning at ANY Y level
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
            return;
        }
        
        // No slime cubes in this chunk - use DEFAULT to let vanilla logic handle it
        event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.DEFAULT);
    }
    
    /**
     * Handle finalize spawn - additional spawn control as backup
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        // Only handle slime spawning
        if (!(event.getEntity() instanceof Slime)) {
            return;
        }
        
        BlockPos spawnPos = event.getEntity().blockPosition();
        ChunkPos chunkPos = new ChunkPos(spawnPos);
        
        // Double-check: if there's a powered slime cube, prevent the spawn
        if (SlimeCubeBlockEntity.hasPoweredSlimeCubeInChunk(chunkPos)) {
            event.setCanceled(true);
            event.setSpawnCancelled(true);
            return;
        }
        
        // If there's an active slime cube, we already allowed it in SpawnPlacementCheck
        // Nothing more to do here
    }
    
    /**
     * Periodically attempt to spawn additional slimes in chunks with active slime cubes
     * This provides the "increased rate" aspect of the original feature
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        
        // Every 5 seconds (100 ticks), try to spawn slimes in chunks with active slime cubes
        if (tickCounter % 100 == 0) {
            for (SlimeCubeBlockEntity cube : SlimeCubeBlockEntity.cubes) {
                if (cube.isRemoved() || cube.isPowered()) {
                    continue;
                }
                
                // Get the level from the block entity
                if (cube.getLevel() instanceof ServerLevel serverLevel) {
                    BlockPos cubePos = cube.getBlockPos();
                    
                    // Try to spawn a slime nearby
                    if (random.nextFloat() < 0.3f) { // 30% chance every 5 seconds
                        attemptSlimeSpawn(serverLevel, cubePos);
                    }
                }
            }
        }
    }
    
    /**
     * Attempt to spawn a slime near a slime cube
     */
    private static void attemptSlimeSpawn(ServerLevel level, BlockPos cubePos) {
        // Find a random position within 8 blocks horizontally and 4 blocks vertically
        int x = cubePos.getX() + random.nextInt(17) - 8;
        int y = cubePos.getY() + random.nextInt(9) - 4;
        int z = cubePos.getZ() + random.nextInt(17) - 8;
        
        BlockPos spawnPos = new BlockPos(x, y, z);
        
        // Check if the position is valid for spawning (air blocks)
        if (level.isEmptyBlock(spawnPos) && level.isEmptyBlock(spawnPos.above())) {
            // Create and spawn a slime
            Slime slime = EntityType.SLIME.create(level);
            if (slime != null) {
                // Set slime size (1-3, where 1 is small, 2 is medium, 3 is large)
                int size = random.nextInt(3) + 1;
                slime.setSize(size, true);
                
                // Position the slime
                slime.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
                
                // Finalize spawn and add to world
                slime.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), 
                    MobSpawnType.NATURAL, null);
                
                level.addFreshEntity(slime);
            }
        }
    }
}