package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.guis.GuiKingKits;
import com.faris.kingkits.guis.GuiPreviewKit;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.hooks.PvPKits;
import com.faris.kingkits.listeners.KingCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                                    sender.sendMessage(r("&aKits List (" + kitList.size() + "):"));
                                    if (!kitList.isEmpty()) {
                                        if (this.getPlugin().configValues.sortAlphabetically) Collections.sort(kitList, Utils.ALPHABETICAL_ORDER);
                                        for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
                                            String kitName = kitList.get(kitPos).split(" ")[0];
                                            if (sender.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
                                                sender.sendMessage(r("&6" + (kitPos + 1) + ". " + kitName));
                                            } else {
                                                if (this.getPlugin().configValues.cmdKitListPermissions)
                                                    sender.sendMessage(r("&4" + (kitPos + 1) + ". " + kitName));
                                            }
                                        }
                                    } else {
                                        sender.sendMessage(r("&4There are no kits."));
                                    }
                                } else {
                                    PvPKits.showKitMenu((Player) sender);
                                }
                            } else {
                                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to list the kits.");
                            }
                        } else if (args.length == 1) {
                            if (!this.isConsole(sender)) {
                                Player player = (Player) sender;
                                String kitName = args[0];
                                List<String> kitList = this.getPlugin().getKitList();
                                List<String> kitListLC = Utils.toLowerCaseList(kitList);
                                if (kitListLC.contains(kitName.toLowerCase()))
                                    kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
                                try {
                                    final Kit kit = this.getPlugin().kitList.get(kitName);
                                    if (kit != null && kit.hasCooldown() && !player.hasPermission(this.getPlugin().permissions.kitBypassCooldown)) {
                                        if (this.getPlugin().getCooldownConfig().contains(player.getName() + "." + kit.getRealName())) {
                                            long currentCooldown = this.getPlugin().getCooldown(player.getName(), kit.getRealName());
                                            if (System.currentTimeMillis() - currentCooldown >= kit.getCooldown() * 1000) {
                                                this.getPlugin().getCooldownConfig().set(player.getName() + "." + kit.getRealName(), null);
                                                this.getPlugin().saveCooldownConfig();
                                            } else {
                                                player.sendMessage(ChatColor.RED + "You must wait " + (kit.getCooldown() - ((System.currentTimeMillis() - currentCooldown) / 1000)) + " second(s) before using this kit again.");
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
                                sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
                            }
                        } else if (args.length == 2) {
                            if (sender.hasPermission(this.getPlugin().permissions.kitUseOtherCommand)) {
                                String strTarget = args[1];
                                Player target = sender.getServer().getPlayer(strTarget);
                                if (target != null && target.isOnline()) {
                                    String kitName = args[0];
                                    List<String> kitList = this.getPlugin().getKitList();
                                    List<String> kitListLC = Utils.toLowerCaseList(kitList);
                                    if (kitListLC.contains(kitName.toLowerCase()))
                                        kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
                                    try {
                                        SetKit.setKit(this.getPlugin(), target, kitName, false);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        sender.sendMessage(ChatColor.RED + "An error occurred.");
                                        return true;
                                    }
                                    sender.sendMessage(ChatColor.GOLD + "You set " + target.getName() + "'s kit. This may not have been successful if you typed an invalid kit name, or if they already have a kit, or if they do not have permission to use that kit or they do not have enough money.");
                                } else {
                                    sender.sendMessage(ChatColor.RED + "That player does not exist or is not online.");
                                }
                            } else {
                                this.sendNoAccess(sender);
                            }
                        } else {
                            Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <player>]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "This command is disabled in the configuration.");
                }
            } else {
                this.sendNoAccess(sender);
            }
            return true;
        }
        return false;
    }
}
