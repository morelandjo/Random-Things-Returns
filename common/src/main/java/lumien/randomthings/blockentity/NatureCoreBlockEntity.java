package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
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
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Replace sand with grass/dirt
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

        // Animal spawning from the biome's creature list
        if (RANDOM.nextInt(400) == 0) {
            AABB area = new AABB(pos).inflate(5, 5, 5);
            List<Animal> closeAnimals = level.getEntitiesOfClass(Animal.class, area);

            if (closeAnimals.size() < 2) {
                int rX = pos.getX() + RANDOM.nextInt(11) - 5;
                int rY = pos.getY() + RANDOM.nextInt(5) - 2;
                int rZ = pos.getZ() + RANDOM.nextInt(11) - 5;

                BlockPos spawnPos = new BlockPos(rX, rY, rZ);

                var spawnList = level.getBiome(spawnPos).value().getMobSettings().getMobs(MobCategory.CREATURE);
                if (!spawnList.isEmpty()) {
                    MobSpawnSettings.SpawnerData spawnerData = spawnList.getRandom(RANDOM).orElse(null);
                    if (spawnerData != null) {
                        try {
                            EntityType<?> entityType = spawnerData.type;
                            if (entityType.create(serverLevel) instanceof Animal animal) {
                                animal.moveTo(rX + 0.5, rY, rZ + 0.5, RANDOM.nextFloat() * 360.0F, 0.0F);

                                if (animal.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER)
                                    && animal.checkSpawnObstruction(serverLevel)) {
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

        // Bonemeal effect
        if (RANDOM.nextInt(100) == 0) {
            int rX = pos.getX() + RANDOM.nextInt(11) - 5;
            int rY = pos.getY() + RANDOM.nextInt(4) - 3;
            int rZ = pos.getZ() + RANDOM.nextInt(11) - 5;

            BlockPos targetPos = new BlockPos(rX, rY, rZ);
            BlockState targetState = level.getBlockState(targetPos);

            if (targetState.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget(level, targetPos, targetState, false)
                    && growable.isBonemealSuccess(level, RANDOM, targetPos, targetState)) {

                    level.levelEvent(2005, targetPos, 0);
                    growable.performBonemeal(serverLevel, RANDOM, targetPos, targetState);
                }
            }
        }

        // Plant trees in a ring around the core
        if (RANDOM.nextInt(600) == 0) {
            double radius = RANDOM.nextInt(20) + 10;
            double angle = RANDOM.nextDouble() * Math.PI * 2;

            int x = (int) Math.floor(pos.getX() + radius * Math.cos(angle));
            int z = (int) Math.floor(pos.getZ() + radius * Math.sin(angle));
            int y = pos.getY() + RANDOM.nextInt(4) - 3;

            BlockPos targetPos = new BlockPos(x, y, z);

            boolean hasSpace = true;
            outer:
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (!level.isEmptyBlock(targetPos.above().offset(dx, 0, dz))) {
                        hasSpace = false;
                        break outer;
                    }
                }
            }

            if (hasSpace) {
                BlockPos saplingPos = targetPos.above();
                BlockState belowState = level.getBlockState(saplingPos.below());
                boolean canSurvive = belowState.is(BlockTags.DIRT)
                    || belowState.getBlock() == Blocks.GRASS_BLOCK
                    || belowState.getBlock() == Blocks.FARMLAND;
                boolean hasLightForSapling = level.getRawBrightness(saplingPos, 0) >= 9;

                if (canSurvive && hasLightForSapling) {
                    level.levelEvent(2005, targetPos, 0);
                    level.setBlock(saplingPos, Blocks.OAK_SAPLING.defaultBlockState(), 3);
                }
            }
        }

        // Rebuild the nature core structure
        if (RANDOM.nextInt(600) == 0) {
            rebuildNatureCoreStructure(serverLevel, pos);
        }
    }

    private static void rebuildNatureCoreStructure(ServerLevel level, BlockPos corePos) {
        BlockState jungleLog = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState jungleLeaves = Blocks.JUNGLE_LEAVES.defaultBlockState();

        // 3x3x3 checkerboard of logs/leaves, matching worldgen (core stays at the center)
        for (int layer = -1; layer <= 1; layer++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (layer == 0 && dx == 0 && dz == 0) {
                        continue; // The Nature Core itself
                    }
                    boolean log = ((dx + dz + 2) % 2) == 0;
                    trySetBlock(level, corePos.offset(dx, layer, dz), log ? jungleLog : jungleLeaves);
                }
            }
        }
    }

    private static void trySetBlock(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState currentState = level.getBlockState(pos);

        // Only replace air and easily replaceable plants — never player-placed blocks
        if (currentState.isAir()
            || currentState.is(Blocks.GRASS)
            || currentState.is(Blocks.TALL_GRASS)
            || currentState.is(Blocks.FERN)
            || currentState.is(Blocks.LARGE_FERN)
            || currentState.is(Blocks.DEAD_BUSH)
            || currentState.is(BlockTags.FLOWERS)
            || currentState.canBeReplaced()) {

            level.setBlock(pos, state, 3);
        }
    }
}
