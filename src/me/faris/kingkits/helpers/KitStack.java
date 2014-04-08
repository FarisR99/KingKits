package me.faris.kingkits.helpers;

import org.bukkit.inventory.ItemStack;

public class KitStack {

	private String kitName = null;
	private ItemStack itemStack = null;

	public KitStack(String theKit, ItemStack theStack) {
		this.kitName = theKit;
		this.itemStack = theStack;
	}

	public String getKitName() {
		return this.kitName;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

}
