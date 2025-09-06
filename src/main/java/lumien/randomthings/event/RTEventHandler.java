package lumien.randomthings.event;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class RTEventHandler {
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Clear rain shield cache every tick to ensure rain shielding changes
        // are properly reflected and to prevent memory leaks
        RainShieldBlockEntity.clearRainCache();
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
}