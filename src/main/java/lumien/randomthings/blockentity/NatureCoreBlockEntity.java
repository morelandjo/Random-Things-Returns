package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class NatureCoreBlockEntity extends BlockEntity {
    
    private static final RandomSource RANDOM = RandomSource.create();
    
    public NatureCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.NATURE_CORE.get(), pos, blockState);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, NatureCoreBlockEntity blockEntity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Replace Sand with Grass/Dirt
            if (RANDOM.nextInt(40) == 0) {
                int rX = pos.getX() + RANDOM.nextInt(11) - 5;
                int rY = pos.getY() + RANDOM.nextInt(4) - 3;
                int rZ = pos.getZ() + RANDOM.nextInt(11) - 5;
                
                BlockPos targetPos = new BlockPos(rX, rY, rZ);
                BlockState targetState = level.getBlockState(targetPos);
                
                if (targetState.is(Blocks.SAND) || targetState.is(Blocks.RED_SAND)) {
                    if (level.isEmptyBlock(targetPos.above())) {
                        level.setBlock(targetPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    } else {
                        level.setBlock(targetPos, Blocks.DIRT.defaultBlockState(), 3);
                    }
                }
            }
            
            // Animal Spawning
            if (RANDOM.nextInt(400) == 0) {
                AABB area = new AABB(pos).inflate(5, 5, 5);
                List<Animal> closeAnimals = level.getEntitiesOfClass(Animal.class, area);
                
                if (closeAnimals.size() < 2) {
                    int rX = pos.getX() + RANDOM.nextInt(11) - 5;
                    int rY = pos.getY() + RANDOM.nextInt(5) - 2;
                    int rZ = pos.getZ() + RANDOM.nextInt(11) - 5;
                    
                    BlockPos spawnPos = new BlockPos(rX, rY, rZ);
                    
                    // Try to spawn a passive mob from biome spawn list
                    var biome = level.getBiome(spawnPos);
                    var spawnList = biome.value().getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.CREATURE);
                    
                    if (!spawnList.isEmpty()) {
                        MobSpawnSettings.SpawnerData spawnerData = spawnList.getRandom(RANDOM).orElse(null);
                        if (spawnerData != null) {
                            try {
                                EntityType<?> entityType = spawnerData.type;
                                if (entityType.create(serverLevel) instanceof Animal animal) {
                                    animal.moveTo(rX + 0.5, rY, rZ + 0.5, RANDOM.nextFloat() * 360.0F, 0.0F);
                                    
                                    if (animal.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER) && 
                                        animal.checkSpawnObstruction(serverLevel)) {
                                        serverLevel.addFreshEntity(animal);
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore spawn failures
                            }
                        }
                    }
                }
            }
            
            // Bonemeal Effect
            if (RANDOM.nextInt(100) == 0) {
                int rX = pos.getX() + RANDOM.nextInt(11) - 5;
                int rY = pos.getY() + RANDOM.nextInt(4) - 3;
                int rZ = pos.getZ() + RANDOM.nextInt(11) - 5;
                
                BlockPos targetPos = new BlockPos(rX, rY, rZ);
                BlockState targetState = level.getBlockState(targetPos);
                
                if (targetState.getBlock() instanceof BonemealableBlock growable) {
                    if (growable.isValidBonemealTarget(level, targetPos, targetState) &&
                        growable.isBonemealSuccess(level, RANDOM, targetPos, targetState)) {
                        
                        level.levelEvent(2005, targetPos, 0);
                        growable.performBonemeal(serverLevel, RANDOM, targetPos, targetState);
                    }
                }
            }
            
            // Plant Trees
            if (RANDOM.nextInt(600) == 0) {
                double radius = RANDOM.nextInt(20) + 10;
                double angle = RANDOM.nextDouble() * Math.PI * 2;
                
                int x = (int) Math.floor(pos.getX() + radius * Math.cos(angle));
                int z = (int) Math.floor(pos.getZ() + radius * Math.sin(angle));
                int y = pos.getY() + RANDOM.nextInt(4) - 3;
                
                BlockPos targetPos = new BlockPos(x, y, z);
                
                // Check if we can place a sapling
                boolean hasSpace = true;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos checkPos = targetPos.above().offset(dx, 0, dz);
                        if (!level.isEmptyBlock(checkPos)) {
                            hasSpace = false;
                            break;
                        }
                    }
                    if (!hasSpace) break;
                }
                
                if (hasSpace) {
                    BlockState saplingState = Blocks.OAK_SAPLING.defaultBlockState();
                    
                    // Check if sapling can survive (replicate SaplingBlock.canSurvive logic)
                    BlockPos saplingPos = targetPos.above();
                    BlockState belowState = level.getBlockState(saplingPos.below());
                    boolean canSurvive = belowState.is(net.minecraft.tags.BlockTags.DIRT) || 
                                       belowState.getBlock() == Blocks.GRASS_BLOCK ||
                                       belowState.getBlock() == Blocks.FARMLAND;
                    boolean hasLightForSapling = level.getRawBrightness(saplingPos, 0) >= 9;
                    
                    if (canSurvive && hasLightForSapling) {
                        level.levelEvent(2005, targetPos, 0);
                        level.setBlock(saplingPos, saplingState, 3);
                    }
                }
            }
            
            // Rebuild Nature Core Structure
            if (RANDOM.nextInt(600) == 0) {
                rebuildNatureCoreStructure(serverLevel, pos);
            }
        }
    }
    
    private static void rebuildNatureCoreStructure(ServerLevel level, BlockPos corePos) {
        BlockState jungleLog = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState jungleLeaves = Blocks.JUNGLE_LEAVES.defaultBlockState();
        
        // Rebuild the 3x3x3 structure around the Nature Core (same as generation)
        BlockPos basePos = corePos.offset(0, -1, 0); // Core is at layer 1
        
        // Layer 0 (Ground level - below core)
        trySetBlock(level, basePos.offset(-1, 0, -1), jungleLog);
        trySetBlock(level, basePos.offset( 0, 0, -1), jungleLeaves);
        trySetBlock(level, basePos.offset( 1, 0, -1), jungleLog);
        trySetBlock(level, basePos.offset(-1, 0,  0), jungleLeaves);
        trySetBlock(level, basePos.offset( 0, 0,  0), jungleLog);
        trySetBlock(level, basePos.offset( 1, 0,  0), jungleLeaves);
        trySetBlock(level, basePos.offset(-1, 0,  1), jungleLog);
        trySetBlock(level, basePos.offset( 0, 0,  1), jungleLeaves);
        trySetBlock(level, basePos.offset( 1, 0,  1), jungleLog);
        
        // Layer 1 (Core level - around the nature core)
        trySetBlock(level, corePos.offset(-1, 0, -1), jungleLog);
        trySetBlock(level, corePos.offset( 0, 0, -1), jungleLeaves);
        trySetBlock(level, corePos.offset( 1, 0, -1), jungleLog);
        trySetBlock(level, corePos.offset(-1, 0,  0), jungleLeaves);
        // Skip center - that's the Nature Core itself
        trySetBlock(level, corePos.offset( 1, 0,  0), jungleLeaves);
        trySetBlock(level, corePos.offset(-1, 0,  1), jungleLog);
        trySetBlock(level, corePos.offset( 0, 0,  1), jungleLeaves);
        trySetBlock(level, corePos.offset( 1, 0,  1), jungleLog);
        
        // Layer 2 (Top - above core)
        trySetBlock(level, corePos.offset(-1, 1, -1), jungleLog);
        trySetBlock(level, corePos.offset( 0, 1, -1), jungleLeaves);
        trySetBlock(level, corePos.offset( 1, 1, -1), jungleLog);
        trySetBlock(level, corePos.offset(-1, 1,  0), jungleLeaves);
        trySetBlock(level, corePos.offset( 0, 1,  0), jungleLog);
        trySetBlock(level, corePos.offset( 1, 1,  0), jungleLeaves);
        trySetBlock(level, corePos.offset(-1, 1,  1), jungleLog);
        trySetBlock(level, corePos.offset( 0, 1,  1), jungleLeaves);
        trySetBlock(level, corePos.offset( 1, 1,  1), jungleLog);
    }
    
    private static void trySetBlock(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState currentState = level.getBlockState(pos);
        
        // Only replace air, grass, dirt, or other easily replaceable blocks
        // Don't replace player-placed blocks like stone, ores, etc.
        if (currentState.isAir() || 
            currentState.is(Blocks.SHORT_GRASS) ||
            currentState.is(Blocks.TALL_GRASS) ||
            currentState.is(Blocks.FERN) ||
            currentState.is(Blocks.LARGE_FERN) ||
            currentState.is(Blocks.DEAD_BUSH) ||
            currentState.is(net.minecraft.tags.BlockTags.FLOWERS) ||
            currentState.canBeReplaced()) {
            
            level.setBlock(pos, state, 3);
        }
    }
}