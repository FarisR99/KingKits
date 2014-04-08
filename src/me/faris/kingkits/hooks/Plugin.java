package me.faris.kingkits.hooks;

import me.faris.kingkits.KingKits;

public class Plugin {
	private static KingKits pvpKits = null;

	public Plugin(KingKits plugin) {
		pvpKits = plugin;
	}

	public static KingKits getPlugin() {
		return pvpKits;
	}

	public static boolean isInitialised() {
		return pvpKits != null;
	}

}
