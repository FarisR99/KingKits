package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.gui.GuiKingKits;
import com.faris.kingkits.gui.GuiPreviewKit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.KingCommand;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitCommand extends KingCommand {

	public KitCommand(KingKits instance) {
		super(instance);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(CommandSender sender, String command, String[] args) {
		if (command.equalsIgnoreCase("pvpkit")) {
			if (sender.hasPermission(this.getPlugin().permissions.kitUseCommand)) {
				if (this.getPlugin().cmdValues.pvpKits) {
					if (this.isConsole(sender) || this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(((Player) sender).getWorld().getName())) {
						if (args.length == 0) {
							if (sender.hasPermission(this.getPlugin().permissions.kitList)) {
								if (this.isConsole(sender) || (!this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui") && !this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu"))) {
									List<String> kitList = new ArrayList<String>(this.getPlugin().kitList.keySet());
									Lang.sendMessage(sender, Lang.GEN_KIT_LIST_TITLE, String.valueOf(kitList.size()));
									if (!kitList.isEmpty()) {
										if (this.getPlugin().configValues.sortAlphabetically)
											Collections.sort(kitList, Utilities.ALPHABETICAL_ORDER);
										for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
											String kitName = kitList.get(kitPos).split(" ")[0];
											if (sender.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
												sender.sendMessage(rCC("&6" + (kitPos + 1) + ". " + kitName));
											} else {
												if (this.getPlugin().configValues.kitListPermissions)
													sender.sendMessage(rCC("&4" + (kitPos + 1) + ". " + kitName));
											}
										}
									} else {
										Lang.sendMessage(sender, Lang.GEN_NO_KITS);
									}
								} else {
									KingKitsAPI.showKitMenu((Player) sender);
								}
							} else {
								Lang.sendMessage(sender, Lang.COMMAND_KIT_LIST_NO_PERMISSION);
							}
						} else if (args.length == 1) {
							if (!this.isConsole(sender)) {
								Player player = (Player) sender;
								String kitName = args[0];
								List<String> kitList = this.getPlugin().getKitList();
								List<String> kitListLC = Utilities.toLowerCaseList(kitList);
								if (kitListLC.contains(kitName.toLowerCase()))
									kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
								try {
									final Kit kit = KingKitsAPI.getKitByName(kitName, false);
									if (kit != null && kit.hasCooldown() && !player.hasPermission(this.getPlugin().permissions.kitBypassCooldown)) {
										if (this.getPlugin().getCooldownConfig().contains(player.getName() + "." + kit.getRealName())) {
											long currentCooldown = this.getPlugin().getCooldown(player.getName(), kit.getRealName());
											if (System.currentTimeMillis() - currentCooldown >= kit.getCooldown() * 1000) {
												this.getPlugin().getCooldownConfig().set(player.getName() + "." + kit.getRealName(), null);
												this.getPlugin().saveCooldownConfig();
											} else {
												Lang.sendMessage(player, Lang.KIT_DELAY, String.valueOf((kit.getCooldown() - ((System.currentTimeMillis() - currentCooldown) / 1000))));
												return true;
											}
										}
									}
									if (this.getPlugin().configValues.showKitPreview && !player.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
										if (!GuiKingKits.guiKitMenuMap.containsKey(player.getName()) && !GuiKingKits.guiPreviewKitMap.containsKey(player.getName())) {
											if (this.getPlugin().getKitsConfig().contains(kitName)) {
												GuiPreviewKit guiPreviewKit = new GuiPreviewKit(player, kitName);
												guiPreviewKit.openMenu();
											} else {
												SetKit.setKingKit(player, kitName, true);
											}
										} else {
											SetKit.setKingKit(player, kitName, true);
										}
									} else {
										SetKit.setKingKit(player, kitName, true);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							} else {
								Lang.sendMessage(sender, Lang.COMMAND_GEN_IN_GAME);
							}
						} else if (args.length == 2) {
							if (sender.hasPermission(this.getPlugin().permissions.kitUseOtherCommand)) {
								String strTarget = args[1];
								Player target = sender.getServer().getPlayer(strTarget);
								if (target != null && target.isOnline()) {
									String kitName = args[0];
									List<String> kitList = this.getPlugin().getKitList();
									List<String> kitListLC = Utilities.toLowerCaseList(kitList);
									if (kitListLC.contains(kitName.toLowerCase()))
										kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
									try {
										SetKit.setKit(target, kitName, false);
									} catch (Exception ex) {
										ex.printStackTrace();
										Lang.sendMessage(sender, Lang.COMMAND_GEN_ERROR);
										return true;
									}
									Lang.sendMessage(sender, Lang.COMMAND_KIT_OTHER_PLAYER, target.getName());
								} else {
									Lang.sendMessage(sender, Lang.COMMAND_GEN_NOT_ONLINE, strTarget);
								}
							} else {
								this.sendNoAccess(sender);
							}
						} else {
							Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <player>]");
						}
					} else {
						Lang.sendMessage(sender, Lang.COMMAND_GEN_WORLD);
					}
				} else {
					Lang.sendMessage(sender, Lang.COMMAND_GEN_DISABLED);
				}
			} else {
				this.sendNoAccess(sender);
			}
			return true;
		}
		return false;
	}
}
