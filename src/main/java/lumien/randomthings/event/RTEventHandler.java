package lumien.randomthings.event;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.handler.RTWorldSavedData;
import lumien.randomthings.handler.EscapeRopeHandler;
import lumien.randomthings.item.LavaCharmItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.ObsidianSkullItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
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

    /**
     * Update Lava Charm charge for all players carrying one
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only process on server side
        if (player.level().isClientSide) {
            return;
        }

        // Find Lava Charm in player's inventory
        ItemStack lavaCharm = findLavaCharmInInventory(player);

        if (!lavaCharm.isEmpty()) {
            LavaCharmItem.tickCharge(lavaCharm);
        }
    }

    /**
     * Handle lava damage protection from Lava Charm
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Only apply to players
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DamageSource source = event.getSource();

        // Handle lava damage
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) &&
            source.getMsgId().equals("lava")) {
            handleLavaProtection(event, player);
        }
        // Handle other fire damage
        else if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            handleFireProtection(event, player);
        }
    }

    /**
     * Handle lava damage protection
     */
    private static void handleLavaProtection(LivingDamageEvent.Pre event, ServerPlayer player) {
        ItemStack lavaCharm = findLavaCharmInInventory(player);

        if (!lavaCharm.isEmpty()) {
            int charge = LavaCharmItem.getCharge(lavaCharm);

            if (charge > 0) {
                // Use one charge and cancel the damage
                LavaCharmItem.useCharge(lavaCharm);
                event.setNewDamage(0);
            }
        }
    }

    /**
     * Handle fire damage reduction from Obsidian Skull
     */
    private static void handleFireProtection(LivingDamageEvent.Pre event, ServerPlayer player) {
        ItemStack obsidianSkull = findObsidianSkullInInventory(player);

        if (!obsidianSkull.isEmpty()) {
            float damage = event.getOriginalDamage();

            // Calculate chance to negate damage
            // Lower damage has higher chance of being negated
            // Formula from original: chance = 1 - (damage/100 * damage^2)
            float chance = damage / 100.0f;
            chance *= damage * damage;

            // Random roll
            if (player.getRandom().nextFloat() > chance) {
                // Negate the damage
                event.setNewDamage(0);
            }
        }
    }

    /**
     * Find Lava Charm in player's inventory
     */
    private static ItemStack findLavaCharmInInventory(Player player) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof LavaCharmItem) {
                return stack;
            }
        }

        // Check armor slots
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof LavaCharmItem) {
                return stack;
            }
        }

        // Check offhand
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof LavaCharmItem) {
            return offhand;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Find Obsidian Skull in player's inventory
     */
    private static ItemStack findObsidianSkullInInventory(Player player) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ObsidianSkullItem) {
                return stack;
            }
        }

        // Check armor slots
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof ObsidianSkullItem) {
                return stack;
            }
        }

        // Check offhand
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof ObsidianSkullItem) {
            return offhand;
        }

        return ItemStack.EMPTY;
    }
}