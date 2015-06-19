package com.faris.kingkits.listener;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public abstract class KingCommand implements CommandExecutor {
	private KingKits plugin = null;

	public KingCommand(KingKits pluginInstance) {
		this.plugin = pluginInstance;
	}

	/**
	 * Ignore this method *
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return this.onCommand(sender, cmd.getName(), args);
	}

	/**
	 * Called when a command is typed by a player or the console *
	 */
	protected abstract boolean onCommand(CommandSender sender, String command, String[] args);

	/**
	 * Returns the plugin instance *
	 */
	protected KingKits getPlugin() {
		return this.plugin;
	}

	/**
	 * Returns whether or not a String contains illegal characters. (i.e. Non-alphanumerical)
	 *
	 * @param strMessage - The String.
	 * @return Whether or not a String contains illegal characters.
	 */
	protected boolean containsIllegalCharacters(String strMessage) {
		return !strMessage.matches("[a-zA-Z0-9_ ]*");
	}

	/**
	 * Returns if a sender is not a player *
	 */
	protected boolean isConsole(CommandSender sender) {
		return sender != null && !(sender instanceof Player);
	}

	/**
	 * Returns if a string is a boolean *
	 */
	protected boolean isBoolean(String booleanString) {
		return booleanString.equalsIgnoreCase("true") || booleanString.equalsIgnoreCase("false");
	}

	/**
	 * Returns if a string is a double *
	 */
	protected boolean isDouble(String doubleString) {
		try {
			double d = Double.parseDouble(doubleString);
			return Math.floor(d) != d;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Returns if a string is numeric *
	 */
	protected boolean isInteger(String numericString) {
		try {
			double d = Double.parseDouble(numericString);
			return Math.floor(d) == d;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Returns a string with the real colours *
	 */
	protected String rCC(String message) {
		return Utilities.replaceChatColour(message);
	}

	/**
	 * Send the "no access" message to a player/console *
	 */
	protected void sendNoAccess(CommandSender sender) {
		Lang.sendMessage(sender, Lang.COMMAND_GEN_NO_PERMISSION);
	}

}
