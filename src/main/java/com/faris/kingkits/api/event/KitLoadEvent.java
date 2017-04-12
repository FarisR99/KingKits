package com.faris.kingkits.api.event;

import com.faris.kingkits.Kit;
import com.faris.kingkits.config.CustomConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitLoadEvent extends Event {

	private static HandlerList handlerList = new HandlerList();

	private Kit kit = null;
	private CustomConfiguration config = null;

	public KitLoadEvent(Kit kit, CustomConfiguration config) {
		super();
		this.kit = kit;
		this.config = config;
	}

	public CustomConfiguration getConfig() {
		return this.config;
	}

	public Kit getKit() {
		return this.kit;
	}

	public void setKit(Kit kit) {
		this.kit = kit;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

}
