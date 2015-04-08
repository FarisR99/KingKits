package com.faris.kingkits.hooks;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.UUIDFetcher;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;


@Deprecated
/**
 * Moved to {@link com.faris.kingkits.KingKitsAPI} so that the class will never be affected by package renames.
 */
public class PvPKits {

	
	/**
	 * Get KingKits' logger.
	 */
	public static Logger getPluginLogger() {
		return KingKits.getInstance().getLogger();
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
		return KingKitsAPI.createKit(kitName, itemsInKit, potionEffects, guiItem, costOfKit);
	}

	
	/**
	 * Create a kit.
	 * Returns if the creation of the kit is successful.
	 *
	 * @param kitName The kit name.
	 * @param itemsInKit The items in the kit with their slot number.
	 * @param potionEffects The potion effects in the kit. Set to null if there are none.
	 * @param guiItem The item to be shown in the GUI Inventory when using GUI mode. Set to null if you want it to be a diamond sword.
	 * @param costOfKit The cost of the kit.
	 */
	public static boolean createKit(String kitName, Map<Integer, ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		return KingKitsAPI.createKit(kitName, itemsInKit, potionEffects, guiItem, costOfKit);
	}

	
	/**
	 * Delete a kit.
	 * Returns if the deletion of the kit is successful.
	 *
	 * @param kitName The name of the kit to be deleted.
	 */
	public static boolean deleteKit(String kitName) {
		return KingKitsAPI.deleteKit(kitName);
	}

	
	/**
	 * Returns the player's killstreak.
	 * Returns 0 if the player doesn't exist, is null or has no killstreak.
	 *
	 * @param player The player.
	 */
	public static long getKillstreak(Player player) {
		return KingKitsAPI.getKillstreak(player);
	}

	
	/**
	 * Returns the player's killstreak.
	 * Returns 0 if the player doesn't exist or has no killstreak.
	 *
	 * @param player The name of the player.
	 */
	public static long getKillstreak(String player) {
		return KingKitsAPI.getKillstreak(player);
	}

	
	/**
	 * Get player's kit : Returns null if the player is null or doesn't have a kit.
	 *
	 * @param player The player to get the kit name from.
	 */
	public static String getKit(Player player) {
		return KingKitsAPI.getKit(player);
	}

	
	/**
	 * Get player's kit : Returns null if the player doesn't have a kit.
	 *
	 * @param player The player to get the kit name from.
	 */
	public static String getKit(String player) {
		return KingKitsAPI.getKit(player);
	}

	
	/**
	 * Get a kit by its name.
	 *
	 * @param kitName The kit's name.
	 * @return The kit.
	 */
	public static Kit getKitByName(String kitName) {
		return KingKitsAPI.getKitByName(kitName, false);
	}

	
	/**
	 * Get a kit by its name.
	 *
	 * @param kitName The kit's name.
	 * @param playerName The player's name.
	 * @return The kit. Empty if the failed to get the player's UUID.
	 */
	public static Kit getKitByName(String kitName, String playerName) {
		try {
			return KingKitsAPI.getKitByName(kitName, UUIDFetcher.lookupName(playerName).getId());
		} catch (Exception ex) {
		}
		return null;
	}

	
	/**
	 * Get a kit by its name, ignoring cases for display name.
	 *
	 * @param kitName The kit's name.
	 * @return The kit.
	 */
	public static Kit getKitByNameCaseInsensitive(String kitName) {
		return KingKitsAPI.getKitByName(kitName, true);
	}

	
	/**
	 * Get a list of the register kits.
	 *
	 * @return A list of registered kits.
	 */
	public static List<Kit> getKits() {
		return KingKitsAPI.getKits();
	}

	
	/**
	 * Get players using a specific kit : Returns an empty list if the kit doesn't exist or no players are using that kit.
	 *
	 * @param kitName The kit to obtain the list of players.
	 */
	public static List<String> getPlayersUsingKit(String kitName) {
		List<String> playersUsingKit = new ArrayList<String>();
		List<String> playersInKitMap = new ArrayList<String>(KingKits.getInstance().usingKits.keySet());
		for (int pos = 0; pos < KingKits.getInstance().usingKits.size(); pos++) {
			String kit = KingKits.getInstance().usingKits.get(pos);
			if (kit.equalsIgnoreCase(kitName)) {
				playersUsingKit.add(playersInKitMap.get(pos));
			}
		}
		return playersUsingKit;
	}

	
	/**
	 * Get players using and their kits : Returns an empty map if no one is using a kit.
	 */
	public static Map<String, String> getPlayersAndKits() {
		return KingKitsAPI.getPlayersKits();
	}

	
	/**
	 * Returns the target player's score.
	 * Returns -1 if the player doesn't exist in the scores configuration or the player is null.
	 *
	 * @param player The player who's score is meant to be returned.
	 */
	public static int getScore(Player player) {
		return KingKitsAPI.getScore(player.getUniqueId());
	}

	
	/**
	 * Returns the target player's score.
	 *
	 * @param playerUUID The player who's score is meant to be returned. Returns -1 if the player doesn't exist in the scores configuration.
	 */
	public static int getScore(String playerUUID) {
		return KingKitsAPI.getScore(playerUUID != null ? UUID.fromString(playerUUID) : null);
	}

	
	/**
	 * Returns a Map of all the player's scores and their usernames.
	 */
	public static Map<UUID, Integer> getScores() {
		return KingKitsAPI.getScores();
	}

	
	/**
	 * Returns if a player has a kit.
	 *
	 * @param player The player to check.
	 */
	public static boolean hasKit(Player player) {
		return KingKitsAPI.hasKit(player);
	}

	
	/**
	 * Returns if a player has a kit.
	 *
	 * @param player The player to check.
	 */
	public static boolean hasKit(String player) {
		return KingKitsAPI.hasKit(player);
	}

	
	/**
	 * Returns if a player has a kit.
	 *
	 * @param player The player to check.
	 * @param ignoreOPs Input true if you should ignore OPs.
	 */
	public static boolean hasKit(Player player, boolean ignoreOPs) {
		return KingKitsAPI.hasKit(player, ignoreOPs);
	}

	
	/**
	 * Returns if a player has a kit.
	 *
	 * @param player The player to check.
	 * @param ignoreOPs Input true if you should ignore OPs.
	 */
	public static boolean hasKit(String player, boolean ignoreOPs) {
		return KingKitsAPI.hasKit(player, ignoreOPs);
	}

	
	/**
	 * Check if a kit belongs to a player.
	 *
	 * @param kitName The kit name.
	 * @param playerName The player to check.
	 */
	public static boolean isUserKit(String kitName, String playerName) {
		try {
			return KingKitsAPI.isUserKit(kitName, UUIDFetcher.lookupName(playerName).getId());
		} catch (Exception ex) {
		}
		return false;
	}

	
	/**
	 * Returns if a kit exists.
	 *
	 * @param kitName The kit name to check.
	 */
	public static boolean kitExists(String kitName) {
		return KingKitsAPI.kitExists(kitName);
	}

	
	/**
	 * Remove a player from a kit.
	 * Note: Doesn't clear the player's inventory.
	 *
	 * @param player The player to remove from a kit.
	 */
	public static void removePlayer(Player player) {
		KingKitsAPI.removePlayer(player);
	}

	
	/**
	 * Remove a player from a kit.
	 * Note: Doesn't clear the player's inventory.
	 *
	 * @param player The player's name to remove from a kit.
	 */
	public static void removePlayer(String player) {
		KingKitsAPI.removePlayer(player);
	}

	
	/**
	 * Remove a player from a kit.
	 * Note: Clears a player's inventory.
	 *
	 * @param pluginLogger Your plugin's logger.
	 * @param player The player who's kit is to be changed.
	 * @param kit The kit that the player should be set as.
	 */
	public static void setPlayerKit(Logger pluginLogger, String player, String kit) {
		KingKitsAPI.setPlayerKit(pluginLogger, player, kit);
	}

	
	/**
	 * Set the score of a player.
	 *
	 * @param player The player who's score is meant to be set.
	 * @param value The new score of the player.
	 */
	public static void setScore(Player player, int value) {
		KingKitsAPI.setScore(player, value);
	}

	
	/**
	 * Set the score of a player.
	 *
	 * @param uuid The UUID of the player whose score is meant to be set.
	 * @param value The new score of the player.
	 */
	public static void setScore(String uuid, int value) {
		try {
			KingKitsAPI.setScore(UUID.fromString(uuid), value);
		} catch (Exception ex) {
		}
	}

	
	/**
	 * Display the Kit GUI menu to a player.
	 *
	 * @param player The Kit GUI viewer.
	 */
	public static void showKitMenu(Player player) {
		KingKitsAPI.showKitMenu(player);
	}

	
	/**
	 * Display the Kit GUI menu to a player.
	 *
	 * @param player The Kit GUI viewer.
	 * @param ignoreChecks Ignore the checking of whether the kit list mode is set to GUI/Menu.
	 */
	public static void showKitMenu(Player player, boolean ignoreChecks) {
		KingKitsAPI.showKitMenu(player, ignoreChecks);
	}

}