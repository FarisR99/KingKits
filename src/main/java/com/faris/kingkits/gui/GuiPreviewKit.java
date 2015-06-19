package com.faris.kingkits.gui;

import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.ArrayList;
import java.util.List;

public class GuiPreviewKit extends GuiKingKits {

	private List<ItemStack> guiItemStacks = null;
	private int oldKitMenuPage = 1;

	/**
	 * Create a new Kit GUI preview instance.
	 *
	 * @param player - The player that is using the menu
	 * @param kitName - The kit name
	 */
	public GuiPreviewKit(Player player, String kitName) {
		this(player, kitName, 1);
	}

	/**
	 * Create a new Kit GUI preview instance.
	 *
	 * @param player - The player that is using the menu
	 * @param kitName - The kit name
	 */
	public GuiPreviewKit(Player player, String kitName, int oldKitMenuPage) {
		super(player, player.getServer().createInventory(player, 45, Utilities.trimString(Lang.GUI_PREVIEW_TITLE.getMessage(kitName), 32)));
		Kit kit = KingKitsAPI.getKitByName(kitName, true);
		this.guiItemStacks = kit != null ? kit.getMergedItems() : new ArrayList<ItemStack>();
		this.oldKitMenuPage = oldKitMenuPage;
	}

	/**
	 * Create a new Kit GUI preview instance.
	 *
	 * @param player - The player that is using the menu
	 * @param kit - The kit
	 */
	public GuiPreviewKit(Player player, Kit kit) {
		super(player, player.getServer().createInventory(player, 45, Lang.GUI_PREVIEW_TITLE.getMessage(kit != null ? kit.getRealName() : "Empty")));
		this.guiItemStacks = kit != null ? kit.getMergedItems() : new ArrayList<ItemStack>();
	}

	@Override
	public boolean openMenu() {
		try {
			if (guiPreviewKitMap.containsKey(this.getPlayerName())) {
				GuiPreviewKit guiPreviewKitMenu = guiPreviewKitMap.get(this.getPlayerName());
				if (guiPreviewKitMenu != null) guiPreviewKitMenu.closeMenu(true, true);
				guiPreviewKitMap.remove(this.getPlayerName());
			}
			if (super.openMenu()) {
				guiPreviewKitMap.put(this.getPlayerName(), this);
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public void closeMenu(boolean unregisterEvents, boolean closeInventory) {
		super.closeMenu(unregisterEvents, closeInventory);
		guiPreviewKitMap.remove(this.getPlayerName());
	}

	@Override
	protected void fillInventory() {
		this.guiInventory.clear();

		for (int itemStackPos = 0; itemStackPos < this.guiItemStacks.size(); itemStackPos++) {
			if (itemStackPos < 36) {
				ItemStack itemStack = this.guiItemStacks.get(itemStackPos);
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					this.guiInventory.addItem(itemStack);
				}
			}
		}
		if (this.getPlugin().configValues.kitListMode.equalsIgnoreCase("GUI") || this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu")) {
			ItemStack backItem = new ItemStack(Material.STONE_BUTTON);
			ItemMeta backItemMeta = backItem.getItemMeta();
			if (backItemMeta != null) {
				backItemMeta.setDisplayName(Lang.GUI_PREVIEW_BACK.getMessage());
				backItem.setItemMeta(backItemMeta);
			}
			this.guiInventory.setItem(this.guiInventory.getSize() - 5, backItem);
		}
	}

	@EventHandler
	protected void onPlayerClickInventory(InventoryClickEvent event) {
		try {
			if (event.getWhoClicked().getName().equals(this.getPlayerName())) {
				event.setCancelled(true);
				if (event.getWhoClicked() instanceof Player) {
					final Player player = (Player) event.getWhoClicked();
					if (event.getSlot() == this.guiInventory.getSize() - 5 && event.getCurrentItem().getType() == Material.STONE_BUTTON) {
						this.closeMenu(true, true);
						if (!guiKitMenuMap.containsKey(event.getWhoClicked().getName())) {
							player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
								public void run() {
									if (player.isOnline()) KingKitsAPI.showKitMenu(player, oldKitMenuPage);
								}
							}, 3L);
						}
					} else {
						player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
							@SuppressWarnings("deprecation")
							public void run() {
								if (player.isOnline()) player.updateInventory();
							}
						}, 2L);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	protected void onPlayerCloseInventory(InventoryCloseEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null) {
				if (event.getPlayer() instanceof Player) {
					if (this.getPlayerName().equals(event.getPlayer().getName())) {
						this.closeMenu(true, false);
					}
				}
			}
		} catch (Exception ignored) {
		}
	}

}
