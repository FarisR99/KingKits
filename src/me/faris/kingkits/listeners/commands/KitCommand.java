package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.guis.GuiKitMenu;
import me.faris.kingkits.guis.GuiPreviewKit;
import me.faris.kingkits.hooks.PvPKits;
import me.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KitCommand extends PlayerCommand {

	public KitCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("pvpkit")) {
			if (player.hasPermission(this.getPlugin().permissions.kitUseCommand)) {
				if (this.getPlugin().cmdValues.pvpKits) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(player.getWorld().getName())) {
						if (args.length == 0) {
							if (player.hasPermission(this.getPlugin().permissions.kitList)) {
								if (!this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui") && !this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu")) {
									List<String> kitList = new ArrayList<String>();
									if (this.getPlugin().getKitsConfig().contains("Kits")) {
										kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
									}
									player.sendMessage(r("&aKits List (" + kitList.size() + "):"));
									if (!kitList.isEmpty()) {
										for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
											String kitName = kitList.get(kitPos).split(" ")[0];
											if (player.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
												player.sendMessage(r("&6" + (kitPos + 1) + ". " + kitName));
											} else {
												if (this.getPlugin().configValues.cmdKitListPermissions) player.sendMessage(r("&4" + (kitPos + 1) + ". " + kitName));
											}
										}
									} else {
										player.sendMessage(r("&4There are no kits."));
									}
								} else {
									PvPKits.showKitMenu(player);
								}
							} else {
								player.sendMessage(ChatColor.DARK_RED + "You do not have permission to list the kits.");
							}
						} else if (args.length == 1) {
							if (!this.getPlugin().configValues.kitCooldown || (this.getPlugin().configValues.kitCooldown && !this.getPlugin().kitCooldownPlayers.contains(player.getName()))) {
								String kitName = args[0];
								List<String> kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
								List<String> kitListLC = new ArrayList<String>();
								for (int pos0 = 0; pos0 < kitList.size(); pos0++) {
									kitListLC.add(kitList.get(pos0).toLowerCase());
								}
								if (kitListLC.contains(kitName.toLowerCase())) {
									kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
								}
								try {
									if (this.getPlugin().configValues.showKitPreview && !player.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
										if (!GuiKitMenu.playerMenus.containsKey(player.getName()) && !GuiPreviewKit.playerMenus.containsKey(player.getName())) {
											if (this.getPlugin().getKitsConfig().contains(kitName)) {
												GuiPreviewKit guiPreviewKit = new GuiPreviewKit(player, kitName);
												guiPreviewKit.openMenu();
											} else {
												SetKit.setKingKit(this.getPlugin(), player, kitName, true);
											}
										} else {
											SetKit.setKingKit(this.getPlugin(), player, kitName, true);
										}
									} else {
										SetKit.setKingKit(this.getPlugin(), player, kitName, true);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							} else {
								player.sendMessage(ChatColor.RED + "There is a " + this.getPlugin().configValues.kitCooldownTime + " second(s) cooldown when choosing kits!");
							}
						} else if (args.length == 2) {
							if (player.hasPermission(this.getPlugin().permissions.kitUseOtherCommand)) {
								String strTarget = args[1];
								Player target = player.getServer().getPlayer(strTarget);
								if (target != null && target.isOnline()) {
									String kitName = args[0];
									List<String> kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
									List<String> kitListLC = new ArrayList<String>();
									for (String kit : kitList)
										kitListLC.add(kit);
									if (kitListLC.contains(kitName.toLowerCase())) kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
									try {
										SetKit.setKit(this.getPlugin(), target, kitName, false);
									} catch (Exception ex) {
										ex.printStackTrace();
										player.sendMessage(ChatColor.RED + "An error occured.");
										return true;
									}
									player.sendMessage(ChatColor.GOLD + "You set " + target.getName() + "'s kit. This may not have been successful if you typed an invalid kit name, they already have a kit, they do not have permission to use that kit or they do not have enough money.");
								} else {
									player.sendMessage(ChatColor.RED + "That player does not exist or is not online.");
								}
							} else {
								this.sendNoAccess(player);
							}
						} else {
							player.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<kit>|<kit> <player>]")));
						}
					} else {
						player.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "This command is disabled in the configuration.");
				}
			} else {
				this.sendNoAccess(player);
			}
			return true;
		}
		return false;
	}
}
