package com.faris.kingkits.api.event;

import com.faris.kingkits.Kit;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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

	/**
	 * Get the kit the player is using.
	 *
	 * @return The kit.
	 */
	public Kit getKit() {
		return this.kit;
	}

	/**
	 * Get the player.
	 * <p></p>
	 * Use {@link KitPlayer#getBukkitPlayer()} to get the Bukkit player instance.
	 *
	 * @return The player.
	 */
	public KitPlayer getPlayer() {
		return this.kitPlayer;
	}

	/**
	 * Get the kit the player was previously using.
	 *
	 * @return The previous kit.
	 */
	public Kit getPreviousKit() {
		return this.oldKit;
	}

	/**
	 * Check if the player was using a kit previously.
	 *
	 * @return Whether or not the player was using a kit previously.
	 */
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
