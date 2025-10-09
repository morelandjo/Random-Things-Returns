package lumien.randomthings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record PortkeyTarget(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<PortkeyTarget> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(PortkeyTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(PortkeyTarget::pos)
        ).apply(instance, PortkeyTarget::new)
    );

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, PortkeyTarget> STREAM_CODEC =
        StreamCodec.composite(
            net.minecraft.resources.ResourceKey.streamCodec(net.minecraft.core.registries.Registries.DIMENSION),
            PortkeyTarget::dimension,
            BlockPos.STREAM_CODEC,
            PortkeyTarget::pos,
            PortkeyTarget::new
        );
}
