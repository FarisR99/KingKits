package me.faris.kingkits.listeners;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class KingCommand implements CommandExecutor {
	private KingKits plugin = null;

	public KingCommand(KingKits pluginInstance) {
		this.plugin = pluginInstance;
	}

	/** Ignore this method **/
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return this.onCommand(sender, cmd.getName(), args);
	}

	/** Called when a command is typed by a player or the console **/
	protected abstract boolean onCommand(CommandSender sender, String command, String[] args);

	/** Returns the plugin instance **/
	protected KingKits getPlugin() {
		return this.plugin;
	}

	/** Returns a list of objects as a List **/
	protected Object getList(Object... objects) {
		List<Object> list = new ArrayList<Object>();
		for (Object o : objects)
			list.add(o);
		return list;
	}

	/** Returns if a sender is not a player **/
	protected boolean isConsole(CommandSender sender) {
		if (sender != null) {
			return !(sender instanceof Player);
		} else {
			return false;
		}
	}

	/** Returns if a string is a boolean **/
	protected boolean isBoolean(String booleanString) {
		return booleanString.equalsIgnoreCase("true") || booleanString.equalsIgnoreCase("false");
	}

	/** Returns if a string is a double **/
	protected boolean isDouble(String doubleString) {
		try {
			double d = Double.parseDouble(doubleString);
			return Math.floor(d) != d;
		} catch (Exception ex) {
			return false;
		}
	}

	/** Returns if a string is numeric **/
	protected boolean isNumeric(String numericString) {
		try {
			double d = Double.parseDouble(numericString);
			return Math.floor(d) == d;
		} catch (Exception ex) {
			return false;
		}
	}

	/** Returns a string with the real colours **/
	protected String r(String message) {
		return this.plugin.replaceAllColours(message);
	}

	/** Send the "no access" message to a player/console **/
	protected void sendNoAccess(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_RED + "You do not have access to this command.");
	}

}
