package net.burningtnt.mca.pca;

import carpet.CarpetServer;
import io.netty.buffer.Unpooled;
import net.burningtnt.mca.network.NetworkingHandle;
import net.burningtnt.mca.network.PacketState;
import net.burningtnt.mca.pca.impl.BlockEntityPCASynchronizeHandle;
import net.burningtnt.mca.pca.impl.EntityPCASynchronizeHandle;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
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
    public static final AbstractPCASynchronizeHandle<?>[] HANDLES = {H_ENTITY, H_BE};

    public static void initialize() {
        NetworkingHandle.register(ENABLE_PCA_SYNC_PROTOCOL, PacketState.S2C, null);
        NetworkingHandle.register(DISABLE_PCA_SYNC_PROTOCOL, PacketState.S2C, null);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            ServerPlayerEntity player = handler.player;
            NetworkingHandle.send(player, ENABLE_PCA_SYNC_PROTOCOL, new PacketByteBuf(Unpooled.buffer()));
        });

        for (AbstractPCASynchronizeHandle<?> handle : HANDLES) {
            handle.register();
        }
    }

    private PCAProtocol() {
    }

    private static Identifier ofPacket(String id) {
        return Identifier.of("pca", id);
    }

    public static boolean shouldEnableLargeBarrel() {
        if (FabricLoader.getInstance().isModLoaded("carpet")) {
            return (boolean) CarpetServer.settingsManager.getCarpetRule("largeBarrel").value();
        } else {
            return false;
        }
    }
}
