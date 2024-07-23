package net.burningtnt.pca.utils;

import carpet.CarpetServer;
import net.fabricmc.loader.api.FabricLoader;

public class CarpetSettings {
	public static boolean getBoolRuleValue(String ruleName, boolean defaultValue)
	{
		if (FabricLoader.getInstance().isModLoaded("carpet")) {
			return (boolean) CarpetServer.settingsManager.getCarpetRule(ruleName).value();
		} else {
			return defaultValue;
		}
	}
}
