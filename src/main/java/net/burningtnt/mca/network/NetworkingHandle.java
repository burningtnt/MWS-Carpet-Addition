package net.burningtnt.mca.network;

import io.netty.buffer.ByteBuf;
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
            Identifier identifier,
            CustomPayload.Id<LegacyByteBufferPayload> payloadID,
            PacketCodec<ByteBuf, LegacyByteBufferPayload> codec,
            PacketReceiver receiver
    ) {
        private static final Map<Identifier, LegacyPacket> PACKETS = new ConcurrentHashMap<>();

        public LegacyPacket(Identifier identifier, PacketReceiver receiver) {
            this(identifier, new CustomPayload.Id<>(identifier), receiver);
        }

        private LegacyPacket(Identifier identifier, CustomPayload.Id<LegacyByteBufferPayload> payloadID, PacketReceiver receiver) {
            this(identifier, payloadID, new PacketCodec<>() {
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
            }, receiver);
        }

        public static LegacyPacket register(Identifier identifier, PacketReceiver receiver) {
            LegacyPacket value = new LegacyPacket(identifier, receiver);
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

    private record LegacyByteBufferPayload(Id<? extends CustomPayload> id, ByteBuf buf) implements CustomPayload {
        @Override
        public Id<? extends CustomPayload> getId() {
            return id;
        }
    }

    public static void register(Identifier identifier, PacketState state, PacketReceiver receiver) {
        boolean clientSide = state.isClientSide(), serverSide = state.isServerSide();

        if (serverSide && receiver == null) {
            throw new IllegalArgumentException("S2C or BOTH packets should provide a receiver.");
        }

        LegacyPacket packet = LegacyPacket.register(identifier, receiver);

        if (serverSide) {
            PayloadTypeRegistry.playC2S().register(packet.payloadID(), packet.codec());
            ServerPlayNetworking.registerGlobalReceiver(packet.payloadID(), (payload, context) -> context.server().execute(() -> {
                packet.receiver().handle(context.server(), context.player(), context.responseSender(), new PacketByteBuf(payload.buf()));
            }));
        }

        if (clientSide) {
            PayloadTypeRegistry.playS2C().register(packet.payloadID(), packet.codec());
        }
    }

    public static void send(ServerPlayerEntity player, Identifier identifier, ByteBuf buf) {
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new LegacyByteBufferPayload(LegacyPacket.get(identifier).payloadID(), buf)));
    }
}
