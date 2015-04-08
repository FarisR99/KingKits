package com.faris.kingkits.listener.event.custom;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;

public class PlayerCreateKitEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private String kitName = "";
	private Map<Integer, ItemStack> kitContents = new HashMap<Integer, ItemStack>();
	private List<ItemStack> armourContents = new ArrayList<ItemStack>();

	private boolean isCancelled = false;
	private boolean isUserKit = false;

	/**
	 * Create a new PlayerCreateKitEvent instance.
	 *
	 * @param player - The player.
	 * @param kitName - The new kit.
	 * @param newKitItems - The kit items.
	 * @param isUserKit - Whether the kit is a private user kit or not.
	 */
	public PlayerCreateKitEvent(Player player, String kitName, final List<ItemStack> newKitItems, List<ItemStack> armourContents, boolean isUserKit) {
		this(player, kitName, new HashMap<Integer, ItemStack>() {
			{
				for (int i = 0; i < newKitItems.size(); i++) {
					this.put(i, newKitItems.get(i));
				}
			}
		}, armourContents, isUserKit);

	}

	/**
	 * Create a new PlayerCreateKitEvent instance.
	 *
	 * @param player - The player.
	 * @param kitName - The new kit.
	 * @param newKitItems - The kit items.
	 * @param isUserKit - Whether the kit is a private user kit or not.
	 */
	public PlayerCreateKitEvent(Player player, String kitName, Map<Integer, ItemStack> newKitItems, List<ItemStack> armourContents, boolean isUserKit) {
		super(player);
		this.kitName = kitName;
		this.kitContents = newKitItems;
		this.armourContents = armourContents;
		this.isUserKit = isUserKit;
	}

	/**
	 * Returns the kit's name *
	 */
	public String getKit() {
		return this.kitName;
	}

	/**
	 * Returns an unmodifiable List of armour in the new kit *
	 */
	public List<ItemStack> getKitArmour() {
		return Collections.unmodifiableList(this.armourContents);
	}

	/**
	 * Returns an unmodifiable List of items in the new kit *
	 */
	public List<ItemStack> getKitContents() {
		return new ArrayList<ItemStack>(this.kitContents.values());
	}

	public Map<Integer, ItemStack> getKitContentsWithSlots() {
		return Collections.unmodifiableMap(this.kitContents);
	}

	public boolean isUserKit() {
		return this.isUserKit;
	}

	/**
	 * Set the item contents of the new kit *
	 */
	public void setArmourContents(List<ItemStack> armourContents) {
		if (armourContents != null) this.armourContents = armourContents;
	}

	/**
	 * Set the item contents of the new kit *
	 */
	public void setKitContents(List<ItemStack> kitContents) {
		if (kitContents != null) {
			this.kitContents = new HashMap<Integer, ItemStack>();
			for (int i = 0; i < kitContents.size(); i++) {
				ItemStack kitContent = kitContents.get(i);
				this.kitContents.put(i, kitContent == null ? new ItemStack(Material.AIR) : kitContent);
			}
		}
	}

	/**
	 * Set the item contents of the new kit *
	 */
	public void setKitContents(Map<Integer, ItemStack> kitContents) {
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
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean flag) {
		this.isCancelled = flag;
	}

}
