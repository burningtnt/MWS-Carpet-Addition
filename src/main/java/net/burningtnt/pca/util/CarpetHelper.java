package net.burningtnt.pca.util;

import carpet.CarpetServer;
import net.fabricmc.loader.api.FabricLoader;

public class CarpetHelper {
	public static boolean getBoolRuleValue(String ruleName, boolean defaultValue)
	{
		if (FabricLoader.getInstance().isModLoaded("carpet")) {
			return (boolean) CarpetServer.settingsManager.getCarpetRule(ruleName).value();
		} else {
			return defaultValue;
		}
	}
}
