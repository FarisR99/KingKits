package com.faris.kingkits.listeners.event.custom;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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

    /**
     * Create a new KingKitsReloadEvent instance.
     *
     * @param sender - The CommandSender that is reloading the configuration.
     * @param failedException - If the reload has not been successful, this will hold the error thrown instance. If successful, this will be null.
     */
    public KingKitsReloadEvent(CommandSender sender, Exception failedException) {
        this.theSender = sender;
        this.failedException = failedException;
    }

    /**
     * Get the error exception.
     * @return The Exception if failed to reload, if succeeded, this will return null.
     */
    public Exception getException() {
        return this.failedException;
    }

    /**
     * Returns the player *
     */
    public CommandSender getSender() {
        return this.theSender;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Check whether or not the reload has been successful.
     * @return True if the reload has not been successful, false if it has been successful.
     */
    public boolean hasFailed() {
        return this.failedException != null;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
