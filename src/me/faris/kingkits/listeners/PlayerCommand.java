package me.faris.kingkits.listeners;

import me.faris.kingkits.KingKits;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerCommand extends KingCommand {

	public PlayerCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String command, String[] args) {
		if (this.isConsole(sender)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
			return true;
		}
		return this.onCommand((Player) sender, command, args);
	}

	protected abstract boolean onCommand(Player p, String command, String[] args);

}
