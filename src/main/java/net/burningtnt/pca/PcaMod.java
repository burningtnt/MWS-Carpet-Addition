package net.burningtnt.pca;

import net.burningtnt.pca.network.NetworkingHandle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class PcaMod implements ModInitializer {
    @Nullable
    public static MinecraftServer server = null;

    public static boolean pcaSyncProtocol = true;

    public static final String MOD_ID = "pca-protocol";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(
            () -> new IllegalArgumentException("Cannot find myself!")
    ).getMetadata().getVersion().getFriendlyString();

    public static void init(MinecraftServer server) {
        PcaMod.server = server;
    }

    public static Identifier ofID(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        NetworkingHandle.register();
    }
}
