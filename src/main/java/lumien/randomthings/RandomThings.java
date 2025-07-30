package lumien.randomthings;

import com.mojang.logging.LogUtils;
import lumien.randomthings.block.FertilizedDirtBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.renderer.DiviningRodRenderer;
import lumien.randomthings.client.screen.ModScreens;
import lumien.randomthings.client.vfx.VFXHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.menu.ModMenuTypes;
import lumien.randomthings.worldgen.ModFeatures;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
        ModFeatures.FEATURES.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::setupCommon);
        modEventBus.addListener(this::setupClient);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerNetworking);

        // Register game events
        // Note: Event handlers are registered as listeners below, not as class instance

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
        event.enqueueWork(() -> {
            // Client-side initialization
            
            // Register render layers for transparent blocks
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOOD_ROSE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_REDSTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_OF_STICKS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOCK_OF_STICKS_RETURNING.get(), RenderType.translucent());
        });

        // Register client events manually
        LOGGER.info("DEBUG: Registering client events manually");
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.ClientModEvents::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(lumien.randomthings.client.ClientModEvents::onClientTick);
    }

    private void registerScreens(final RegisterMenuScreensEvent event) {
        ModScreens.register(event);
    }

}