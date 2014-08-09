package me.faris.kingkits.listeners.commands;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.helpers.Lang;
import me.faris.kingkits.helpers.Utils;
import me.faris.kingkits.hooks.PvPKits;
import me.faris.kingkits.listeners.PlayerCommand;
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
                            player.sendMessage(r("&cDescription: &Delete a PvP Kit."));
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
                                    for (int pos3 = 0; pos3 < player.getServer().getOnlinePlayers().length; pos3++) {
                                        Player target = player.getServer().getOnlinePlayers()[pos3];
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
