package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom render types for the Light Redirector to handle z-fighting issues.
 */
public class LightRedirectorRenderType extends RenderType {
    
    private LightRedirectorRenderType(String name, VertexFormat format, VertexFormat.Mode mode, 
                                     int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, 
                                     Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
    
    /**
     * Creates a render type for swapped blocks that prevents z-fighting.
     * Uses polygon offset to render slightly in front of existing geometry.
     */
    public static RenderType swappedBlock() {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_SOLID_SHADER)
            .setLightmapState(LIGHTMAP)
            .setTextureState(BLOCK_SHEET)
            .setTransparencyState(NO_TRANSPARENCY)
            .setDepthTestState(new RenderStateShard.DepthTestStateShard("<=", 515))
            .setCullState(CULL)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setLayeringState(new RenderStateShard.LayeringStateShard(
                "polygon_offset_layering",
                () -> {
                    com.mojang.blaze3d.systems.RenderSystem.polygonOffset(-1.0f, -10.0f);
                    com.mojang.blaze3d.systems.RenderSystem.enablePolygonOffset();
                },
                () -> {
                    com.mojang.blaze3d.systems.RenderSystem.polygonOffset(0.0f, 0.0f);
                    com.mojang.blaze3d.systems.RenderSystem.disablePolygonOffset();
                }
            ))
            .createCompositeState(false);
            
        return create("light_redirector_swapped",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            compositeState);
    }
    
    /**
     * Creates a render type for blocks that should appear on top without z-fighting.
     * Disables depth writing so it always appears on top.
     */
    public static RenderType overlayBlock() {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_SOLID_SHADER)
            .setLightmapState(LIGHTMAP)
            .setTextureState(BLOCK_SHEET)
            .setTransparencyState(NO_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(CULL)
            .setWriteMaskState(COLOR_WRITE)  // Only write color, not depth
            .createCompositeState(false);
            
        return create("light_redirector_overlay",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            compositeState);
    }
}