package lumien.randomthings.client;

import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import lumien.randomthings.block.ColoredGrassBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.entity.ModEntityTypes;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import lumien.randomthings.client.screen.AdvancedItemCollectorScreen;
import lumien.randomthings.client.screen.ChunkAnalyzerScreen;
import lumien.randomthings.client.screen.AdvancedRedstoneRepeaterScreen;
import lumien.randomthings.client.screen.AdvancedRedstoneTorchScreen;
import lumien.randomthings.client.screen.AnalogEmitterScreen;
import lumien.randomthings.client.screen.ChatDetectorScreen;
import lumien.randomthings.client.screen.DyeingMachineScreen;
import lumien.randomthings.client.screen.EntityDetectorScreen;
import lumien.randomthings.client.screen.GlobalChatDetectorScreen;
import lumien.randomthings.client.screen.IgniterScreen;
import lumien.randomthings.client.screen.IronDropperScreen;
import lumien.randomthings.client.screen.ImbuingStationScreen;
import lumien.randomthings.client.screen.ItemFilterScreen;
import lumien.randomthings.client.screen.OnlineDetectorScreen;
import lumien.randomthings.item.ColoredGrassItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.menu.ModMenuTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;

/**
 * Common client-side initialization, invoked from each loader's client entry point
 * (Fabric {@code ClientModInitializer}, Forge {@code FMLClientSetupEvent}). Screen factories, color
 * handlers, and block-entity/entity renderers are registered here as features are ported.
 */
@Environment(EnvType.CLIENT)
public final class RandomThingsClient {

    private RandomThingsClient() {
    }

    public static void init() {
        MenuRegistry.registerScreenFactory(ModMenuTypes.ONLINE_DETECTOR.get(), OnlineDetectorScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.ENTITY_DETECTOR.get(), EntityDetectorScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.CHAT_DETECTOR.get(), ChatDetectorScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.GLOBAL_CHAT_DETECTOR.get(), GlobalChatDetectorScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.ADVANCED_ITEM_COLLECTOR.get(), AdvancedItemCollectorScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.ITEM_FILTER.get(), ItemFilterScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.DYEING_MACHINE.get(), DyeingMachineScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.IMBUING_STATION.get(), ImbuingStationScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.ANALOG_EMITTER.get(), AnalogEmitterScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.ADVANCED_REDSTONE_TORCH.get(), AdvancedRedstoneTorchScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.ADVANCED_REDSTONE_REPEATER.get(), AdvancedRedstoneRepeaterScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.IGNITER.get(), IgniterScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.IRON_DROPPER.get(), IronDropperScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.CHUNK_ANALYZER.get(), ChunkAnalyzerScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.SOUND_DAMPENER.get(), lumien.randomthings.client.screen.SoundDampenerScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.PORTABLE_SOUND_DAMPENER.get(), lumien.randomthings.client.screen.PortableSoundDampenerScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.SOUND_RECORDER.get(), lumien.randomthings.client.screen.SoundRecorderScreen::new);

        // Clientbound: receive chunk-scan results.
        dev.architectury.networking.NetworkManager.registerReceiver(
            dev.architectury.networking.NetworkManager.Side.S2C,
            lumien.randomthings.network.ChunkAnalyzerResultPacket.ID,
            (buf, ctx) -> {
                lumien.randomthings.util.ChunkAnalyzerResult result =
                    lumien.randomthings.network.ChunkAnalyzerResultPacket.decode(buf).result();
                ctx.queue(() -> ChunkAnalyzerClientData.setLatestResult(result));
            });

        // The temporary floo fireplace renders nothing itself (it spawns its own particles).
        EntityRendererRegistry.register(ModEntityTypes.TEMPORARY_FLOO_FIREPLACE, NoopRenderer::new);
        // Thrown weather egg renders as its item; the weather cloud only emits particles.
        EntityRendererRegistry.register(ModEntityTypes.THROWN_WEATHER_EGG, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.WEATHER_CLOUD, NoopRenderer::new);
        // Artificial end portal renders as particles only.
        EntityRendererRegistry.register(ModEntityTypes.ARTIFICIAL_END_PORTAL, NoopRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SPECTRE_ILLUMINATOR, NoopRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.TIME_ACCELERATOR, NoopRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ECLIPSED_CLOCK, lumien.randomthings.client.renderer.EclipsedClockRenderer::new);
        dev.architectury.registry.client.rendering.BlockEntityRendererRegistry.register(
            lumien.randomthings.blockentity.ModBlockEntityTypes.ANCIENT_FURNACE.get(),
            lumien.randomthings.client.renderer.AncientFurnaceRenderer::new);
        dev.architectury.registry.client.rendering.BlockEntityRendererRegistry.register(
            lumien.randomthings.blockentity.ModBlockEntityTypes.PLANT_CHEST.get(),
            lumien.randomthings.client.renderer.PlantChestRenderer::new);
        dev.architectury.registry.client.level.entity.EntityModelLayerRegistry.register(
            lumien.randomthings.client.renderer.PlantChestRenderer.PLANT_CHEST_LAYER,
            lumien.randomthings.client.model.PlantChestModel::createLayerDefinition);
        EntityRendererRegistry.register(ModEntityTypes.GOLDEN_EGG, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.GOLDEN_CHICKEN, lumien.randomthings.client.renderer.entity.GoldenChickenRenderer::new);
        dev.architectury.registry.client.rendering.BlockEntityRendererRegistry.register(
            lumien.randomthings.blockentity.ModBlockEntityTypes.DIAPHANOUS_BLOCK.get(),
            lumien.randomthings.client.renderer.DiaphanousBlockRenderer::new);

        // Clientbound: eclipsed clock animation trigger.
        dev.architectury.networking.NetworkManager.registerReceiver(
            dev.architectury.networking.NetworkManager.Side.S2C,
            lumien.randomthings.network.EclipsedClockAnimationPacket.ID,
            (buf, ctx) -> {
                int entityId = lumien.randomthings.network.EclipsedClockAnimationPacket.decode(buf).entityId();
                ctx.queue(() -> {
                    net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
                    if (level != null && level.getEntity(entityId) instanceof lumien.randomthings.entity.EclipsedClockEntity clock) {
                        clock.triggerAnimation();
                    }
                });
            });

        // Divining rods: mark nearby matching ores with coloured particles each client tick.
        dev.architectury.event.events.client.ClientTickEvent.CLIENT_POST.register(mc -> lumien.randomthings.client.renderer.DiviningRodRenderer.get().tick());

        registerColors();
        registerItemProperties();
    }

    /** Item model predicates via Architectury's ItemPropertiesRegistry (no access widener needed). */
    private static void registerItemProperties() {
        net.minecraft.resources.ResourceLocation angle =
            new net.minecraft.resources.ResourceLocation(lumien.randomthings.lib.ModConstants.MOD_ID, "angle");
        dev.architectury.registry.item.ItemPropertiesRegistry.register(
            ModItems.EMERALD_COMPASS.get(), angle,
            new CompassAngles(lumien.randomthings.item.EmeraldCompassItem::getTarget));
        dev.architectury.registry.item.ItemPropertiesRegistry.register(
            ModItems.GOLDEN_COMPASS.get(), angle,
            new CompassAngles(lumien.randomthings.item.GoldenCompassItem::getTarget));

        // Previously deferred predicates, now registrable cross-loader:
        dev.architectury.registry.item.ItemPropertiesRegistry.register(
            ModItems.TIME_IN_A_BOTTLE.get(),
            new net.minecraft.resources.ResourceLocation(lumien.randomthings.lib.ModConstants.MOD_ID, "fill_level"),
            (stack, level, entity, seed) ->
                Math.min(1.0F, lumien.randomthings.item.TimeInABottleItem.getStoredTime(stack) / (20.0F * 60 * 60 * 8)));
        dev.architectury.registry.item.ItemPropertiesRegistry.register(
            ModItems.SOUND_PATTERN.get(),
            new net.minecraft.resources.ResourceLocation(lumien.randomthings.lib.ModConstants.MOD_ID, "has_sound"),
            (stack, level, entity, seed) ->
                lumien.randomthings.item.ItemSoundPattern.getSoundLocation(stack) != null ? 1.0F : 0.0F);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(
            ModItems.SOUND_RECORDER.get(),
            new net.minecraft.resources.ResourceLocation(lumien.randomthings.lib.ModConstants.MOD_ID, "recording"),
            (stack, level, entity, seed) ->
                lumien.randomthings.item.SoundRecorderItem.isRecording(stack) ? 1.0F : 0.0F);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(
            ModItems.ECLIPSED_CLOCK.get(),
            new net.minecraft.resources.ResourceLocation(lumien.randomthings.lib.ModConstants.MOD_ID, "time"),
            (stack, level, entity, seed) -> {
                int time = lumien.randomthings.util.RTNbt.getInt(stack, lumien.randomthings.util.RTDataKeys.TARGET_TIME, 6000);
                float f = (time % 24000) / 24000.0F - 0.25F;
                if (f < 0.0F) {
                    ++f;
                }
                float f1 = 1.0F - (float) ((Math.cos(f * Math.PI) + 1.0D) / 2.0D);
                return f + (f1 - f) / 3.0F;
            });
    }

    private static void registerColors() {
        // Colored Grass block — tint by the per-block COLOR state.
        ColorHandlerRegistry.registerBlockColors(
            (state, level, pos, tintIndex) -> dyeRgb(state.getValue(ColoredGrassBlock.COLOR)),
            ModBlocks.COLORED_GRASS.get());

        // Glass tints + cutout render layers.
        ColorHandlerRegistry.registerBlockColors(
            (state, level, pos, tintIndex) -> (level != null && pos != null)
                ? BiomeColors.getAverageGrassColor(level, pos) : 0x7CB518,
            ModBlocks.BIOME_GLASS.get(), ModBlocks.BIOME_STONE.get());
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> 0x7CB518,
            ModItems.BIOME_STONE_SMOOTH.get(), ModItems.BIOME_STONE_COBBLE.get(), ModItems.BIOME_STONE_BRICK.get(),
            ModItems.BIOME_STONE_CRACKED.get(), ModItems.BIOME_STONE_CHISELED.get());
        // Spectre coils — tint by coil tier (block + item share the same colour source).
        ColorHandlerRegistry.registerBlockColors(
            (state, level, pos, tintIndex) -> state.getBlock() instanceof lumien.randomthings.block.SpectreCoilBlock coil
                ? coil.getCoilType().getColor() : 0xFFFFFF,
            ModBlocks.SPECTRE_COIL_NORMAL.get(), ModBlocks.SPECTRE_COIL_REDSTONE.get(), ModBlocks.SPECTRE_COIL_ENDER.get(),
            ModBlocks.SPECTRE_COIL_NUMBER.get(), ModBlocks.SPECTRE_COIL_GENESIS.get());
        ColorHandlerRegistry.registerItemColors(
            (stack, tintIndex) -> stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                && blockItem.getBlock() instanceof lumien.randomthings.block.SpectreCoilBlock coil
                ? coil.getCoilType().getColor() : 0xFFFFFF,
            ModItems.SPECTRE_COIL_NORMAL.get(), ModItems.SPECTRE_COIL_REDSTONE.get(), ModItems.SPECTRE_COIL_ENDER.get(),
            ModItems.SPECTRE_COIL_NUMBER.get(), ModItems.SPECTRE_COIL_GENESIS.get());

        ColorHandlerRegistry.registerItemColors(
            (stack, tintIndex) -> stack.getItem() instanceof lumien.randomthings.item.SpectreChargerItem charger
                ? charger.getTier().getColor() : 0xFFFFFF,
            ModItems.SPECTRE_CHARGER_NORMAL.get(), ModItems.SPECTRE_CHARGER_REDSTONE.get(),
            ModItems.SPECTRE_CHARGER_ENDER.get(), ModItems.SPECTRE_CHARGER_GENESIS.get());

        ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> 0x3C44AA, ModBlocks.LAPIS_GLASS.get());
        ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> 0xFFFFFF, ModBlocks.QUARTZ_GLASS.get());
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> 0x7CB518, ModItems.BIOME_GLASS.get());
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> 0x3C44AA, ModItems.LAPIS_GLASS.get());
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> 0xFFFFFF, ModItems.QUARTZ_GLASS.get());
        RenderTypeRegistry.register(RenderType.cutout(),
            ModBlocks.LAPIS_GLASS.get(), ModBlocks.QUARTZ_GLASS.get(), ModBlocks.BIOME_GLASS.get());
        RenderTypeRegistry.register(RenderType.cutout(),
            ModBlocks.BLOOD_ROSE.get(), ModBlocks.GLOWING_MUSHROOM.get(), ModBlocks.LOTUS.get());
        RenderTypeRegistry.register(RenderType.cutout(),
            ModBlocks.BEANSPROUT.get(), ModBlocks.BEANSTALK.get(), ModBlocks.SPECIALBEANSTALK.get(), ModBlocks.BEANPOD.get());
        RenderTypeRegistry.register(RenderType.cutout(), ModBlocks.PITCHER_PLANT.get(), ModBlocks.TRIGGER_GLASS.get(),
            ModBlocks.BLOCK_OF_STICKS.get(), ModBlocks.BLOCK_OF_STICKS_RETURNING.get(), ModBlocks.PEACE_CANDLE.get(),
            ModBlocks.BLAZING_FIRE.get());
        RenderTypeRegistry.register(RenderType.translucent(),
            ModBlocks.COMPRESSED_SLIME_BLOCK.get(), ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(),
            ModBlocks.SPECTRE_ENERGY_INJECTOR.get());
        RenderTypeRegistry.register(RenderType.cutout(),
            ModBlocks.SPECTRE_COIL_NORMAL.get(), ModBlocks.SPECTRE_COIL_REDSTONE.get(), ModBlocks.SPECTRE_COIL_ENDER.get(),
            ModBlocks.SPECTRE_COIL_NUMBER.get(), ModBlocks.SPECTRE_COIL_GENESIS.get());

        // Colored Grass items — tint each by its stored DyeColor.
        ItemLike[] grassItems = {
            ModItems.COLORED_GRASS_WHITE.get(), ModItems.COLORED_GRASS_ORANGE.get(), ModItems.COLORED_GRASS_MAGENTA.get(),
            ModItems.COLORED_GRASS_LIGHT_BLUE.get(), ModItems.COLORED_GRASS_YELLOW.get(), ModItems.COLORED_GRASS_LIME.get(),
            ModItems.COLORED_GRASS_PINK.get(), ModItems.COLORED_GRASS_GRAY.get(), ModItems.COLORED_GRASS_LIGHT_GRAY.get(),
            ModItems.COLORED_GRASS_CYAN.get(), ModItems.COLORED_GRASS_PURPLE.get(), ModItems.COLORED_GRASS_BLUE.get(),
            ModItems.COLORED_GRASS_BROWN.get(), ModItems.COLORED_GRASS_GREEN.get(), ModItems.COLORED_GRASS_RED.get(),
            ModItems.COLORED_GRASS_BLACK.get()
        };
        ColorHandlerRegistry.registerItemColors(
            (stack, tintIndex) -> stack.getItem() instanceof ColoredGrassItem grass
                ? (0xFF000000 | dyeRgb(grass.getColor()))
                : 0xFFFFFFFF,
            grassItems);
    }

    private static int dyeRgb(DyeColor color) {
        float[] c = color.getTextureDiffuseColors();
        return ((int) (c[0] * 255f) << 16) | ((int) (c[1] * 255f) << 8) | (int) (c[2] * 255f);
    }
}
