package me.faris.kingkits.guis;

import java.util.HashMap;
import java.util.Map;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.helpers.KitStack;
import me.faris.kingkits.hooks.Plugin;
import me.faris.kingkits.listeners.commands.SetKit;

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
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiKitMenu implements Listener {

	public static Map<String, GuiKitMenu> playerMenus = new HashMap<String, GuiKitMenu>();

	private KingKits thePlugin = null;
	private Player thePlayer = null;
	private String guiTitle = null;
	private KitStack[] guiKitStacks = null;
	private Inventory guiInventory = null;

	/** 
	 * Create a new gui menu instance.
	 * @param player - The player that is using the menu
	 * @param title - The title of the menu
	 * @param kitStacks - The kits in the menu
	 */
	public GuiKitMenu(Player player, String title, KitStack[] kitStacks) {
		this.thePlugin = Plugin.getPlugin();
		this.thePlayer = player;
		this.guiTitle = title;
		this.guiKitStacks = kitStacks;

		if (Plugin.isInitialised()) Bukkit.getPluginManager().registerEvents(this, Plugin.getPlugin());
		else Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("KingKits"));
	}

	/** Opens the menu for the player **/
	public void openMenu() {
		try {
			if (!playerMenus.containsKey(this.thePlayer.getName())) {
				this.closeMenu(false, true);

				int menuSize = 36;
				if (this.guiKitStacks.length > 32) menuSize = 45;
				this.guiInventory = this.thePlayer.getServer().createInventory(null, menuSize, this.guiTitle);
				for (int i = 0; i < this.guiKitStacks.length; i++) {
					try {
						ItemStack currentStack = this.guiKitStacks[i].getItemStack();
						if (currentStack != null) {
							if (currentStack.getType() != Material.AIR) {
								if (currentStack.getItemMeta() != null) {
									ItemMeta itemMeta = currentStack.getItemMeta();
									ChatColor kitColour = this.thePlayer.hasPermission("kingkits.kits." + this.guiKitStacks[i].getKitName().toLowerCase()) ? ChatColor.GREEN : ChatColor.DARK_RED;
									itemMeta.setDisplayName(ChatColor.RESET + "" + kitColour + this.guiKitStacks[i].getKitName());
									currentStack.setItemMeta(itemMeta);
								}
								this.guiInventory.addItem(currentStack);
							}
						}
					} catch (Exception ex) {
						continue;
					}
				}
				this.thePlayer.openInventory(this.guiInventory);
				playerMenus.put(this.thePlayer.getName(), this);
			}
		} catch (Exception ex) {
		}
	}

	/** Closes the menu for the player and unregisters the event **/
	public void closeMenu(boolean unregisterEvents, boolean closeInventory) {
		try {
			if (unregisterEvents) HandlerList.unregisterAll(this);
			if (closeInventory) this.thePlayer.closeInventory();
			if (this.thePlayer != null) playerMenus.remove(this.thePlayer.getName());
		} catch (Exception ex) {
		}
	}

	/** Returns the player that is opening the menu **/
	public Player getPlayer() {
		return this.thePlayer;
	}

	/** Sets the player that is opening the menu **/
	public GuiKitMenu setPlayer(Player player) {
		this.thePlayer = player;
		return this;
	}

	/** Returns the title of the menu **/
	public String getTitle() {
		return this.guiTitle;
	}

	/** Sets the title of the menu **/
	public GuiKitMenu setTitle(String title) {
		this.guiTitle = title;
		return this;
	}

	/** Returns the kit item stacks **/
	public KitStack[] getKitStacks() {
		return this.guiKitStacks;
	}

	/** Sets the kit item stacks **/
	public GuiKitMenu setKitStacks(KitStack[] kitStacks) {
		this.guiKitStacks = kitStacks;
		return this;
	}

	/** Handles when a player clicks an item **/
	@EventHandler
	protected void onPlayerClickSlot(InventoryClickEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null && event.getWhoClicked() != null) {
				if (event.getWhoClicked() instanceof Player) {
					if (event.getSlot() >= 0) {
						if (event.getSlotType() == SlotType.CONTAINER) {
							if (event.getWhoClicked().getName().equals(this.thePlayer.getName()) && event.getInventory().getTitle().equals(this.guiInventory.getTitle())) {
								if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
									event.setCurrentItem(null);
									event.setCancelled(true);
									if (this.guiKitStacks.length >= event.getSlot()) {
										final String kitName = this.guiKitStacks[event.getSlot()].getKitName();
										if (kitName != null) {
											if (this.thePlugin.configValues.showKitPreview && !event.getWhoClicked().hasPermission("kingkits.kits." + kitName.toLowerCase())) {
												this.closeMenu(true, true);
												if (!GuiPreviewKit.playerMenus.containsKey(event.getWhoClicked().getName())) {
													final Player player = (Player) event.getWhoClicked();
													player.getServer().getScheduler().runTaskLater(this.thePlugin, new Runnable() {
														public void run() {
															if (player != null) {
																if (!GuiPreviewKit.playerMenus.containsKey(player.getName())) new GuiPreviewKit(player, kitName).openMenu();
															}
														}
													}, 5L);
												}
												return;
											} else {
												SetKit.setKingKit(this.thePlugin, (Player) event.getWhoClicked(), kitName, true);
											}
										}
									}
									this.closeMenu(true, true);
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			if (event.getInventory() != null && this.guiInventory != null) {
				if (this.thePlayer.getName().equals(event.getWhoClicked().getName()) && event.getInventory().getTitle().equals(this.guiInventory.getTitle())) {
					event.setCurrentItem(null);
					event.setCancelled(true);
					this.closeMenu(true, true);
				}
			}
		}
	}

	/** Handles when a player exits the menu **/
	@EventHandler(priority = EventPriority.HIGH)
	protected void onPlayerCloseInventory(InventoryCloseEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null) {
				if (event.getPlayer() instanceof Player) {
					if (this.thePlayer.getName().equals(event.getPlayer()) && event.getInventory().getTitle().equals(this.guiInventory.getTitle())) this.closeMenu(true, false);
				}
			}
		} catch (Exception ex) {
		}
	}
}
