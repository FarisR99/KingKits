package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import org.bukkit.command.*;

public abstract class KingKitsCommand implements CommandExecutor {

	private KingKits plugin = null;

	public KingKitsCommand(KingKits pluginInstance) {
		this.plugin = pluginInstance;
	}

	protected KingKits getPlugin() {
		return this.plugin;
	}

}
