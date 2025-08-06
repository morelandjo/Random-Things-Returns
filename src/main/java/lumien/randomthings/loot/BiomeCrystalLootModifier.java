package lumien.randomthings.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class BiomeCrystalLootModifier extends LootModifier {
    
    public static final MapCodec<BiomeCrystalLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
        codecStart(instance).apply(instance, BiomeCrystalLootModifier::new));
    
    protected BiomeCrystalLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }
    
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RandomSource random = context.getRandom();
        
        // 20% chance to generate a biome-specific crystal
        if (random.nextFloat() < 0.2f) {
            // Get all biomes from registry (includes all 1.21.1 biomes)
            var biomeRegistry = context.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
            var biomes = biomeRegistry.holders().toList();
            
            if (!biomes.isEmpty()) {
                // Pick a random biome (includes modern biomes like jagged_peaks, cherry_grove, etc.)
                Holder<Biome> randomBiome = biomes.get(random.nextInt(biomes.size()));
                ResourceKey<Biome> biomeKey = randomBiome.getKey();
                
                if (biomeKey != null) {
                    ItemStack biomeCrystal = BiomeCrystalItem.createForBiome(biomeKey);
                    generatedLoot.add(biomeCrystal);
                }
            }
        }
        
        return generatedLoot;
    }
    
    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}