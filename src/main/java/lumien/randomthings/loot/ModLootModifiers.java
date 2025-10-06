package lumien.randomthings.loot;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.lib.ModConstants;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ModConstants.MOD_ID);

    public static final Supplier<MapCodec<BiomeCrystalLootModifier>> BIOME_CRYSTAL_LOOT =
        LOOT_MODIFIERS.register("biome_crystal", () -> BiomeCrystalLootModifier.CODEC);

    public static final Supplier<MapCodec<OceanMonumentLootModifier>> OCEAN_MONUMENT_LOOT =
        LOOT_MODIFIERS.register("ocean_monument", () -> OceanMonumentLootModifier.CODEC);

    public static final Supplier<MapCodec<AddItemLootModifier>> ADD_ITEM =
        LOOT_MODIFIERS.register("add_item", () -> AddItemLootModifier.CODEC);
}