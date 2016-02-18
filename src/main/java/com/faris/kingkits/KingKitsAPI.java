package com.faris.kingkits;

import com.faris.kingkits.controller.KitController;
import com.faris.kingkits.controller.PlayerController;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import org.bukkit.entity.*;

import java.util.*;

public class KingKitsAPI {

	/**
	 * Get the killstreak of a player.
	 *
	 * @param player The player
	 * @return The player's killstreak.
	 */
	public static int getKillstreak(Player player) {
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		return kitPlayer != null ? kitPlayer.getKillstreak() : 0;
	}

	/**
	 * Get the name of the kit that a player is using.
	 *
	 * @param player The player
	 * @return The name of the kit that the player is using.
	 */
	public static String getKit(Player player) {
		if (player == null) return null;
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		return kitPlayer != null && kitPlayer.hasKit() ? kitPlayer.getKit().getName() : null;
	}

	/**
	 * Get a list of all the kits.
	 *
	 * @return A list of all the kits.
	 */
	public static List<Kit> getKits() {
		return new ArrayList<>(KitController.getInstance().getKits().values());
	}

	/**
	 * Get a Map of all the players and the name of the kit they are using.
	 *
	 * @return A Map of all the players and the name of the kit they are using.
	 */
	public static Map<String, String> getPlayersKits() {
		return new HashMap<String, String>() {{
			for (KitPlayer kitPlayer : PlayerController.getInstance().getAllPlayers()) {
				if (kitPlayer.getUsername() != null && kitPlayer.hasKit())
					this.put(kitPlayer.getUsername(), kitPlayer.getKit().getName());
			}
		}};
	}

	/**
	 * Get the score of an online/offline player.
	 *
	 * @param player The player
	 * @return The score of the player.
	 */
	public static int getScore(Player player) {
		return player != null ? getScore(player.getUniqueId()) : 0;
	}

	/**
	 * Get the score of an online/offline player.
	 *
	 * @param playerUUID The player's UUID
	 * @return The score of the player.
	 */
	public static int getScore(UUID playerUUID) {
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(playerUUID);
		if (kitPlayer != null) {
			return kitPlayer.getScore();
		} else {
			OfflineKitPlayer offlineKitPlayer = PlayerController.getInstance().getOfflinePlayer(playerUUID);
			return offlineKitPlayer != null ? offlineKitPlayer.getScore() : 0;
		}
	}

	/**
	 * Check if a player has a kit.
	 *
	 * @param player The player
	 * @return Whether the player has a kit or not.
	 */
	public static boolean hasKit(Player player) {
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		return kitPlayer != null && kitPlayer.hasKit();
	}

	/**
	 * Check if a player is using a specified kit.
	 * This returns false if the player is using a user kit.
	 *
	 * @param kitName The kit's name
	 * @param player The player
	 * @return Whether a player is using the kit (and it is not a user kit) or not.
	 */
	public static boolean isUsingKit(String kitName, Player player) {
		return isUsingKit(kitName, player, false);
	}

	/**
	 * Check if a player is using a specified kit.
	 *
	 * @param kitName The kit's name
	 * @param player The player
	 * @param allowUserKits Whether to allow the checking of user kits too
	 * @return Whether a player is using the kit or not.
	 */
	public static boolean isUsingKit(String kitName, Player player, boolean allowUserKits) {
		return isUsingKit(kitName, player, allowUserKits, false);
	}

	/**
	 * Check if a player is using a specified kit (ignore case).
	 *
	 * @param kitName The kit's name
	 * @param player The player
	 * @param allowUserKits Whether to allow the checking of user kits too
	 * @param ignoreCase Whether or not to ignore the case of the kit name
	 * @return Whether a player is using the kit or not.
	 */
	public static boolean isUsingKit(String kitName, Player player, boolean allowUserKits, boolean ignoreCase) {
		if (player != null) {
			KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
			if (kitPlayer != null) {
				if (kitPlayer.hasKit()) {
					Kit kit = kitPlayer.getKit();
					if (!allowUserKits) {
						return !kit.isUserKit() && (ignoreCase ? kit.getName().equalsIgnoreCase(kitName) : kit.getName().equals(kitName));
					} else {
						return ignoreCase ? kit.getName().equalsIgnoreCase(kitName) : kit.getName().equals(kitName);
					}
				} else {
					return kitName == null;
				}
			}
		}
		return false;
	}

	/**
	 * Check if a kit exists.
	 *
	 * @param kitName The name of the kit to check
	 * @return Whether a kit with the specified name exists or not.
	 */
	public static boolean kitExists(String kitName) {
		return KitController.getInstance().getKit(kitName) != null;
	}

}