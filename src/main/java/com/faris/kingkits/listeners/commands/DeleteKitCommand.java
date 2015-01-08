package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.hooks.PvPKits;
import com.faris.kingkits.listeners.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class DeleteKitCommand extends PlayerCommand {

    public DeleteKitCommand(KingKits instance) {
        super(instance);
    }

    @Override
    protected boolean onCommand(Player player, String command, String[] args) {
        if (command.equalsIgnoreCase("deletekit")) {
            if (player.hasPermission(this.getPlugin().permissions.kitDeleteCommand)) {
                if (this.getPlugin().cmdValues.deleteKits) {
                    if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(player.getWorld().getName())) {
                        if (args.length == 0) {
                            Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit>");
                            player.sendMessage(r("&cDescription: &4Delete a PvP Kit."));
                        } else if (args.length == 1) {
                            String kitName = args[0];
                            List<String> listKits = this.getPlugin().getKitList();
                            List<String> listKitsLC = Utils.toLowerCaseList(listKits);
                            if (listKitsLC.contains(kitName.toLowerCase())) {
                                try {
                                    kitName = listKits.get(listKitsLC.indexOf(kitName.toLowerCase()));
                                    this.getPlugin().getKitsConfig().set(kitName, null);
                                    this.getPlugin().saveKitsConfig();
                                    if (this.getPlugin().kitList.containsKey(kitName))
                                        this.getPlugin().kitList.remove(kitName);

                                    player.sendMessage(r("&4" + kitName + " &6was successfully deleted."));
                                    for (Player target : Utils.getOnlinePlayers()) {
                                        if (target != null) {
                                            if (this.getPlugin().usingKits.containsKey(target.getName())) {
                                                String targetKit = this.getPlugin().usingKits.get(target.getName());
                                                if (targetKit.equalsIgnoreCase(kitName)) {
                                                    PvPKits.removePlayer(target.getName());
                                                    if (!player.getName().equals(target.getName())) {
                                                        target.getInventory().clear();
                                                        target.getInventory().setArmorContents(null);
                                                        for (PotionEffect potionEffect : target.getActivePotionEffects())
                                                            target.removePotionEffect(potionEffect.getType());

                                                        target.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " deleted the kit you were using!");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    player.sendMessage(r("&4" + kitName + "&6's deletion was unsuccessful."));
                                }
                            } else {
                                player.sendMessage(ChatColor.DARK_RED + "That kit doesn't exist.");
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
