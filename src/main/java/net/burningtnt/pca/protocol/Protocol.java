package net.burningtnt.pca.protocol;

import io.netty.buffer.Unpooled;
import net.burningtnt.pca.PCAMod;
import net.burningtnt.pca.network.NetworkingHandle;
import net.burningtnt.pca.network.PacketState;
import net.burningtnt.pca.protocol.impl.BlockEntitySynchronizeHandle;
import net.burningtnt.pca.protocol.impl.EntitySynchronizeHandle;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public final class Protocol {
    public static final Identifier ENABLE_PCA_SYNC_PROTOCOL = ofID("enable_pca_sync_protocol");
    public static final Identifier DISABLE_PCA_SYNC_PROTOCOL = ofID("disable_pca_sync_protocol");

    public static final Identifier ENTITY_START = ofID("sync_entity");
    public static final Identifier ENTITY_CANCEL = ofID("cancel_sync_entity");
    public static final Identifier ENTITY_DATA = ofID("update_entity");

    public static final Identifier BE_START = ofID("sync_block_entity");
    public static final Identifier BE_CANCEL = ofID("cancel_sync_block_entity");
    public static final Identifier BE_DATA = ofID("update_block_entity");

    public static final SynchronizeHandle<Entity> H_ENTITY = new EntitySynchronizeHandle();
    public static final SynchronizeHandle<BlockEntity> H_BE = new BlockEntitySynchronizeHandle();
    public static final List<SynchronizeHandle<?>> HANDLES = List.of(H_ENTITY, H_BE);

    public static void initialize() {
        NetworkingHandle.register(ENABLE_PCA_SYNC_PROTOCOL, PacketState.S2C, null);
        NetworkingHandle.register(DISABLE_PCA_SYNC_PROTOCOL, PacketState.S2C, null);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            if (PCAMod.pcaSyncProtocol) {
                NetworkingHandle.send(handler.player, ENABLE_PCA_SYNC_PROTOCOL, new PacketByteBuf(Unpooled.buffer()));
            }
        });

        for (SynchronizeHandle<?> handle : HANDLES) {
            handle.register();
        }
    }

    private Protocol() {
    }

    private static Identifier ofID(String path) {
        return Identifier.of("pca", path);
    }
}
