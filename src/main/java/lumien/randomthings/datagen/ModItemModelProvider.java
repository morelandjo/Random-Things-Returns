package lumien.randomthings.datagen;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Simple items that use basic generated models
        simpleItem(ModItems.BLOOD_ROSE_PETAL.get());

        // Divining Rods - these have complex models with animations so we'll use existing
        simpleItem(ModItems.DIVINING_ROD_VANILLA.get());
        simpleItem(ModItems.DIVINING_ROD_COAL.get());
        simpleItem(ModItems.DIVINING_ROD_IRON.get());
        simpleItem(ModItems.DIVINING_ROD_GOLD.get());
        simpleItem(ModItems.DIVINING_ROD_DIAMOND.get());
        simpleItem(ModItems.DIVINING_ROD_EMERALD.get());
        simpleItem(ModItems.DIVINING_ROD_LAPIS.get());
        simpleItem(ModItems.DIVINING_ROD_REDSTONE.get());

        // Block items that don't need special handling
        // (Most block items inherit from their block models automatically)
    }

    private void simpleItem(Item item) {
        simpleItem(item.getDescriptionId().replace("item." + ModConstants.MOD_ID + ".", ""));
    }

    private void simpleItem(String name) {
        getBuilder(name)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + name));
    }
}