package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.listeners.event.custom.PlayerKitEvent;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

public class SetKit {
    private static KingKits pl;

    public static void setKingKit(KingKits plugin, Player player, String kitName, boolean sendMessages) throws Exception {
        final Kit kit = setKit(plugin, player, sendMessages, kitName);
        if (kit != null) {
            if (kit.hasCooldown() && !player.hasPermission(plugin.permissions.kitBypassCooldown)) {
                final String playerName = player.getName();
                final String newKitName = kit.getRealName();
                pl.getCooldownConfig().set(playerName + "." + newKitName, System.currentTimeMillis());
                pl.saveCooldownConfig();
            }
        }
    }


    public static boolean setKit(KingKits plugin, Player player, String kitName, boolean sendMessages) throws Exception {
        return setKit(plugin, player, sendMessages, kitName) != null;
    }

    @SuppressWarnings("deprecation")
    public static Kit setKit(KingKits plugin, Player player, boolean sendMessages, String kitName) throws Exception {
        if (plugin == null | player == null || kitName == null) return null;
        pl = plugin;
        if (plugin.configValues.pvpWorlds.contains("All") || plugin.configValues.pvpWorlds.contains(player.getWorld().getName())) {
            List<String> kitList = plugin.getConfigKitList();
            List<String> kitListLC = Utils.toLowerCaseList(kitList);
            if (kitListLC.contains(kitName.toLowerCase())) {
                try {
                    kitName = kitList.get(kitList.indexOf(kitName));
                } catch (Exception ex) {
                    try {
                        kitName = kitName.substring(0, 0).toUpperCase() + kitName.substring(1);
                    } catch (Exception ex2) {
                    }
                }
                if (player.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
                    if (plugin.configValues.oneKitPerLife) {
                        if (plugin.configValues.opBypass) {
                            if (!player.isOp()) {
                                if (plugin.playerKits.containsKey(player.getName())) {
                                    if (sendMessages) player.sendMessage(r("&6You have already chosen a kit!"));
                                    return null;
                                }
                            }
                        } else {
                            if (plugin.usingKits.containsKey(player.getName())) {
                                if (sendMessages) player.sendMessage(r("&6You have already chosen a kit!"));
                                return null;
                            }
                        }
                    }
                    String oldKit = plugin.playerKits.containsKey(player.getName()) ? plugin.playerKits.get(player.getName()) : "";
                    Kit newKit = plugin.kitList.get(kitName);
                    if (newKit == null) return null;
                    PlayerKitEvent playerKitEvent = new PlayerKitEvent(player, kitName, oldKit, newKit.getItemsWithSlot(), newKit.getArmour(), newKit.getPotionEffects());
                    playerKitEvent.setCommands(newKit.getCommands());
                    player.getServer().getPluginManager().callEvent(playerKitEvent);
                    if (!playerKitEvent.isCancelled()) {
                        if (plugin.configValues.vaultValues.useEconomy && plugin.configValues.vaultValues.useCostPerKit) {
                            try {
                                net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) plugin.vault.getEconomy();
                                double kitCost = newKit.getCost();
                                if (kitCost < 0) kitCost *= -1;
                                if (economy.hasAccount(player.getName())) {
                                    if (economy.getBalance(player.getName()) >= kitCost) {
                                        economy.withdrawPlayer(player.getName(), kitCost);
                                        if (kitCost != 0)
                                            player.sendMessage(ChatColor.GREEN + plugin.getEconomyMessage(kitCost));
                                    } else {
                                        if (sendMessages)
                                            player.sendMessage(ChatColor.GREEN + "You do not have enough money to change kits.");
                                        return null;
                                    }
                                } else {
                                    if (sendMessages)
                                        player.sendMessage(ChatColor.GREEN + "You do not have enough money to change kits.");
                                    return null;
                                }
                            } catch (Exception ex) {
                            }
                        }

                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.setGameMode(GameMode.SURVIVAL);
                        for (PotionEffect potionEffect : player.getActivePotionEffects())
                            player.removePotionEffect(potionEffect.getType());
                        Map<Integer, ItemStack> kitItems = playerKitEvent.getKitContentsWithSlots();
                        for (Map.Entry<Integer, ItemStack> kitItem : kitItems.entrySet()) {
                            try {
                                if (kitItem.getValue() != null && kitItem.getValue().getType() != Material.AIR) {
                                    int slot = kitItem.getKey();
                                    if (slot >= 0 && slot < player.getInventory().getSize()) {
                                        player.getInventory().setItem(slot, kitItem.getValue());
                                    }
                                }
                            } catch (Exception ex) {
                                continue;
                            }
                        }
                        List<ItemStack> armourItems = playerKitEvent.getKitArmour();
                        for (ItemStack armourItem : armourItems) {
                            if (armourItem != null) {
                                String strArmourType = armourItem.getType().toString().toLowerCase();
                                if (strArmourType.endsWith("helmet")) player.getInventory().setHelmet(armourItem);
                                else if (strArmourType.endsWith("chestplate"))
                                    player.getInventory().setChestplate(armourItem);
                                else if (strArmourType.endsWith("leggings") || strArmourType.endsWith("pants"))
                                    player.getInventory().setLeggings(armourItem);
                                else if (strArmourType.endsWith("boots"))
                                    player.getInventory().setBoots(armourItem);
                                else player.getInventory().addItem(armourItem);
                            }
                        }
                        player.updateInventory();
                        player.addPotionEffects(playerKitEvent.getPotionEffects());

                        if (plugin.configValues.commandToRun.length() > 0) {
                            String cmdToRun = plugin.configValues.commandToRun;
                            cmdToRun = cmdToRun.replaceAll("<kit>", kitName);
                            cmdToRun = cmdToRun.replaceAll("<player>", player.getName());
                            player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmdToRun);
                        }
                        for (String cmdToRun : playerKitEvent.getCommands()) {
                            cmdToRun = cmdToRun.replaceAll("<player>", player.getName());
                            player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmdToRun);
                        }
                        plugin.playerKits.remove(player.getName());
                        plugin.usingKits.remove(player.getName());
                        if (plugin.configValues.opBypass) {
                            if (!player.isOp()) {
                                plugin.playerKits.put(player.getName(), newKit.getRealName());
                            }
                        } else {
                            plugin.playerKits.put(player.getName(), newKit.getRealName());
                        }
                        plugin.usingKits.put(player.getName(), newKit.getRealName());
                        if (plugin.configValues.customMessages != "" && plugin.configValues.customMessages != "''")
                            player.sendMessage(r(plugin.configValues.customMessages).replaceAll("<kit>", kitName));
                        if (plugin.configValues.kitParticleEffects) {
                            player.playEffect(player.getLocation().add(0, 1, 0), Effect.ENDER_SIGNAL, (byte) 0);
                        }
                        return newKit;
                    } else {
                        for (PotionEffect potionEffect : player.getActivePotionEffects())
                            player.removePotionEffect(potionEffect.getType());
                    }
                } else {
                    if (sendMessages)
                        player.sendMessage(r("&cYou do not have permission to use the kit &4" + kitName + "&c."));
                }
            } else {
                if (sendMessages) player.sendMessage(r("&4" + kitName + " &6does not exist."));
            }
        }
        return null;
    }

    private static String r(String val) {
        return Utils.replaceChatColour(val);
    }

    public static boolean isNumeric(String val) {
        try {
            @SuppressWarnings("unused")
            int i = Integer.parseInt(val);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isShort(String val) {
        try {
            @SuppressWarnings("unused")
            short i = Short.parseShort(val);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
