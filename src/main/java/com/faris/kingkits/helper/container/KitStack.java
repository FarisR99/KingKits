package com.faris.kingkits.helper.container;

import org.bukkit.inventory.*;

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
