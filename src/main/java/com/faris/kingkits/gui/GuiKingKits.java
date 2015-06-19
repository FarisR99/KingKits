package com.faris.kingkits.gui;

import com.faris.kingkits.KingKits;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

import java.util.HashMap;
import java.util.Map;

public abstract class GuiKingKits implements Listener {

	public static Map<String, GuiKitMenu> guiKitMenuMap = new HashMap<>();
	public static Map<String, GuiPreviewKit> guiPreviewKitMap = new HashMap<>();

	private Player player = null;
	private String playerName = null;
	protected Inventory guiInventory = null;

	/**
	 * Create a new GUI instance.
	 *
	 * @param player - The player that is using the menu
	 * @param inventory - The inventory.
	 */
	public GuiKingKits(Player player, Inventory inventory) {
		Validate.notNull(player);

		this.player = player;
		this.playerName = this.player.getName();

		this.guiInventory = inventory != null ? inventory : this.player.getServer().createInventory(this.player, InventoryType.CHEST);

		if (KingKits.getInstance() != null)
			this.player.getServer().getPluginManager().registerEvents(this, this.getPlugin());
		else
			this.player.getServer().getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("KingKits"));
	}

	public boolean openMenu() {
		this.closeMenu(false, false);
		if (!guiKitMenuMap.containsKey(this.playerName) && !guiPreviewKitMap.containsKey(this.playerName)) {
			if (this.getPlayer() != null) {
				this.fillInventory();
				this.getPlayer().openInventory(this.guiInventory);
				return true;
			}
		}
		return false;
	}

	protected abstract void fillInventory();

	public void closeMenu(boolean unregisterEvents, boolean closeInventory) {
		if (this.guiInventory != null) this.guiInventory.clear();
		if (unregisterEvents) HandlerList.unregisterAll(this);
		if (closeInventory && this.player != null) this.player.closeInventory();
	}

	@EventHandler
	protected abstract void onPlayerClickInventory(InventoryClickEvent event);

	@EventHandler(priority = EventPriority.LOW)
	protected void onPlayerCloseInventory(InventoryCloseEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null) {
				if (event.getPlayer() instanceof Player) {
					if (this.playerName.equals(event.getPlayer().getName())) {
						this.closeMenu(true, false);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Player getPlayer() {
		return this.player;
	}

	protected String getPlayerName() {
		return this.playerName != null ? this.playerName : "";
	}

	protected KingKits getPlugin() {
		return KingKits.getInstance();
	}

}
