package lumien.randomthings.event;

import dev.architectury.event.events.common.LootEvent;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/**
 * Injects mod loot into vanilla chest tables (replaces the NeoForge global loot modifiers of the
 * 1.21.1 source): Magic Hood in dungeon chests (5%) and village toolsmith chests (15%).
 */
public final class LootHandler {

    private LootHandler() {
    }

    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register((lootDataManager, id, context, builtin) -> {
            if (!builtin) {
                return;
            }
            if (BuiltInLootTables.SIMPLE_DUNGEON.equals(id)) {
                context.addPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.MAGIC_HOOD.get())
                        .when(LootItemRandomChanceCondition.randomChance(0.05F))));
            } else if (BuiltInLootTables.VILLAGE_TOOLSMITH.equals(id)) {
                context.addPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.MAGIC_HOOD.get())
                        .when(LootItemRandomChanceCondition.randomChance(0.15F))));
            }
        });
    }
}
