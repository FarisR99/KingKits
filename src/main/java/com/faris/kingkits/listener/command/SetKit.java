package com.faris.kingkits.listener.command;

import com.faris.kingkits.*;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.event.custom.PlayerKitEvent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SetKit {

	public static boolean setKit(Player player, String kitName, boolean sendMessages) throws Exception {
		return setKit(player, sendMessages, kitName, true) != null;
	}

	public static boolean setKit(Player player, String kitName, boolean sendMessages, boolean useEconomy) {
		return setKit(player, sendMessages, kitName, useEconomy) != null;
	}

	@SuppressWarnings("deprecation")
	public static Kit setKit(Player player, boolean sendMessages, String kitName, boolean useEconomy) {
		if (player == null || kitName == null) return null;
		KingKits plugin = KingKits.getInstance();
		if (Utilities.inPvPWorld(player)) {
			Kit newKit = KingKitsAPI.getKitByName(Utilities.stripColour(kitName), player.getUniqueId());
			if (newKit != null) {
				kitName = newKit.getRealName();
				if (newKit.isUserKit() || player.hasPermission("kingkits.kits." + newKit.getRealName().toLowerCase())) {
					if (plugin.configValues.oneKitPerLife) {
						if (plugin.configValues.opBypass) {
							if (!player.isOp()) {
								if (plugin.playerKits.containsKey(player.getName())) {
									if (sendMessages) Lang.sendMessage(player, Lang.KIT_ALREADY_CHOSEN);
									return null;
								}
							}
						} else {
							if (plugin.usingKits.containsKey(player.getName())) {
								if (sendMessages) Lang.sendMessage(player, Lang.KIT_ALREADY_CHOSEN);
								return null;
							}
						}
					}
					String oldKit = plugin.playerKits.containsKey(player.getName()) ? plugin.playerKits.get(player.getName()) : "";
					PlayerKitEvent playerKitEvent = new PlayerKitEvent(player, kitName, oldKit, newKit.getItemsWithSlot(), newKit.getArmour(), newKit.getPotionEffects());
					playerKitEvent.setCommands(newKit.getCommands());
					player.getServer().getPluginManager().callEvent(playerKitEvent);
					if (!playerKitEvent.isCancelled()) {
						if (useEconomy && plugin.configValues.vaultValues.useEconomy && plugin.configValues.vaultValues.useCostPerKit && !player.hasPermission("kingkits.free." + newKit.getRealName().toLowerCase())) {
							try {
								net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) Vault.getEconomy();
								double kitCost = newKit.getCost();
								if (economy != null && kitCost > 0D) {
									if (economy.hasAccount(player)) {
										if (economy.getBalance(player) >= kitCost) {
											economy.withdrawPlayer(player, kitCost);
											player.sendMessage(ChatColor.GREEN + plugin.getEconomyMessage(kitCost).replace("<name>", newKit.getName()));
										} else {
											if (sendMessages) Lang.sendMessage(player, Lang.KIT_NOT_ENOUGH_MONEY);
											return null;
										}
									} else {
										if (sendMessages) Lang.sendMessage(player, Lang.KIT_NOT_ENOUGH_MONEY);
										return null;
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						player.setGameMode(GameMode.SURVIVAL);
						for (PotionEffect potionEffect : player.getActivePotionEffects())
							player.removePotionEffect(potionEffect.getType());

						List<ItemStack> droppedItems = new ArrayList<>();
						if (plugin.configValues.replaceItems) {
							player.getInventory().clear();
							player.getInventory().setArmorContents(null);
							Map<Integer, ItemStack> kitItems = playerKitEvent.getKitContentsWithSlots();
							for (Map.Entry<Integer, ItemStack> kitItem : kitItems.entrySet()) {
								try {
									if (kitItem.getValue() != null && kitItem.getValue().getType() != Material.AIR) {
										int slot = kitItem.getKey();
										if (slot >= 0 && slot < player.getInventory().getSize()) {
											player.getInventory().setItem(slot, kitItem.getValue());
										} else {
											droppedItems.add(kitItem.getValue());
										}
									}
								} catch (Exception ignored) {
								}
							}
						} else {
							List<ItemStack> kitItems = playerKitEvent.getKitContents();
							for (ItemStack kitItem : kitItems) {
								try {
									if (kitItem != null && kitItem.getType() != Material.AIR) {
										droppedItems.addAll(player.getInventory().addItem(kitItem).values());
									}
								} catch (Exception ignored) {
								}
							}
						}

						List<ItemStack> armourItems = playerKitEvent.getKitArmour();
						List<ItemStack> leftOverArmour = new ArrayList<>();
						for (ItemStack armourItem : armourItems) {
							if (armourItem != null) {
								String strArmourType = armourItem.getType().toString().toLowerCase();
								if (strArmourType.endsWith("helmet") && (plugin.configValues.replaceItems || Utilities.isItemNull(player.getInventory().getHelmet())))
									player.getInventory().setHelmet(armourItem);
								else if (strArmourType.endsWith("chestplate") && (plugin.configValues.replaceItems || Utilities.isItemNull(player.getInventory().getChestplate())))
									player.getInventory().setChestplate(armourItem);
								else if ((strArmourType.endsWith("leggings") || strArmourType.endsWith("pants")) && (plugin.configValues.replaceItems || Utilities.isItemNull(player.getInventory().getLeggings())))
									player.getInventory().setLeggings(armourItem);
								else if (strArmourType.endsWith("boots") && (plugin.configValues.replaceItems || Utilities.isItemNull(player.getInventory().getBoots())))
									player.getInventory().setBoots(armourItem);
								else if ((plugin.configValues.replaceItems || (!strArmourType.endsWith("helmet") && !strArmourType.endsWith("chestplate") && !strArmourType.endsWith("leggings") && !strArmourType.endsWith("pants") && !strArmourType.endsWith("boots"))) && player.getInventory().getHelmet() == null)
									leftOverArmour.add(armourItem);
								else player.getInventory().addItem(armourItem);
							}
						}
						if (!leftOverArmour.isEmpty()) {
							ItemStack oldHelmet = player.getInventory().getItem(0);
							player.getInventory().setHelmet(leftOverArmour.get(0));
							if (oldHelmet != null && oldHelmet.getType() != Material.AIR)
								droppedItems.addAll(player.getInventory().addItem(oldHelmet).values());
							for (int i = 1; i < leftOverArmour.size(); i++) {
								droppedItems.addAll(player.getInventory().addItem(leftOverArmour.get(i)).values());
							}
						}

						if (plugin.configValues.dropItemsOnFullInventory && !droppedItems.isEmpty()) {
							Location dropLocation = player.getLocation().clone().add(0D, 0.5D, 0D);
							for (ItemStack droppedItem : droppedItems) {
								if (droppedItem != null && droppedItem.getType() != Material.AIR)
									player.getWorld().dropItemNaturally(dropLocation, droppedItem);
							}
						}

						player.setMaxHealth(newKit.getMaxHealth());
						player.addPotionEffects(playerKitEvent.getPotionEffects());

						plugin.playerKits.remove(player.getName());
						plugin.usingKits.remove(player.getName());
						if (plugin.configValues.opBypass) {
							if (!player.isOp()) plugin.playerKits.put(player.getName(), newKit.getRealName());
						} else {
							plugin.playerKits.put(player.getName(), newKit.getRealName());
						}
						plugin.usingKits.put(player.getName(), newKit.getRealName());
						if (plugin.configValues.commandToRun.length() > 0) {
							String cmdToRun = plugin.configValues.commandToRun;
							cmdToRun = cmdToRun.replace("<kit>", kitName);
							cmdToRun = cmdToRun.replace("<player>", player.getName()).replace("<displayname>", player.getDisplayName());
							player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmdToRun);
						}
						for (String cmdToRun : playerKitEvent.getCommands()) {
							cmdToRun = cmdToRun.replace("<kit>", kitName);
							cmdToRun = cmdToRun.replace("<player>", player.getName()).replace("<displayname>", player.getDisplayName());
							player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmdToRun);
						}
						if (!plugin.configValues.customMessages.equals("") && !plugin.configValues.customMessages.equals("''"))
							player.sendMessage(r(plugin.configValues.customMessages).replace("<player>", player.getName()).replace("<displayname>", player.getDisplayName()).replace("<kit>", kitName));
						if (plugin.configValues.kitParticleEffects)
							player.playEffect(player.getLocation().add(0, 1, 0), Effect.ENDER_SIGNAL, (byte) 0);

						player.updateInventory();
						return newKit;
					} else {
						for (PotionEffect potionEffect : player.getActivePotionEffects())
							player.removePotionEffect(potionEffect.getType());
					}
				} else {
					if (sendMessages) Lang.sendMessage(player, Lang.KIT_NO_PERMISSION, kitName);
				}
			} else {
				if (sendMessages) Lang.sendMessage(player, Lang.KIT_NONEXISTENT, kitName);
			}
		}
		return null;
	}

	public static void setKitWithDelay(Player player, String kitName, boolean sendMessages) {
		setKitWithDelay(player, kitName, sendMessages, true);
	}

	public static void setKitWithDelay(Player player, String kitName, boolean sendMessages, boolean useEconomy) {
		final Kit kit = setKit(player, sendMessages, kitName, useEconomy);
		if (kit != null) {
			KingKits pl = KingKits.getInstance();
			if (kit.hasCooldown() && !player.hasPermission(Permissions.KIT_COOLDOWN_BYPASS)) {
				final UUID playerUUID = player.getUniqueId();
				final String newKitName = kit.getRealName();
				pl.getCooldownConfig().set(playerUUID.toString() + "." + newKitName, System.currentTimeMillis());
				pl.saveCooldownConfig();
			}
		}
	}

	private static String r(String message) {
		return Utilities.replaceChatColour(message);
	}

}
