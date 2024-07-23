package net.burningtnt.pca.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PCAPayload(Identifier identifier, PacketByteBuf buf) implements CustomPayload {
    @Override
    public Id<? extends CustomPayload> getId() {
        return NetworkingHandle.ofPayloadID(identifier);
    }
}
