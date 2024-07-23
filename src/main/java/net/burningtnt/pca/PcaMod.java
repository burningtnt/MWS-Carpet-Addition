package net.burningtnt.pca;

import net.burningtnt.pca.network.NetworkingHandle;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PcaMod implements ModInitializer {
    public static boolean pcaSyncProtocol = true;

    public static final Logger LOGGER = LogManager.getLogger("pca-protocol");

    @Override
    public void onInitialize() {
        NetworkingHandle.register();
    }
}
