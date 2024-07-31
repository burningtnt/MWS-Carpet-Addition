package net.burningtnt.mca;

import net.burningtnt.mca.carpet.MWSCarpetExtension;
import net.burningtnt.mca.impl.pcaSyncProtocol.PCAProtocol;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MWSCarpetAddition implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("mws-carpet-addition");

    @Override
    public void onInitialize() {
        MWSCarpetAddition.LOGGER.info("Welcome using MWS's Carpet Addition. MWS Server: QQ 853022836");

        MWSCarpetExtension.register();
        PCAProtocol.initialize();
    }
}
