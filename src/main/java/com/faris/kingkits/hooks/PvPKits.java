package com.faris.kingkits.hooks;

import com.faris.kingkits.Kit;
import com.faris.kingkits.guis.GuiKitMenu;
import com.faris.kingkits.helpers.KitStack;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.listeners.commands.SetKit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.logging.Logger;

public class PvPKits {

    /**
     * Get KingKit's logger.
     */
    public static Logger getPluginLogger() {
        return Plugin.getPlugin().getLogger();
    }

    /**
     * Returns if a player has a kit.
     *
     * @param player The player to check.
     */
    public static boolean hasKit(Player player) {
        if (player == null) return false;
        return hasKit(player.getName());
    }

    /**
     * Returns if a player has a kit.
     *
     * @param player The player to check.
     */
    public static boolean hasKit(String player) {
        return Plugin.getPlugin().usingKits.containsKey(player);
    }

    /**
     * Returns if a player has a kit.
     *
     * @param player The player to check.
     * @param ignoreOPs Input true if you should ignore OPs.
     */
    public static boolean hasKit(Player player, boolean ignoreOPs) {
        if (player == null) return false;
        return hasKit(player.getName(), ignoreOPs);
    }

    /**
     * Returns if a player has a kit.
     *
     * @param player The player to check.
     * @param ignoreOPs Input true if you should ignore OPs.
     */
    public static boolean hasKit(String player, boolean ignoreOPs) {
        if (ignoreOPs) {
            return Plugin.getPlugin().playerKits.containsKey(player);
        } else {
            return Plugin.getPlugin().usingKits.containsKey(player);
        }
    }

    /**
     * Get player's kit : Returns null if the player is null or doesn't have a kit.
     *
     * @param player The player to get the kit name from.
     */
    public static String getKit(Player player) {
        if (player == null) return null;
        return getKit(player.getName());
    }

    /**
     * Get player's kit : Returns null if the player doesn't have a kit.
     *
     * @param player The player to get the kit name from.
     */
    public static String getKit(String player) {
        if (hasKit(player)) return Plugin.getPlugin().usingKits.get(player);
        else return null;
    }

    /**
     * Get a kit by its name.
     *
     * @param kitName - The kit's name.
     * @return The kit.
     */
    public static Kit getKitByName(String kitName) {
        return kitName != null ? Plugin.getPlugin().kitList.get(kitName) : null;
    }

    /**
     * Get a list of the register kits.
     *
     * @return A list of registered kits.
     */
    public static List<Kit> getKits() {
        return new ArrayList<Kit>(Plugin.getPlugin().kitList.values());
    }

    /**
     * Get players using a specific kit : Returns an empty list if the kit doesn't exist or no players are using that kit.
     *
     * @param kitName The kit to obtain the list of players.
     */
    public static List<String> getPlayersUsingKit(String kitName) {
        List<String> playersUsingKit = new ArrayList<String>();
        List<String> playersInKitMap = new ArrayList<String>(Plugin.getPlugin().usingKits.keySet());
        for (int pos = 0; pos < Plugin.getPlugin().usingKits.size(); pos++) {
            String kit = Plugin.getPlugin().usingKits.get(pos);
            if (kitName.equalsIgnoreCase(kit)) {
                playersUsingKit.add(playersInKitMap.get(pos));
            }
        }
        return playersUsingKit;
    }

    /**
     * Get players using and their kits : Returns an empty map if no one is using a kit.
     */
    public static Map<String, String> getPlayersAndKits() {
        return Plugin.getPlugin().usingKits;
    }

    /**
     * Returns if a kit exists.
     * Note: Case sensitive.
     *
     * @param kitName The kit name to check.
     */
    public static boolean kitExists(String kitName) {
        List<String> kitList = new ArrayList<String>();
        if (Plugin.getPlugin().getKitsConfig().contains("Kits"))
            kitList = Plugin.getPlugin().getKitList();
        List<String> kitListLC = Utils.toLowerCaseList(kitList);
        return kitListLC.contains(kitName.toLowerCase());
    }

    /**
     * Remove a player from a kit.
     * Note: Doesn't clear the player's inventory.
     *
     * @param player The player to remove from a kit.
     */
    public static void removePlayer(Player player) {
        if (player != null) removePlayer(player.getName());
    }

    /**
     * Remove a player from a kit.
     * Note: Doesn't clear the player's inventory.
     *
     * @param player The player's name to remove from a kit.
     */
    public static void removePlayer(String player) {
        if (hasKit(player, false)) {
            Plugin.getPlugin().usingKits.remove(player);
            Plugin.getPlugin().playerKits.remove(player);
        }
    }

    /**
     * Remove a player from a kit.
     * Note: Clears a player's inventory.
     *
     * @param pluginLogger Your plugin's logger.
     * @param player The player who's kit is to be changed.
     * @param kit The kit that the player should be set as.
     */
    @SuppressWarnings("deprecation")
    public static void setPlayerKit(Logger pluginLogger, String player, String kit) {
        boolean useSyso = false;
        if (pluginLogger == null) useSyso = true;
        if (kitExists(kit)) {
            Player target = Bukkit.getPlayerExact(player);
            if (target != null) {
                if (target.isOnline()) {
                    if (hasKit(target.getName(), false)) {
                        target.getInventory().clear();
                        target.getInventory().setArmorContents(null);
                        removePlayer(target.getName());
                    }
                    try {
                        SetKit.setKit(Plugin.getPlugin(), target, kit, true);
                    } catch (Exception ex) {
                        String msg = "Error, couldn't set the player's kit to " + kit + ".";
                        if (useSyso) System.out.println(msg);
                        else pluginLogger.info(msg);
                        String msg2 = "Error Log: \n" + ex.getMessage();
                        if (useSyso) System.out.println(msg2);
                        else pluginLogger.info(msg2);
                    }
                } else {
                    String msg = "Target player '" + player + "' is not online/does not exist.";
                    if (useSyso) System.out.println(msg);
                    else pluginLogger.info(msg);
                }
            } else {
                String msg = "Target player '" + player + "' is not online/does not exist.";
                if (useSyso) System.out.println(msg);
                else pluginLogger.info(msg);
            }
        } else {
            String msg = "Kit " + kit + " doesn't exist.";
            if (useSyso) System.out.println(msg);
            else pluginLogger.info(msg);
        }
    }

    /**
     * Returns the target player's score.
     * Returns -1 if the player doesn't exist in the scores configuration or the player is null.
     *
     * @param player The player who's score is meant to be returned.
     */
    public static int getScore(Player player) {
        if (player == null) return -1;
        return getScore(player.getUniqueId().toString());
    }

    /**
     * Returns the target player's score.
     *
     * @param playerUUID The player who's score is meant to be returned. Returns -1 if the player doesn't exist in the scores configuration.
     */
    public static int getScore(String playerUUID) {
        UUID uuid = UUID.fromString(playerUUID);
        if (Plugin.getPlugin().playerScores.containsKey(uuid)) {
            return (Integer) Plugin.getPlugin().playerScores.get(uuid);
        } else {
            return -1;
        }
    }

    /**
     * Returns a Map of all the player's scores and their usernames.
     * Note: The return type of the map is (String, Object) which is actually a Map of (String, Integer), just cast all the values to integer.
     */
    public static Map<UUID, Object> getScores() {
        return Plugin.getPlugin().playerScores;
    }

    /**
     * Set the score of a player.
     *
     * @param player The player who's score is meant to be set.
     * @param value The new score of the player.
     */
    public static void setScore(Player player, int value) {
        if (player != null) setScore(player.getUniqueId().toString(), value);
    }

    /**
     * Set the score of a player.
     *
     * @param uuid The UUID of the player whose score is meant to be set.
     * @param value The new score of the player.
     */
    public static void setScore(String uuid, int value) {
        UUID playerUUID = UUID.fromString(uuid);
        if (Plugin.getPlugin().playerScores.containsKey(playerUUID)) Plugin.getPlugin().playerScores.remove(playerUUID);
        if (value < 0) value *= -1;
        if (value > Integer.MAX_VALUE) value -= Integer.MAX_VALUE;
        Plugin.getPlugin().playerScores.put(playerUUID, value);
    }

    /**
     * Create a kit.
     * Returns if the creation of the kit is successful.
     *
     * @param kitName The kit name.
     * @param itemsInKit The items in the kit.
     * @param potionEffects The potion effects in the kit. Set to null if there are none.
     * @param guiItem The item to be shown in the GUI Inventory when using GUI mode. Set to null if you want it to be a diamond sword.
     * @param costOfKit The cost of the kit.
     */
    public static boolean createKit(String kitName, List<ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
        if (!itemsInKit.isEmpty()) {
            boolean containsKit = kitExists(kitName);
            if (containsKit) {
                List<String> currentKits = Plugin.getPlugin().getConfigKitList();
                List<String> currentKitsLC = Utils.toLowerCaseList(currentKits);
                if (currentKitsLC.contains(kitName.toLowerCase()))
                    kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));

                Plugin.getPlugin().getKitsConfig().set(kitName, null);
                Plugin.getPlugin().saveKitsConfig();
            }
            Kit kit = new Kit(kitName, costOfKit, itemsInKit, potionEffects).setGuiItem(guiItem != null ? guiItem : new ItemStack(Material.DIAMOND_SWORD));
            Plugin.getPlugin().getKitsConfig().set(kitName, kit.serialize());
            Plugin.getPlugin().saveKitsConfig();
            Plugin.getPlugin().kitList.put(kitName, kit);

            try {
                Plugin.getPlugin().getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
            } catch (Exception ex) {
                getPluginLogger().warning(ex.getClass().getSimpleName() + " error: " + ex.getMessage());
            }
            return true;
        }
        return false;
    }

    /**
     * Delete a kit.
     * Returns if the deletion of the kit is successful.
     *
     * @param kitName The name of the kit to be deleted.
     */
    public static boolean deleteKit(String kitName) {
        List<String> kits = Plugin.getPlugin().getConfigKitList();
        List<String> kitsLC = Utils.toLowerCaseList(kits);
        if (kitsLC.contains(kitName.toLowerCase())) {
            kitName = kits.get(kitsLC.indexOf(kitName.toLowerCase()));
            Plugin.getPlugin().getKitsConfig().set(kitName, null);
            Plugin.getPlugin().saveKitsConfig();
            Plugin.getPlugin().kitList.remove(kitName);
            return true;
        }
        return false;
    }

    /**
     * Returns the player's killstreak.
     * Returns 0 if the player doesn't exist, is null or has no killstreak.
     *
     * @param player The player.
     */
    public static long getKillstreak(Player player) {
        if (player == null) return 0L;
        return getKillstreak(player.getName());
    }

    /**
     * Returns the player's killstreak.
     * Returns 0 if the player doesn't exist or has no killstreak.
     *
     * @param player The name of the player.
     */
    public static long getKillstreak(String player) {
        if (Plugin.getPlugin().playerKillstreaks.containsKey(player))
            return Plugin.getPlugin().playerKillstreaks.get(player);
        else return 0L;
    }

    @SuppressWarnings("deprecation")
    /**
     * Display the Kit GUI menu to a player.
     * @param player - The Kit GUI viewer.
     */
    public static void showKitMenu(Player player) {
        if (Plugin.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui") || Plugin.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu")) {
            List<String> kitNames = new ArrayList<String>(Plugin.getPlugin().kitList.keySet());
            if (Plugin.getPlugin().configValues.sortAlphabetically)
                Collections.sort(kitNames, Utils.ALPHABETICAL_ORDER);
            List<Kit> kitValues = new ArrayList<Kit>();
            for (String kitName : kitNames) kitValues.add(Plugin.getPlugin().kitList.get(kitName));

            KitStack[] kitStacks = new KitStack[kitValues.size()];
            for (int index = 0; index < kitValues.size(); index++) {
                Kit kit = kitValues.get(index);
                kitStacks[index] = new KitStack(kit.getName(), kit.getGuiItem());
            }
            ChatColor menuColour = kitStacks.length > 0 ? ChatColor.DARK_BLUE : ChatColor.RED;
            new GuiKitMenu(player, menuColour + "PvP Kits", kitStacks).openMenu();
        }
    }

}