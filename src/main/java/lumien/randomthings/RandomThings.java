package lumien.randomthings;

import com.mojang.logging.LogUtils;
import lumien.randomthings.block.FertilizedDirtBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.PlayerInterfaceBlockEntity;
import lumien.randomthings.command.BeanDebugCommand;
import lumien.randomthings.event.RTEventHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.loot.ModLootModifiers;
import lumien.randomthings.recipe.ModRecipeSerializers;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.menu.ModMenuTypes;
import lumien.randomthings.worldgen.ModFeatures;
import lumien.randomthings.worldgen.ModStructureProcessors;
import lumien.randomthings.handler.spectre.ModChunkGenerators;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(ModConstants.MOD_ID)
public class RandomThings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static RandomThings INSTANCE;

    public RandomThings(IEventBus modEventBus) {
        System.out.println("CONSTRUCTOR DEBUG: RandomThings constructor called - this should appear!");
        INSTANCE = this;
        LOGGER.info("DEBUG: RandomThings constructor called");

        // Register our deferred registers
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModStructureProcessors.STRUCTURE_PROCESSORS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        lumien.randomthings.recipe.ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModChunkGenerators.CHUNK_GENERATORS.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::setupCommon);
        modEventBus.addListener(this::registerNetworking);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerEntityAttributes);
        
        // Register client-side events only if on client
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::setupClient);
            modEventBus.addListener(this::registerScreens);
            modEventBus.addListener(this::registerRenderers);
            modEventBus.addListener(this::registerLayerDefinitions);
            modEventBus.addListener(this::registerGuiOverlays);
            modEventBus.addListener(lumien.randomthings.client.ClientModEvents::registerBlockColors);
            modEventBus.addListener(lumien.randomthings.client.ClientModEvents::registerItemColors);
            modEventBus.addListener(lumien.randomthings.client.ClientModEvents::onClientSetup);
            modEventBus.addListener(lumien.randomthings.client.ClientModEvents::onModifyBakingResult);
        }

        // Register game events
        // Note: Event handlers are registered as listeners below, not as class instance

        // Register rain shield event handler
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onServerTick);
        // Register spirit spawning and dragon defeat tracking
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onLivingDeath);
        // Register other RT event handlers
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onBlockBreak);
        // RTEventHandler::onClientTick is wired by ClientModEvents on the client only.
        // Registering it here would force NeoForge to resolve ClientTickEvent.Post on the
        // dedicated server, which has no neoforge.client.event package and crashes.
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onLivingDamage);
        // Water Walking Boots and Lava Waders event handlers
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onLivingJump);
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onWaterWalkingTick);
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onLavaWalkingTick);
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onLavaWadersTick);
        // Portkey teleportation handler
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onItemPickup);
        // Spectre Anchor event handlers
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(RTEventHandler::onPlayerClone);
        // Super Lubricent Boots handler
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.handler.SuperLubricentBootsHandler::onPlayerTickPost);

        // Register commands
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        
        // Register hoe event for fertilized dirt
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (event.getItemStack().getItem() instanceof net.minecraft.world.item.HoeItem) {
                Level level = event.getLevel();
                BlockPos pos = event.getPos();
                BlockState state = level.getBlockState(pos);

                if (state.getBlock() == ModBlocks.FERTILIZED_DIRT.get() && !state.getValue(FertilizedDirtBlock.TILLED)) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    if (!level.isClientSide) {
                        level.setBlock(pos, state.setValue(FertilizedDirtBlock.TILLED, true), 3);
                        level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        });
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Common setup tasks
            // Network registration handled in registerNetworking method
        });
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        RTPacketHandler.register(event);
    }

    private void setupClient(final FMLClientSetupEvent event) {
        LOGGER.info("DEBUG: setupClient called");
        lumien.randomthings.client.ClientProxy.setupClient(event);
    }

    private void registerScreens(final RegisterMenuScreensEvent event) {
        lumien.randomthings.client.ClientProxy.registerScreens(event);
    }

    private void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        lumien.randomthings.client.ClientProxy.registerRenderers(event);
    }
    
    private void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        lumien.randomthings.client.ClientProxy.registerLayerDefinitions(event);
    }

    private void registerGuiOverlays(final net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        // Register the lava charm bar overlay above player health
        event.registerAbove(
            net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH,
            ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "lava_charm_bar"),
            new lumien.randomthings.client.LavaCharmOverlay()
        );
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntityTypes.PLAYER_INTERFACE.get(),
            (playerInterface, side) -> playerInterface.getItemHandler(side)
        );

        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntityTypes.ADVANCED_ITEM_COLLECTOR.get(),
            (advCollector, side) -> advCollector.getFilterSlot()
        );

        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntityTypes.INVENTORY_REROUTER.get(),
            (rerouter, side) -> rerouter.getMappedHandler(side)
        );

        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntityTypes.DYEING_MACHINE.get(),
            (machine, side) -> machine.getSidedHandler(side)
        );

        // Register fluid handler capabilities for ender buckets
        event.registerItem(
            Capabilities.FluidHandler.ITEM,
            (stack, ctx) -> new net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack(ModDataComponents.FLUID_CONTENT, stack, 1000),
            ModItems.ENDER_BUCKET.get()
        );

        event.registerItem(
            Capabilities.FluidHandler.ITEM,
            (stack, ctx) -> new net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack(ModDataComponents.FLUID_CONTENT, stack, 10000),
            ModItems.REINFORCED_ENDER_BUCKET.get()
        );

        // Register energy capability for Spectre Energy Injector
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> lumien.randomthings.block.SpectreEnergyInjectorBlock.getEnergyCapability(level, pos, side),
            ModBlocks.SPECTRE_ENERGY_INJECTOR.get()
        );
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BeanDebugCommand.register(event.getDispatcher());
    }

    private void registerEntityAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.SPIRIT.get(), SpiritEntity.createAttributes().build());
        event.put(ModEntityTypes.GOLDEN_CHICKEN.get(), lumien.randomthings.entity.GoldenChickenEntity.createAttributes().build());
    }

}