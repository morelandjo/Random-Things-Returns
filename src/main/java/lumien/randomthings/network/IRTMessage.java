package lumien.randomthings.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IRTMessage extends CustomPacketPayload {
    void handle(IPayloadContext context);
}