package com.faris.kingkits.listeners.event.custom;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KingKitsPreReloadEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private CommandSender theSender = null;
	private boolean isCanceled = false;

	/**
	 * Create a new KingKitsPreReloadEvent instance.
	 * @param sender - The CommandSender that is reloading the configuration.
	 **/
	public KingKitsPreReloadEvent(CommandSender sender) {
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

	@Override
	public boolean isCancelled() {
		return this.isCanceled;
	}

	@Override
	public void setCancelled(boolean flag) {
		this.isCanceled = flag;
	}

}
