package net.burningtnt.pca.protocol;

import net.minecraft.util.Identifier;

public final class ProtocolConstants {
    public static final Identifier ENABLE_PCA_SYNC_PROTOCOL = ofID("enable_pca_sync_protocol");
    public static final Identifier DISABLE_PCA_SYNC_PROTOCOL = ofID("disable_pca_sync_protocol");
    public static final Identifier UPDATE_ENTITY = ofID("update_entity");
    public static final Identifier UPDATE_BLOCK_ENTITY = ofID("update_block_entity");

    public static final Identifier[] S2C_PACKAGES = {
            ENABLE_PCA_SYNC_PROTOCOL,
            DISABLE_PCA_SYNC_PROTOCOL,
            UPDATE_ENTITY,
            UPDATE_BLOCK_ENTITY
    };

    public static final Identifier SYNC_BLOCK_ENTITY = ofID("sync_block_entity");
    public static final Identifier SYNC_ENTITY = ofID("sync_entity");
    public static final Identifier CANCEL_SYNC_BLOCK_ENTITY = ofID("cancel_sync_block_entity");
    public static final Identifier CANCEL_SYNC_ENTITY = ofID("cancel_sync_entity");

    public static final Identifier[] C2S_PACKAGES = {
            SYNC_BLOCK_ENTITY,
            SYNC_ENTITY,
            CANCEL_SYNC_BLOCK_ENTITY,
            CANCEL_SYNC_ENTITY
    };

    private ProtocolConstants() {
    }

    private static Identifier ofID(String path) {
        return Identifier.of("pca", path);
    }
}
