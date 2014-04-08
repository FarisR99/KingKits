package me.faris.kingkits.listeners.event.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerKitEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private Player thePlayer = null;
	private String kitName = "";
	private String oldKit = "";
	private List<ItemStack> kitContents = new ArrayList<ItemStack>();

	private boolean isCanceled = false;

	/**
	 * Create a new PlayerKitEvent instance.
	 * @param player - The player.
	 * @param kitName - The new kit.
	 **/
	public PlayerKitEvent(Player player, String kitName) {
		this.thePlayer = player;
		this.kitName = kitName;
	}

	/**
	 * Create a new PlayerKitEvent instance.
	 * @param player - The player.
	 * @param kitName - The new kit.
	 * @param oldKit - The previous kit the player was.
	 */
	public PlayerKitEvent(Player player, String kitName, String oldKit) {
		this.thePlayer = player;
		this.kitName = kitName;
		this.oldKit = oldKit;
	}

	/**
	 * Create a new PlayerKitEvent instance.
	 * @param player - The player.
	 * @param kitName - The new kit.
	 * @param oldKit - The previous kit the player was.
	 * @param newKitItems - The kit items.
	 */
	public PlayerKitEvent(Player player, String kitName, String oldKit, List<ItemStack> newKitItems) {
		this.thePlayer = player;
		this.kitName = kitName;
		this.oldKit = oldKit;
		this.kitContents = newKitItems;
	}

	/** Returns the player **/
	public Player getPlayer() {
		return this.thePlayer;
	}

	/** Returns the kit's name **/
	public String getKit() {
		return this.kitName;
	}

	/** Returns an unmodifiable List of items in the kit **/
	public List<ItemStack> getKitContents() {
		return Collections.unmodifiableList(this.kitContents);
	}

	/** Returns the player's old kit name **/
	public String getOldKit() {
		return this.oldKit;
	}

	/** Set the item contents of the kit **/
	public void setKitContents(List<ItemStack> kitContents) {
		if (kitContents != null) this.kitContents = kitContents;
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
