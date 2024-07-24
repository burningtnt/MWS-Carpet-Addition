package net.burningtnt.mca.pca;

import io.netty.buffer.Unpooled;
import net.burningtnt.mca.network.NetworkingHandle;
import net.burningtnt.mca.network.PacketState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractPCASynchronizeHandle<T> {
    private final Identifier startPacketID, cancelPacketID, dataPacketID;

    protected AbstractPCASynchronizeHandle(Identifier startPacketID, Identifier cancelPacketID, Identifier dataPacketID) {
        this.startPacketID = startPacketID;
        this.cancelPacketID = cancelPacketID;
        this.dataPacketID = dataPacketID;
    }

    private static final Object[] EMPTY_TARGET = new Object[0];

    @SuppressWarnings("unchecked")
    protected T[] locateTargets(ServerPlayerEntity player, PacketByteBuf buf) {
        return (T[]) EMPTY_TARGET;
    }

    protected abstract boolean encodeTarget(PacketByteBuf buf, T value);

    private final Map<ServerPlayerEntity, T[]> currentTargets = new HashMap<>();

    private final Map<T, Set<ServerPlayerEntity>> currentWatchers = new HashMap<>();

    public final void register() {
        NetworkingHandle.register(startPacketID, PacketState.C2S, (server, player, sender, buf) -> startSync(player, buf));
        NetworkingHandle.register(cancelPacketID, PacketState.C2S, (server, player, sender, buf) -> stopSync(player));
        NetworkingHandle.register(dataPacketID, PacketState.S2C, null);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> stopSync(handler.player));
    }

    private void startSync(ServerPlayerEntity player, PacketByteBuf buf) {
        T[] targets = locateTargets(player, buf);
        removeWatchers(player, currentTargets.put(player, targets));

        for (T target : targets) {
            currentWatchers.computeIfAbsent(target, k -> new HashSet<>()).add(player);
        }

        for (T target : targets) {
            PacketByteBuf buf2 = new PacketByteBuf(Unpooled.buffer());
            if (encodeTarget(buf2, target)) {
                NetworkingHandle.send(player, dataPacketID, buf2);
            }
        }
    }

    private void stopSync(ServerPlayerEntity player) {
        removeWatchers(player, currentTargets.remove(player));
    }

    private void removeWatchers(ServerPlayerEntity player, T[] targets) {
        if (targets == null) {
            return;
        }

        for (T target : targets) {
            Set<ServerPlayerEntity> watchers = currentWatchers.get(target);
            if (watchers == null) {
                continue;
            }

            watchers.remove(player);
            if (watchers.isEmpty()) {
                currentWatchers.remove(target);
            }
        }
    }

    public final void tickTarget(T target) {
        Set<ServerPlayerEntity> watchers = currentWatchers.get(target);
        if (watchers == null) {
            return;
        }

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        if (!encodeTarget(buf, target)) {
            return;
        }

        for (ServerPlayerEntity player : watchers) {
            NetworkingHandle.send(player, dataPacketID, buf.duplicate());
        }
    }
}
