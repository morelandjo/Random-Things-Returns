package lumien.randomthings.worldgen;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModStructureProcessors {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS = 
        DeferredRegister.create(BuiltInRegistries.STRUCTURE_PROCESSOR, ModConstants.MOD_ID);
    
    public static final Supplier<StructureProcessorType<PeaceCandleStructureProcessor>> PEACE_CANDLE_PROCESSOR = 
        STRUCTURE_PROCESSORS.register("peace_candle_processor", 
            () -> () -> PeaceCandleStructureProcessor.CODEC);
}