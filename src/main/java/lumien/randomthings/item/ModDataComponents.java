package lumien.randomthings.item;

import com.mojang.serialization.Codec;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = 
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ModConstants.MOD_ID);
    
    public static final Supplier<DataComponentType<ResourceLocation>> BIOME_CRYSTAL_BIOME = 
        DATA_COMPONENTS.register("biome_crystal_biome", 
            () -> DataComponentType.<ResourceLocation>builder()
                .persistent(ResourceLocation.CODEC)
                .networkSynchronized(ResourceLocation.STREAM_CODEC)
                .build());
                
    public static final Supplier<DataComponentType<UUID>> PLAYER_UUID = 
        DATA_COMPONENTS.register("player_uuid", 
            () -> DataComponentType.<UUID>builder()
                .persistent(UUIDUtil.CODEC)
                .networkSynchronized(UUIDUtil.STREAM_CODEC)
                .build());
                
    public static final Supplier<DataComponentType<ResourceLocation>> DIAPHANOUS_BLOCK_STATE = 
        DATA_COMPONENTS.register("diaphanous_block_state", 
            () -> DataComponentType.<ResourceLocation>builder()
                .persistent(ResourceLocation.CODEC)
                .networkSynchronized(ResourceLocation.STREAM_CODEC)
                .build());
                
    public static final Supplier<DataComponentType<Boolean>> DIAPHANOUS_INVERTED = 
        DATA_COMPONENTS.register("diaphanous_inverted", 
            () -> DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                .build());
                
}