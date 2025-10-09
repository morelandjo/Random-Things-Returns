package lumien.randomthings.event;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.handler.RTWorldSavedData;
import lumien.randomthings.handler.EscapeRopeHandler;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import lumien.randomthings.item.LavaCharmItem;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.ObsidianSkullItem;
import lumien.randomthings.item.WaterWalkingBootsItem;
import lumien.randomthings.item.ObsidianWaterWalkingBootsItem;
import lumien.randomthings.item.LavaWadersItem;
import lumien.randomthings.util.PortkeyTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        // Process redstone signal handler
        RedstoneSignalHandler handler = RedstoneSignalHandler.get(event.getServer());
        handler.tick(event.getServer());
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
     * Handle lava damage protection from Lava Charm or Lava Waders
     */
    private static void handleLavaProtection(LivingDamageEvent.Pre event, ServerPlayer player) {
        ItemStack lavaCharm = findLavaCharmInInventory(player);

        // Check for Lava Waders first
        ItemStack boots = player.getInventory().getArmor(0);
        if (boots.getItem() instanceof LavaWadersItem) {
            if (boots.has(ModDataComponents.LAVA_CHARM_CHARGE.get())) {
                int charge = boots.get(ModDataComponents.LAVA_CHARM_CHARGE.get());

                if (charge > 0) {
                    // Use one charge and cancel the damage
                    boots.set(ModDataComponents.LAVA_CHARM_CHARGE.get(), charge - 1);
                    boots.set(ModDataComponents.LAVA_CHARM_COOLDOWN.get(), LavaCharmItem.RECHARGE_COOLDOWN);
                    event.setNewDamage(0);
                    return;
                }
            }
        }

        // Fall back to Lava Charm in inventory
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
     * Handle fire damage reduction from Obsidian Skull, Obsidian Water Walking Boots, or Lava Waders
     */
    private static void handleFireProtection(LivingDamageEvent.Pre event, ServerPlayer player) {
        ItemStack obsidianSkull = findObsidianSkullInInventory(player);

        // Also check for Obsidian Water Walking Boots or Lava Waders
        ItemStack boots = player.getInventory().getArmor(0);
        boolean hasFireProtection = !obsidianSkull.isEmpty() ||
                                   boots.getItem() instanceof ObsidianWaterWalkingBootsItem ||
                                   boots.getItem() instanceof LavaWadersItem;

        if (hasFireProtection) {
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

    /**
     * Handle water/lava walking - allows jumping out of liquids while on the surface
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        // Only apply to players
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if player is wearing water walking boots (includes Lava Waders)
        if (!isWearingWaterWalkingBoots(player)) {
            return;
        }

        // Skip if sneaking (allows entering water/lava)
        if (player.isCrouching()) {
            return;
        }

        Level level = player.level();
        BlockPos liquidPos = BlockPos.containing(Math.floor(player.getX()), Math.floor(player.getY()), Math.floor(player.getZ()));
        BlockPos airPos = BlockPos.containing((int) player.getX(), (int) (player.getY() + player.getBbHeight()), (int) player.getZ());

        BlockState liquidState = level.getBlockState(liquidPos);
        BlockState airState = level.getBlockState(airPos);

        boolean isOnWater = liquidState.getFluidState().getType() == Fluids.WATER &&
                           airState.isAir() &&
                           player.isInWater();

        boolean isOnLava = liquidState.getFluidState().getType() == Fluids.LAVA &&
                          airState.isAir() &&
                          isWearingLavaWaders(player); // Only Lava Waders can jump from lava

        // Check if player is at liquid surface
        if (isOnWater || isOnLava) {
            // Apply upward velocity boost to jump out of liquid
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.22, motion.z);
        }
    }

    /**
     * Handle water walking - make water surface solid when player is above it
     * This uses PlayerTickEvent to continuously check and adjust player position
     */
    @SubscribeEvent
    public static void onWaterWalkingTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Check if player is wearing water walking boots
        if (!isWearingWaterWalkingBoots(player)) {
            return;
        }

        // Skip if sneaking (allows entering water)
        if (player.isCrouching()) {
            return;
        }

        Level level = player.level();

        // Check the block directly below the player's feet
        BlockPos belowPos = BlockPos.containing(player.getX(), player.getY() - 0.1, player.getZ());
        BlockState belowState = level.getBlockState(belowPos);

        // Check if there's water below the player
        if (belowState.getFluidState().getType() != Fluids.WATER) {
            return;
        }

        // Check if player is close to the water surface
        double playerY = player.getY();
        double waterY = belowPos.getY() + 1.0; // Top of the water block

        // If player is near or in the water surface
        if (playerY < waterY + 0.2) {
            // Check if player is not already fully submerged
            BlockPos headPos = BlockPos.containing(player.getX(), player.getY() + player.getBbHeight(), player.getZ());
            BlockState headState = level.getBlockState(headPos);

            // Only walk on water if head is above water
            if (headState.getFluidState().getType() != Fluids.WATER) {
                // Set player on top of water
                player.setPos(player.getX(), waterY, player.getZ());

                // Reset vertical velocity if falling
                if (player.getDeltaMovement().y < 0) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0, motion.z);
                }

                // Mark player as on ground so they can walk normally
                player.setOnGround(true);
                player.fallDistance = 0;
            }
        }
    }

    /**
     * Handle lava walking - make lava surface solid when player is above it
     * This uses PlayerTickEvent to continuously check and adjust player position
     * Only works with Lava Waders. Does NOT consume charges when walking on surface.
     */
    @SubscribeEvent
    public static void onLavaWalkingTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only Lava Waders can walk on lava
        if (!isWearingLavaWaders(player)) {
            return;
        }

        // Skip if sneaking (allows entering lava)
        if (player.isCrouching()) {
            return;
        }

        Level level = player.level();

        // Check the block directly below the player's feet
        BlockPos belowPos = BlockPos.containing(player.getX(), player.getY() - 0.1, player.getZ());
        BlockState belowState = level.getBlockState(belowPos);

        // Check if there's lava below the player
        if (belowState.getFluidState().getType() != Fluids.LAVA) {
            return;
        }

        // Check if player is close to the lava surface
        double playerY = player.getY();
        double lavaY = belowPos.getY() + 1.0; // Top of the lava block

        // If player is near or in the lava surface
        if (playerY < lavaY + 0.2) {
            // Check if player is not already fully submerged
            BlockPos headPos = BlockPos.containing(player.getX(), player.getY() + player.getBbHeight(), player.getZ());
            BlockState headState = level.getBlockState(headPos);

            // Only walk on lava if head is above lava
            if (headState.getFluidState().getType() != Fluids.LAVA) {
                // Set player on top of lava
                player.setPos(player.getX(), lavaY, player.getZ());

                // Reset vertical velocity if falling
                if (player.getDeltaMovement().y < 0) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0, motion.z);
                }

                // Mark player as on ground so they can walk normally
                player.setOnGround(true);
                player.fallDistance = 0;

                // Extinguish fire when walking on lava surface
                player.clearFire();
            }
        }
    }

    /**
     * Manage Lava Waders charge system
     * Ticks charge and cooldown similar to Lava Charm
     */
    @SubscribeEvent
    public static void onLavaWadersTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only process on server side
        if (player.level().isClientSide) {
            return;
        }

        // Check if wearing Lava Waders
        ItemStack boots = player.getInventory().getArmor(0);
        if (!(boots.getItem() instanceof LavaWadersItem)) {
            return;
        }

        // Use existing Lava Charm tick logic
        LavaCharmItem.tickCharge(boots);
    }

    /**
     * Check if player is wearing water walking boots (includes Lava Waders)
     */
    private static boolean isWearingWaterWalkingBoots(Player player) {
        ItemStack boots = player.getInventory().getArmor(0); // 0 = boots slot

        return boots.getItem() instanceof WaterWalkingBootsItem ||
               boots.getItem() instanceof ObsidianWaterWalkingBootsItem ||
               boots.getItem() instanceof LavaWadersItem;
    }

    /**
     * Check if player is wearing lava waders specifically
     */
    private static boolean isWearingLavaWaders(Player player) {
        ItemStack boots = player.getInventory().getArmor(0); // 0 = boots slot
        return boots.getItem() instanceof LavaWadersItem;
    }

    /**
     * Handle portkey pickup teleportation
     * When a player picks up a primed portkey (age > 100 ticks), teleport them to the target location
     */
    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();
        Player player = event.getPlayer();

        // Check if it's a portkey
        if (stack.getItem() != ModItems.PORTKEY.get()) {
            return;
        }

        // Check if it has a target set
        PortkeyTarget target = stack.get(ModDataComponents.PORTKEY_TARGET.get());
        if (target == null) {
            return;
        }

        // Check if it's primed (age > 100 ticks)
        Integer age = stack.get(ModDataComponents.PORTKEY_AGE.get());
        if (age == null || age <= 100) {
            return;
        }

        // Only process on server side
        if (player.level().isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();
        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Check if player is in the same dimension as the target
        if (!serverLevel.dimension().equals(target.dimension())) {
            // Cannot teleport cross-dimension, allow normal pickup
            return;
        }

        // Find a safe landing spot within 2 blocks of the target
        BlockPos targetPos = target.pos();
        List<BlockPos> possiblePositions = new ArrayList<>();

        // Search in a 5x5 horizontal area around the target, going down up to 10 blocks
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                for (int checkY = targetPos.getY(); checkY >= targetPos.getY() - 10 && checkY >= serverLevel.getMinBuildHeight(); checkY--) {
                    BlockPos checkPos = new BlockPos(targetPos.getX() + offsetX, checkY, targetPos.getZ() + offsetZ);

                    // Check if this position has a solid block with air above (safe landing spot)
                    if (serverLevel.getBlockState(checkPos).isFaceSturdy(serverLevel, checkPos, Direction.UP)) {
                        BlockPos abovePos = checkPos.above();
                        BlockPos abovePos2 = abovePos.above();

                        if (serverLevel.getBlockState(abovePos).isAir() && serverLevel.getBlockState(abovePos2).isAir()) {
                            possiblePositions.add(checkPos);
                            break; // Found a valid position at this X,Z, no need to check lower
                        }
                    }
                }
            }
        }

        // If we found valid positions, teleport to a random one
        if (!possiblePositions.isEmpty()) {
            Collections.shuffle(possiblePositions);
            BlockPos teleportTarget = possiblePositions.get(0);

            // Play teleport sound at original position
            serverLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Teleport player to the target location (on top of the block)
            serverPlayer.teleportTo(teleportTarget.getX() + 0.5, teleportTarget.getY() + 1, teleportTarget.getZ() + 0.5);

            // Play teleport sound at new position
            serverLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Remove the item entity - this prevents pickup since the entity no longer exists
            itemEntity.discard();
        }
        // If no valid positions found, allow normal pickup (player can try again or keep it)
    }
}