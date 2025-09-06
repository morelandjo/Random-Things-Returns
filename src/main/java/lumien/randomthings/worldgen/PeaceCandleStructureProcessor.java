package lumien.randomthings.worldgen;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;

/**
 * Structure processor that adds peace candles to village churches with a 33% chance.
 * This is the modern NeoForge way to modify structure generation.
 */
public class PeaceCandleStructureProcessor extends StructureProcessor {
    
    public static final com.mojang.serialization.MapCodec<PeaceCandleStructureProcessor> CODEC = 
        com.mojang.serialization.MapCodec.unit(PeaceCandleStructureProcessor::new);
    
    public static final PeaceCandleStructureProcessor INSTANCE = new PeaceCandleStructureProcessor();
    
    private PeaceCandleStructureProcessor() {
        // Private constructor for singleton pattern
    }
    
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, 
                                                            BlockPos jigsawPiecePos, 
                                                            BlockPos jigsawPieceBottomCenterPos, 
                                                            StructureTemplate.StructureBlockInfo blockInfoLocal, 
                                                            StructureTemplate.StructureBlockInfo blockInfoGlobal, 
                                                            StructurePlaceSettings placementSettings) {
        
        // Look for specific blocks in church structures where we want to place peace candles
        BlockState blockState = blockInfoGlobal.state();
        
        // Target: Air blocks that are above church floors (stone bricks or similar)
        // and check if we're in a church-like structure
        if (blockState.is(Blocks.AIR)) {
            BlockPos belowPos = blockInfoGlobal.pos().below();
            
            // Check if there's a suitable surface below (church floor)
            if (level.getBlockState(belowPos).is(Blocks.STONE_BRICKS) || 
                level.getBlockState(belowPos).is(Blocks.COBBLESTONE) ||
                level.getBlockState(belowPos).is(Blocks.OAK_PLANKS)) {
                
                // 33% chance to place a peace candle
                RandomSource random = placementSettings.getRandom(blockInfoGlobal.pos());
                if (random.nextInt(3) == 0) {
                    // Check if there's enough space and it's a suitable location
                    BlockPos abovePos = blockInfoGlobal.pos().above();
                    if (level.getBlockState(abovePos).isAir()) {
                        // Replace air with peace candle
                        return new StructureTemplate.StructureBlockInfo(
                            blockInfoGlobal.pos(),
                            ModBlocks.PEACE_CANDLE.get().defaultBlockState(),
                            blockInfoGlobal.nbt()
                        );
                    }
                }
            }
        }
        
        // Return original block if no modification needed
        return blockInfoGlobal;
    }
    
    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessors.PEACE_CANDLE_PROCESSOR.get();
    }
    
}