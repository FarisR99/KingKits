package me.faris.kingkits.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.guis.GuiKitMenu;
import me.faris.kingkits.helpers.KitStack;
import me.faris.kingkits.listeners.commands.SetKit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;

public class PvPKits {
	private static KingKits plugin = null;

	/**
	 * Ignore this method, it's just for PvPKits plugin.
	 */
	public PvPKits(KingKits pvpKitsPlugin) {
		plugin = pvpKitsPlugin;
	}

	/**
	 * Get KingKit's logger.
	 */
	public static Logger getPluginLogger() {
		return plugin.getLogger();
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 */
	public static boolean hasKit(Player player) {
		if (player == null) return false;
		return hasKit(player.getName());
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 */
	public static boolean hasKit(String player) {
		return plugin.usingKits.containsKey(player);
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 * @param ignoreOPs
	 *            Input true if you should ignore OPs.
	 */
	public static boolean hasKit(Player player, boolean ignoreOPs) {
		if (player == null) return false;
		return hasKit(player.getName(), ignoreOPs);
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 * @param ignoreOPs
	 *            Input true if you should ignore OPs.
	 */
	public static boolean hasKit(String player, boolean ignoreOPs) {
		if (ignoreOPs) {
			return plugin.playerKits.containsKey(player);
		} else {
			return plugin.usingKits.containsKey(player);
		}
	}

	/**
	 * Get player's kit : Returns null if the player is null or doesn't have a kit.
	 *
	 * @param player
	 *            The player to get the kit name from.
	 */
	public static String getKit(Player player) {
		if (player == null) return null;
		return getKit(player.getName());
	}

	/**
	 * Get player's kit : Returns null if the player doesn't have a kit.
	 *
	 * @param player
	 *            The player to get the kit name from.
	 */
	public static String getKit(String player) {
		if (hasKit(player)) return plugin.usingKits.get(player);
		else return null;
	}

	/**
	 * Get players using a specific kit : Returns an empty list if the kit doesn't exist or no players are using that kit.
	 * 
	 * @param kitName
	 *            The kit to obtain the list of players.
	 */
	public static List<String> getPlayersUsingKit(String kitName) {
		List<String> playersUsingKit = new ArrayList<String>();
		List<String> playersInKitMap = new ArrayList<String>(plugin.usingKits.keySet());
		for (int pos = 0; pos < plugin.usingKits.size(); pos++) {
			String kit = plugin.usingKits.get(pos);
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
		return plugin.usingKits;
	}

	/**
	 * Returns if a kit exists.
	 * Note: Case sensitive.
	 * 
	 * @param kitName
	 *            The kit name to check.
	 */
	public static boolean kitExists(String kitName) {
		boolean kitExists = false;
		List<String> kitList = new ArrayList<String>();
		if (plugin.getKitsConfig().contains("Kits")) kitList = plugin.getKitsConfig().getStringList("Kits");
		List<String> kitListLC = new ArrayList<String>();
		for (int pos = 0; pos < kitList.size(); pos++)
			kitListLC.add(kitList.get(pos).toLowerCase());
		if (kitListLC.contains(kitName.toLowerCase())) kitExists = true;
		return kitExists;
	}

	/**
	 * Remove a player from a kit.
	 * Note: Doesn't clear the player's inventory.
	 * 
	 * @param player
	 *            The player to remove from a kit.
	 */
	public static void removePlayer(Player player) {
		if (player != null) removePlayer(player.getName());
	}

	/**
	 * Remove a player from a kit.
	 * Note: Doesn't clear the player's inventory.
	 * 
	 * @param player
	 *            The player's name to remove from a kit.
	 */
	public static void removePlayer(String player) {
		if (hasKit(player, false)) {
			plugin.usingKits.remove(player);
			plugin.playerKits.remove(player);
		}
	}

	/**
	 * Remove a player from a kit.
	 * Note: Clears a player's inventory.
	 * 
	 * @param pluginLogger
	 *            Your plugin's logger.
	 * @param player
	 *            The player who's kit is to be changed.
	 * @param kit
	 *            The kit that the player should be set as.
	 */
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
						SetKit.setKit(plugin, target, kit, true);
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
	 * @param player
	 *            The player who's score is meant to be returned.
	 */
	public static int getScore(Player player) {
		if (player == null) return -1;
		return getScore(player.getName());
	}

	/**
	 * Returns the target player's score.
	 * 
	 * @param player
	 *            The player who's score is meant to be returned. Returns -1 if the player doesn't exist in the scores configuration.
	 */
	public static int getScore(String player) {
		if (plugin.playerScores.containsKey(player)) {
			return (int) plugin.playerScores.get(player);
		} else {
			return -1;
		}
	}

	/**
	 * Returns a Map of all the player's scores and their usernames.
	 * Note: The return type of the map is (String, Object) which is actually a Map of (String, Integer), just cast all the values to integer.
	 */
	public static Map<String, Object> getScores() {
		return plugin.playerScores;
	}

	/**
	 * Set the score of a player.
	 * 
	 * @param player
	 *            The player who's score is meant to be set.
	 * @param value
	 *            The new score of the player.
	 */
	public static void setScore(Player player, int value) {
		if (player != null) setScore(player.getName(), value);
	}

	/**
	 * Set the score of a player.
	 * 
	 * @param player
	 *            The player who's score is meant to be set.
	 * @param value
	 *            The new score of the player.
	 */
	public static void setScore(String player, int value) {
		if (plugin.playerScores.containsKey(player)) plugin.playerScores.remove(player);
		if (value < 0) value *= -1;
		if (value > Integer.MAX_VALUE) value -= Integer.MAX_VALUE;
		plugin.playerScores.put(player, value);
	}

	/**
	 * Create a kit.
	 * Returns if the creation of the kit is successful.
	 * 
	 * @param kitName
	 *            The kit name.
	 * @param itemsInKit
	 *            The items in the kit.
	 * @param potionEffects
	 *            The potion effects in the kit. Set to null if there are none.
	 * @param guiItem
	 *            The item to be shown in the GUI Inventory when using GUI mode. Set to null if you want it to be a diamond sword.
	 * @param costOfKit
	 *            The cost of the kit.
	 */
	@SuppressWarnings("deprecation")
	public static boolean createKit(String kitName, List<ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		boolean containsKit = plugin.getKitsConfig().contains(kitName);
		if (!itemsInKit.isEmpty()) {
			if (containsKit) {
				List<String> currentKits = plugin.getKitsConfig().getStringList("Kits");
				List<String> currentKitsLC = KingKits.toLowerCaseList(currentKits);
				if (currentKitsLC.contains(kitName.toLowerCase())) kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));

				List<String> configItems = plugin.getKitsConfig().getStringList(kitName);
				boolean modifiedE = false;
				boolean modifiedL = false;
				boolean modifiedD = false;
				for (String itemInKit : configItems) {
					try {
						int itemID = Integer.parseInt(itemInKit.split(" ")[0]);
						if (plugin.getEnchantsConfig().contains(kitName + " " + itemID)) {
							plugin.getEnchantsConfig().set(kitName + " " + itemID, null);
							modifiedE = true;
						}
						if (plugin.getLoresConfig().contains(kitName + " " + itemID)) {
							plugin.getLoresConfig().set(kitName + " " + itemID, null);
							modifiedL = true;
						}
						if (plugin.getDyesConfig().contains(kitName + " " + itemID)) {
							plugin.getDyesConfig().set(kitName + " " + itemID, null);
							modifiedD = true;
						}
					} catch (Exception ex) {
						continue;
					}
				}
				plugin.getKitsConfig().set(kitName, null);
				plugin.saveKitsConfig();
				plugin.getGuiItemsConfig().set(kitName, null);
				plugin.saveGuiItemsConfig();
				plugin.getCPKConfig().set(kitName, null);
				plugin.saveCPKConfig();
				if (modifiedE) {
					plugin.saveEnchantsConfig();
				}
				if (modifiedL) {
					plugin.saveLoresConfig();
				}
				if (modifiedD) {
					plugin.saveDyesConfig();
				}
				plugin.getPotionsConfig().set(kitName, null);
				plugin.savePotionsConfig();
			}
			List<String> strItemsInKit = new ArrayList<String>();
			for (ItemStack itemInKit : itemsInKit) {
				if (itemInKit.hasItemMeta()) {
					if (itemInKit.getItemMeta().hasDisplayName()) strItemsInKit.add(itemInKit.getType().getId() + " " + itemInKit.getAmount() + " " + itemInKit.getDurability() + " " + itemInKit.getItemMeta().getDisplayName());
					else strItemsInKit.add(itemInKit.getType().getId() + " " + itemInKit.getAmount() + " " + itemInKit.getDurability());
				} else strItemsInKit.add(itemInKit.getType().getId() + " " + itemInKit.getAmount() + " " + itemInKit.getDurability());
				if (!itemInKit.getEnchantments().isEmpty()) {
					for (Entry<Enchantment, Integer> itemE : itemInKit.getEnchantments().entrySet()) {
						plugin.getEnchantsConfig().set(kitName + " " + itemInKit.getType().getId(), itemE.getKey().getName() + " " + itemE.getValue());
					}
					plugin.saveEnchantsConfig();
				}
				if (itemInKit.hasItemMeta()) {
					if (itemInKit.getItemMeta().hasLore()) {
						plugin.getLoresConfig().set(kitName + " " + itemInKit.getType().getId(), itemInKit.getItemMeta().getLore());
						plugin.saveLoresConfig();
					}
				}
				if (itemInKit.getType() == Material.LEATHER_HELMET || itemInKit.getType() == Material.LEATHER_CHESTPLATE || itemInKit.getType() == Material.LEATHER_LEGGINGS || itemInKit.getType() == Material.LEATHER_BOOTS) {
					try {
						if (itemInKit.hasItemMeta()) {
							if (itemInKit.getItemMeta() instanceof LeatherArmorMeta) {
								LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemInKit.getItemMeta();
								if (armorMeta.getColor() != null) {
									plugin.getDyesConfig().set(kitName + " " + itemInKit.getType().getId(), armorMeta.getColor().asRGB());
									plugin.saveDyesConfig();
								}
							}
						}
					} catch (Exception ex) {
					}
				}
			}

			if (potionEffects != null) {
				if (!potionEffects.isEmpty()) {
					List<String> strPotionEffects = new ArrayList<String>();
					for (PotionEffect potionEffect : potionEffects) {
						strPotionEffects.add(potionEffect.getType().getName() + " " + (potionEffect.getDuration() / 20) + " " + potionEffect.getAmplifier());
					}
					plugin.getPotionsConfig().addDefault(kitName, strPotionEffects);
					plugin.savePotionsConfig();
				}
			}

			if (guiItem != null) {
				if (guiItem.getType() != Material.AIR) plugin.getGuiItemsConfig().set(kitName, guiItem.getType().getId());
				else plugin.getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
			} else plugin.getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
			plugin.saveGuiItemsConfig();

			if (costOfKit < 0D) costOfKit *= -1D;
			plugin.getCPKConfig().set(kitName, costOfKit);
			plugin.saveCPKConfig();

			List<String> nKitList = new ArrayList<String>();
			if (plugin.getKitsConfig().contains("Kits")) nKitList = plugin.getKitsConfig().getStringList("Kits");
			if (!containsKit) nKitList.add(kitName);
			plugin.getKitsConfig().set("Kits", nKitList);
			plugin.getKitsConfig().set(kitName, strItemsInKit);
			plugin.saveKitsConfig();
			plugin.kitsItems.put(kitName, itemsInKit);

			try {
				plugin.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
			} catch (Exception ex) {
			}
			return true;
		}
		return false;
	}

	/** 
	 * Delete a kit.
	 * Returns if the deletion of the kit is successful.
	 * 
	 * @param kitName
	 *            The name of the kit to be deleted.
	 */
	public static boolean deleteKit(String kitName) {
		List<String> kits = plugin.getKitsConfig().getStringList("Kits");
		List<String> kitsLC = new ArrayList<String>();
		for (String kit : kits)
			kitsLC.add(kit.toLowerCase());
		if (kitsLC.contains(kitName.toLowerCase())) {
			kitName = kits.get(kitsLC.indexOf(kitName.toLowerCase()));
			List<String> itemsInKit = plugin.getKitsConfig().getStringList(kitName);
			for (String item : itemsInKit) {
				try {
					String[] itemSplit = item.split(" ");
					int itemID = Integer.parseInt(itemSplit[0]);
					if (plugin.getEnchantsConfig().contains(kitName + " " + itemID)) {
						plugin.getEnchantsConfig().set(kitName + " " + itemID, null);
						plugin.saveEnchantsConfig();
					}
					if (plugin.getLoresConfig().contains(kitName + " " + itemID)) {
						plugin.getLoresConfig().set(kitName + " " + itemID, null);
						plugin.saveEnchantsConfig();
					}
					if (plugin.getDyesConfig().contains(kitName + " " + itemID)) {
						plugin.getDyesConfig().set(kitName + " " + itemID, null);
						plugin.saveDyesConfig();
					}
				} catch (Exception ex) {
					continue;
				}
			}
			List<String> kitList = plugin.getKitsConfig().getStringList("Kits");
			kitList.remove(kitName);
			plugin.getKitsConfig().set("Kits", kitList);
			plugin.getKitsConfig().set(kitName, null);
			plugin.saveKitsConfig();
			if (plugin.getPotionsConfig().contains(kitName)) {
				plugin.getPotionsConfig().set(kitName, null);
				plugin.savePotionsConfig();
			}
			if (plugin.getGuiItemsConfig().contains(kitName)) {
				plugin.getGuiItemsConfig().set(kitName, null);
				plugin.saveGuiItemsConfig();
			}
			if (plugin.getCPKConfig().contains(kitName)) {
				plugin.getCPKConfig().set(kitName, null);
				plugin.saveCPKConfig();
			}
			plugin.kitsItems.remove(kitName);
			return true;
		}
		return false;
	}

	/**
	 * Returns the player's killstreak.
	 * Returns 0 if the player doesn't exist, is null or has no killstreak.
	 * 
	 * @param player
	 *            The player.
	 */
	public static long getKillstreak(Player player) {
		if (player == null) return 0L;
		return getKillstreak(player.getName());
	}

	/**
	 * Returns the player's killstreak.
	 * Returns 0 if the player doesn't exist or has no killstreak.
	 * 
	 * @param player
	 *            The name of the player.
	 */
	public static long getKillstreak(String player) {
		if (plugin.playerKillstreaks.containsKey(player)) return plugin.playerKillstreaks.get(player);
		else return 0L;
	}

	@SuppressWarnings("deprecation")
	/**
	 * Display the Kit GUI menu to a player.
	 * @param player - The Kit GUI viewer.
	 */
	public static void showKitMenu(Player player) {
		if (plugin.configValues.kitListMode.equalsIgnoreCase("Gui") || plugin.configValues.kitListMode.equalsIgnoreCase("Menu")) {
			if (!GuiKitMenu.playerMenus.containsKey(player.getName())) {
				List<String> kitList = new ArrayList<String>();
				if (plugin.getKitsConfig().contains("Kits")) kitList = plugin.getKitsConfig().getStringList("Kits");
				KitStack[] kitStacks = new KitStack[kitList.size()];
				boolean modifiedConfig = false;
				for (int i = 0; i < kitList.size(); i++) {
					String kitName = kitList.get(i);
					try {
						if (kitName.contains(" ")) {
							kitName = kitName.split(" ")[0];
						}
					} catch (Exception ex) {
					}
					try {
						ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD, 1);
						if (plugin.getGuiItemsConfig().contains(kitName)) {
							String guiItemSplit[] = plugin.getGuiItemsConfig().getString(kitName).split(" ");
							if (guiItemSplit.length > 1) {
								try {
									itemStack = new ItemStack(Integer.parseInt(guiItemSplit[0]), 1);
									itemStack.setDurability(Short.parseShort(guiItemSplit[1]));
								} catch (Exception ex) {
									continue;
								}
							} else itemStack = new ItemStack(Integer.parseInt(guiItemSplit[0]), 1);
						} else {
							plugin.getGuiItemsConfig().set(kitName, itemStack.getType().getId());
							modifiedConfig = true;
						}
						kitStacks[i] = new KitStack(kitName, itemStack);
					} catch (Exception ex) {
						plugin.getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
						modifiedConfig = true;
						continue;
					}
				}
				if (modifiedConfig) plugin.saveGuiItemsConfig();
				ChatColor menuColour = !kitList.isEmpty() ? ChatColor.DARK_BLUE : ChatColor.RED;
				new GuiKitMenu(player, menuColour + "PvP Kits", kitStacks).openMenu();
			}
		}
	}

}