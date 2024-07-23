package net.burningtnt.pca.network;

import io.netty.buffer.ByteBuf;
import net.burningtnt.pca.PCASyncProtocol;
import net.burningtnt.pca.protocol.ProtocolConstants;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkingHandle {
    private NetworkingHandle() {
    }

    private static final Map<Identifier, CustomPayload.Id<PCAPayload>> PAYLOAD_ID = new ConcurrentHashMap<>();

    private static final Map<Identifier, PacketCodec<ByteBuf, PCAPayload>> CODEC_MAP = new ConcurrentHashMap<>();

    public static CustomPayload.Id<PCAPayload> ofPayloadID(Identifier identifier) {
        return PAYLOAD_ID.computeIfAbsent(identifier, CustomPayload.Id::new);
    }

    private static PacketCodec<ByteBuf, PCAPayload> ofCodec(Identifier id) {
        return CODEC_MAP.computeIfAbsent(id, identifier -> new PacketCodec<>() {
            @Override
            public PCAPayload decode(ByteBuf byteBuf) {
                ByteBuf data = byteBuf.alloc().buffer(byteBuf.readableBytes());
                byteBuf.readBytes(data);

                return new PCAPayload(identifier, new PacketByteBuf(data));
            }

            @Override
            public void encode(ByteBuf byteBuf, PCAPayload value) {
                byteBuf.writeBytes(value.buf());
            }
        });
    }

    public static void register() {
        for (Identifier identifier : ProtocolConstants.S2C_PACKAGES) {
            PayloadTypeRegistry.playS2C().register(ofPayloadID(identifier), ofCodec(identifier));
        }

        for (Identifier identifier : ProtocolConstants.C2S_PACKAGES) {
            PayloadTypeRegistry.playC2S().register(ofPayloadID(identifier), ofCodec(identifier));
        }

        for (Identifier identifier : ProtocolConstants.C2S_PACKAGES) {
            ServerPlayNetworking.registerGlobalReceiver(
                    ofPayloadID(identifier),
                    (payload, context) -> context.server().execute(() -> receive(context.player(), payload.identifier(), payload.buf()))
            );
        }
    }

    public static void send(ServerPlayerEntity player, Identifier identifier, PacketByteBuf buf) {
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new PCAPayload(identifier, buf)));
    }

    public static void receive(ServerPlayerEntity player, Identifier identifier, PacketByteBuf buf) {
        if (identifier.equals(ProtocolConstants.SYNC_BLOCK_ENTITY)) {
            PCASyncProtocol.syncBlockEntity(player, buf);
        }
        if (identifier.equals(ProtocolConstants.SYNC_ENTITY)) {
            PCASyncProtocol.syncEntityHandler(player, buf);
        }
        if (identifier.equals(ProtocolConstants.CANCEL_SYNC_BLOCK_ENTITY)) {
            PCASyncProtocol.cancelSyncBlockEntityHandler(player);
        }
        if (identifier.equals(ProtocolConstants.CANCEL_SYNC_ENTITY)) {
            PCASyncProtocol.cancelSyncEntityHandler(player);
        }
    }
}
