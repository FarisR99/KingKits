package com.faris.kingkits.listener.event.custom;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.*;

/**
 * Called when a player is about to change kits.
 */
public class PlayerKitEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private String kitName = "";
	private String oldKit = "";
	private Map<Integer, ItemStack> kitContents = new HashMap<>();
	private List<ItemStack> armourItems = new ArrayList<>();
	private List<PotionEffect> potionEffects = new ArrayList<>();
	private List<String> kitCommands = new ArrayList<>();

	private boolean isCancelled = false;

	public PlayerKitEvent(Player player, String kitName) {
		super(player);
		this.kitName = kitName;
	}

	public PlayerKitEvent(Player player, String kitName, String oldKit) {
		super(player);
		this.kitName = kitName;
		this.oldKit = oldKit;
	}

	public PlayerKitEvent(Player player, String kitName, String oldKit, Map<Integer, ItemStack> newKitItems, List<ItemStack> armourItems) {
		super(player);
		this.kitName = kitName;
		this.oldKit = oldKit;
		this.kitContents = newKitItems;
		this.armourItems = armourItems;
	}

	public PlayerKitEvent(Player player, String kitName, String oldKit, Map<Integer, ItemStack> newKitItems, List<ItemStack> armourItems, List<PotionEffect> potionEffects) {
		super(player);
		this.kitName = kitName;
		this.oldKit = oldKit;
		this.kitContents = newKitItems;
		this.armourItems = armourItems;
		this.potionEffects = potionEffects;
	}

	/**
	 * @return An unmodifiable List of all the kit-specific commands to run.
	 */
	public List<String> getCommands() {
		return Collections.unmodifiableList(this.kitCommands);
	}

	/**
	 * @return The kit's name.
	 */
	public String getKit() {
		return this.kitName;
	}

	/**
	 * @return An unmodifiable List of all the armour in the kit.
	 */
	public List<ItemStack> getKitArmour() {
		return Collections.unmodifiableList(this.armourItems);
	}

	/**
	 * @return An unmodifiable List of all the items in the kit.
	 */
	public List<ItemStack> getKitContents() {
		return new ArrayList<>(this.kitContents.values());
	}

	/**
	 * @return An unmodifiable Map of all the items in the kit and their slots.
	 */
	public Map<Integer, ItemStack> getKitContentsWithSlots() {
		return Collections.unmodifiableMap(this.kitContents);
	}

	/**
	 * @return An unmodifiable List of all the potion effects.
	 */
	public List<PotionEffect> getPotionEffects() {
		return Collections.unmodifiableList(this.potionEffects);
	}

	/**
	 * @return The player's old kit's name.
	 */
	public String getOldKit() {
		return this.oldKit;
	}

	/**
	 * Set the commands to run.
	 *
	 * @param commands The commands to run
	 */
	public void setCommands(List<String> commands) {
		this.kitCommands = commands != null ? commands : new ArrayList<String>();
	}

	/**
	 * Set the armour contents.
	 *
	 * @param armourContents The armour contents
	 */
	public void setKitArmour(List<ItemStack> armourContents) {
		this.armourItems = armourContents != null ? armourContents : new ArrayList<ItemStack>();
	}

	/**
	 * Set the item contents.
	 *
	 * @param kitContents The items
	 */
	public void setKitContents(List<ItemStack> kitContents) {
		this.kitContents = new HashMap<>();
		if (kitContents != null) {
			for (int i = 0; i < kitContents.size(); i++) {
				ItemStack kitContent = kitContents.get(i);
				if (kitContent != null) this.kitContents.put(i, kitContent);
			}
		}
	}

	/**
	 * Set the item contents.
	 *
	 * @param kitContents The items and their slots
	 */
	public void setKitContents(Map<Integer, ItemStack> kitContents) {
		this.kitContents = kitContents != null ? kitContents : new HashMap<Integer, ItemStack>();
	}

	/**
	 * Set the potion effects
	 *
	 * @param potionEffects The potion effects
	 */
	public void setPotionEffects(List<PotionEffect> potionEffects) {
		this.potionEffects = potionEffects != null ? potionEffects : new ArrayList<PotionEffect>();
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
