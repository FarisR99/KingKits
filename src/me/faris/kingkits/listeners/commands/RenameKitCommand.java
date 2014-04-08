package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RenameKitCommand extends PlayerCommand {

	public RenameKitCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("renamekit")) {
			try {
				if (p.hasPermission(this.getPlugin().permissions.kitRenameCommand)) {
					if (this.getPlugin().cmdValues.renameKits) {
						if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
							if (args.length == 0) {
								p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<name> <newname>]")));
								p.sendMessage(this.r("&cDescription: &4Rename a PvP kit."));
							} else if (args.length == 2) {
								String strKit = args[0];
								String strNewKit = args[1];
								List<String> currentKits = this.getPlugin().getKitsConfig().getStringList("Kits");
								if (this.getPlugin().getKitsConfig().contains(strKit)) {
									List<String> currentKitsLC = new ArrayList<String>();
									for (String kit : currentKits)
										currentKitsLC.add(kit.toLowerCase());
									if (!this.getPlugin().getKitsConfig().contains(strNewKit) && !currentKitsLC.contains(strNewKit.toLowerCase())) {
										if (currentKitsLC.contains(strKit.toLowerCase())) strKit = currentKits.get(currentKitsLC.indexOf(strKit.toLowerCase()));

										List<String> strItemsInKit = this.getPlugin().getKitsConfig().getStringList(strKit);
										boolean modifiedE = false, modifiedL = false, modifiedD = false;
										for (String itemInKit : strItemsInKit) {
											try {
												String[] split = itemInKit.split(" ");
												int itemID = Integer.parseInt(split[0]);
												if (this.getPlugin().getEnchantsConfig().contains(strKit + " " + itemID)) {
													this.getPlugin().getEnchantsConfig().set(strNewKit + " " + itemID, this.getPlugin().getEnchantsConfig().getStringList(strKit + " " + itemID));
													this.getPlugin().getEnchantsConfig().set(strKit + " " + itemID, null);
													modifiedE = true;
												}
												if (this.getPlugin().getLoresConfig().contains(strKit + " " + itemID)) {
													this.getPlugin().getLoresConfig().set(strNewKit + " " + itemID, this.getPlugin().getLoresConfig().getStringList(strKit + " " + itemID));
													this.getPlugin().getLoresConfig().set(strKit + " " + itemID, null);
													modifiedL = true;
												}
												if (this.getPlugin().getDyesConfig().contains(strKit + " " + itemID)) {
													this.getPlugin().getDyesConfig().set(strNewKit + " " + itemID, this.getPlugin().getDyesConfig().getInt(strKit + " " + itemID));
													this.getPlugin().getDyesConfig().set(strKit + " " + itemID, null);
													modifiedD = true;
												}
											} catch (Exception ex) {
											}
										}
										if (modifiedE) {
											this.getPlugin().saveEnchantsConfig();
										}
										if (modifiedL) {
											this.getPlugin().saveLoresConfig();
										}
										if (modifiedD) {
											this.getPlugin().saveDyesConfig();
										}

										List<String> newKits = this.getPlugin().getKitsConfig().getStringList("Kits");
										if (newKits.contains(strKit)) newKits.set(newKits.indexOf(strKit), strNewKit);
										else newKits.add(strNewKit);
										this.getPlugin().getKitsConfig().set("Kits", newKits);
										this.getPlugin().getKitsConfig().set(strKit, null);
										this.getPlugin().getKitsConfig().set(strNewKit, strItemsInKit);
										this.getPlugin().saveKitsConfig();

										this.getPlugin().getPotionsConfig().set(strNewKit, this.getPlugin().getPotionsConfig().getStringList(strKit));
										this.getPlugin().getPotionsConfig().set(strKit, null);
										this.getPlugin().savePotionsConfig();

										this.getPlugin().getGuiItemsConfig().set(strNewKit, this.getPlugin().getGuiItemsConfig().getInt(strKit));
										this.getPlugin().getGuiItemsConfig().set(strKit, null);
										this.getPlugin().saveGuiItemsConfig();

										this.getPlugin().getCPKConfig().set(strNewKit, this.getPlugin().getCPKConfig().getInt(strKit));
										this.getPlugin().getCPKConfig().set(strKit, null);
										this.getPlugin().saveCPKConfig();

										if (this.getPlugin().kitsItems.containsKey(strKit)) this.getPlugin().kitsItems.put(strNewKit, this.getPlugin().kitsItems.remove(strKit));

										p.sendMessage(ChatColor.GOLD + "Successfully renamed " + strKit + " to " + strNewKit + ".");
									} else {
										p.sendMessage(ChatColor.RED + strNewKit + " already exists.");
									}
								} else {
									p.sendMessage(ChatColor.RED + strKit + " doesn't exist.");
								}
							} else {
								p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<name> <newname>]")));
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
			} catch (Exception ex) {
				p.sendMessage(ChatColor.RED + "An error occured.");
			}
			return true;
		}
		return false;
	}

}
