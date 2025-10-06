package lumien.randomthings.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lumien.randomthings.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Loot modifier for adding Water Walking Boots to Ocean Monument treasure rooms.
 * This replaces the old ASM-based world generation hook from 1.12.2.
 */
public class OceanMonumentLootModifier extends LootModifier {

    public static final MapCodec<OceanMonumentLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
        codecStart(instance).apply(instance, OceanMonumentLootModifier::new));

    protected OceanMonumentLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RandomSource random = context.getRandom();

        // Add water walking boots to the loot
        // This matches the original behavior from WorldGenOceanChest.java
        // 50% chance for water walking boots
        if (random.nextBoolean()) {
            generatedLoot.add(new ItemStack(ModItems.WATER_WALKING_BOOTS.get()));
        }
        // Note: The original also had Bottle of Air as alternative, but that's not ported yet

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
