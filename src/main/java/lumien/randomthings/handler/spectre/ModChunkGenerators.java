package lumien.randomthings.handler.spectre;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModChunkGenerators {
    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, ModConstants.MOD_ID);

    public static final Supplier<com.mojang.serialization.MapCodec<? extends ChunkGenerator>> SPECTRE =
        CHUNK_GENERATORS.register("spectre", () -> SpectreChunkGenerator.CODEC);
}
