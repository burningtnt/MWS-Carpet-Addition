package net.burningtnt.pca.network;

import io.netty.buffer.ByteBuf;
import net.burningtnt.pca.PCAMod;
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

    private record PacketObject(
            Identifier identifier,
            CustomPayload.Id<LegacyByteBufferPayload> payloadID,
            PacketCodec<ByteBuf, LegacyByteBufferPayload> codec,
            PacketReceiver receiver
    ) {
        private static final Map<Identifier, PacketObject> PACKETS = new ConcurrentHashMap<>();

        private PacketObject(Identifier identifier, PacketReceiver receiver) {
            this(identifier, new CustomPayload.Id<>(identifier), new PacketCodec<>() {
                @Override
                public LegacyByteBufferPayload decode(ByteBuf byteBuf) {
                    ByteBuf data = byteBuf.alloc().buffer(byteBuf.readableBytes());
                    byteBuf.readBytes(data);

                    return new LegacyByteBufferPayload(identifier, data);
                }

                @Override
                public void encode(ByteBuf byteBuf, LegacyByteBufferPayload value) {
                    byteBuf.writeBytes(value.buf());
                }
            }, receiver);
        }

        public static PacketObject register(Identifier identifier, PacketReceiver receiver) {
            PacketObject value = new PacketObject(identifier, receiver);
            if (PACKETS.putIfAbsent(identifier, value) != null) {
                PCAMod.LOGGER.warn(String.format("Cannot register packet %s.", identifier), new IllegalStateException(String.format("Packet %s has been registered twice.", identifier)));
                return null;
            }
            return value;
        }
    }

    private record LegacyByteBufferPayload(Identifier identifier, ByteBuf buf) implements CustomPayload {
        @Override
        public Id<? extends CustomPayload> getId() {
            PacketObject value = PacketObject.PACKETS.get(identifier);
            if (value == null) {
                throw new IllegalArgumentException(String.format("Packet %s hasn't been registered.", identifier));
            }
            return value.payloadID();
        }
    }

    public static void register(Identifier identifier, PacketState state, PacketReceiver receiver) {
        boolean clientSide = state.isClientSide(), serverSide = state.isServerSide();

        if (serverSide && receiver == null) {
            throw new IllegalArgumentException("S2C or BOTH packets should provide a receiver.");
        }

        PacketObject packet = PacketObject.register(identifier, receiver);
        if (packet == null) {
            return;
        }

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
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new LegacyByteBufferPayload(identifier, buf)));
    }
}
