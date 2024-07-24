package net.burningtnt.mca.carpet;

import carpet.api.settings.Rule;

public final class MWSCarpetSettings {
    private MWSCarpetSettings() {
    }

    @Rule(
            categories = "mcs"
    )
    public static boolean legacyStackableShulkerBoxes = false;
}
