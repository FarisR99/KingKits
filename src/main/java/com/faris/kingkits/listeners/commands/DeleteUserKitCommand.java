package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.listeners.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DeleteUserKitCommand extends PlayerCommand {

    public DeleteUserKitCommand(KingKits instance) {
        super(instance);
    }

    @Override
    protected boolean onCommand(Player player, String command, String[] args) {
        if (command.equalsIgnoreCase("deleteukit")) {
            if (player.hasPermission(this.getPlugin().permissions.kitUDeleteCommand)) {
                if (this.getPlugin().cmdValues.deleteKits) {
                    if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(player.getWorld().getName())) {
                        if (args.length == 0) {
                            Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit>");
                            player.sendMessage(r("&cDescription: &4Delete a personal PvP Kit."));
                        } else if (args.length == 1) {
                            String kitName = args[0];
                            List<String> listKits = this.getPlugin().getKitList(player.getName());
                            List<String> listKitsLC = Utils.toLowerCaseList(listKits);
                            if (listKitsLC.contains(kitName.toLowerCase())) {
                                try {
                                    kitName = listKits.get(listKitsLC.indexOf(kitName.toLowerCase()));
                                    this.getPlugin().getUserKitsConfig().set(player.getName() + "." + kitName, null);
                                    this.getPlugin().saveUserKitsConfig();
                                    if (this.getPlugin().userKitList.containsKey(player.getName())) {
                                        List<Kit> kitList = this.getPlugin().userKitList.get(player.getName());
                                        if (kitList == null) kitList = new ArrayList<Kit>();
                                        int deleteIndex = -1;
                                        for (int i = 0; i < kitList.size(); i++) {
                                            Kit targetKit = kitList.get(i);
                                            if (targetKit != null && targetKit.getRealName().toLowerCase().equals(kitName.toLowerCase())) {
                                                deleteIndex = i;
                                                break;
                                            }
                                        }
                                        if (deleteIndex != -1) kitList.remove(deleteIndex);
                                        if (kitList.isEmpty()) {
                                            this.getPlugin().userKitList.remove(player.getName());
                                            this.getPlugin().getUserKitsConfig().set(player.getName(), null);
                                            this.getPlugin().saveUserKitsConfig();
                                        } else {
                                            this.getPlugin().userKitList.put(player.getName(), kitList);
                                        }
                                    }

                                    player.sendMessage(r("&4" + kitName + " &6was successfully deleted."));
                                    if (this.getPlugin().usingKits.containsKey(player.getName()) && this.getPlugin().usingKits.get(player.getName()).equalsIgnoreCase(kitName)) {
                                        this.getPlugin().usingKits.remove(player.getName());
                                        this.getPlugin().playerKits.remove(player.getName());
                                    }
                                } catch (Exception ex) {
                                    player.sendMessage(r("&4" + kitName + "&6's deletion was unsuccessful."));
                                }
                            } else {
                                player.sendMessage(ChatColor.DARK_RED + "That user kit doesn't exist.");
                            }
                        } else {
                            Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit>");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot use that command in this world.");
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
