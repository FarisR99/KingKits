package com.faris.kingkits.gui;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.UUIDFetcher;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.helper.container.KitStack;
import com.faris.kingkits.listener.command.SetKit;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.ArrayList;
import java.util.List;

public class GuiKitMenu extends GuiKingKits {
	private KitStack[] guiKitStacks = null;

	/**
	 * Create a new gui menu instance.
	 *
	 * @param player - The player that is using the menu
	 * @param title - The title of the menu
	 * @param kitStacks - The kits in the menu
	 */
	public GuiKitMenu(Player player, String title, KitStack[] kitStacks) {
		super(player, player.getServer().createInventory(null, KingKits.getInstance() != null ? KingKits.getInstance().configValues.guiSize : 36, title));
		this.guiKitStacks = kitStacks;
	}

	@Override
	public boolean openMenu() {
		try {
			if (guiKitMenuMap.containsKey(this.getPlayerName())) {
				GuiKitMenu guiKitMenu = guiKitMenuMap.get(this.getPlayerName());
				if (guiKitMenu != null) guiKitMenu.closeMenu(true, true);
				guiKitMenuMap.remove(this.getPlayerName());
			}
			if (super.openMenu()) {
				guiKitMenuMap.put(this.getPlayerName(), this);
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
		guiKitMenuMap.remove(this.getPlayerName());
	}

	@Override
	protected void fillInventory() {
		if (this.getPlugin().configValues.sortAlphabetically) {
			for (int i = 0; i < this.guiKitStacks.length; i++) {
				try {
					ItemStack currentStack = this.guiKitStacks[i].getItemStack();
					if (currentStack != null) {
						if (currentStack.getType() != Material.AIR) {
							if (currentStack.getItemMeta() != null) {
								ItemMeta itemMeta = currentStack.getItemMeta();
								Kit targetKit = KingKitsAPI.getKitByName(this.guiKitStacks[i].getKitName(), (this.getPlayer() != null ? this.getPlayer().getUniqueId() : UUIDFetcher.lookupName(this.getPlayerName()).getId()));

								ChatColor kitColour = this.getPlayer().hasPermission("kingkits.kits." + (targetKit != null ? targetKit.getRealName().toLowerCase() : Utilities.stripColour(this.guiKitStacks[i].getKitName().toLowerCase()))) ? ChatColor.GREEN : ChatColor.DARK_RED;
								itemMeta.setDisplayName(ChatColor.RESET + "" + kitColour + this.guiKitStacks[i].getKitName());

								if (targetKit != null && targetKit.hasDescription()) {
									List<String> kitDescription = new ArrayList<String>();
									for (String descriptionLine : targetKit.getDescription()) {
										descriptionLine = Utilities.replaceChatColour(descriptionLine);
										descriptionLine = descriptionLine.replace("<player>", this.getPlayerName());
										descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
										descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
										descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
										descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
										kitDescription.add(descriptionLine);
									}
									itemMeta.setLore(kitDescription);
								}

								currentStack.setItemMeta(itemMeta);
							}
							this.guiInventory.addItem(currentStack);
						}
					}
				} catch (Exception ex) {
					continue;
				}
			}
		} else {
			List<ItemStack> addItems = new ArrayList<ItemStack>();
			for (int i = 0; i < this.guiKitStacks.length; i++) {
				try {
					ItemStack currentStack = this.guiKitStacks[i].getItemStack();
					if (currentStack != null) {
						if (currentStack.getType() != Material.AIR) {
							Kit targetKit = KingKitsAPI.getKitByName(this.guiKitStacks[i].getKitName(), (this.getPlayer() != null ? this.getPlayer().getUniqueId() : UUIDFetcher.lookupName(this.getPlayerName()).getId()));
							if (currentStack.getItemMeta() != null) {
								ItemMeta itemMeta = currentStack.getItemMeta();
								ChatColor kitColour = this.getPlayer().hasPermission("kingkits.kits." + (targetKit != null ? targetKit.getRealName().toLowerCase() : Utilities.stripColour(this.guiKitStacks[i].getKitName().toLowerCase()))) ? ChatColor.GREEN : ChatColor.DARK_RED;
								itemMeta.setDisplayName(ChatColor.RESET + "" + kitColour + this.guiKitStacks[i].getKitName());
								currentStack.setItemMeta(itemMeta);
							}
							if (targetKit != null && targetKit.getGuiPosition() > 0 && targetKit.getGuiPosition() < this.guiInventory.getSize()) {
								try {
									this.guiInventory.setItem(targetKit.getGuiPosition() - 1, currentStack);
								} catch (Exception ex) {
									ex.printStackTrace();
									addItems.add(currentStack);
								}
							} else {
								addItems.add(currentStack);
							}
						}
					}
				} catch (Exception ex) {
					continue;
				}
			}
			for (ItemStack itemStack : addItems) {
				this.guiInventory.addItem(itemStack);
			}
		}
	}

	/**
	 * Returns the kit item stacks *
	 */
	public KitStack[] getKitStacks() {
		return this.guiKitStacks;
	}

	/**
	 * Sets the kit item stacks *
	 */
	public GuiKitMenu setKitStacks(KitStack[] kitStacks) {
		this.guiKitStacks = kitStacks;
		return this;
	}

	@EventHandler
	protected void onPlayerClickInventory(InventoryClickEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null && event.getWhoClicked() != null) {
				if (event.getWhoClicked() instanceof Player) {
					if (event.getSlot() >= 0) {
						if (event.getSlotType() == SlotType.CONTAINER) {
							if (event.getWhoClicked().getName().equals(this.getPlayerName())) {
								if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
									ItemStack clickedItem = event.getCurrentItem();
									event.setCurrentItem(null);
									event.setCancelled(true);
									this.closeMenu(true, true);
									if (clickedItem != null && clickedItem.getType() != Material.AIR && clickedItem.getItemMeta() != null) {
										final String kitName = Utilities.stripColour(clickedItem.getItemMeta().getDisplayName());
										if (kitName != null) {
											final Kit kit = KingKitsAPI.getKitByName(kitName, event.getWhoClicked().getUniqueId());
											if (kit != null) {
												if (KingKitsAPI.isUserKit(kit.getRealName(), event.getWhoClicked().getUniqueId()) || event.getWhoClicked().hasPermission("kingkits.kits." + (kit.getRealName().toLowerCase()))) {
													final Player player = (Player) event.getWhoClicked();
													boolean validCooldown = true;
													if (kit != null && kit.hasCooldown() && !player.hasPermission(this.getPlugin().permissions.kitBypassCooldown)) {
														if (this.getPlugin().getCooldownConfig().contains(player.getName() + "." + kit.getRealName())) {
															long currentCooldown = this.getPlugin().getCooldown(player.getName(), kit.getRealName());
															if (System.currentTimeMillis() - currentCooldown >= kit.getCooldown() * 1000) {
																this.getPlugin().getCooldownConfig().set(player.getName() + "." + kit.getRealName(), null);
																this.getPlugin().saveCooldownConfig();
															} else {
																Lang.sendMessage(player, Lang.KIT_DELAY, String.valueOf(kit.getCooldown() - ((System.currentTimeMillis() - currentCooldown) / 1000)));
																validCooldown = false;
															}
														}
													}
													if (validCooldown) {
														SetKit.setKingKit(player, kit != null ? kit.getRealName() : kitName, true);
													}
												} else if (this.getPlugin().configValues.showKitPreview) {
													if (!guiPreviewKitMap.containsKey(event.getWhoClicked().getName())) {
														final Player player = (Player) event.getWhoClicked();
														player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
															public void run() {
																if (player != null) {
																	if (!guiPreviewKitMap.containsKey(player.getName())) {
																		new GuiPreviewKit(player, kitName).openMenu();
																	}
																}
															}
														}, 3L);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			if (event.getInventory() != null && this.guiInventory != null) {
				if (this.getPlayer().equals(event.getWhoClicked().getName())) {
					event.setCurrentItem(null);
					event.setCancelled(true);
					this.closeMenu(true, true);
				}
			}
			ex.printStackTrace();
		}
	}
}
