package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.hooks.PvPKits;
import com.faris.kingkits.listeners.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RenameUserKitCommand extends PlayerCommand {

    public RenameUserKitCommand(KingKits pluginInstance) {
        super(pluginInstance);
    }

    @Override
    protected boolean onCommand(Player p, String command, String[] args) {
        if (command.equalsIgnoreCase("renameukit")) {
            try {
                if (p.hasPermission(this.getPlugin().permissions.kitURenameCommand)) {
                    if (this.getPlugin().cmdValues.renameUKits) {
                        if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
                            if (args.length == 0) {
                                Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
                                p.sendMessage(this.r("&cDescription: &4Rename a user PvP kit."));
                            } else if (args.length == 2) {
                                String strKit = args[0];
                                String strNewKit = args[1];
                                List<String> currentKits = this.getPlugin().getKitList(p.getName());
                                List<String> currentKitsLC = Utils.toLowerCaseList(currentKits);
                                if (currentKitsLC.contains(strKit.toLowerCase()))
                                    strKit = currentKits.get(currentKitsLC.indexOf(strKit.toLowerCase()));
                                if (!this.getPlugin().getUserKitsConfig().contains(p.getName() + "." + strNewKit) && !currentKitsLC.contains(strNewKit.toLowerCase())) {
                                    if (PvPKits.isUserKit(strKit, p.getName())) {
                                        final Kit kit = PvPKits.getKitByName(strKit, p.getName());
                                        strKit = kit.getRealName();
                                        kit.setName(strNewKit);
                                        kit.setRealName(strNewKit);
                                        this.getPlugin().getUserKitsConfig().set(p.getName() + "." + strKit, null);
                                        this.getPlugin().getUserKitsConfig().set(p.getName() + "." + strNewKit, kit.serialize());
                                        this.getPlugin().saveUserKitsConfig();

                                        if (this.getPlugin().userKitList.containsKey(p.getName())) {
                                            List<Kit> kitList = this.getPlugin().userKitList.get(p.getName());
                                            if (kitList == null) kitList = new ArrayList<Kit>();
                                            int deleteIndex = -1;
                                            for (int i = 0; i < kitList.size(); i++) {
                                                Kit targetKit = kitList.get(i);
                                                if (targetKit != null && targetKit.getRealName().toLowerCase().equals(strKit)) {
                                                    deleteIndex = i;
                                                    break;
                                                }
                                            }
                                            if (deleteIndex != -1) {
                                                kitList.remove(deleteIndex);
                                                if (kitList.isEmpty()) {
                                                    this.getPlugin().userKitList.remove(p.getName());
                                                    this.getPlugin().getUserKitsConfig().set(p.getName(), null);
                                                    this.getPlugin().saveUserKitsConfig();
                                                } else {
                                                    this.getPlugin().userKitList.put(p.getName(), kitList);
                                                }
                                            }
                                        }
                                        List<Kit> kitList = this.getPlugin().userKitList.get(p.getName());
                                        if (kitList == null) kitList = new ArrayList<Kit>();
                                        if (!kitList.contains(kit)) kitList.add(kit);
                                        this.getPlugin().userKitList.put(p.getName(), kitList);

                                        if (this.getPlugin().usingKits.containsKey(p.getName()) && this.getPlugin().usingKits.get(p.getName()).equalsIgnoreCase(strKit)) this.getPlugin().usingKits.put(p.getName(), strNewKit);

                                        p.sendMessage(ChatColor.GOLD + "Successfully renamed " + strKit + " to " + strNewKit + ".");
                                    } else {
                                        p.sendMessage(ChatColor.RED + strKit + " doesn't exist.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + strNewKit + " already exists.");
                                }
                            } else {
                                Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
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
                p.sendMessage(ChatColor.RED + "An error occurred.");
            }
            return true;
        }
        return false;
    }
}
