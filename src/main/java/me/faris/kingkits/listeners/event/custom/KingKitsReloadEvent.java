package me.faris.kingkits.listeners.event.custom;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KingKitsReloadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private CommandSender theSender = null;

	/**
	 * Create a new KingKitsReloadEvent instance.
	 * @param sender - The CommandSender that is reloading the configuration.
	 **/
	public KingKitsReloadEvent(CommandSender sender) {
		this.theSender = sender;
	}

	/** Returns the player **/
	public CommandSender getSender() {
		return this.theSender;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
