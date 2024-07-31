package net.burningtnt.mca.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private record LegacyPacket(
            CustomPayload.Id<LegacyByteBufferPayload> payloadID,
            PacketState state
    ) {
        private static final Map<Identifier, LegacyPacket> PACKETS = new ConcurrentHashMap<>();

        public static LegacyPacket register(Identifier identifier, PacketState state) {
            LegacyPacket value = new LegacyPacket(new CustomPayload.Id<>(identifier), state);
            if (PACKETS.putIfAbsent(identifier, value) != null) {
                throw new IllegalStateException(String.format("Packet %s has been registered twice.", identifier));
            }
            return value;
        }

        public static LegacyPacket get(Identifier identifier) {
            LegacyPacket value = PACKETS.get(identifier);
            if (value == null) {
                throw new IllegalArgumentException(String.format("Packet %s hasn't been registered.", identifier));
            }
            return value;
        }
    }

    private record LegacyByteBufferPayload(
            Id<? extends CustomPayload> payloadID,
            ByteBuf buf
    ) implements CustomPayload {
        @Override
        public Id<? extends CustomPayload> getId() {
            return payloadID;
        }
    }

    private static PacketCodec<ByteBuf, LegacyByteBufferPayload> ofCodec(CustomPayload.Id<? extends CustomPayload> payloadID) {
        return new PacketCodec<>() {
            @Override
            public LegacyByteBufferPayload decode(ByteBuf byteBuf) {
                ByteBuf data = byteBuf.retainedDuplicate();
                byteBuf.readerIndex(byteBuf.readerIndex() + byteBuf.readableBytes());
                return new LegacyByteBufferPayload(payloadID, data);
            }

            @Override
            public void encode(ByteBuf byteBuf, LegacyByteBufferPayload value) {
                byteBuf.writeBytes(value.buf());
            }
        };
    }

    public static void register(Identifier identifier, PacketState state, PacketReceiver receiver) {
        boolean isS2C = state.isS2C(), isC2S = state.isC2S();

        if (isC2S && receiver == null) {
            throw new IllegalArgumentException("S2C or BOTH packets should provide a receiver.");
        }

        CustomPayload.Id<LegacyByteBufferPayload> payloadID = LegacyPacket.register(identifier, state).payloadID();
        PacketCodec<ByteBuf, LegacyByteBufferPayload> codec = ofCodec(payloadID);

        if (isC2S) {
            PayloadTypeRegistry.playC2S().register(payloadID, codec);
            ServerPlayNetworking.registerGlobalReceiver(payloadID, (payload, context) -> context.server().execute(
                    () -> receiver.handle(context.server(), context.player(), context.responseSender(), new PacketByteBuf(payload.buf()))
            ));
        }

        if (isS2C) {
            PayloadTypeRegistry.playS2C().register(payloadID, codec);
        }
    }

    public static final ByteBuf NULL_BUFFER = Unpooled.buffer(0, 0);

    public static void send(ServerPlayerEntity player, Identifier identifier, ByteBuf buf) {
        LegacyPacket packet = LegacyPacket.get(identifier);
        if (!packet.state().isS2C()) {
            throw new IllegalArgumentException(String.format("Packet %s cannot be send from server to client.", identifier));
        }
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new LegacyByteBufferPayload(packet.payloadID(), buf)));
    }
}
