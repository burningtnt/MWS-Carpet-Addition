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
        CarpetServer.settingsManager.parseSettingsClass(MWSCarpetSettings.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Map.of(
                "carpet.rule.legacyStackableShulkerBoxes.desc", "Use legacy implementation, which would not let hoppers stack shulker boxs and comparators would output like the way in vanilla Minecraft."
        );
    }
}
