package me.faris.kingkits.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.hooks.Plugin;
import me.faris.kingkits.hooks.PvPKits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiPreviewKit implements Listener {
	public static Map<String, GuiPreviewKit> playerMenus = new HashMap<String, GuiPreviewKit>();

	private KingKits thePlugin = null;
	private Player thePlayer = null;
	private String guiTitle = null;
	private List<ItemStack> guiItemStacks = null;
	private Inventory guiInventory = null;

	/** 
	 * Create a new Kit GUI preview instance.
	 * @param player - The player that is using the menu
	 * @param kitName - The kit name
	 */
	public GuiPreviewKit(Player player, String kitName) {
		this.thePlugin = Plugin.getPlugin();
		this.thePlayer = player;
		this.guiTitle = ChatColor.RED + kitName + ChatColor.DARK_GRAY + " kit preview";
		this.guiItemStacks = this.getPlugin().kitList.containsKey(kitName) ? this.getPlugin().kitList.get(kitName).getMergedItems() : new ArrayList<ItemStack>();

		int menuSize = 45;
		this.guiInventory = this.thePlayer.getServer().createInventory(null, menuSize, this.guiTitle);

		if (Plugin.isInitialised()) Bukkit.getPluginManager().registerEvents(this, Plugin.getPlugin());
		else Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("KingKits"));
	}

	public void openMenu() {
		if (!playerMenus.containsKey(this.thePlayer.getName())) {
			this.closeMenu(false, true);

			this.guiInventory.clear();
			for (int itemStackPos = 0; itemStackPos < this.guiItemStacks.size(); itemStackPos++) {
				if (itemStackPos < 36) {
					ItemStack itemStack = this.guiItemStacks.get(itemStackPos);
					if (itemStack != null && itemStack.getType() != Material.AIR) {
						this.guiInventory.addItem(itemStack);
					}
				}
			}
			ItemStack backItem = new ItemStack(Material.STONE_BUTTON);
			ItemMeta backItemMeta = backItem.getItemMeta();
			if (backItemMeta != null) {
				backItemMeta.setDisplayName(ChatColor.AQUA + "Back");
				backItem.setItemMeta(backItemMeta);
			}
			this.guiInventory.setItem(this.guiInventory.getSize() - 1, backItem);
			this.thePlayer.openInventory(this.guiInventory);
			playerMenus.put(this.thePlayer.getName(), this);
		}
	}

	public void closeMenu(boolean unregisterEvents, boolean closeInventory) {
		if (this.guiInventory != null) this.guiInventory.clear();
		if (unregisterEvents) HandlerList.unregisterAll(this);
		if (closeInventory) this.thePlayer.closeInventory();
		playerMenus.remove(this.thePlayer.getName());
	}

	@EventHandler
	protected void onPlayerClickInventory(InventoryClickEvent event) {
		try {
			if (playerMenus.containsKey(event.getWhoClicked().getName()) && event.getWhoClicked().getName().equals(this.thePlayer.getName())) {
				event.setCancelled(true);
				if (event.getWhoClicked() instanceof Player) {
					final Player player = (Player) event.getWhoClicked();
					if (event.getSlot() == this.guiInventory.getSize() - 1 && event.getCurrentItem().getType() == Material.STONE_BUTTON) {
						this.closeMenu(true, true);
						if (!GuiKitMenu.playerMenus.containsKey(event.getWhoClicked().getName())) {
							player.getServer().getScheduler().runTaskLater(this.thePlugin, new Runnable() {
								public void run() {
									if (player != null) {
										PvPKits.showKitMenu(player);
									}
								}
							}, 2L);
						}
					} else {
						player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
							@SuppressWarnings("deprecation")
							public void run() {
								if (player != null && player.isOnline()) player.updateInventory();
							}
						}, 5L);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	protected void onPlayerCloseInventory(InventoryCloseEvent event) {
		try {
			if (event.getPlayer().getName().equals(this.thePlayer.getName())) {
				if (playerMenus.containsKey(event.getPlayer().getName())) {
					this.closeMenu(true, false);
				} else {
					HandlerList.unregisterAll(this);
				}
			}
		} catch (Exception ex) {
		}
	}

	private KingKits getPlugin() {
		return this.thePlugin;
	}

}
