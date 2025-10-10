package lumien.randomthings.client;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.model.PortkeyCamoModel;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.client.events.LightRedirectorClientEvents;
import lumien.randomthings.client.renderer.DiviningRodRenderer;
import lumien.randomthings.client.renderer.DiaphanousBlockRenderer;
import lumien.randomthings.client.renderer.PositionFilterRenderer;
import lumien.randomthings.client.RedstoneInterfaceRenderer;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.EclipsedClockItem;
import lumien.randomthings.item.TimeInABottleItem;
import lumien.randomthings.item.RedstoneActivatorItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.BiomeColorUtils;
import lumien.randomthings.event.RTEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientModEvents {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor biomeBlockColor = (state, level, pos, tintIndex) -> {
            if (level != null && pos != null) {
                return BiomeColorUtils.getBiomeColor(level, pos);
            }
            return 0x7CB518; // Default green
        };

        // Register biome color handler for biome blocks
        event.register(biomeBlockColor,
            ModBlocks.BIOME_STONE.get(),
            ModBlocks.BIOME_GLASS.get()
        );

        // Register lapis color handler for lapis glass
        BlockColor lapisGlassColor = (state, level, pos, tintIndex) -> 0x3C44AA; // Lapis blue color
        event.register(lapisGlassColor, ModBlocks.LAPIS_GLASS.get());

        // Register quartz glass color handler
        BlockColor quartzGlassColor = (state, level, pos, tintIndex) -> 0xFFFFFF; // White color
        event.register(quartzGlassColor, ModBlocks.QUARTZ_GLASS.get());

        // Register trigger glass color handler
        BlockColor triggerGlassColor = (state, level, pos, tintIndex) -> {
            // Red color when not triggered, color doesn't matter when invisible
            return 0xFF4444; // Red color
        };
        event.register(triggerGlassColor, ModBlocks.TRIGGER_GLASS.get());

        // Register compressed slime block color handler
        BlockColor compressedSlimeColor = (state, level, pos, tintIndex) -> {
            int compression = state.getValue(lumien.randomthings.block.CompressedSlimeBlock.COMPRESSION);
            int[] compressionColors = {0xC8C8C8, 0x969696, 0x646464}; // Light gray, medium gray, dark gray
            return compressionColors[compression];
        };

        event.register(compressedSlimeColor, ModBlocks.COMPRESSED_SLIME_BLOCK.get());

        // Register rune base block color handler - tints based on dye color
        BlockColor runeBaseColor = (state, level, pos, tintIndex) -> {
            if (tintIndex >= 0 && tintIndex < DyeColor.values().length) {
                return DyeColor.byId(tintIndex).getFireworkColor();
            }
            return 0xFFFFFF;
        };
        event.register(runeBaseColor, ModBlocks.RUNE_BASE.get());

        // Remove the destabilizer color handler for now - let's try without tinting
    }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register lapis glass item color
        ItemColor lapisGlassItemColor = (stack, tintIndex) -> 0x3C44AA; // Lapis blue color
        event.register(lapisGlassItemColor, ModItems.LAPIS_GLASS.get());
        
        // Register quartz glass item color
        ItemColor quartzGlassItemColor = (stack, tintIndex) -> 0xFFFFFF; // White color
        event.register(quartzGlassItemColor, ModItems.QUARTZ_GLASS.get());
        
        // Register trigger glass item color
        ItemColor triggerGlassItemColor = (stack, tintIndex) -> 0xFF4444; // Red color
        event.register(triggerGlassItemColor, ModItems.TRIGGER_GLASS.get());

        // Register rune dust item color - tints based on dye color data component
        ItemColor runeDustItemColor = (stack, tintIndex) -> {
            DyeColor color = stack.get(lumien.randomthings.item.ModDataComponents.RUNE_COLOR.get());
            // getFireworkColor() returns RGB without alpha, need to add alpha channel (0xFF prefix for full opacity)
            return (color != null) ? (0xFF000000 | color.getFireworkColor()) : 0xFFFFFFFF;
        };
        event.register(runeDustItemColor, ModItems.RUNE_DUST.get());
    }
    
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            
            DiviningRodRenderer.get().render(
                event.getPoseStack(),
                bufferSource,
                event.getPartialTick().getGameTimeDeltaPartialTick(false)
            );

            // Render Position Filter cube overlay
            PositionFilterRenderer.get().render(
                event.getPoseStack(),
                bufferSource,
                event.getPartialTick().getGameTimeDeltaPartialTick(false)
            );

            // Render redstone interface connection lines - temporarily disabled
            // RedstoneInterfaceRenderer.renderRedstoneInterfaceLines(
            //     event.getPoseStack(),
            //     bufferSource,
            //     event.getCamera().getPosition()
            // );

            // Explicitly flush the buffer source to ensure rendering
            bufferSource.endBatch();
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        DiviningRodRenderer.get().tick();
        DiaphanousBlockRenderer.tick();
        // Call RTEventHandler to increment clientAnimationCounter for Time Accelerator rotation
        RTEventHandler.onClientTick(event);
    }
    
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        // Wrap the portkey model to support camo
        net.minecraft.client.resources.model.ModelResourceLocation portkeyLocation =
            new net.minecraft.client.resources.model.ModelResourceLocation(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("randomthings", "portkey"),
                "inventory"
            );

        net.minecraft.client.resources.model.BakedModel originalModel = event.getModels().get(portkeyLocation);
        if (originalModel != null) {
            event.getModels().put(portkeyLocation, new PortkeyCamoModel(originalModel));
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register render types for translucent blocks
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LAPIS_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.QUARTZ_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRIGGER_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLAZING_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLOWING_MUSHROOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SLIME_CUBE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RUNE_BASE.get(), RenderType.cutout());
            
            // Register item properties
            ItemProperties.register(ModItems.ECLIPSED_CLOCK.get(), ResourceLocation.parse("randomthings:time"), 
                EclipsedClockItem.getTimePropertyFunction());
                
            ItemProperties.register(ModItems.TIME_IN_A_BOTTLE.get(), ResourceLocation.parse("randomthings:fill_level"), 
                (stack, level, entity, id) -> TimeInABottleItem.getFillLevel(stack));
                
            ItemProperties.register(ModItems.EMERALD_COMPASS.get(), ResourceLocation.parse("angle"),
                (stack, level, entity, id) -> {
                    if (stack.getItem() instanceof lumien.randomthings.item.EmeraldCompassItem compassItem) {
                        return compassItem.getCompassAngle(stack, level, entity);
                    }
                    return 0.0F;
                });

            ItemProperties.register(ModItems.GOLDEN_COMPASS.get(), ResourceLocation.parse("angle"),
                (stack, level, entity, id) -> {
                    if (stack.getItem() instanceof lumien.randomthings.item.GoldenCompassItem compassItem) {
                        return compassItem.getCompassAngle(stack, level, entity);
                    }
                    return 0.0F;
                });

            // Register redstone activator duration property for model switching
            ItemProperties.register(ModItems.REDSTONE_ACTIVATOR.get(), ResourceLocation.parse("randomthings:duration"),
                (stack, level, entity, id) -> {
                    if (stack.getItem() instanceof RedstoneActivatorItem activatorItem) {
                        return activatorItem.getDurationIndex(stack);
                    }
                    return 1.0F; // Default to middle duration
                });

            // Register sound pattern has_sound property for empty/full model switching
            ItemProperties.register(ModItems.SOUND_PATTERN.get(), ResourceLocation.parse("randomthings:has_sound"),
                (stack, level, entity, id) -> {
                    if (stack.getItem() instanceof lumien.randomthings.item.ItemSoundPattern) {
                        return lumien.randomthings.item.ItemSoundPattern.getSoundLocation(stack) != null ? 1.0F : 0.0F;
                    }
                    return 0.0F;
                });
        });
    }
}