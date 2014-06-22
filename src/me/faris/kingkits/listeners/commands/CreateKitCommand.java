package me.faris.kingkits.listeners.commands;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Kit;
import me.faris.kingkits.helpers.Lang;
import me.faris.kingkits.helpers.Utils;
import me.faris.kingkits.listeners.PlayerCommand;
import me.faris.kingkits.listeners.event.custom.PlayerCreateKitEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

public class CreateKitCommand extends PlayerCommand {

    public CreateKitCommand(KingKits instance) {
        super(instance);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean onCommand(Player p, String command, String[] args) {
        if (command.equalsIgnoreCase("createkit")) {
            if (p.hasPermission(this.getPlugin().permissions.kitCreateCommand)) {
                if (this.getPlugin().cmdValues.createKits) {
                    if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
                        if (args.length == 0) {
                            Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
                            p.sendMessage(this.r("&cDescription: &4Create your own PvP kit with every item in your inventory."));
                        } else if (args.length > 0 && args.length < 3) {
                            String kitName = args[0];

                            boolean containsKit = this.getPlugin().getKitsConfig().contains(kitName);
                            if (!containsKit) {
                                List<String> currentKits = this.getPlugin().getKitList();
                                List<String> currentKitsLC = Utils.toLowerCaseList(currentKits);
                                if (currentKitsLC.contains(kitName.toLowerCase()))
                                    kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));
                                containsKit = this.getPlugin().getKitsConfig().contains(kitName);
                            }

                            if (!this.containsIllegalChars(kitName)) {
                                if (args.length == 2) {
                                    if (args[1].contains(":")) {
                                        String[] guiSplit = args[1].split(":");
                                        if (guiSplit.length == 2) {
                                            if (!this.isNumeric(guiSplit[0]) || !this.isNumeric(guiSplit[1])) {
                                                p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
                                                return true;
                                            }
                                        } else {
                                            if (!this.isNumeric(args[1])) {
                                                p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
                                                return true;
                                            }
                                        }
                                    } else {
                                        if (!this.isNumeric(args[1])) {
                                            p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
                                            return true;
                                        }
                                    }
                                }

                                List<ItemStack> itemsInInv = new ArrayList<ItemStack>();
                                List<ItemStack> armourInInv = new ArrayList<ItemStack>();
                                for (ItemStack item : p.getInventory().getContents())
                                    if (item != null && item.getType() != Material.AIR) itemsInInv.add(item);
                                for (ItemStack armour : p.getInventory().getArmorContents())
                                    if (armour != null && armour.getType() != Material.AIR) armourInInv.add(armour);
                                PlayerCreateKitEvent createKitEvent = new PlayerCreateKitEvent(p, kitName, itemsInInv, armourInInv);
                                p.getServer().getPluginManager().callEvent(createKitEvent);

                                if (!createKitEvent.isCancelled()) {
                                    itemsInInv = createKitEvent.getKitContents();
                                    armourInInv = createKitEvent.getKitArmour();
                                    if (itemsInInv.size() > 0 || armourInInv.size() > 0) {
                                        if (containsKit) {
                                            this.getPlugin().getKitsConfig().set(kitName, null);
                                            this.getPlugin().saveKitsConfig();
                                            if (this.getPlugin().kitList.containsKey(kitName))
                                                this.getPlugin().kitList.remove(kitName);
                                        }

                                        final Kit kit = new Kit(kitName, itemsInInv).setArmour(armourInInv);
                                        if (args.length == 2) {
                                            ItemStack guiItem = null;
                                            try {
                                                guiItem = new ItemStack(Integer.parseInt(args[1]));
                                            } catch (Exception ex) {
                                            }
                                            try {
                                                if (args[1].contains(":")) {
                                                    String[] guiSplit = args[1].split(":");
                                                    guiItem = new ItemStack(Integer.parseInt(guiSplit[0]));
                                                    guiItem.setDurability(Short.parseShort(guiSplit[1]));
                                                }
                                            } catch (Exception ex) {
                                            }
                                            if (guiItem != null) {
                                                if (guiItem.getType() != Material.AIR) {
                                                    kit.setGuiItem(guiItem);
                                                }
                                            }
                                        }
                                        this.getPlugin().getKitsConfig().set(kitName, kit.serialize());
                                        this.getPlugin().kitList.put(kitName, kit);
                                        this.getPlugin().saveKitsConfig();

                                        try {
                                            p.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
                                        } catch (Exception ex) {
                                        }
                                        if (containsKit)
                                            p.sendMessage(this.r("&4" + kitName + "&6 has been overwritten."));
                                        else p.sendMessage(this.r("&4" + kitName + "&6 has been created."));

                                        if (this.getPlugin().configValues.removeItemsOnCreateKit) {
                                            p.getInventory().clear();
                                            p.getInventory().setArmorContents(null);
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You have nothing in your inventory!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "A plugin has not allowed you to create this kit.");
                                }
                            } else {
                                p.sendMessage(this.r("&6The kit name must only consist of letters, numbers or underscores."));
                            }
                        } else {
                            p.sendMessage(this.r("&cUsage: &4/" + command.toLowerCase() + " [<kit>|<kit> <guiitem>]"));
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

    private boolean containsIllegalChars(String strMessage) {
        return !strMessage.matches("[a-zA-Z0-9_ ]*");
    }

}
