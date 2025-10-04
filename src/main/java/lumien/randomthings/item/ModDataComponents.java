package lumien.randomthings.item;

import com.mojang.serialization.Codec;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.util.ChunkAnalyzerResult;
import lumien.randomthings.util.ItemFilterData;
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
                
    public static final Supplier<DataComponentType<ChunkAnalyzerResult>> CHUNK_ANALYZER_RESULT = 
        DATA_COMPONENTS.register("chunk_analyzer_result", 
            () -> DataComponentType.<ChunkAnalyzerResult>builder()
                .persistent(ChunkAnalyzerResult.CODEC)
                .build());
                
    public static final Supplier<DataComponentType<Integer>> TARGET_TIME = 
        DATA_COMPONENTS.register("target_time", 
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());
                
    public static final Supplier<DataComponentType<Integer>> STORED_TIME = 
        DATA_COMPONENTS.register("stored_time", 
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());
                
    public static final Supplier<DataComponentType<Integer>> COMPASS_TARGET_X = 
        DATA_COMPONENTS.register("compass_target_x", 
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());
                
    public static final Supplier<DataComponentType<Integer>> COMPASS_TARGET_Z =
        DATA_COMPONENTS.register("compass_target_z",
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());

    public static final Supplier<DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent>> FLUID_CONTENT =
        DATA_COMPONENTS.register("fluid_content",
            () -> DataComponentType.<net.neoforged.neoforge.fluids.SimpleFluidContent>builder()
                .persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC)
                .networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC)
                .build());

    public static final Supplier<DataComponentType<String>> SENDER_NAME =
        DATA_COMPONENTS.register("sender_name",
            () -> DataComponentType.<String>builder()
                .persistent(Codec.STRING)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
                .build());

    public static final Supplier<DataComponentType<String>> RECEIVER_NAME =
        DATA_COMPONENTS.register("receiver_name",
            () -> DataComponentType.<String>builder()
                .persistent(Codec.STRING)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
                .build());

    public static final Supplier<DataComponentType<Boolean>> ENDER_LETTER_SIGNED =
        DATA_COMPONENTS.register("ender_letter_signed",
            () -> DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                .build());

    // Position Filter data components
    public static final Supplier<DataComponentType<String>> POSITION_DIMENSION =
        DATA_COMPONENTS.register("position_dimension",
            () -> DataComponentType.<String>builder()
                .persistent(Codec.STRING)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8)
                .build());

    public static final Supplier<DataComponentType<Integer>> POSITION_X =
        DATA_COMPONENTS.register("position_x",
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());

    public static final Supplier<DataComponentType<Integer>> POSITION_Y =
        DATA_COMPONENTS.register("position_y",
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());

    public static final Supplier<DataComponentType<Integer>> POSITION_Z =
        DATA_COMPONENTS.register("position_z",
            () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                .build());

    public static final Supplier<DataComponentType<ItemFilterData>> ITEM_FILTER_DATA =
        DATA_COMPONENTS.register("item_filter_data",
            () -> DataComponentType.<ItemFilterData>builder()
                .persistent(ItemFilterData.CODEC)
                .build());

}