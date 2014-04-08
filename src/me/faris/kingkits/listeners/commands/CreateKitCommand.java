package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.listeners.PlayerCommand;
import me.faris.kingkits.listeners.event.custom.PlayerCreateKitEvent;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.permissions.Permission;

public class CreateKitCommand extends PlayerCommand {

	public CreateKitCommand(KingKits instance) {
		super(instance);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("createkit")) {
			if (p.hasPermission(this.getPlugin().permissions.kitCreateCommand)) {
				if (this.getPlugin().cmdValues.createKits) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
						if (args.length == 0) {
							p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<kit>|<kit> <guiitem>]")));
							p.sendMessage(this.r("&cDescription: &4Create your own PvP kit with every item in your inventory."));
						} else if (args.length > 0 && args.length < 3) {
							String kitName = args[0];
							boolean containsKit = this.getPlugin().getKitsConfig().contains(kitName);
							if (!this.containsIllegalChars(kitName)) {
								if (args.length == 2) {
									if (args[1].contains(":")) {
										String[] guiSplit = args[1].split(":");
										if (guiSplit.length == 2) {
											if (!this.isNumeric(guiSplit[0]) || !this.isNumeric(guiSplit[1])) {
												p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
												return true;
											}
										} else {
											if (!this.isNumeric(args[1])) {
												p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
												return true;
											}
										}
									} else {
										if (!this.isNumeric(args[1])) {
											p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
											return true;
										}
									}
								}

								List<String> currentKits = this.getPlugin().getKitsConfig().getStringList("Kits");
								List<String> currentKitsLC = KingKits.toLowerCaseList(currentKits);
								if (currentKitsLC.contains(kitName.toLowerCase())) kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));

								List<ItemStack> itemsInInv = new ArrayList<ItemStack>();
								for (ItemStack item : p.getInventory().getContents())
									if (item != null) if (item.getType() != Material.AIR) itemsInInv.add(item);
								for (ItemStack armour : p.getInventory().getArmorContents())
									if (armour != null) if (armour.getType() != Material.AIR) itemsInInv.add(armour);
								PlayerCreateKitEvent createKitEvent = new PlayerCreateKitEvent(p, kitName, itemsInInv);
								p.getServer().getPluginManager().callEvent(createKitEvent);

								if (!createKitEvent.isCancelled()) {
									itemsInInv = createKitEvent.getKitContents();
									if (itemsInInv.size() > 0) {
										if (containsKit) {
											List<String> itemsInKit = this.getPlugin().getKitsConfig().getStringList(kitName);
											boolean modifiedE = false, modifiedL = false, modifiedD = false;
											for (String itemInKit : itemsInKit) {
												try {
													int itemID = Integer.parseInt(itemInKit.split(" ")[0]);
													if (this.getPlugin().getEnchantsConfig().contains(kitName + " " + itemID)) {
														this.getPlugin().getEnchantsConfig().set(kitName + " " + itemID, null);
														modifiedE = true;
													}
													if (this.getPlugin().getLoresConfig().contains(kitName + " " + itemID)) {
														this.getPlugin().getLoresConfig().set(kitName + " " + itemID, null);
														modifiedL = true;
													}
													if (this.getPlugin().getDyesConfig().contains(kitName + " " + itemID)) {
														this.getPlugin().getDyesConfig().set(kitName + " " + itemID, null);
														modifiedD = true;
													}
												} catch (Exception ex) {
													continue;
												}
											}
											this.getPlugin().getKitsConfig().set(kitName, null);
											this.getPlugin().saveKitsConfig();
											this.getPlugin().getGuiItemsConfig().set(kitName, null);
											this.getPlugin().saveGuiItemsConfig();
											this.getPlugin().getCPKConfig().set(kitName, null);
											this.getPlugin().saveCPKConfig();
											if (modifiedE) {
												this.getPlugin().saveEnchantsConfig();
											}
											if (modifiedL) {
												this.getPlugin().saveLoresConfig();
											}
											if (modifiedD) {
												this.getPlugin().saveDyesConfig();
											}
											this.getPlugin().getPotionsConfig().set(kitName, null);
											this.getPlugin().savePotionsConfig();
											if (this.getPlugin().kitsItems.containsKey(kitName)) this.getPlugin().kitsItems.remove(kitName);
										}

										List<String> strItemsInKit = new ArrayList<String>();
										List<ItemStack> itemsInKit = new ArrayList<ItemStack>();
										for (int pos2 = 0; pos2 < itemsInInv.size(); pos2++) {
											ItemStack itemToAdd = itemsInInv.get(pos2);
											if (itemToAdd != null && itemToAdd.getType() != Material.AIR) {
												itemsInKit.add(itemToAdd);
											}
										}
										for (int pos3 = 0; pos3 < itemsInKit.size(); pos3++) {
											ItemStack item = itemsInKit.get(pos3);
											if (item.getItemMeta() != null) {
												if (item.getItemMeta().hasDisplayName()) {
													strItemsInKit.add(item.getType().getId() + " " + item.getAmount() + " " + item.getDurability() + " " + item.getItemMeta().getDisplayName());
												} else {
													strItemsInKit.add(item.getType().getId() + " " + item.getAmount() + " " + item.getDurability());
												}
											} else {
												strItemsInKit.add(item.getType().getId() + " " + item.getAmount() + " " + item.getDurability());
											}
											if (item.getEnchantments().size() > 0) {
												List<String> enchantments = new ArrayList<String>();
												List<Enchantment> enchantmentsE = new ArrayList<Enchantment>(item.getEnchantments().keySet());
												for (int pos6 = 0; pos6 < enchantmentsE.size(); pos6++) {
													String eName = enchantmentsE.get(pos6).getName();
													int level = item.getEnchantmentLevel(enchantmentsE.get(pos6));
													enchantments.add(eName + " " + level);
												}
												this.getPlugin().getEnchantsConfig().set(kitName + " " + item.getType().getId(), enchantments);
												this.getPlugin().saveEnchantsConfig();
											}
											if (item.hasItemMeta()) {
												if (item.getItemMeta().hasLore()) {
													this.getPlugin().getLoresConfig().set(kitName + " " + item.getType().getId(), item.getItemMeta().getLore());
													this.getPlugin().saveLoresConfig();
												}
											}
											if (item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_BOOTS) {
												try {
													if (item.getItemMeta() != null) {
														if (item.getItemMeta() instanceof LeatherArmorMeta) {
															LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
															if (armorMeta.getColor() != null) {
																this.getPlugin().getDyesConfig().set(kitName + " " + item.getType().getId(), armorMeta.getColor().asRGB());
																this.getPlugin().saveDyesConfig();
															}
														}
													}
												} catch (Exception ex) {
												}
											}
										}
										List<String> nKitList = new ArrayList<String>();
										if (this.getPlugin().getKitsConfig().contains("Kits")) nKitList = this.getPlugin().getKitsConfig().getStringList("Kits");
										if (!containsKit) nKitList.add(kitName);
										this.getPlugin().getKitsConfig().set("Kits", nKitList);
										this.getPlugin().getKitsConfig().set(kitName, strItemsInKit);
										this.getPlugin().saveKitsConfig();
										this.getPlugin().kitsItems.put(kitName, itemsInKit);

										boolean addGuiMenuItem = true;
										if (args.length == 2) {
											ItemStack guiItem = null;
											try {
												guiItem = new ItemStack(Integer.parseInt(args[1]));
											} catch (Exception ex) {
											}
											try {
												if (args[1].contains(":")) {
													String[] guiSplit = args[1].split(":");
													guiItem = new ItemStack(Integer.parseInt(guiSplit[0]));
													guiItem.setDurability(Short.parseShort(guiSplit[1]));
												}
											} catch (Exception ex) {
											}
											if (guiItem != null) {
												if (guiItem.getType() != Material.AIR) {
													addGuiMenuItem = false;
													if (guiItem.getDurability() != 0) this.getPlugin().getGuiItemsConfig().set(kitName, guiItem.getType().getId() + " " + guiItem.getDurability());
													else this.getPlugin().getGuiItemsConfig().set(kitName, guiItem.getType().getId());
												}
											}
										}
										if (addGuiMenuItem) this.getPlugin().getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
										this.getPlugin().saveGuiItemsConfig();

										this.getPlugin().getCPKConfig().set(kitName, this.getPlugin().getEconomyConfig().getDouble("Cost per kit"));
										this.getPlugin().saveCPKConfig();

										try {
											p.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
										} catch (Exception ex) {
										}
										if (containsKit) p.sendMessage(this.r("&4" + kitName + "&6 has been overwritten."));
										else p.sendMessage(this.r("&4" + kitName + "&6 has been created."));

										if (this.getPlugin().configValues.removeItemsOnCreateKit) {
											p.getInventory().clear();
											p.getInventory().setArmorContents(null);
										}
									} else {
										p.sendMessage(ChatColor.RED + "You have nothing in your inventory!");
									}
								} else {
									p.sendMessage(ChatColor.RED + "A plugin has not allowed you to create this kit.");
								}
							} else {
								p.sendMessage(this.r("&6The kit name must only consist of letters, numbers or underscores."));
							}
						} else {
							p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
						}
					} else {
						p.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
					}
				} else {
					p.sendMessage(ChatColor.RED + "This command is disabled in the configuration.");
				}
			} else {
				this.sendNoAccess(p);
			}
			return true;
		}
		return false;
	}

	private boolean containsIllegalChars(String strMessage) {
		return !strMessage.matches("[a-zA-Z0-9_ ]*");
	}

}
