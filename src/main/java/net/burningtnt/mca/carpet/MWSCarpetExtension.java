package net.burningtnt.mca.carpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import java.util.Map;

public final class MWSCarpetExtension implements CarpetExtension {
    private MWSCarpetExtension() {
    }

    public static void register() {
        CarpetServer.manageExtension(new MWSCarpetExtension());
    }

    @Override
    public void onGameStarted() {
        MWSCarpetSettings.initialize();
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return MWSCarpetSettings.TRANSLATION_MAP;
    }
}
