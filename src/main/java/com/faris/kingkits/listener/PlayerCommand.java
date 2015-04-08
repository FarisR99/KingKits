package com.faris.kingkits.listener;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.helper.Lang;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public abstract class PlayerCommand extends KingCommand {

	public PlayerCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String command, String[] args) {
		if (this.isConsole(sender)) {
			Lang.sendMessage(sender, Lang.COMMAND_GEN_IN_GAME);
			return true;
		}
		return this.onCommand((Player) sender, command, args);
	}

	protected abstract boolean onCommand(Player player, String command, String[] args);

}
