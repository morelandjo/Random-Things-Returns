package lumien.randomthings.handler.spectre;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class ModChunkGenerators {

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.CHUNK_GENERATOR);

    public static final RegistrySupplier<Codec<? extends ChunkGenerator>> SPECTRE =
        CHUNK_GENERATORS.register("spectre", () -> SpectreChunkGenerator.CODEC);

    private ModChunkGenerators() {
    }

    public static void register() {
        CHUNK_GENERATORS.register();
    }
}
