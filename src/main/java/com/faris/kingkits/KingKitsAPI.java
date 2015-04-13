package com.faris.kingkits;

import com.faris.kingkits.gui.GuiKitMenu;
import com.faris.kingkits.gui.GuiPreviewKit;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.helper.container.KitStack;
import com.faris.kingkits.listener.command.SetKit;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.permissions.*;
import org.bukkit.potion.*;

import java.util.*;
import java.util.logging.Logger;

public class KingKitsAPI {

	/**
	 * Create a kit.
	 *
	 * @param kitName The name of the kit
	 * @param itemsInKit The items in the kit
	 * @param potionEffects The potion effects given when using the kit
	 * @param guiItem The item shown in the GUI (can be null)
	 * @param costOfKit The cost of the kit
	 * @return Whether the creation of the kit has been successful or not.
	 */
	public static boolean createKit(String kitName, List<ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		Map<Integer, ItemStack> mapItemsInKit = new HashMap<>();
		if (itemsInKit != null) {
			for (int i = 0; i < itemsInKit.size(); i++) {
				mapItemsInKit.put(i, itemsInKit.get(i));
			}
		}
		return createKit(kitName, mapItemsInKit, potionEffects, guiItem, costOfKit);
	}

	/**
	 * Create a kit.
	 *
	 * @param kitName The name of the kit
	 * @param itemsInKit The items in the kit with their slot numbers
	 * @param potionEffects The potion effects given when using the kit
	 * @param guiItem The item shown in the GUI (can be null)
	 * @param costOfKit The cost of the kit
	 * @return Whether the creation of the kit has been successful or not.
	 */
	public static boolean createKit(String kitName, Map<Integer, ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		if (KingKits.getInstance() == null) return false;
		if (itemsInKit != null && !itemsInKit.isEmpty()) {
			boolean containsKit = kitExists(kitName);
			if (containsKit) {
				List<String> currentKits = KingKits.getInstance().getConfigKitList();
				List<String> currentKitsLC = Utilities.toLowerCaseList(currentKits);
				if (currentKitsLC.contains(kitName.toLowerCase()))
					kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));
				KingKits.getInstance().getKitsConfig().set(kitName, null);
				KingKits.getInstance().saveKitsConfig();
				KingKits.getInstance().kitList.remove(kitName);
			}
			Kit kit = new Kit(kitName, costOfKit, itemsInKit, potionEffects == null ? new ArrayList<PotionEffect>() : potionEffects).setGuiItem(guiItem != null ? guiItem : new ItemStack(Material.DIAMOND_SWORD));
			KingKits.getInstance().getKitsConfig().set(kitName, kit.serialize());
			KingKits.getInstance().saveKitsConfig();
			KingKits.getInstance().kitList.put(kitName, kit);

			try {
				KingKits.getInstance().getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
			} catch (Exception ex) {
				getPluginLogger().warning(ex.getClass().getSimpleName() + " error: " + ex.getMessage());
			}
			return true;
		}
		return false;
	}

	/**
	 * Create a user kit.
	 *
	 * @param playerUUID The UUID of the owner of the kit
	 * @param kitName The name of the kit
	 * @param itemsInKit The items in the user kit
	 * @param potionEffects The potion effects given when using the user kit
	 * @param guiItem The item shown in the GUI (can be null)
	 * @param costOfKit The cost of the user kit
	 * @return Whether the creation of the user kit has been successful or not.
	 */
	public static boolean createUserKit(UUID playerUUID, String kitName, List<ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		Map<Integer, ItemStack> mapItemsInKit = new HashMap<>();
		if (itemsInKit != null) {
			for (int i = 0; i < itemsInKit.size(); i++) {
				mapItemsInKit.put(i, itemsInKit.get(i));
			}
		}
		return createUserKit(playerUUID, kitName, mapItemsInKit, potionEffects, guiItem, costOfKit);
	}


	/**
	 * Create a user kit.
	 *
	 * @param playerUUID The user kit's owner's UUID
	 * @param kitName The name of the kit
	 * @param itemsInKit The items in the user kit with their slot numbers
	 * @param potionEffects The potion effects given when using the user kit
	 * @param guiItem The item shown in the GUI (can be null)
	 * @param costOfKit The cost of the user kit
	 * @return Whether the creation of the user kit has been successful or not.
	 */
	public static boolean createUserKit(UUID playerUUID, String kitName, Map<Integer, ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		if (KingKits.getInstance() == null) return false;
		if (playerUUID != null && kitName != null && itemsInKit != null && !itemsInKit.isEmpty()) {
			if (isUserKit(kitName, playerUUID)) deleteUserKit(playerUUID, kitName);
			Kit kit = new Kit(kitName, costOfKit, itemsInKit, potionEffects == null ? new ArrayList<PotionEffect>() : potionEffects).setGuiItem(guiItem != null ? guiItem : new ItemStack(Material.DIAMOND_SWORD)).setUserKit(true);
			KingKits.getInstance().getUserKitsConfig().set(playerUUID.toString() + "." + kitName, kit.serialize());
			KingKits.getInstance().saveUserKitsConfig();
			List<Kit> currentUserKits = KingKits.getInstance().userKitList.get(playerUUID);
			if (currentUserKits == null) currentUserKits = new ArrayList<>();
			currentUserKits.add(kit);
			KingKits.getInstance().userKitList.put(playerUUID, currentUserKits);
			return true;
		}
		return false;
	}

	/**
	 * Delete a kit.
	 *
	 * @param kitName The name of the kit
	 * @return Whether the deletion of the kit has been successful or not.
	 */
	public static boolean deleteKit(String kitName) {
		if (kitName != null) {
			List<String> kits = KingKits.getInstance() != null ? KingKits.getInstance().getConfigKitList() : new ArrayList<String>();
			List<String> kitsLC = Utilities.toLowerCaseList(kits);
			if (kitsLC.contains(kitName.toLowerCase())) {
				kitName = kits.get(kitsLC.indexOf(kitName.toLowerCase()));
				KingKits.getInstance().getKitsConfig().set(kitName, null);
				KingKits.getInstance().saveKitsConfig();
				KingKits.getInstance().kitList.remove(kitName);
				return true;
			}
		}
		return false;
	}


	/**
	 * Delete a user kit.
	 *
	 * @param playerUUID The user kit's owner's UUID
	 * @param kitName The name of the user kit
	 * @return Whether the deletion of the user kit has been successful or not.
	 */
	public static boolean deleteUserKit(UUID playerUUID, String kitName) {
		if (kitName != null) {
			List<String> kits = KingKits.getInstance() != null ? KingKits.getInstance().getKitList(playerUUID) : new ArrayList<String>();
			List<String> kitsLC = Utilities.toLowerCaseList(kits);
			if (kitsLC.contains(kitName.toLowerCase())) {
				kitName = kits.get(kitsLC.indexOf(kitName.toLowerCase()));
				KingKits.getInstance().getUserKitsConfig().set(playerUUID.toString() + "." + kitName, null);
				List<Kit> userKits = KingKits.getInstance().userKitList.get(playerUUID);
				if (userKits != null) {
					for (int i = 0; i < userKits.size(); i++) {
						Kit kit = userKits.get(i);
						if (kit != null && (kit.getRealName().equalsIgnoreCase(kitName) || Utilities.stripColour(kit.getName()).equalsIgnoreCase(kitName))) {
							userKits.remove(i);
							break;
						}
					}
					KingKits.getInstance().userKitList.put(playerUUID, userKits);
					if (userKits.isEmpty()) KingKits.getInstance().getUserKitsConfig().set(playerUUID.toString(), null);
				}
				KingKits.getInstance().saveUserKitsConfig();
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the killstreak of a player.
	 *
	 * @param player The player
	 * @return The player's killstreak.
	 */
	public static long getKillstreak(Player player) {
		return player != null ? getKillstreak(player.getName()) : 0L;
	}

	/**
	 * Get the killstreak of a player.
	 *
	 * @param playerUsername The player's username
	 * @return The player's killstreak.
	 */
	public static long getKillstreak(String playerUsername) {
		return KingKits.getInstance() != null && KingKits.getInstance().playerKillstreaks.containsKey(playerUsername) ? KingKits.getInstance().playerKillstreaks.get(playerUsername) : 0L;
	}

	/**
	 * Get the name of the kit that a player is using.
	 *
	 * @param player The player
	 * @return The name of the kit that the player is using.
	 */
	public static String getKit(Player player) {
		if (player == null) return null;
		return getKit(player.getName());
	}


	/**
	 * Get the name of the kit that a player is using.
	 *
	 * @param playerUsername The player's username
	 * @return The name of the kit that the player is using.
	 */
	public static String getKit(String playerUsername) {
		return KingKits.getInstance() != null && KingKits.getInstance().usingKits != null ? KingKits.getInstance().usingKits.get(playerUsername) : null;
	}

	/**
	 * Get a kit by the kit's name.
	 * This does not get user kits.
	 *
	 * @param kitName The name of the kit
	 * @param caseInsensitive True if the casing of the letters does not matter
	 * @return The kit.
	 */
	public static Kit getKitByName(String kitName, boolean caseInsensitive) {
		if (KingKits.getInstance() == null) return null;
		String strippedKitName = Utilities.stripColour(kitName);
		for (Kit kit : KingKits.getInstance().kitList.values()) {
			if (kit != null && (caseInsensitive ? strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getRealName())) || strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getName())) : strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getRealName())) || strippedKitName.equals(Utilities.stripColour(kit.getName()))))
				return kit;
		}
		return strippedKitName != null ? KingKits.getInstance().kitList.get(strippedKitName) : null;
	}

	/**
	 * Get a kit by the kit's name.
	 * This also gets user kits.
	 *
	 * @param kitName The name of the kit (case insensitive)
	 * @param playerUUID The player's UUID (can be null)
	 * @return The kit.
	 */
	public static Kit getKitByName(String kitName, UUID playerUUID) {
		if (KingKits.getInstance() == null) return null;
		String strippedKitName = Utilities.stripColour(kitName);
		if (playerUUID != null) {
			Kit kit = getUserKitByName(kitName, playerUUID);
			if (kit != null) return kit;
		}
		if (strippedKitName != null) {
			for (Kit kit : KingKits.getInstance().kitList.values()) {
				if (kit != null && (strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getRealName())) || strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getName()))))
					return kit;
			}
		}
		return strippedKitName != null ? KingKits.getInstance().kitList.get(strippedKitName) : null;
	}

	/**
	 * Get a list of all the kits.
	 *
	 * @return A list of all the kits.
	 */
	public static List<Kit> getKits() {
		return KingKits.getInstance() != null ? new ArrayList<>(KingKits.getInstance().kitList.values()) : new ArrayList<Kit>();
	}

	/**
	 * Get a Map of all the players and the name of the kit they are using.
	 *
	 * @return A Map of all the players and the name of the kit they are using.
	 */
	public static Map<String, String> getPlayersKits() {
		return KingKits.getInstance() != null ? Collections.unmodifiableMap(KingKits.getInstance().usingKits) : new HashMap<String, String>();
	}

	/**
	 * Get KingKit's plugin logger.
	 *
	 * @return KingKit's plugin logger.
	 */
	public static Logger getPluginLogger() {
		if (KingKits.getInstance() == null) return Logger.getLogger("Minecraft");
		return KingKits.getInstance().getLogger();
	}

	/**
	 * Get the score of a player.
	 *
	 * @param player The player
	 * @return The score of the player.
	 */
	public static int getScore(Player player) {
		return player != null ? getScore(player.getUniqueId()) : 0;
	}

	/**
	 * Get the score of a player.
	 *
	 * @param playerUUID The player's UUID
	 * @return The score of the player.
	 */
	public static int getScore(UUID playerUUID) {
		return KingKits.getInstance() != null && KingKits.getInstance().playerScores.containsKey(playerUUID) ? (Integer) KingKits.getInstance().playerScores.get(playerUUID) : 0;
	}

	/**
	 * Get a Map of all the players and their scores.
	 *
	 * @return The scores of all the players.
	 */
	public static Map<UUID, Integer> getScores() {
		Map<UUID, Integer> playerScores = new HashMap<>();
		if (KingKits.getInstance() != null) {
			for (Map.Entry<UUID, Object> entryScores : KingKits.getInstance().playerScores.entrySet()) {
				if (entryScores.getValue() instanceof Integer)
					playerScores.put(entryScores.getKey(), (Integer) entryScores.getValue());
			}
		}
		return playerScores;
	}

	/**
	 * Get a user kit by the user kit's name.
	 *
	 * @param kitName The user kit's name
	 * @param playerUUID The UUID of the owner of the kit
	 * @return The user kit.
	 */
	public static Kit getUserKitByName(String kitName, UUID playerUUID) {
		if (KingKits.getInstance() == null) return null;
		String strippedKitName = Utilities.stripColour(kitName);
		if (playerUUID != null && strippedKitName != null && KingKits.getInstance().userKitList.containsKey(playerUUID)) {
			List<Kit> kits = KingKits.getInstance().userKitList.get(playerUUID);
			if (kits != null) {
				for (Kit kit : kits) {
					if (kit != null && (strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getRealName())) || strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getName()))))
						return kit;
				}
			}
		}
		return null;
	}

	/**
	 * Check if a player has a kit.
	 *
	 * @param player The player
	 * @return Whether the player has a kit or not.
	 */
	public static boolean hasKit(Player player) {
		return player != null && hasKit(player.getName(), false);
	}

	/**
	 * Check if a player has a kit.
	 *
	 * @param playerUsername The player's username
	 * @return Whether the player has a kit or not.
	 */
	public static boolean hasKit(String playerUsername) {
		return hasKit(playerUsername, false);
	}

	/**
	 * Check if a player has a kit.
	 *
	 * @param player The player
	 * @param ignoreOPs Whether or not to ignore OPs
	 * @return Whether the player has a kit or not.
	 */
	public static boolean hasKit(Player player, boolean ignoreOPs) {
		return player != null && hasKit(player.getName(), ignoreOPs);
	}


	/**
	 * Check if a player has a kit.
	 *
	 * @param playerUsername The player's username
	 * @param ignoreOPs Whether or not to ignore OPs
	 * @return Whether the player has a kit or not.
	 */
	public static boolean hasKit(String playerUsername, boolean ignoreOPs) {
		return KingKits.getInstance() != null && (ignoreOPs ? KingKits.getInstance().playerKits.get(playerUsername) != null : KingKits.getInstance().usingKits.get(playerUsername) != null);
	}

	/**
	 * Check if a kit is a user kit.
	 *
	 * @param kitName The kit's name
	 */
	public static boolean isUserKit(String kitName, UUID playerUUID) {
		String strippedKitName = Utilities.stripColour(kitName);
		if (playerUUID != null && !strippedKitName.isEmpty() && KingKits.getInstance().userKitList.containsKey(playerUUID)) {
			List<Kit> kits = KingKits.getInstance().userKitList.get(playerUUID);
			if (kits != null) {
				for (Kit kit : kits) {
					if (kit != null && (strippedKitName.equalsIgnoreCase(Utilities.stripColour(kit.getRealName())) || strippedKitName.equals(Utilities.stripColour(kit.getName())))) {
						return true;
					}
				}
			}
		}
		return false;
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
		if (kitName != null && player != null) {
			String playerKit = getKit(player);
			if (playerKit != null) {
				if (!allowUserKits) {
					Kit kit = getKitByName(playerKit, player.getUniqueId());
					return kit != null && !kit.isUserKit() && kit.getName().equalsIgnoreCase(kitName);
				} else {
					return kitName.equalsIgnoreCase(playerKit);
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
		if (kitName != null) {
			List<String> kitList = new ArrayList<>();
			if (KingKits.getInstance().getKitsConfig().contains("Kits")) kitList = KingKits.getInstance().getKitList();
			List<String> kitListLC = Utilities.toLowerCaseList(kitList);
			return kitListLC.contains(kitName.toLowerCase());
		}
		return false;
	}

	/**
	 * Remove a player from the kits list.
	 *
	 * @param player The player
	 */
	public static void removePlayer(Player player) {
		if (player != null) removePlayer(player.getName());
	}

	/**
	 * Remove a player from the kits list.
	 *
	 * @param playerUsername The player's username
	 */
	public static void removePlayer(String playerUsername) {
		KingKits.getInstance().usingKits.remove(playerUsername);
		KingKits.getInstance().playerKits.remove(playerUsername);
	}

	/**
	 * Set the kit of a player.
	 *
	 * @param playerUsername The player's username
	 * @param kitName The name of the kit
	 */
	public static void setPlayerKit(String playerUsername, String kitName) {
		setPlayerKit(getPluginLogger(), playerUsername, kitName);
	}

	/**
	 * Set the kit of a player.
	 *
	 * @param pluginLogger The plugin's logger
	 * @param playerUsername The player's username
	 * @param kitName The name of the kit
	 */
	public static void setPlayerKit(Logger pluginLogger, String playerUsername, String kitName) {
		boolean useSyso = playerUsername == null;
		if (kitExists(kitName)) {
			Player target = Bukkit.getPlayerExact(playerUsername);
			if (target != null && target.isOnline()) {
				if (hasKit(target.getName(), false)) {
					target.getInventory().clear();
					target.getInventory().setArmorContents(null);
					removePlayer(target.getName());
				}
				try {
					SetKit.setKit(target, kitName, true);
				} catch (Exception ex) {
					String msg = "Error, couldn't set the player's kit to " + kitName + ".";
					if (useSyso) System.out.println(msg);
					else pluginLogger.info(msg);
					String msg2 = "Error Log: \n" + ex.getMessage();
					if (useSyso) System.out.println(msg2);
					else pluginLogger.info(msg2);
				}
			} else {
				String msg = "Target player '" + playerUsername + "' is not online/does not exist.";
				if (useSyso) System.out.println(msg);
				else pluginLogger.info(msg);
			}
		} else {
			String msg = "Kit " + kitName + " doesn't exist.";
			if (useSyso) System.out.println(msg);
			else pluginLogger.info(msg);
		}
	}

	/**
	 * Set the score of a player.
	 *
	 * @param player The player
	 * @param score The score
	 */
	public static void setScore(Player player, int score) {
		if (player != null) setScore(player.getUniqueId(), score);
	}

	/**
	 * Set the score of a player.
	 *
	 * @param playerUUID The player's UUID
	 * @param score The score
	 */
	public static void setScore(UUID playerUUID, int score) {
		if (playerUUID != null) {
			if (KingKits.getInstance().playerScores.containsKey(playerUUID))
				KingKits.getInstance().playerScores.remove(playerUUID);
			if (score < 0) score *= -1;
			KingKits.getInstance().playerScores.put(playerUUID, score);
		}
	}

	/**
	 * Show the kit menu to a player.
	 *
	 * @param player The player
	 */
	public static void showKitMenu(Player player) {
		showKitMenu(player, false);
	}

	/**
	 * Show the kit menu to a player.
	 *
	 * @param player The player
	 * @param ignoreChecks Whether or not to ignore checking the configuration 'Kit list mode'
	 */
	public static void showKitMenu(Player player, boolean ignoreChecks) {
		if (KingKits.getInstance() != null && player != null && (!ignoreChecks || KingKits.getInstance().configValues.kitListMode.equalsIgnoreCase("Gui") || KingKits.getInstance().configValues.kitListMode.equalsIgnoreCase("Menu"))) {
			List<Kit> kitValues = new ArrayList<>();
			if (KingKits.getInstance().configValues.sortAlphabetically) {
				List<String> kitNames = new ArrayList<>(KingKits.getInstance().kitList.keySet());
				if (KingKits.getInstance().userKitList.containsKey(player.getUniqueId())) {
					List<Kit> userKits = KingKits.getInstance().userKitList.get(player.getUniqueId());
					for (Kit userKit : userKits) {
						if (userKit != null) kitNames.add(userKit.getRealName());
					}
				}
				Collections.sort(kitNames, Utilities.ALPHABETICAL_ORDER);

				for (Iterator<String> kitIterator = kitNames.iterator(); kitIterator.hasNext(); ) {
					Kit kit = getKitByName(kitIterator.next(), player.getUniqueId());
					if (kit != null) {
						if (!kit.isUserKit() && !KingKits.getInstance().configValues.kitListPermissions) {
							if (!player.hasPermission("kingkits.kits." + kit.getRealName().toLowerCase())) continue;
						}
						kitValues.add(kit);
					}
				}
			} else {
				for (Kit kit : KingKits.getInstance().kitList.values()) {
					if (kit != null) {
						if (!KingKits.getInstance().configValues.kitListPermissions) {
							if (!player.hasPermission("kingkits.kits." + kit.getRealName().toLowerCase())) continue;
						}
						kitValues.add(kit);
					}
				}
				if (KingKits.getInstance().userKitList.containsKey(player.getUniqueId()))
					kitValues.addAll(KingKits.getInstance().userKitList.get(player.getUniqueId()));
			}

			KitStack[] kitStacks = new KitStack[kitValues.size()];
			for (int index = 0; index < kitValues.size(); index++) {
				Kit kit = kitValues.get(index);
				kitStacks[index] = new KitStack(kit.getName(), kit.getGuiItem());
			}
			ChatColor menuColour = kitStacks.length > 0 ? ChatColor.AQUA : ChatColor.RED;
			new GuiKitMenu(player, KingKits.getInstance().configValues.guiTitle.replace("<menucolour>", menuColour.toString()), kitStacks).openMenu();
		}
	}

	/**
	 * Show the kit preview menu to a player.
	 *
	 * @param player The player
	 * @param kitName The kit's name
	 */
	public static void showKitPreview(Player player, String kitName) {
		if (KingKits.getInstance() != null && player != null && kitName != null) {
			Kit kit = getKitByName(kitName, false);
			if (kit != null) new GuiPreviewKit(player, kit.getRealName()).openMenu();
		}
	}

}