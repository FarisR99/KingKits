package me.faris.kingkits.listeners.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKilledEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Player theKiller = null, theDead = null;
	
	public PlayerKilledEvent(Player killer, Player dead) {
		this.theKiller = killer;
		this.theDead = dead;
	}

	/** Returns the dead player **/
	public Player getDead() {
		return this.theDead;
	}

	/** Returns the killer **/
	public Player getPlayer() {
		return this.theKiller;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
