package lumien.randomthings.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.neoforged.neoforge.common.Tags;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class BiomeColorUtils {
    
    private static final Cache<String, Integer> COLOR_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    
    public static int getBiomeColor(BlockAndTintGetter level, BlockPos pos) {
        if (level == null) {
            return 0x7CB518; // Default green color
        }
        
        // Sample 3x3 area like original mod
        int totalR = 0, totalG = 0, totalB = 0;
        int samples = 0;
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos samplePos = pos.offset(x, 0, z);
                
                // Get biome from BlockAndTintGetter
                Holder<Biome> biome;
                try {
                    if (level instanceof Level levelInstance) {
                        biome = levelInstance.getBiome(samplePos);
                    } else if (level instanceof net.minecraft.client.renderer.chunk.RenderChunkRegion) {
                        // Use Level through the Level that RenderChunkRegion wraps
                        java.lang.reflect.Field levelField = level.getClass().getDeclaredField("level");
                        levelField.setAccessible(true);
                        Level wrappedLevel = (Level) levelField.get(level);
                        biome = wrappedLevel.getBiome(samplePos);
                    } else {
                        return 0x7CB518;
                    }
                } catch (Exception e) {
                    return 0x7CB518;
                }
                
                String cacheKey = biome.getKey().location().toString() + "_" + samplePos.getX() + "_" + samplePos.getZ();
                Integer cachedColor = COLOR_CACHE.getIfPresent(cacheKey);
                
                int color;
                if (cachedColor != null) {
                    color = cachedColor;
                } else {
                    color = calculateBiomeColor(biome, samplePos);
                    COLOR_CACHE.put(cacheKey, color);
                }
                
                totalR += (color >> 16) & 0xFF;
                totalG += (color >> 8) & 0xFF;
                totalB += color & 0xFF;
                samples++;
            }
        }
        
        if (samples == 0) {
            return 0x7CB518;
        }
        
        int avgR = totalR / samples;
        int avgG = totalG / samples;
        int avgB = totalB / samples;
        
        return (avgR << 16) | (avgG << 8) | avgB;
    }
    
    public static int getBiomeColor(Holder<Biome> biome) {
        if (biome == null) {
            return 0x7CB518;
        }
        
        String cacheKey = "biome_" + biome.getKey().location().toString();
        Integer cachedColor = COLOR_CACHE.getIfPresent(cacheKey);
        
        if (cachedColor != null) {
            return cachedColor;
        }
        
        int color = calculateBiomeColor(biome, BlockPos.ZERO);
        COLOR_CACHE.put(cacheKey, color);
        return color;
    }
    
    private static int calculateBiomeColor(Holder<Biome> biome, BlockPos pos) {
        Biome biomeValue = biome.value();
        BiomeSpecialEffects effects = biomeValue.getSpecialEffects();
        
        // Get base colors like original mod
        int foliageColor = biomeValue.getFoliageColor();
        int waterColor = effects.getWaterColor();
        int grassColor = biomeValue.getGrassColor(pos.getX(), pos.getZ());
        
        // Convert to Color objects for blending
        Color foliage = new Color(foliageColor);
        Color water = new Color(waterColor);
        Color grass = new Color(grassColor);
        
        // Blend like original: blend(blend(foliage, water, 0.5f), grass, 0.5f)
        Color result = blend(blend(foliage, water, 0.5f), grass, 0.5f);
        
        // Apply biome type modifiers exactly like original
        result = applyBiomeTypeModifiers(biome, result);
        
        // Remove alpha channel for Minecraft color format
        return result.getRGB() & 0xFFFFFF;
    }
    
    private static Color blend(Color c1, Color c2, float ratio) {
        float ir = 1.0f - ratio;
        
        int r = (int) (c1.getRed() * ratio + c2.getRed() * ir);
        int g = (int) (c1.getGreen() * ratio + c2.getGreen() * ir);
        int b = (int) (c1.getBlue() * ratio + c2.getBlue() * ir);
        
        return new Color(Math.min(255, Math.max(0, r)), 
                        Math.min(255, Math.max(0, g)), 
                        Math.min(255, Math.max(0, b)));
    }
    
    private static Color applyBiomeTypeModifiers(Holder<Biome> biome, Color colorResult) {
        String biomeName = biome.getKey().location().getPath();
        
        // Priority-based biome detection to handle modern 1.21.1 biomes
        
        // 1. NETHER biomes (highest priority)
        if (biome.is(BiomeTags.IS_NETHER)) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.8f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.5f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.3f)));
        }
        
        // 2. END biomes
        if (biome.is(BiomeTags.IS_END)) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 0.4f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.1f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.4f)));
        }
        
        // 3. SNOWY/MOUNTAIN biomes (includes jagged_peaks, frozen_peaks, snowy_slopes)
        if (biomeName.contains("snowy") || biomeName.contains("frozen") || 
            biomeName.contains("peaks") || biomeName.contains("slopes") ||
            biomeName.contains("ice") || biomeName.equals("grove")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.4f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.4f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.5f)));
        }
        
        // 4. DESERT/SANDY biomes
        if (biomeName.contains("desert") || biomeName.contains("badlands") || biomeName.contains("mesa")) {
            if (biomeName.contains("badlands") || biomeName.contains("mesa")) {
                // MESA modifier - orange/brown
                return new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), 
                                Math.min(255, (int) (colorResult.getGreen() * 0.8f)), 
                                Math.min(255, (int) (colorResult.getBlue() * 0.5f)));
            } else {
                // SANDY modifier
                return new Color(Math.min(255, (int) (colorResult.getRed() * 1.0f)), 
                                Math.min(255, (int) (colorResult.getGreen() * 0.9f)), 
                                Math.min(255, (int) (colorResult.getBlue() * 0.7f)));
            }
        }
        
        // 5. OCEAN biomes
        if (biome.is(BiomeTags.IS_OCEAN) || biomeName.contains("ocean")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 0.4f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.4f)), 
                            Math.min(255, colorResult.getBlue()));
        }
        
        // 6. JUNGLE biomes
        if (biomeName.contains("jungle")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.5f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.2f)));
        }
        
        // 7. SWAMP biomes
        if (biomeName.contains("swamp") || biomeName.contains("mangrove")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 0.4f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.6f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.4f)));
        }
        
        // 8. MUSHROOM biomes (magical)
        if (biomeName.contains("mushroom")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.3f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.5f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.3f)));
        }
        
        // 9. FOREST biomes (various types)
        if (biome.is(BiomeTags.IS_FOREST) || biomeName.contains("forest")) {
            if (biomeName.contains("dark_forest")) {
                // Dark forest - much darker
                return new Color(Math.min(255, (int) (colorResult.getRed() * 0.6f)), 
                                Math.min(255, (int) (colorResult.getGreen() * 0.7f)), 
                                Math.min(255, (int) (colorResult.getBlue() * 0.6f)));
            } else if (biomeName.contains("birch")) {
                // Birch forest - lighter, more yellow
                return new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), 
                                Math.min(255, (int) (colorResult.getGreen() * 1.0f)), 
                                Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
            } else {
                // Regular forest
                return new Color(Math.min(255, (int) (colorResult.getRed() * 0.8f)), 
                                Math.min(255, (int) (colorResult.getGreen() * 0.9f)), 
                                Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
            }
        }
        
        // 10. TAIGA biomes (coniferous)
        if (biomeName.contains("taiga")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.0f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.9f)));
        }
        
        // 11. SAVANNA biomes
        if (biomeName.contains("savanna")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.2f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.9f)));
        }
        
        // 12. BEACH biomes
        if (biome.is(BiomeTags.IS_BEACH) || biomeName.contains("beach")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.35f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.3f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.1f)));
        }
        
        // 13. RIVER biomes
        if (biomeName.contains("river")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 0.6f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.6f)), 
                            Math.min(255, colorResult.getBlue()));
        }
        
        // 14. PLAINS biomes
        if (biomeName.contains("plains") || biomeName.contains("meadow")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 0.95f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 0.95f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.9f)));
        }
        
        // 15. Special modern biomes
        if (biomeName.equals("cherry_grove")) {
            // Cherry grove - pink tint
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.3f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.2f)));
        }
        
        if (biomeName.contains("bamboo")) {
            // Bamboo jungle - very green
            return new Color(Math.min(255, colorResult.getRed()), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.4f)), 
                            Math.min(255, colorResult.getBlue()));
        }
        
        // 16. MOUNTAIN biomes (non-snowy peaks)
        if (biomeName.contains("stony_peaks") || biomeName.contains("stony_shore")) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.1f)));
        }
        
        // 17. COLD biomes (general cold but not snowy)
        if (biome.is(Tags.Biomes.IS_COLD)) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 0.9f)), 
                            Math.min(255, colorResult.getGreen()), 
                            Math.min(255, (int) (colorResult.getBlue() * 1.1f)));
        }
        
        // 18. HOT biomes (general hot)
        if (biome.is(Tags.Biomes.IS_HOT)) {
            return new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), 
                            Math.min(255, (int) (colorResult.getGreen() * 1.0f)), 
                            Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
        }
        
        // Default - no modification
        return colorResult;
    }
    
    public static boolean isMagicalBiome(Holder<Biome> biome) {
        String biomeName = biome.getKey().location().getPath();
        return biomeName.contains("mushroom") || 
               biomeName.contains("warped") || 
               biomeName.contains("crimson") ||
               biomeName.contains("end") ||
               biomeName.contains("cherry");
    }
}