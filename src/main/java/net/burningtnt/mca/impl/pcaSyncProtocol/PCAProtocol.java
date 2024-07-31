package net.burningtnt.mca.impl.pcaSyncProtocol;

import net.burningtnt.mca.carpet.MWSCarpetSettings;
import net.burningtnt.mca.network.NetworkingHandle;
import net.burningtnt.mca.network.PacketState;
import net.burningtnt.mca.impl.pcaSyncProtocol.impl.BlockEntityPCASynchronizeHandle;
import net.burningtnt.mca.impl.pcaSyncProtocol.impl.EntityPCASynchronizeHandle;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class PCAProtocol {
    public static final Identifier ENABLE_PCA_SYNC_PROTOCOL = ofPacket("enable_pca_sync_protocol");
    public static final Identifier DISABLE_PCA_SYNC_PROTOCOL = ofPacket("disable_pca_sync_protocol");

    public static final Identifier ENTITY_START = ofPacket("sync_entity");
    public static final Identifier ENTITY_CANCEL = ofPacket("cancel_sync_entity");
    public static final Identifier ENTITY_DATA = ofPacket("update_entity");

    public static final Identifier BE_START = ofPacket("sync_block_entity");
    public static final Identifier BE_CANCEL = ofPacket("cancel_sync_block_entity");
    public static final Identifier BE_DATA = ofPacket("update_block_entity");

    public static final AbstractPCASynchronizeHandle<Entity> H_ENTITY = new EntityPCASynchronizeHandle();
    public static final AbstractPCASynchronizeHandle<BlockEntity> H_BE = new BlockEntityPCASynchronizeHandle();

    public static void initialize() {
        NetworkingHandle.register(ENABLE_PCA_SYNC_PROTOCOL, PacketState.S2C, null);
        NetworkingHandle.register(DISABLE_PCA_SYNC_PROTOCOL, PacketState.S2C, null);

        H_ENTITY.register();
        H_BE.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            if (!MWSCarpetSettings.pcaSyncProtocol) {
                return;
            }

            ServerPlayerEntity player = handler.player;
            NetworkingHandle.send(player, ENABLE_PCA_SYNC_PROTOCOL, NetworkingHandle.NULL_BUFFER);
        });

        MWSCarpetSettings.registerListener("pcaSyncProtocol", server -> {
            Identifier packet = MWSCarpetSettings.pcaSyncProtocol ? ENABLE_PCA_SYNC_PROTOCOL : DISABLE_PCA_SYNC_PROTOCOL;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                NetworkingHandle.send(player, packet, NetworkingHandle.NULL_BUFFER);
            }
        });
    }

    private PCAProtocol() {
    }

    private static Identifier ofPacket(String id) {
        return Identifier.of("pca", id);
    }
}
