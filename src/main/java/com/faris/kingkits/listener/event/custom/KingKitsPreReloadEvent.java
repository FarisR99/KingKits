package com.faris.kingkits.listener.event.custom;

import org.bukkit.command.*;
import org.bukkit.event.*;

/**
 * Called before the KingKits' configuration reloads.
 */
public class KingKitsPreReloadEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private CommandSender theSender = null;
	private boolean isCanceled = false;

	public KingKitsPreReloadEvent(CommandSender sender) {
		this.theSender = sender;
	}

	/**
	 * Get the reloader.
	 *
	 * @return The reloader.
	 */
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
