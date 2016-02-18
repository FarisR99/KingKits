package com.faris.kingkits.api.event;

import com.faris.kingkits.Kit;
import com.faris.kingkits.player.KitPlayer;
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

	public Kit getKit() {
		return this.kit;
	}

	public KitPlayer getPlayer() {
		return this.kitPlayer;
	}

	public void setArmour(ItemStack[] armour) {
		if (this.kit != null) {
			this.kit.setArmour(armour != null ? armour : new ItemStack[4]);
		}
	}

	public void setItems(Map<Integer, ItemStack> items) {
		if (this.kit != null) {
			this.kit.setItems(items != null ? items : new HashMap<Integer, ItemStack>());
		}
	}

	public void setPotionEffects(List<PotionEffect> potionEffects) {
		if (this.kit != null) {
			this.kit.setPotionEffects(potionEffects != null ? potionEffects : new ArrayList<PotionEffect>());
		}
	}

	public void setKit(Kit kit) {
		if (kit != null) this.kit = kit;
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
