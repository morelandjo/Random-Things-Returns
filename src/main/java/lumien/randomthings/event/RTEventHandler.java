package lumien.randomthings.event;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.handler.RTWorldSavedData;
import lumien.randomthings.handler.EscapeRopeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class RTEventHandler {
    
    public static int clientAnimationCounter;
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        clientAnimationCounter++;
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Clear rain shield cache every tick to ensure rain shielding changes
        // are properly reflected and to prevent memory leaks
        RainShieldBlockEntity.clearRainCache();

        // Process escape rope tasks
        EscapeRopeHandler.getInstance().tick();
    }
    
    /**
     * Prevent snow formation in rain shield protected areas
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState placedState = event.getPlacedBlock();
        
        // Check if it's snow being placed by weather
        if (placedState.getBlock() == Blocks.SNOW && level.isRaining()) {
            if (!RainShieldBlockEntity.shouldRain(level, pos)) {
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * Prevent fire from being extinguished by rain in protected areas
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        
        // If fire is being broken during rain, check if it's in a protected area
        if (state.getBlock() instanceof BaseFireBlock && level.isRaining()) {
            if (!RainShieldBlockEntity.shouldRain(level, pos)) {
                // Cancel the break event to prevent rain from extinguishing fire
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * Spirit spawning system - spawn spirits when entities are killed by players
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        
        // Handle Ender Dragon defeat tracking
        if (event.getEntity() instanceof EnderDragon) {
            ServerLevel serverLevel = (ServerLevel) event.getEntity().level();
            RTWorldSavedData worldData = RTWorldSavedData.get(serverLevel);
            worldData.setDragonDefeated(true);
        }
        
        // Spirit spawning logic
        if (event.getSource().getEntity() != null && 
            event.getSource().getEntity() instanceof Player player &&
            !(player instanceof FakePlayer) &&
            !(event.getEntity() instanceof SpiritEntity)) {
            
            ServerLevel serverLevel = (ServerLevel) event.getEntity().level();
            RTWorldSavedData worldData = RTWorldSavedData.get(serverLevel);
            
            // Base chance: 1%
            double spawnChance = 0.01;
            
            // +7% if dragon is defeated
            if (worldData.isDragonDefeated()) {
                spawnChance += 0.07;
            }
            
            // +2% max under full moon at night
            if (serverLevel.canSeeSky(event.getEntity().blockPosition()) && !serverLevel.isDay()) {
                double moonPhaseFactor = serverLevel.getMoonBrightness();
                spawnChance += moonPhaseFactor * 0.02;
            }
            
            // Roll for spirit spawn
            if (serverLevel.random.nextDouble() < spawnChance) {
                SpiritEntity spirit = new SpiritEntity(ModEntityTypes.SPIRIT.get(), serverLevel);
                spirit.setPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
                serverLevel.addFreshEntity(spirit);
            }
        }
    }
}