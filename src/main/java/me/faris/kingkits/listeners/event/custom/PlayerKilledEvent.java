package me.faris.kingkits.listeners.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerKilledEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();

	private Player theDead = null;
	
	public PlayerKilledEvent(Player killer, Player dead) {
		super(killer);
		this.theDead = dead;
	}

	/** Returns the dead player **/
	public Player getDead() {
		return this.theDead;
	}

	/** Returns the killer **/
	public Player getKiller() {
		return this.getPlayer();
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
