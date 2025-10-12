package lumien.randomthings.client;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.client.renderer.DiaphanousBlockRenderer;
import lumien.randomthings.client.renderer.EclipsedClockRenderer;
import lumien.randomthings.client.renderer.PortkeyItemRenderer;
import lumien.randomthings.client.renderer.PortkeyRenderer;
import lumien.randomthings.client.renderer.TimeAcceleratorRenderer;
import lumien.randomthings.client.renderer.FluidDisplayBlockEntityRenderer;
import lumien.randomthings.client.renderer.LightRedirectorRenderer;
import lumien.randomthings.client.renderer.block_entity.RuneBaseBlockEntityRenderer;
import lumien.randomthings.client.screen.ModScreens;
import lumien.randomthings.entity.ModEntityTypes;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientProxy {
    
    public static void setupClient(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize Portkey custom renderer
            PortkeyItemRenderer.setRenderer(new PortkeyRenderer());

            // Register render layers for transparent blocks
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOOD_ROSE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LOTUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PITCHER_PLANT.get(), RenderType.cutout());
            
            // Bean System blocks need cutout rendering
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BEANSPROUT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BEANSTALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPECIALBEANSTALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BEANPOD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_REDSTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAIN_SHIELD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_OF_STICKS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_OF_STICKS_RETURNING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BIOME_GLASS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIAPHANOUS_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_DISPLAY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), RenderType.translucent());
        });

        // Register client events
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.ClientModEvents::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.ClientModEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.events.RainShieldClientEvents::onPlaySound);
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.events.RainShieldClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.events.SoundDampenerClientEvents::onPlaySound);
    }

    public static void registerScreens(final RegisterMenuScreensEvent event) {
        ModScreens.register(event);
    }

    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers
        event.registerBlockEntityRenderer(ModBlockEntityTypes.DIAPHANOUS_BLOCK.get(), DiaphanousBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.FLUID_DISPLAY.get(), FluidDisplayBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.LIGHT_REDIRECTOR.get(), LightRedirectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.RUNE_BASE.get(), RuneBaseBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PLANT_CHEST.get(), context -> {
            lumien.randomthings.client.renderer.PlantChestRenderer renderer = new lumien.randomthings.client.renderer.PlantChestRenderer(context);
            lumien.randomthings.client.renderer.PlantChestItemRenderer.setRenderer(renderer);
            return renderer;
        });
        
        // Entity renderers
        event.registerEntityRenderer(ModEntityTypes.ECLIPSED_CLOCK.get(), EclipsedClockRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TIME_ACCELERATOR.get(), TimeAcceleratorRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPIRIT.get(), lumien.randomthings.client.renderer.SpiritRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.STABLE_ENDER_PEARL.get(), net.minecraft.client.renderer.entity.ItemEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GOLDEN_EGG.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GOLDEN_CHICKEN.get(), lumien.randomthings.client.renderer.entity.GoldenChickenRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPECTRE_ILLUMINATOR.get(), lumien.randomthings.client.renderer.entity.SpectreIlluminatorRenderer::new);
    }
    
    public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(lumien.randomthings.client.renderer.PlantChestRenderer.PLANT_CHEST_LAYER, 
            lumien.randomthings.client.model.PlantChestModel::createLayerDefinition);
    }
}