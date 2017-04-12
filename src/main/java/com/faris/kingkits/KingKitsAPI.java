package com.faris.kingkits;

import com.faris.kingkits.controller.KitController;
import com.faris.kingkits.controller.PlayerController;
import com.faris.kingkits.helper.util.KitUtilities;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * The API class for KingKits.
 * All methods are static.
 */
public class KingKitsAPI {

	private KingKitsAPI() {
	}

	/**
	 * Get the killstreak of a player.
	 *
	 * @param player The player
	 * @return The player's killstreak.
	 */
	public static int getKillstreak(final Player player) {
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		return kitPlayer != null ? kitPlayer.getKillstreak() : 0;
	}

	/**
	 * Get the name of the kit that a player is using.
	 *
	 * @param player The player
	 * @return The name of the kit that the player is using.
	 */
	public static String getKit(final Player player) {
		if (player == null) return null;
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		return kitPlayer != null && kitPlayer.hasKit() ? kitPlayer.getKit().getName() : null;
	}

	/**
	 * Search for a kit by name. (case sensitive)
	 *
	 * @param kitName The kit name
	 * @return The kit. (null if does not exist)
	 */
	public static Kit getKit(String kitName) {
		return KitController.getInstance().getKit(kitName);
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
	 * Search for a kit by name or multiple kits with the same name
	 *
	 * @param kitName The kit name
	 * @return The search result for the specified kit.
	 */
	public static KitUtilities.KitSearchResult getKits(String kitName) {
		return KitUtilities.getKits(kitName);
	}

	/**
	 * Get a Map of all the players and the name of the kit they are using.
	 *
	 * @return A Map of all the players and the name of the kit they are using.
	 */
	public static Map<String, String> getPlayersKits() {
		return new HashMap<String, String>() {{
			for (KitPlayer kitPlayer : PlayerController.getInstance().getAllPlayers()) {
				if (kitPlayer.getUsername() != null && kitPlayer.hasKit()) {
					this.put(kitPlayer.getUsername(), kitPlayer.getKit().getName());
				}
			}
		}};
	}

	/**
	 * Get the score of an online/offline player.
	 *
	 * @param player The player
	 * @return The score of the player.
	 */
	public static int getScore(final Player player) {
		return player != null ? getScore(player.getUniqueId()) : 0;
	}

	/**
	 * Get the score of an online/offline player.
	 *
	 * @param playerUUID The player's UUID
	 * @return The score of the player.
	 */
	public static int getScore(final UUID playerUUID) {
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
	public static boolean hasKit(final Player player) {
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		return kitPlayer != null && kitPlayer.hasKit();
	}

	/**
	 * Check if a player is using a specified kit.
	 * <p></p>
	 * This returns false if the player is using a user kit.
	 *
	 * @param kitName The kit's name
	 * @param player The player
	 * @return Whether a player is using the kit (and it is not a user kit) or not.
	 */
	public static boolean isUsingKit(final String kitName, final Player player) {
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
	public static boolean isUsingKit(final String kitName, final Player player, final boolean allowUserKits) {
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
	public static boolean isUsingKit(final String kitName, final Player player, final boolean allowUserKits, final boolean ignoreCase) {
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
	public static boolean kitExists(final String kitName) {
		return KitController.getInstance().getKit(kitName) != null;
	}

	/**
	 * Set the kit of a player.
	 *
	 * @param player The player
	 * @param kit The kit
	 * @return Whether or not the kit was successfully set.
	 */
	public static boolean setKit(final Player player, Kit kit) {
		return KitUtilities.setKit(player, kit);
	}

	/**
	 * Set the kit of a player.
	 *
	 * @param player The player
	 * @param kit The kit
	 * @param ignoreOneKitPerLife Whether or not to set the kit regardless of 'One kit per life'
	 * @param ignoreCooldown Whether or not to set the kit regardless of the player's cooldown for this kit (if any)
	 * @param ignoreCost Whether or not to charge the player when setting this kit
	 * @return Whether or not the kit was successfully set.
	 */
	public static boolean setKit(final Player player, Kit kit, boolean ignoreOneKitPerLife, boolean ignoreCooldown, boolean ignoreCost) {
		return KitUtilities.setKit(player, kit, ignoreOneKitPerLife, ignoreCooldown, ignoreCost);
	}

	/**
	 * Update a modified kit.
	 *
	 * @param kit The kit
	 */
	public static void updateKit(final Kit kit) {
		if (kit != null) {
			if (KitController.getInstance().getKit(kit.getName()) != null) {
				KitController.getInstance().removeKit(kit);
				KitController.getInstance().deleteKit(kit);
			}
			KitController.getInstance().addKit(kit);
			KitController.getInstance().saveKit(kit);
		}
	}

}