package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.listeners.event.custom.PlayerKitEvent;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SetKit {
	private static KingKits pl;

	public static void setKingKit(KingKits plugin, Player player, String kitName, boolean sendMessages) throws Exception {
		if (setKit(plugin, player, kitName, sendMessages)) {
			if (plugin.configValues.kitCooldown && !player.hasPermission(plugin.permissions.kitBypassCooldown)) {
				final String playerName = player.getName();
				plugin.kitCooldownPlayers.add(playerName);
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						if (pl != null) pl.kitCooldownPlayers.remove(playerName);
					}
				}, plugin.configValues.kitCooldownTime * 20L);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean setKit(KingKits plugin, Player player, String kitName, boolean sendMessages) throws Exception {
		if (plugin == null | player == null || kitName == null) return false;

		pl = plugin;
		if (plugin.configValues.pvpWorlds.contains("All") || plugin.configValues.pvpWorlds.contains(player.getWorld().getName())) {
			if (plugin.getKitsConfig().contains("Kits")) {
				List<String> kitList = plugin.getKitsConfig().getStringList("Kits");
				List<String> kitListLC = new ArrayList<String>();
				for (int pos0 = 0; pos0 < kitList.size(); pos0++) {
					kitListLC.add(kitList.get(pos0).toLowerCase());
				}
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
										return false;
									}
								}
							} else {
								if (plugin.usingKits.containsKey(player.getName())) {
									if (sendMessages) player.sendMessage(r("&6You have already chosen a kit!"));
									return false;
								}
							}
						}
						String oldKit = plugin.playerKits.containsKey(player.getName()) ? plugin.playerKits.get(player.getName()) : "";
						PlayerKitEvent playerKitEvent = new PlayerKitEvent(player, kitName, oldKit, plugin.kitsItems.get(kitName));
						player.getServer().getPluginManager().callEvent(playerKitEvent);
						if (!playerKitEvent.isCancelled()) {
							if (plugin.configValues.vaultValues.useEconomy && plugin.configValues.vaultValues.useCostPerKit) {
								try {
									net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) plugin.vault.getEconomy();
									double kitCost = 0D;
									if (plugin.getCPKConfig().contains(kitName)) kitCost = plugin.getCPKConfig().getDouble(kitName);
									else kitCost = plugin.configValues.vaultValues.costPerKit;
									if (kitCost < 0) kitCost *= -1;
									if (economy.hasAccount(player.getName())) {
										if (economy.getBalance(player.getName()) >= kitCost) {
											economy.withdrawPlayer(player.getName(), kitCost);
											if (kitCost != 0) player.sendMessage(ChatColor.GREEN + plugin.getEconomyMessage(kitCost));
										} else {
											if (sendMessages) player.sendMessage(ChatColor.GREEN + "You do not have enough money to change kits.");
											return false;
										}
									} else {
										if (sendMessages) player.sendMessage(ChatColor.GREEN + "You do not have enough money to change kits.");
										return false;
									}
								} catch (Exception ex) {
								}
							}

							player.getInventory().clear();
							player.getInventory().setArmorContents(null);
							player.setGameMode(GameMode.SURVIVAL);
							for (PotionEffect potionEffect : player.getActivePotionEffects())
								player.removePotionEffect(potionEffect.getType());
							List<ItemStack> kitItems = playerKitEvent.getKitContents();
							for (ItemStack itemToGive : kitItems) {
								if (itemToGive != null) {
									if (itemToGive.getType() == Material.DIAMOND_HELMET || itemToGive.getType() == Material.IRON_HELMET || itemToGive.getType() == Material.CHAINMAIL_HELMET || itemToGive.getType() == Material.GOLD_HELMET || itemToGive.getType() == Material.LEATHER_HELMET || itemToGive.getType() == Material.SKULL_ITEM) {
										player.getInventory().setHelmet(itemToGive);
									} else if (itemToGive.getType() == Material.DIAMOND_CHESTPLATE || itemToGive.getType() == Material.IRON_CHESTPLATE || itemToGive.getType() == Material.CHAINMAIL_CHESTPLATE || itemToGive.getType() == Material.GOLD_CHESTPLATE || itemToGive.getType() == Material.LEATHER_CHESTPLATE) {
										player.getInventory().setChestplate(itemToGive);
									} else if (itemToGive.getType() == Material.DIAMOND_LEGGINGS || itemToGive.getType() == Material.IRON_LEGGINGS || itemToGive.getType() == Material.CHAINMAIL_LEGGINGS || itemToGive.getType() == Material.GOLD_LEGGINGS || itemToGive.getType() == Material.LEATHER_LEGGINGS) {
										player.getInventory().setLeggings(itemToGive);
									} else if (itemToGive.getType() == Material.DIAMOND_BOOTS || itemToGive.getType() == Material.IRON_BOOTS || itemToGive.getType() == Material.CHAINMAIL_BOOTS || itemToGive.getType() == Material.GOLD_BOOTS || itemToGive.getType() == Material.LEATHER_BOOTS) {
										player.getInventory().setBoots(itemToGive);
									} else {
										player.getInventory().addItem(itemToGive);
									}
								}
							}
							player.updateInventory();

							if (plugin.getPotionsConfig().contains(kitName)) {
								List<String> strPotions = plugin.getPotionsConfig().getStringList(kitName);
								for (int pos0 = 0; pos0 < strPotions.size(); pos0++) {
									String strPotionName = strPotions.get(pos0);
									String strPotion = "";
									String strTime = "";
									String strAmplifier = "";
									String[] split = strPotionName.split(" ");
									int time = 0;
									int amplifier = 0;
									if (split.length > 0) {
										strPotion = split[0];
									} else {
										continue;
									}
									if (split.length > 1) {
										strAmplifier = split[1];
										if (isNumeric(strAmplifier)) {
											amplifier = Integer.parseInt(strAmplifier);
											if (amplifier < 0) amplifier = Integer.MAX_VALUE;
										} else {
											if (strAmplifier.equalsIgnoreCase("I")) amplifier = 0;
											else if (strAmplifier.equalsIgnoreCase("II")) amplifier = 1;
											else if (strAmplifier.equalsIgnoreCase("III")) amplifier = 2;
											else if (strAmplifier.equalsIgnoreCase("IV")) amplifier = 3;
											else if (strAmplifier.equalsIgnoreCase("V")) amplifier = 4;
											else if (strAmplifier.equalsIgnoreCase("VI")) amplifier = 5;
											else if (strAmplifier.equalsIgnoreCase("VII")) amplifier = 6;
											else if (strAmplifier.equalsIgnoreCase("VIII")) amplifier = 7;
											else if (strAmplifier.equalsIgnoreCase("X")) amplifier = 8;
											else amplifier = 0;
										}
									} else {
										strAmplifier = "0";
										amplifier = 0;
									}
									if (split.length > 2) {
										strTime = split[2];
										if (isNumeric(strTime)) time = Integer.parseInt(strTime);
										else time = Integer.MAX_VALUE;
									} else {
										time = 300;
									}
									PotionEffectType pet = null;
									if (isNumeric(strPotion)) {
										if (PotionEffectType.getById(Integer.parseInt(strPotion)) != null) {
											pet = PotionEffectType.getById(Integer.parseInt(strPotion));
										}
									} else {
										if (PotionEffectType.getByName(strPotion) != null) {
											pet = PotionEffectType.getByName(strPotion);
										}
									}
									if (pet != null) {
										if (amplifier < 0) amplifier *= -1;
										player.addPotionEffect(new PotionEffect(pet, time * 20, amplifier));
									}
								}
							}

							if (plugin.configValues.commandToRun.length() > 0) {
								String cmdToRun = plugin.configValues.commandToRun;
								cmdToRun = cmdToRun.replaceAll("<kit>", kitName);
								cmdToRun = cmdToRun.replaceAll("<player>", player.getName());
								player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmdToRun);
							}
							plugin.playerKits.remove(player.getName());
							plugin.usingKits.remove(player.getName());
							if (plugin.configValues.opBypass) {
								if (!player.isOp()) {
									plugin.playerKits.put(player.getName(), kitName);
								}
							} else {
								plugin.playerKits.put(player.getName(), kitName);
							}
							plugin.usingKits.put(player.getName(), kitName);
							if (plugin.configValues.customMessages != "" && plugin.configValues.customMessages != "''") player.sendMessage(r(plugin.configValues.customMessages).replaceAll("<kit>", kitName));
							if (plugin.configValues.kitParticleEffects) {
								player.playEffect(player.getLocation().add(0, 1, 0), Effect.ENDER_SIGNAL, (byte) 0);
							}
							return true;
						} else {
							for (PotionEffect potionEffect : player.getActivePotionEffects())
								player.removePotionEffect(potionEffect.getType());
						}
					} else {
						if (sendMessages) player.sendMessage(r("&cYou do not have permission to use the kit &4" + kitName + "&c."));
					}
				} else {
					if (sendMessages) player.sendMessage(r("&4" + kitName + " &6does not exist."));
				}
			} else {
				if (sendMessages) player.sendMessage(r("&4" + kitName + " &6does not exist."));
			}
		}
		return false;
	}

	private static String r(String val) {
		return pl.replaceAllColours(val);
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
