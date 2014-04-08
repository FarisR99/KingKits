package me.faris.kingkits.listeners.event.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerCreateKitEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private Player thePlayer = null;
	private String kitName = "";
	private List<ItemStack> kitContents = new ArrayList<ItemStack>();

	private boolean isCanceled = false;

	/**
	 * Create a new PlayerCreateKitEvent instance.
	 * @param player - The player.
	 * @param kitName - The new kit.
	 * @param newKitItems - The kit items.
	 */
	public PlayerCreateKitEvent(Player player, String kitName, List<ItemStack> newKitItems) {
		this.thePlayer = player;
		this.kitName = kitName;
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

	/** Returns an unmodifiable List of items in the new kit **/
	public List<ItemStack> getKitContents() {
		return Collections.unmodifiableList(this.kitContents);
	}

	/** Set the item contents of the new kit **/
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
