package lumien.randomthings.client;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.renderer.DiviningRodRenderer;
import lumien.randomthings.client.renderer.DiaphanousBlockRenderer;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.BiomeColorUtils;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
        
        // Register compressed slime block color handler
        BlockColor compressedSlimeColor = (state, level, pos, tintIndex) -> {
            int compression = state.getValue(lumien.randomthings.block.CompressedSlimeBlock.COMPRESSION);
            int[] compressionColors = {0xC8C8C8, 0x969696, 0x646464}; // Light gray, medium gray, dark gray
            return compressionColors[compression];
        };
        
        event.register(compressedSlimeColor, ModBlocks.COMPRESSED_SLIME_BLOCK.get());
        
        // Remove the destabilizer color handler for now - let's try without tinting
    }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register lapis glass item color
        ItemColor lapisGlassItemColor = (stack, tintIndex) -> 0x3C44AA; // Lapis blue color
        event.register(lapisGlassItemColor, ModItems.LAPIS_GLASS.get());
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
            
            // Explicitly flush the buffer source to ensure rendering
            bufferSource.endBatch();
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        DiviningRodRenderer.get().tick();
        DiaphanousBlockRenderer.tick();
    }
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register render types for translucent blocks
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BIOME_GLASS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LAPIS_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIAPHANOUS_BLOCK.get(), RenderType.translucent());
            
        });
    }
}