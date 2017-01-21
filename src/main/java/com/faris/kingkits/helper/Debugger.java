package com.faris.kingkits.helper;

import com.faris.kingkits.KingKits;

import java.util.logging.Level;

/**
 * Solely for plugin debugging only
 */
public class Debugger {

	private static boolean debugging = false;

	public static void debugException(Exception ex) {
		debugException(null, ex);
	}

	public static void debugException(String message, Exception ex) {
		if (debugging) {
			KingKits.getInstance().getLogger().log(Level.WARNING, message != null ? "[Debug] " + message : "Debug error", ex);
		}
	}

	public static void debugMessage(String message) {
		if (debugging) KingKits.getInstance().getLogger().info("[Debug] " + message);
	}

}
