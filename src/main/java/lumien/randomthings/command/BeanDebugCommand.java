package lumien.randomthings.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.worldgen.NatureCoreFeature;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BeanDebugCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beandebug")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("spawn")
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 50))
                    .executes(BeanDebugCommand::spawnBeanPlants)))
            .then(Commands.literal("naturecore")
                .executes(BeanDebugCommand::spawnNatureCore))
            .then(Commands.literal("test")
                .executes(BeanDebugCommand::testBeanSystem)));
    }
    
    private static int spawnBeanPlants(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int radius = IntegerArgumentType.getInteger(context, "radius");
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("This command can only be used in-game"));
            return 0;
        }
        
        BlockPos playerPos = BlockPos.containing(source.getPosition());
        RandomSource random = serverLevel.getRandom();
        int spawned = 0;
        int attempted = 0;
        int solidBlocks = 0;
        int emptyBlocks = 0;
        int lightFailures = 0;
        int soilFailures = 0;
        
        source.sendSuccess(() -> Component.literal("§7Debug: Starting spawn attempt at " + playerPos.toShortString()), false);
        
        for (int i = 0; i < radius * 2; i++) {
            int x = playerPos.getX() + random.nextInt(radius * 2) - radius;
            int z = playerPos.getZ() + random.nextInt(radius * 2) - radius;
            
            // Find surface - check from a reasonable height range
            int startY = Math.max(playerPos.getY() + 20, serverLevel.getMaxBuildHeight() - 1);
            int endY = Math.max(playerPos.getY() - 30, serverLevel.getMinBuildHeight());
            
            for (int y = startY; y > endY; y--) {
                BlockPos groundPos = new BlockPos(x, y, z);
                BlockPos plantPos = groundPos.above();
                attempted++;
                
                if (!serverLevel.getBlockState(groundPos).isSolid()) {
                    continue;
                }
                solidBlocks++;
                
                if (!serverLevel.isEmptyBlock(plantPos)) {
                    continue;
                }
                emptyBlocks++;
                
                int lightLevel = serverLevel.getRawBrightness(plantPos, 0);
                if (lightLevel < 8) {
                    lightFailures++;
                    continue;
                }
                
                // Check soil
                BlockState belowState = serverLevel.getBlockState(groundPos);
                boolean canPlant = belowState.is(net.minecraft.tags.BlockTags.DIRT) || 
                                 belowState.getBlock() == net.minecraft.world.level.block.Blocks.GRASS_BLOCK ||
                                 belowState.getBlock() == net.minecraft.world.level.block.Blocks.FARMLAND;
                
                if (!canPlant) {
                    soilFailures++;
                    source.sendSuccess(() -> Component.literal("§cSoil failure at " + groundPos.toShortString() + ": " + belowState.getBlock().getName().getString()), false);
                    continue;
                }
                
                // Successfully place bean sprout
                int age = random.nextInt(8);
                BlockState beanState = ModBlocks.BEANSPROUT.get().defaultBlockState()
                    .setValue(lumien.randomthings.block.BlockBeanSprout.AGE, age);
                serverLevel.setBlock(plantPos, beanState, 3);
                spawned++;
                source.sendSuccess(() -> Component.literal("§aSpawned bean at " + plantPos.toShortString() + " (age " + age + ")"), false);
                break;
            }
        }
        
        final int finalAttempted = attempted;
        final int finalSolidBlocks = solidBlocks;
        final int finalEmptyBlocks = emptyBlocks;
        final int finalLightFailures = lightFailures;
        final int finalSoilFailures = soilFailures;
        final int finalSpawned = spawned;
        
        source.sendSuccess(() -> Component.literal("§7Debug stats - Attempted: " + finalAttempted + ", Solid: " + finalSolidBlocks + ", Empty: " + finalEmptyBlocks + ", Light failures: " + finalLightFailures + ", Soil failures: " + finalSoilFailures), false);
        source.sendSuccess(() -> Component.literal("Spawned " + finalSpawned + " bean plants in radius " + radius), true);
        return spawned;
    }
    
    private static int spawnNatureCore(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("This command can only be used in-game"));
            return 0;
        }
        
        BlockPos playerPos = BlockPos.containing(source.getPosition());
        BlockPos targetPos = playerPos.offset(10, 0, 10);
        
        source.sendSuccess(() -> Component.literal("§7Attempting to place Nature Core at: " + targetPos.toShortString()), false);
        
        // First, find ground level (surface - highest solid block with air above)
        BlockPos groundPos = null;
        for (int y = serverLevel.getMaxBuildHeight() - 4; y >= serverLevel.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            BlockPos abovePos = checkPos.above();
            
            if (serverLevel.getBlockState(checkPos).isSolid() && 
                serverLevel.isEmptyBlock(abovePos)) {
                groundPos = checkPos;
                break;
            }
        }
        
        if (groundPos == null) {
            source.sendFailure(Component.literal("§cNo suitable ground found between Y=" + serverLevel.getMinBuildHeight() + " and " + (serverLevel.getMaxBuildHeight() - 3)));
            return 0;
        }
        
        final BlockPos finalGroundPos = groundPos;
        source.sendSuccess(() -> Component.literal("§7Ground found at: " + finalGroundPos.toShortString()), false);
        source.sendSuccess(() -> Component.literal("§7Ground block: " + serverLevel.getBlockState(finalGroundPos).getBlock().getName().getString()), false);
        
        // Check if 5x5x3 area is clear
        int blockedCount = 0;
        StringBuilder blockedBlocks = new StringBuilder();
        
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 1; dy <= 3; dy++) {
                    BlockPos checkPos = groundPos.offset(dx, dy, dz);
                    if (!serverLevel.isEmptyBlock(checkPos)) {
                        blockedCount++;
                        if (blockedCount <= 3) {
                            if (blockedBlocks.length() > 0) blockedBlocks.append(", ");
                            blockedBlocks.append("(")
                                         .append(dx).append(",").append(dy).append(",").append(dz)
                                         .append("):")
                                         .append(serverLevel.getBlockState(checkPos).getBlock().getName().getString());
                        }
                    }
                }
            }
        }
        
        if (blockedCount > 0) {
            source.sendFailure(Component.literal("§c5x5x3 area check failed: " + blockedCount + " blocks blocking"));
            source.sendFailure(Component.literal("§cFirst few blocked positions: " + blockedBlocks.toString()));
            if (blockedCount > 3) {
                source.sendFailure(Component.literal("§c... and " + (blockedCount - 3) + " more"));
            }
            return 0;
        }
        
        // Try to place the structure (bypass random chance for debug)
        source.sendSuccess(() -> Component.literal("§7Area is clear, attempting to place structure..."), false);
        
        // Directly build the structure instead of using the feature (which has random chance)
        buildNatureCoreStructureDirectly(serverLevel, groundPos.above(), serverLevel.getRandom());
        placePlantChestDirectly(serverLevel, groundPos.above(), serverLevel.getRandom());
        
        source.sendSuccess(() -> Component.literal("§aNature Core structure spawned successfully!"), true);
        return 1;
    }
    
    private static int testBeanSystem(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal("§aTesting bean system:"), false);
        source.sendSuccess(() -> Component.literal("§7- Bean blocks registered: " + 
            (ModBlocks.BEANSPROUT.get() != null ? "§aYES" : "§cNO")), false);
        source.sendSuccess(() -> Component.literal("§7- Nature Core registered: " + 
            (ModBlocks.NATURE_CORE.get() != null ? "§aYES" : "§cNO")), false);
        source.sendSuccess(() -> Component.literal("§7- Plant Chest registered: " + 
            (ModBlocks.PLANT_CHEST.get() != null ? "§aYES" : "§cNO")), false);
        
        source.sendSuccess(() -> Component.literal("§eUse '/beandebug spawn <radius>' to manually spawn bean plants for testing"), false);
        source.sendSuccess(() -> Component.literal("§eUse '/beandebug naturecore' to manually spawn a nature core structure"), false);
        source.sendSuccess(() -> Component.literal("§eBean plants should naturally generate in new chunks"), false);
        
        return 1;
    }
    
    private static void buildNatureCoreStructureDirectly(net.minecraft.server.level.ServerLevel level, BlockPos basePos, net.minecraft.util.RandomSource random) {
        net.minecraft.world.level.block.state.BlockState jungleLog = net.minecraft.world.level.block.Blocks.JUNGLE_LOG.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState jungleLeaves = net.minecraft.world.level.block.Blocks.JUNGLE_LEAVES.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState natureCore = ModBlocks.NATURE_CORE.get().defaultBlockState();
        
        // Layer 0 (Ground level)
        setBlockIfEmpty(level, basePos.offset(-1, 0, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 0, -1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 0, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset(-1, 0,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 0, 0,  0), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 1, 0,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset(-1, 0,  1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 0,  1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 0,  1), jungleLog);
        
        // Layer 1 (Middle - Nature Core)
        setBlockIfEmpty(level, basePos.offset(-1, 1, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 1, -1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 1, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset(-1, 1,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 0, 1,  0), natureCore); // Nature Core in center
        setBlockIfEmpty(level, basePos.offset( 1, 1,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset(-1, 1,  1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 1,  1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 1,  1), jungleLog);
        
        // Layer 2 (Top)
        setBlockIfEmpty(level, basePos.offset(-1, 2, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 2, -1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 2, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset(-1, 2,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 0, 2,  0), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 1, 2,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset(-1, 2,  1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 2,  1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 2,  1), jungleLog);
    }
    
    private static void setBlockIfEmpty(net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
    
    private static void placePlantChestDirectly(net.minecraft.server.level.ServerLevel level, BlockPos basePos, net.minecraft.util.RandomSource random) {
        // Try to place Plant Chest within 5-block radius (11x11 area)
        for (int attempts = 0; attempts < 20; attempts++) {
            int dx = random.nextInt(11) - 5; // -5 to +5
            int dz = random.nextInt(11) - 5; // -5 to +5
            
            BlockPos chestPos = basePos.offset(dx, 0, dz);
            
            // Find ground level
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos checkPos = chestPos.offset(0, dy, 0);
                BlockPos belowPos = checkPos.below();
                
                if (level.getBlockState(belowPos).isSolid() && level.isEmptyBlock(checkPos)) {
                    // Place Plant Chest
                    level.setBlock(checkPos, ModBlocks.PLANT_CHEST.get().defaultBlockState(), 2);
                    
                    // The block entity will automatically set the loot table in its constructor
                    return;
                }
            }
        }
    }
}