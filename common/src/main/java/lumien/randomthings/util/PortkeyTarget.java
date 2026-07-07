package lumien.randomthings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A portkey destination: a dimension + block position. Stored on the item via {@link RTNbt#setCodec}. */
public record PortkeyTarget(net.minecraft.resources.ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<PortkeyTarget> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(PortkeyTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(PortkeyTarget::pos)
        ).apply(instance, PortkeyTarget::new)
    );
}
