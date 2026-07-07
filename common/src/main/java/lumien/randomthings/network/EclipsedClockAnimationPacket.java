package lumien.randomthings.network;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Clientbound: play the Eclipsed Clock's spin animation / time display. */
public record EclipsedClockAnimationPacket(int entityId) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "eclipsed_clock_animation");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    public static EclipsedClockAnimationPacket decode(FriendlyByteBuf buf) {
        return new EclipsedClockAnimationPacket(buf.readVarInt());
    }
}
