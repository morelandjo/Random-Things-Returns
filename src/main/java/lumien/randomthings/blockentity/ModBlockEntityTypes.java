package lumien.randomthings.blockentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = 
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModConstants.MOD_ID);

    public static final Supplier<BlockEntityType<AdvancedRedstoneTorchBlockEntity>> ADVANCED_REDSTONE_TORCH = 
        BLOCK_ENTITY_TYPES.register("advanced_redstone_torch", 
            () -> BlockEntityType.Builder.of(AdvancedRedstoneTorchBlockEntity::new, 
                ModBlocks.ADVANCED_REDSTONE_TORCH.get(), ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get()).build(null));

    public static final Supplier<BlockEntityType<BloodRoseBlockEntity>> BLOOD_ROSE = 
        BLOCK_ENTITY_TYPES.register("blood_rose", 
            () -> BlockEntityType.Builder.of(BloodRoseBlockEntity::new, 
                ModBlocks.BLOOD_ROSE.get()).build(null));
}