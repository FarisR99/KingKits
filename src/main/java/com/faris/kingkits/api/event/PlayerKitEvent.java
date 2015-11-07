package com.faris.kingkits.api.event;

import com.faris.kingkits.Kit;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.event.*;

public class PlayerKitEvent extends Event {

	private static HandlerList handlerList = new HandlerList();

	private KitPlayer kitPlayer = null;
	private Kit oldKit = null;
	private Kit kit = null;

	public PlayerKitEvent(KitPlayer kitPlayer, Kit oldKit, Kit kit) {
		this.kitPlayer = kitPlayer;
		this.oldKit = oldKit;
		this.kit = kit;
	}

	public Kit getKit() {
		return this.kit;
	}

	public KitPlayer getPlayer() {
		return this.kitPlayer;
	}

	public Kit getPreviousKit() {
		return this.oldKit;
	}

	public boolean hasPreviousKit() {
		return this.oldKit != null;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

}
