package com.faris.kingkits.listener.event.custom;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;

/**
 * Called before a player creates a kit.
 */
public class PlayerCreateKitEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private String kitName = "";
	private Map<Integer, ItemStack> kitContents = new HashMap<Integer, ItemStack>();
	private List<ItemStack> armourContents = new ArrayList<ItemStack>();

	private boolean isCancelled = false;
	private boolean isUserKit = false;

	public PlayerCreateKitEvent(Player player, String kitName, final List<ItemStack> newKitItems, List<ItemStack> armourContents, boolean isUserKit) {
		this(player, kitName, new HashMap<Integer, ItemStack>() {
			{
				for (int i = 0; i < newKitItems.size(); i++) this.put(i, newKitItems.get(i));
			}
		}, armourContents, isUserKit);

	}

	public PlayerCreateKitEvent(Player player, String kitName, Map<Integer, ItemStack> newKitItems, List<ItemStack> armourContents, boolean isUserKit) {
		super(player);
		this.kitName = kitName;
		this.kitContents = newKitItems;
		this.armourContents = armourContents;
		this.isUserKit = isUserKit;
	}

	/**
	 * Get the kit's name.
	 *
	 * @return The kit's name.
	 */
	public String getKit() {
		return this.kitName;
	}

	/**
	 * Get a list of the armour in the new kit.
	 *
	 * @return An unmodifiable List of armour in the new kit.
	 */
	public List<ItemStack> getKitArmour() {
		return Collections.unmodifiableList(this.armourContents);
	}

	/**
	 * Get a list of all the items in the new kit.
	 *
	 * @return An unmodifiable List of items in the new kit.
	 */
	public List<ItemStack> getKitContents() {
		return new ArrayList<ItemStack>(this.kitContents.values());
	}

	/**
	 * Get all the items in the new kit and their slots.
	 *
	 * @return An unmodifiable Map of all the items in the new kit and their slots.
	 */
	public Map<Integer, ItemStack> getKitContentsWithSlots() {
		return Collections.unmodifiableMap(this.kitContents);
	}

	/**
	 * Check whether the created kit is a user kit or not.
	 *
	 * @return Whether the created kit is a user kit or not.
	 */
	public boolean isUserKit() {
		return this.isUserKit;
	}

	/**
	 * Set the armour contents of the new kit.
	 *
	 * @param armourContents The armour contents
	 */
	public void setArmourContents(List<ItemStack> armourContents) {
		if (armourContents != null) this.armourContents = armourContents;
	}

	/**
	 * Set the item contents of the new kit.
	 *
	 * @param kitContents The items
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
	 * Set the item contents of the new kit.
	 *
	 * @param kitContents The items
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
