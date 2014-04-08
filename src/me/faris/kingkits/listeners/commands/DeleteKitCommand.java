package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.hooks.PvPKits;
import me.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class DeleteKitCommand extends PlayerCommand {

	public DeleteKitCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("deletekit")) {
			if (p.hasPermission(this.getPlugin().permissions.kitDeleteCommand)) {
				if (this.getPlugin().cmdValues.deleteKits) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
						if (args.length == 0) {
							p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " <kit>")));
							p.sendMessage(r("&cDescription: &Delete a PvP Kit."));
						} else if (args.length == 1) {
							String kitName = args[0];
							if (this.getPlugin().getKitsConfig().contains("Kits")) {
								List<String> listKits = this.getPlugin().getKitsConfig().getStringList("Kits");
								List<String> listKitsLC = new ArrayList<String>();
								for (int pos1 = 0; pos1 < listKits.size(); pos1++) {
									listKitsLC.add(listKits.get(pos1).toLowerCase());
								}
								if (listKitsLC.contains(kitName.toLowerCase())) {
									try {
										kitName = listKits.get(listKitsLC.indexOf(kitName.toLowerCase()));
										List<String> strItemsInKit = this.getPlugin().getKitsConfig().getStringList(kitName);
										for (int pos2 = 0; pos2 < strItemsInKit.size(); pos2++) {
											String strItems = strItemsInKit.get(pos2);
											String[] split = strItems.split(" ");
											int itemID = 0;
											if (split.length > 0) {
												try {
													itemID = Integer.parseInt(split[0]);
												} catch (Exception ex) {
													itemID = 0;
												}
											}
											this.getPlugin().getEnchantsConfig().set(kitName + " " + itemID, null);
											this.getPlugin().saveEnchantsConfig();
											this.getPlugin().getLoresConfig().set(kitName + " " + itemID, null);
											this.getPlugin().saveLoresConfig();
											this.getPlugin().getDyesConfig().set(kitName + " " + itemID, null);
											this.getPlugin().saveDyesConfig();
										}
										listKits.remove(kitName);
										this.getPlugin().getKitsConfig().set("Kits", listKits);
										this.getPlugin().getKitsConfig().set(kitName, null);
										this.getPlugin().saveKitsConfig();
										this.getPlugin().getPotionsConfig().set(kitName, null);
										this.getPlugin().savePotionsConfig();
										if (this.getPlugin().kitsItems.containsKey(kitName)) this.getPlugin().kitsItems.remove(kitName);

										if (this.getPlugin().getGuiItemsConfig().contains(kitName)) {
											this.getPlugin().getGuiItemsConfig().set(kitName, null);
											this.getPlugin().saveGuiItemsConfig();
										}

										p.sendMessage(r("&4" + kitName + " &6was successfully deleted."));
										for (int pos3 = 0; pos3 < p.getServer().getOnlinePlayers().length; pos3++) {
											Player target = p.getServer().getOnlinePlayers()[pos3];
											if (target != null) {
												if (!p.getName().equals(target.getName())) {
													if (this.getPlugin().usingKits.containsKey(target.getName())) {
														String targetKit = this.getPlugin().usingKits.get(target.getName());
														if (targetKit.equalsIgnoreCase(kitName)) {
															target.getInventory().clear();
															target.getInventory().setArmorContents(null);
															for (PotionEffect potionEffect : target.getActivePotionEffects())
																target.removePotionEffect(potionEffect.getType());
															PvPKits.removePlayer(target.getName());

															target.sendMessage(ChatColor.DARK_RED + p.getName() + ChatColor.RED + " deleted the kit you were using!");
														}
													}
												}
											}
										}
									} catch (Exception ex) {
										p.sendMessage(r("&4" + kitName + "&6's deletion was unsuccessful."));
									}
								} else {
									p.sendMessage(ChatColor.DARK_RED + "This kit doesn't exist.");
								}
							} else {
								p.sendMessage(ChatColor.DARK_RED + "This kit doesn't exist.");
							}
						} else {
							p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " <kit>")));
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

}
