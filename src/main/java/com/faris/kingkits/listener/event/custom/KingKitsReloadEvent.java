package com.faris.kingkits.listener.event.custom;

import org.bukkit.command.*;
import org.bukkit.event.*;

/**
 * Called after the KingKits' configuration reloads.
 */
public class KingKitsReloadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private CommandSender theSender = null;
	private Exception failedException = null;

	/**
	 * Create a new KingKitsReloadEvent instance.
	 *
	 * @param sender - The CommandSender that is reloading the configuration.
	 */
	public KingKitsReloadEvent(CommandSender sender) {
		this.theSender = sender;
	}

	public KingKitsReloadEvent(CommandSender sender, Exception failedException) {
		this.theSender = sender;
		this.failedException = failedException;
	}

	/**
	 * Get the error exception.
	 *
	 * @return The Exception if failed to reload, if succeeded, this will return null.
	 */
	public Exception getException() {
		return this.failedException;
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

	/**
	 * Check whether or not the reload has been successful.
	 *
	 * @return True if the reload has not been successful, false if it has been successful.
	 */
	public boolean hasFailed() {
		return this.failedException != null;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
