package com.faris.kingkits.api.event;

import com.faris.kingkits.Kit;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerPreKitEvent extends Event implements Cancellable {

	private static HandlerList handlerList = new HandlerList();

	private boolean cancelled = false;

	private KitPlayer kitPlayer = null;
	private Kit kit = null;

	public PlayerPreKitEvent(KitPlayer kitPlayer, Kit kit) {
		this.kitPlayer = kitPlayer;
		this.kit = kit != null ? kit.clone() : new Kit("null");
	}

	/**
	 * Get the kit the player chose.
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
	 * Set the armour of the kit chosen (does not save)
	 *
	 * @param armour The armour
	 */
	public void setArmour(ItemStack[] armour) {
		if (this.kit != null) {
			this.kit.setArmour(armour != null ? armour : new ItemStack[4]);
		}
	}

	/**
	 * Set the items in the kit chosen (does not save)
	 *
	 * @param items The items
	 */
	public void setItems(Map<Integer, ItemStack> items) {
		if (this.kit != null) {
			this.kit.setItems(items != null ? items : new HashMap<Integer, ItemStack>());
		}
	}

	/**
	 * Set the off-hand item in the kit chosen (does not save)
	 *
	 * @param offHand The off-hand item
	 */
	public void setOffHand(ItemStack offHand) {
		if (this.kit != null) {
			this.kit.setOffHand(offHand == null ? new ItemStack(Material.AIR) : offHand);
		}
	}

	/**
	 * Set the potion effects added when the kit chosen is selected (does not save)
	 *
	 * @param potionEffects The potion effects
	 */
	public void setPotionEffects(List<PotionEffect> potionEffects) {
		if (this.kit != null) {
			this.kit.setPotionEffects(potionEffects != null ? potionEffects : new ArrayList<PotionEffect>());
		}
	}

	/**
	 * Set the kit chosen
	 *
	 * @param kit The kit
	 */
	public void setKit(Kit kit) {
		if (kit != null) this.kit = kit.clone();
		else this.setCancelled(true);
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean flag) {
		this.cancelled = flag;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}

}
