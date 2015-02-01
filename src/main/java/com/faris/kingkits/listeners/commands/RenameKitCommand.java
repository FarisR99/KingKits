package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.listeners.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
                                p.sendMessage(this.r("&cDescription: &4Rename a PvP kit."));
                            } else if (args.length == 2) {
                                String strKit = args[0];
                                String strNewKit = args[1];
                                List<String> currentKits = this.getPlugin().getKitList();
                                List<String> currentKitsLC = Utils.toLowerCaseList(currentKits);
                                if (currentKitsLC.contains(strKit.toLowerCase()))
                                    strKit = currentKits.get(currentKitsLC.indexOf(strKit.toLowerCase()));
                                if (!this.getPlugin().getKitsConfig().contains(strNewKit) && !currentKitsLC.contains(strNewKit.toLowerCase())) {
                                    if (this.getPlugin().kitList.containsKey(strKit)) {
                                        final Kit kit = this.getPlugin().kitList.get(strKit);
                                        this.getPlugin().getKitsConfig().set(strKit, null);
                                        kit.setName(strNewKit);
                                        kit.setRealName(strNewKit);
                                        this.getPlugin().getKitsConfig().set(strNewKit, kit.serialize());
                                        this.getPlugin().saveKitsConfig();

                                        Map<String, String> newKits = new HashMap<String, String>();
                                        for (Map.Entry<String, String> entrySet : this.getPlugin().usingKits.entrySet()) {
                                            if (entrySet.getValue() != null && entrySet.getValue().equals(strKit)) newKits.put(entrySet.getKey(), strNewKit);
                                        }
                                        this.getPlugin().usingKits.putAll(newKits);

                                        newKits = new HashMap<String, String>();
                                        for (Map.Entry<String, String> entrySet : this.getPlugin().playerKits.entrySet()) {
                                            if (entrySet.getValue() != null && entrySet.getValue().equals(strKit)) newKits.put(entrySet.getKey(), strNewKit);
                                        }
                                        this.getPlugin().playerKits.putAll(newKits);

                                        this.getPlugin().kitList.remove(strKit);
                                        this.getPlugin().kitList.put(strNewKit, kit);

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
