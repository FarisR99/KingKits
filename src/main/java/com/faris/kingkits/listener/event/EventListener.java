package com.faris.kingkits.listener.event;

import com.faris.kingkits.*;
import com.faris.kingkits.gui.GuiKingKits;
import com.faris.kingkits.gui.GuiKitMenu;
import com.faris.kingkits.gui.GuiPreviewKit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.command.SetKit;
import com.faris.kingkits.listener.event.custom.PlayerKilledEvent;
import mkremins.fanciful.FancyMessage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.*;

public class EventListener implements Listener {

	/**
	 * Register custom kill event *
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void registerKillEvent(PlayerDeathEvent event) {
		try {
			if (event.getEntity().getKiller() != null) {
				if (!event.getEntity().getUniqueId().equals(event.getEntity().getKiller().getUniqueId()))
					event.getEntity().getServer().getPluginManager().callEvent(new PlayerKilledEvent(event.getEntity().getKiller(), event.getEntity()));
			}
		} catch (Exception ignored) {
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		try {
			// List kits on join
			try {
				if (this.getPlugin().configValues.listKitsOnJoin) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						this.listKitsOnJoin(event.getPlayer());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Kit menu
			try {
				if (this.getPlugin().configValues.kitMenuOnJoin) {
					final Player player = event.getPlayer();
					player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
						@SuppressWarnings("deprecation")
						public void run() {
							if (player.isOnline()) {
								if (!GuiKingKits.guiKitMenuMap.containsKey(player.getName()) && !GuiKingKits.guiPreviewKitMap.containsKey(player.getName())) {
									KingKitsAPI.showKitMenu(player, false);
								}
							}
						}
					}, 15L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		try {
			// Remove kits
			final Player player = event.getPlayer();
			try {
				if (this.getPlugin().configValues.removeItemsOnLeave) {
					if (this.getPlugin().playerKits.containsKey(player.getName()) || this.getPlugin().usingKits.containsKey(player.getName())) {
						player.getInventory().clear();
						player.getInventory().setArmorContents(null);
					}
				}
				if (this.getPlugin().playerKits.containsKey(player.getName()))
					this.getPlugin().playerKits.remove(player.getName());
				if (this.getPlugin().usingKits.containsKey(player.getName())) {
					this.getPlugin().usingKits.remove(player.getName());
					player.setMaxHealth(20D);
				}
				if (GuiKingKits.guiKitMenuMap.containsKey(player.getName())) {
					GuiKitMenu guiKitMenu = GuiKingKits.guiKitMenuMap.get(player.getName());
					if (guiKitMenu != null) guiKitMenu.closeMenu(true, true);
					GuiKingKits.guiKitMenuMap.remove(player.getName());
				}
				if (GuiKingKits.guiPreviewKitMap.containsKey(player.getName())) {
					GuiPreviewKit guiPreviewKit = GuiKingKits.guiPreviewKitMap.get(player.getName());
					if (guiPreviewKit != null) guiPreviewKit.closeMenu(true, true);
					GuiKingKits.guiPreviewKitMap.remove(player.getName());
				}
				player.updateInventory();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Remove potion effects
			try {
				if (this.getPlugin().configValues.removePotionEffectsOnLeave) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects()) {
							PotionEffectType potionEffectType = potionEffectOnPlayer.getType();
							event.getPlayer().removePotionEffect(potionEffectType);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Remove compass target
			try {
				if (this.getPlugin().configValues.rightClickCompass) {
					if (this.getPlugin().compassTargets.containsValue(event.getPlayer().getUniqueId())) {
						Player tracker = null;
						for (Map.Entry<UUID, UUID> compassEntry : this.getPlugin().compassTargets.entrySet()) {
							Player key = Bukkit.getServer().getPlayer(compassEntry.getKey());
							Player value = Bukkit.getServer().getPlayer(compassEntry.getValue());
							if (key != null && value != null && key.isOnline() && value.isOnline()) {
								if (event.getPlayer().getName().equalsIgnoreCase(value.getName()))
									tracker = key;
							}
						}
						if (tracker != null) this.getPlugin().compassTargets.remove(tracker.getUniqueId());
					}
					if (this.getPlugin().compassTargets.containsKey(event.getPlayer().getUniqueId()))
						this.getPlugin().compassTargets.remove(event.getPlayer().getUniqueId());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Remove killstreak
			try {
				this.getPlugin().playerKillstreaks.remove(event.getPlayer().getName());
			} catch (Exception ignored) {
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Quick soup
		try {
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (event.getItem() != null) {
					if (event.getItem().getType() == Material.MUSHROOM_SOUP) {
						if (this.getPlugin().configValues.quickSoup) {
							if (event.getPlayer().hasPermission(Permissions.SOUP_QUICKSOUP)) {
								if (Utilities.inPvPWorld(event.getPlayer())) {
									Player player = event.getPlayer();
									int soupAmount = player.getInventory().getItemInHand().getAmount();
									if (soupAmount > 0) {
										boolean valid = true;
										if (player.getHealth() < player.getMaxHealth()) {
											if (player.getHealth() + (this.getPlugin().configValues.quickSoupHeal * 2) > player.getMaxHealth())
												player.setHealth(player.getMaxHealth());
											else
												player.setHealth(player.getHealth() + (this.getPlugin().configValues.quickSoupHeal * 2));
										} else if (player.getFoodLevel() < 20) {
											if (player.getFoodLevel() + 6 > 20) player.setFoodLevel(20);
											else player.setFoodLevel(player.getFoodLevel() + 6);
										} else {
											valid = false;
										}
										if (valid) {
											if (soupAmount == 1) {
												player.getInventory().setItemInHand(new ItemStack(Material.BOWL, 1));
											} else {
												int newAmount = soupAmount - 1;
												ItemStack newItem = player.getInventory().getItemInHand();
												newItem.setAmount(newAmount);
												player.getInventory().setItemInHand(newItem);
												player.getInventory().addItem(new ItemStack(Material.BOWL, 1));
											}
											event.setCancelled(true);
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractIgnoreCancelled(PlayerInteractEvent event) {
		try {
			// No tool damage
			try {
				if (event.getItem() != null) {
					if (this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
						if (this.isTool(event.getItem().getType()) || event.getItem().getType() == Material.FISHING_ROD || event.getItem().getType() == Material.FLINT_AND_STEEL) {
							boolean repair = false;
							if (this.getPlugin().configValues.disableItemBreaking) {
								repair = true;
							} else {
								Kit kit = KingKitsAPI.getKitByName(this.getPlugin().usingKits.get(event.getPlayer().getName()), false);
								if (kit != null && !kit.canItemsBreak()) {
									repair = true;
								}
							}
							if (repair) event.getItem().setDurability((short) 0);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Right clicking a sign
			try {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (event.getPlayer().getWorld() != null) {
						if (Utilities.inPvPWorld(event.getPlayer())) {
							Player player = event.getPlayer();
							BlockState block = event.getClickedBlock().getState();
							if ((block instanceof Sign)) {
								Sign sign = (Sign) block;
								String firstLine = sign.getLine(0);
								if (firstLine.equals((this.getPlugin().configValues.strSignValidKit.startsWith(ChatColor.BLACK.toString()) ? this.getPlugin().configValues.strSignValidKit.replaceFirst(ChatColor.BLACK.toString(), "") : this.getPlugin().configValues.strSignValidKit))) {
									if (player.hasPermission(Permissions.SIGN_KIT)) {
										String line1 = sign.getLine(1);
										if (line1 != null) {
											if (!line1.equalsIgnoreCase("")) {
												List<String> kitList = this.getPlugin().getKitList();
												List<String> kitListLC = Utilities.toLowerCaseList(kitList);
												if (kitListLC.contains(line1.toLowerCase())) {
													String kitName = kitList.get(kitListLC.indexOf(line1.toLowerCase()));
													try {
														final Kit kit = KingKitsAPI.getKitByName(kitName, false);
														boolean validCooldown = true;
														if (kit != null && kit.hasCooldown() && !player.hasPermission(Permissions.KIT_COOLDOWN_BYPASS)) {
															if (this.getPlugin().getCooldownConfig().contains(player.getUniqueId().toString() + "." + kit.getRealName())) {
																long currentCooldown = this.getPlugin().getCooldown(player.getUniqueId(), kit.getRealName());
																if (System.currentTimeMillis() - currentCooldown >= kit.getCooldown() * 1000) {
																	this.getPlugin().getCooldownConfig().set(player.getUniqueId().toString() + "." + kit.getRealName(), null);
																	this.getPlugin().saveCooldownConfig();
																} else {
																	Utilities.sendDelayMessage(player, kit, currentCooldown);
																	validCooldown = false;
																}
															}
														}
														if (validCooldown)
															SetKit.setKitWithDelay(player, kitName, true);
													} catch (Exception ex) {
														ex.printStackTrace();
														Lang.sendMessage(player, Lang.COMMAND_GEN_ERROR);
													}
												} else {
													Lang.sendMessage(player, Lang.KIT_NONEXISTENT, line1);
													sign.setLine(0, this.getPlugin().configValues.strSignInvalidKit);
													sign.update(true);
												}
											} else {
												Lang.sendMessage(player, Lang.SIGN_GENERAL_INCORRECTLY_SETUP);
												sign.setLine(0, this.getPlugin().configValues.strSignInvalidKit);
												sign.update(true);
											}
										} else {
											Lang.sendMessage(player, Lang.SIGN_GENERAL_INCORRECTLY_SETUP);
											sign.setLine(0, this.getPlugin().configValues.strSignInvalidKit);
											sign.update(true);
										}
									} else {
										Lang.sendMessage(player, Lang.SIGN_USE_NO_PERMISSION);
									}
									event.setCancelled(true);
								} else if (firstLine.equals(this.getPlugin().configValues.strSignValidKitList.startsWith(ChatColor.BLACK.toString()) ? this.getPlugin().configValues.strSignValidKitList.replaceFirst(ChatColor.BLACK.toString(), "") : this.getPlugin().configValues.strSignValidKitList)) {
									if (player.hasPermission(Permissions.SIGN_LIST)) {
										if (!this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui") && !this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu")) {
											List<String> kitList = this.getPlugin().getKitList();
											Lang.sendMessage(player, Lang.GEN_KIT_LIST_TITLE, String.valueOf(kitList.size()));
											if (!kitList.isEmpty()) {
												for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
													String kitName = kitList.get(kitPos).split(" ")[0];
													if (player.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
														if (player.hasPermission(Permissions.KIT_LIST_TOOLTIP)) {
															FancyMessage listMessage = new FancyMessage((kitPos + 1) + ". ").color(ChatColor.GOLD).then(kitName).color(ChatColor.RED);
															Kit targetKit = KingKitsAPI.getKitByName(kitName, true);
															if (targetKit != null && targetKit.hasDescription()) {
																final List<String> kitDescription = new ArrayList<>();
																for (String descriptionLine : targetKit.getDescription()) {
																	descriptionLine = Utilities.replaceChatColour(descriptionLine);
																	descriptionLine = descriptionLine.replace("<player>", player.getName());
																	descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
																	descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
																	descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
																	descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
																	kitDescription.add(descriptionLine);
																}
																if (!kitDescription.isEmpty())
																	listMessage.tooltip(kitDescription);
															}
															listMessage.command("/pvpkit " + kitName).send(player);
														} else {
															player.sendMessage(Utilities.replaceChatColour("&6" + (kitPos + 1) + ". &c" + kitName));
														}
													} else {
														if (this.getPlugin().configValues.kitListPermissions) {
															if (player.hasPermission(Permissions.KIT_LIST_TOOLTIP)) {
																FancyMessage listMessage = new FancyMessage((kitPos + 1) + ". ").color(ChatColor.GOLD).then(kitName).color(ChatColor.DARK_RED);
																Kit targetKit = KingKitsAPI.getKitByName(kitName, true);
																if (targetKit != null && targetKit.hasDescription()) {
																	final List<String> kitDescription = new ArrayList<>();
																	for (String descriptionLine : targetKit.getDescription()) {
																		descriptionLine = Utilities.replaceChatColour(descriptionLine);
																		descriptionLine = descriptionLine.replace("<player>", player.getName());
																		descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
																		descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
																		descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
																		descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
																		kitDescription.add(descriptionLine);
																	}
																	if (!kitDescription.isEmpty())
																		listMessage.tooltip(kitDescription);
																}
																listMessage.command("/pvpkit " + kitName).send(player);
															} else {
																player.sendMessage(Utilities.replaceChatColour("&4" + (kitPos + 1) + ". &4" + kitName));
															}
														}
													}
												}
											} else {
												Lang.sendMessage(player, Lang.GEN_NO_KITS);
											}
										} else {
											KingKitsAPI.showKitMenu(player);
										}
									} else {
										Lang.sendMessage(player, Lang.SIGN_USE_NO_PERMISSION);
									}
									event.setCancelled(true);
								} else if (firstLine.equals(this.getPlugin().configValues.strSignRefillValid.startsWith(ChatColor.BLACK.toString()) ? this.getPlugin().configValues.strSignRefillValid.replaceFirst(ChatColor.BLACK.toString(), "") : this.getPlugin().configValues.strSignRefillValid)) {
									if (player.hasPermission(Permissions.SIGN_REFILL)) {
										String strLine1 = sign.getLine(1);
										if (strLine1 != null && strLine1.equalsIgnoreCase("All")) {
											player.performCommand("refill all");
										} else {
											player.performCommand("refill");
										}
									} else {
										Lang.sendMessage(player, Lang.SIGN_USE_NO_PERMISSION);
									}
									event.setCancelled(true);
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		try {
			// Creating a sign
			try {
				if (Utilities.inPvPWorld(event.getPlayer())) {
					Player p = event.getPlayer();
					String signType = event.getLine(0);
					if (signType.equalsIgnoreCase(this.getPlugin().configValues.strSignKit)) {
						if (p.hasPermission(Permissions.SIGN_CREATE)) {
							if (!event.getLine(1).isEmpty()) {
								event.setLine(0, this.getPlugin().configValues.strSignValidKit);
							} else {
								event.setLine(0, this.getPlugin().configValues.strSignInvalidKit);
								Lang.sendMessage(p, Lang.SIGN_CREATE_SECOND_LINE);
							}
						} else {
							Lang.sendMessage(p, Lang.SIGN_CREATE_NO_PERMISSION, "create");
							event.setLine(0, "");
							event.setLine(1, "");
							event.setLine(2, "");
							event.setLine(3, "");
						}
					} else if (signType.equalsIgnoreCase(this.getPlugin().configValues.strSignKitList)) {
						if (p.hasPermission(Permissions.SIGN_CREATE)) {
							event.setLine(0, this.getPlugin().configValues.strSignValidKitList);
						} else {
							Lang.sendMessage(p, Lang.SIGN_CREATE_NO_PERMISSION, "list");
							event.setLine(0, "");
							event.setLine(1, "");
							event.setLine(2, "");
							event.setLine(3, "");
						}
					} else if (signType.equalsIgnoreCase(this.getPlugin().configValues.strSignRefill)) {
						if (p.hasPermission(Permissions.SIGN_CREATE)) {
							event.setLine(0, this.getPlugin().configValues.strSignRefillValid);
						} else {
							Lang.sendMessage(p, Lang.SIGN_CREATE_NO_PERMISSION, "refill");
							event.setLine(0, "");
							event.setLine(1, "");
							event.setLine(2, "");
							event.setLine(3, "");
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		try {
			boolean inPvPWorld = Utilities.inPvPWorld(event.getEntity());

			// Scores
			try {
				if (this.getPlugin().configValues.scores) {
					if (inPvPWorld) {
						final Player dead = event.getEntity();
						final Player killer = dead.getKiller();
						if (killer != null && !dead.getUniqueId().equals(killer.getUniqueId())) {
							final UUID killerUUID = killer.getUniqueId();
							int newScore = 0;
							try {
								if (!this.getPlugin().playerScores.containsKey(killerUUID))
									this.getPlugin().playerScores.put(killerUUID, 0);
								int currentScore = (Integer) this.getPlugin().playerScores.get(killerUUID);
								newScore = currentScore + this.getPlugin().configValues.scoreIncrement;
								if (newScore > this.getPlugin().configValues.maxScore)
									newScore = this.getPlugin().configValues.maxScore;
								this.getPlugin().playerScores.put(killerUUID, newScore);
								this.getPlugin().getScoresConfig().set("Scores." + killerUUID.toString(), (long) newScore);
								this.getPlugin().saveScoresConfig();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							try {
								int finalScore = newScore;

								List<String> unlockedKits = this.getPlugin().getUnlockedKitsConfig().isList(killerUUID.toString()) ? this.getPlugin().getUnlockedKitsConfig().getStringList(killerUUID.toString()) : new ArrayList<String>();
								List<String> kitsToAdd = new ArrayList<>();
								for (Kit kit : this.getPlugin().kitList.values()) {
									if (kit != null) {
										if (kit.canBeUnlocked()) {
											if (kit.getUnlockScore() <= finalScore) {
												if (Vault.hasVault()) {
													try {
														net.milkbowl.vault.permission.Permission permission = (net.milkbowl.vault.permission.Permission) Vault.getPermissions();
														if (!permission.has(dead, "kingkits.kits." + kit.getRealName().toLowerCase())) {
															permission.playerAdd(dead, "kingkits.kits." + kit.getRealName().toLowerCase());
															kitsToAdd.add(kit.getRealName());
															if (this.getPlugin().configValues.decreaseScoreOnAutoUnlock)
																finalScore -= kit.getUnlockScore();
															Lang.sendMessage(killer, Lang.KIT_UNLOCK, kit.getName());
														}
													} catch (NullPointerException ex) {
														System.err.println("Could not give " + killer.getName() + " the kit '" + kit.getRealName() + " because Vault permissions could not be found.");
													} catch (Exception ex) {
														ex.printStackTrace();
													}
												}
											}
										}
									}
								}
								if (!kitsToAdd.isEmpty()) {
									unlockedKits.addAll(kitsToAdd);
									this.getPlugin().getUnlockedKitsConfig().set(dead.getUniqueId().toString(), unlockedKits);
									this.getPlugin().saveUnlockedKitsConfig();
								}
								if (newScore != finalScore) {
									newScore = finalScore;
									this.getPlugin().playerScores.put(killerUUID, newScore);
									this.getPlugin().getScoresConfig().set("Scores." + killerUUID.toString(), (long) newScore);
									this.getPlugin().saveScoresConfig();
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							try {
								if (KingKitsSQL.sqlEnabled) {
									final int kScore = newScore;
									killer.getServer().getScheduler().runTaskAsynchronously(this.getPlugin(), new Runnable() {
										@Override
										public void run() {
											KingKitsSQL.setScore(killerUUID, kScore);
										}
									});
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (this.getPlugin().configValues.decreaseScoreOnDeath) {
							try {
								if (!this.getPlugin().playerScores.containsKey(killer.getUniqueId()))
									this.getPlugin().playerScores.put(killer.getUniqueId(), 0);
								int currentScore = (Integer) this.getPlugin().playerScores.get(killer.getUniqueId());
								int newScore = currentScore - this.getPlugin().configValues.scoreIncrement;
								if (newScore > this.getPlugin().configValues.maxScore)
									newScore = this.getPlugin().configValues.maxScore;
								else if (newScore < 0) newScore = 0;
								this.getPlugin().playerScores.put(killer.getUniqueId(), newScore);
								this.getPlugin().getScoresConfig().set("Scores." + killer.getUniqueId().toString(), newScore);
								this.getPlugin().saveScoresConfig();

								if (KingKitsSQL.sqlEnabled) {
									final int pScore = newScore;
									final UUID playerUUID = dead.getUniqueId();
									killer.getServer().getScheduler().runTaskAsynchronously(this.getPlugin(), new Runnable() {
										@Override
										public void run() {
											KingKitsSQL.setScore(playerUUID, pScore);
										}
									});
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							try {
								int currentScore = this.getPlugin().playerScores.containsKey(dead.getUniqueId()) ? (Integer) this.getPlugin().playerScores.get(dead.getUniqueId()) : 0;

								List<String> unlockedKits = this.getPlugin().getUnlockedKitsConfig().isList(dead.getUniqueId().toString()) ? this.getPlugin().getUnlockedKitsConfig().getStringList(dead.getUniqueId().toString()) : new ArrayList<String>();
								List<String> kitsToRemove = new ArrayList<>();
								for (String strUnlockedKit : unlockedKits) {
									Kit unlockedKit = KingKitsAPI.getKitByName(strUnlockedKit, false);
									if (unlockedKit != null) {
										if (unlockedKit.canBeUnlocked()) {
											if (unlockedKit.getUnlockScore() > currentScore) {
												kitsToRemove.add(strUnlockedKit);
												if (Vault.hasVault()) {
													try {
														net.milkbowl.vault.permission.Permission permission = (net.milkbowl.vault.permission.Permission) Vault.getPermissions();
														if (permission.has(dead, "kingkits.kits." + unlockedKit.getRealName().toLowerCase()))
															permission.playerRemove(dead, "kingkits.kits." + unlockedKit.getRealName().toLowerCase());
													} catch (Exception ex) {
														ex.printStackTrace();
													}
												}
											}
										}
									}
								}
								if (!kitsToRemove.isEmpty()) {
									unlockedKits.removeAll(kitsToRemove);
									this.getPlugin().getUnlockedKitsConfig().set(dead.getUniqueId().toString(), unlockedKits);
									this.getPlugin().saveUnlockedKitsConfig();
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Core
			try {
				Player player = event.getEntity();
				boolean hadKit = this.getPlugin().playerKits.containsKey(player.getName()) || this.getPlugin().usingKits.containsKey(player.getName());
				if (this.getPlugin().configValues.removeKitOnDeath) {
					if (this.getPlugin().playerKits.containsKey(player.getName()))
						this.getPlugin().playerKits.remove(player.getName());
					if (this.getPlugin().usingKits.containsKey(player.getName()))
						this.getPlugin().usingKits.remove(player.getName());
				}
				if (hadKit) {
					for (PotionEffect activeEffect : player.getActivePotionEffects())
						player.removePotionEffect(activeEffect.getType());

					player.setMaxHealth(20D);
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.updateInventory();
				}
				if (inPvPWorld && !this.getPlugin().configValues.dropItemsOnDeath)
					event.getDrops().clear();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Disable death messages
			try {
				if (this.getPlugin().configValues.disableDeathMessages) {
					if (inPvPWorld) event.setDeathMessage("");
				}
			} catch (Exception ignored) {
			}

			// Remove money.
			if (event.getEntity().getKiller() != null) {
				try {
					if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useMoneyPerDeath) {
						if (!event.getEntity().getUniqueId().equals(event.getEntity().getKiller().getUniqueId())) {
							if (Utilities.inPvPWorld(event.getEntity().getKiller())) {
								if (Vault.hasVault()) {
									net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) Vault.getEconomy();
									if (economy != null) {
										economy.withdrawPlayer(event.getEntity(), this.getPlugin().configValues.vaultValues.moneyPerDeath);
										event.getEntity().sendMessage(this.getPlugin().getMPDMessage(event.getEntity(), this.getPlugin().configValues.vaultValues.moneyPerDeath));
									} else {
										System.err.println("No economy plugin was found!");
									}
								} else {
									System.err.println("Vault is enabled in the KingKits configuration but is not enabled on the server!");
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			// Commands to run on death
			try {
				for (String commandToRun : this.getPlugin().configValues.commandsToRunOnDeath) {
					event.getEntity().getServer().dispatchCommand(event.getEntity().getServer().getConsoleSender(), commandToRun.replace("<player>", event.getEntity().getName()));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Killstreaks
			if (this.getPlugin().configValues.killstreaks && this.getPlugin().playerKillstreaks.containsKey(event.getEntity().getName()))
				this.getPlugin().playerKillstreaks.remove(event.getEntity().getName());
		} catch (Exception ex) {
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		try {
			// Ban dropping items
			try {
				if (event.getItemDrop() != null) {
					if (event.getPlayer().getWorld() != null) {
						if (!this.getPlugin().configValues.dropItems) {
							if (Utilities.inPvPWorld(event.getPlayer())) {
								if (this.getPlugin().configValues.opBypass) {
									if (!event.getPlayer().isOp()) {
										if (this.getPlugin().playerKits.containsKey(event.getPlayer().getName())) {
											if (this.getPlugin().configValues.dropAnimations.contains(event.getItemDrop().getItemStack().getType().getId())) {
												event.getItemDrop().remove();
											} else {
												Lang.sendMessage(event.getPlayer(), Lang.GEN_ITEM_DROP);
												event.setCancelled(true);
											}
										}
									}
								} else {
									if (this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
										if (this.getPlugin().configValues.dropAnimations.contains(event.getItemDrop().getItemStack().getType().getId())) {
											event.getItemDrop().remove();
										} else {
											Lang.sendMessage(event.getPlayer(), Lang.GEN_ITEM_DROP);
											event.setCancelled(true);
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		try {
			// Ban picking items
			try {
				if (event.getItem() != null) {
					if (event.getPlayer().getWorld() != null) {
						if (!this.getPlugin().configValues.allowPickingUpItems) {
							if (Utilities.inPvPWorld(event.getPlayer())) {
								if (this.getPlugin().configValues.opBypass) {
									if (!event.getPlayer().isOp()) {
										if (this.getPlugin().playerKits.containsKey(event.getPlayer().getName())) {
											event.setCancelled(true);
										}
									}
								} else {
									if (this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
										event.setCancelled(true);
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		try {
			// Score chat prefix
			try {
				if (this.getPlugin().configValues.scores) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						Player player = event.getPlayer();
						if (!this.getPlugin().playerScores.containsKey(player.getUniqueId())) {
							this.getPlugin().playerScores.put(player.getUniqueId(), 0);
							this.getPlugin().getScoresConfig().set("Scores." + player.getUniqueId(), 0);
							this.getPlugin().saveScoresConfig();
						}
						event.setFormat(Utilities.replaceChatColour(this.getPlugin().configValues.scoreFormat).replace("<score>", String.valueOf(this.getPlugin().playerScores.get(player.getUniqueId()))) + ChatColor.WHITE + (!this.getPlugin().configValues.scoreFormat.isEmpty() ? " " : "") + event.getFormat());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onPlayerInteractCompass(PlayerInteractEvent event) {
		try {
			if (this.getPlugin().configValues.rightClickCompass) {
				if (Utilities.inPvPWorld(event.getPlayer())) {
					if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (event.getPlayer().getInventory().getItemInHand() != null) {
							if (event.getPlayer().getInventory().getItemInHand().getType() == Material.COMPASS) {
								if (event.getPlayer().hasPermission(Permissions.COMPASS) || event.getPlayer().isOp()) {
									Player nearestPlayer = null;
									double distance = -1D;
									for (Player target : event.getPlayer().getServer().getOnlinePlayers()) {
										if (!target.getName().equalsIgnoreCase(event.getPlayer().getName())) {
											if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(target.getLocation().getWorld().getName())) {
												if (distance == -1D) {
													distance = event.getPlayer().getLocation().distanceSquared(target.getLocation());
													nearestPlayer = target;
												} else {
													double distanceSquared = event.getPlayer().getLocation().distanceSquared(target.getLocation());
													if (distanceSquared <= distance) {
														distance = distanceSquared;
														nearestPlayer = target;
													}
												}
											}
										}
									}
									if (nearestPlayer != null) {
										event.getPlayer().setCompassTarget(nearestPlayer.getLocation());
										Lang.sendMessage(event.getPlayer(), Lang.COMPASS_POINTING_PLAYER, nearestPlayer.getName());
										if (this.getPlugin().compassTargets.containsKey(event.getPlayer().getUniqueId()))
											this.getPlugin().compassTargets.remove(event.getPlayer().getUniqueId());
										this.getPlugin().compassTargets.put(event.getPlayer().getUniqueId(), nearestPlayer.getUniqueId());
									} else {
										event.getPlayer().setCompassTarget(event.getPlayer().getWorld().getSpawnLocation());
										Lang.sendMessage(event.getPlayer(), Lang.COMPASS_POINTING_SPAWN);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		try {
			// Compass tracking updater
			try {
				if (this.getPlugin().configValues.rightClickCompass) {
					if (this.getPlugin().compassTargets.containsValue(event.getPlayer().getUniqueId())) {
						Player tracker = null;
						for (Map.Entry<UUID, UUID> compassTargetsEntry : this.getPlugin().compassTargets.entrySet()) {
							Player key = Bukkit.getPlayer(compassTargetsEntry.getKey());
							Player value = Bukkit.getPlayer(compassTargetsEntry.getValue());
							if (key != null) {
								if (value != null) {
									if (key.isOnline()) {
										if (value.isOnline()) {
											if (event.getPlayer().getUniqueId().equals(value.getUniqueId()))
												tracker = key.getPlayer();
										}
									}
								}
							}
						}
						if (tracker != null) tracker.setCompassTarget(event.getPlayer().getLocation());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		try {
			// Disable block breaking
			try {
				if (this.getPlugin().configValues.banBlockBreakingAndPlacing) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						if (this.getPlugin().configValues.opBypass) {
							if (!event.getPlayer().isOp()) event.setCancelled(true);
						} else {
							event.setCancelled(true);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Disable item breaking
			try {
				if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
					String playerKit = this.getPlugin().usingKits.get(event.getPlayer().getName());
					if (playerKit != null) {
						boolean repair = false;
						if (this.getPlugin().configValues.disableItemBreaking) {
							repair = true;
						} else {
							Kit kit = KingKitsAPI.getKitByName(playerKit, false);
							if (kit != null && !kit.canItemsBreak()) {
								repair = true;
							}
						}
						if (repair) {
							final Player player = event.getPlayer();
							if (player.getItemInHand() != null && (this.isTool(player.getItemInHand().getType()) || player.getItemInHand().getType() == Material.FISHING_ROD || player.getItemInHand().getType() == Material.FLINT_AND_STEEL)) {
								player.getServer().getScheduler().runTask(this.getPlugin(), new Runnable() {
									@Override
									public void run() {
										if (player.isOnline() && player.getItemInHand() != null && (isTool(player.getItemInHand().getType()) || player.getItemInHand().getType() == Material.FISHING_ROD || player.getItemInHand().getType() == Material.FLINT_AND_STEEL)) {
											ItemStack item = player.getItemInHand();
											item.setDurability((short) 0);
											player.setItemInHand(item);
											player.updateInventory();
										}
									}
								});
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		try {
			// Disable block placing
			try {
				if (this.getPlugin().configValues.banBlockBreakingAndPlacing) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						if (this.getPlugin().configValues.opBypass) {
							if (!event.getPlayer().isOp()) event.setCancelled(true);
						} else {
							event.setCancelled(true);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		try {
			// Lock hunger bar
			try {
				if (this.getPlugin().configValues.lockHunger) {
					if (Utilities.inPvPWorld(event.getEntity()))
						event.setFoodLevel(this.getPlugin().configValues.hungerLock);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onPlayerKill(PlayerKilledEvent event) {
		try {
			// Give killer money
			try {
				Player killer = event.getPlayer();
				if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useMoneyPerKill) {
					if (Utilities.inPvPWorld(killer)) {
						if (Vault.hasVault()) {
							net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) Vault.getEconomy();
							if (economy != null) {
								if (!economy.hasAccount(killer)) economy.createPlayerAccount(killer);
								economy.depositPlayer(killer, this.getPlugin().configValues.vaultValues.moneyPerKill);
								killer.sendMessage(this.getPlugin().getMPKMessage(event.getDead(), this.getPlugin().configValues.vaultValues.moneyPerKill));
							} else {
								System.err.println("No economy plugin was found!");
							}
						} else {
							System.err.println("Vault is enabled in the KingKits configuration but is not enabled on the server!");
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Update their killstreak
			try {
				if (this.getPlugin().configValues.killstreaks) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						if (!this.getPlugin().playerKillstreaks.containsKey(event.getPlayer().getName()))
							this.getPlugin().playerKillstreaks.put(event.getPlayer().getName(), 0L);

						long currentKillstreak = this.getPlugin().playerKillstreaks.get(event.getPlayer().getName());
						if (currentKillstreak + 1L > Long.MAX_VALUE - 1)
							this.getPlugin().playerKillstreaks.put(event.getPlayer().getName(), 0L);
						else
							this.getPlugin().playerKillstreaks.put(event.getPlayer().getName(), this.getPlugin().playerKillstreaks.get(event.getPlayer().getName()) + 1L);

						currentKillstreak = this.getPlugin().playerKillstreaks.get(event.getPlayer().getName());
						if (this.getPlugin().getKillstreaksConfig().contains("Killstreak " + currentKillstreak)) {
							List<String> killstreakCommands = this.getPlugin().getKillstreaksConfig().getStringList("Killstreak " + currentKillstreak);
							for (String killstreakCommand : killstreakCommands)
								event.getPlayer().getServer().dispatchCommand(event.getPlayer().getServer().getConsoleSender(), killstreakCommand.replace("<player>", event.getPlayer().getName()).replace("<displayname>", event.getPlayer().getDisplayName()).replace("<killstreak>", "" + currentKillstreak));
						}
						if (KingKitsAPI.hasKit(event.getPlayer())) {
							Kit playerKit = KingKitsAPI.getKitByName(KingKitsAPI.getKit(event.getPlayer()), false);
							if (playerKit != null) {
								if (playerKit.getKillstreaks().containsKey(currentKillstreak)) {
									List<String> killstreakCommands = playerKit.getKillstreaks().get(currentKillstreak);
									for (String killstreakCommand : killstreakCommands)
										event.getPlayer().getServer().dispatchCommand(event.getPlayer().getServer().getConsoleSender(), killstreakCommand.replace("<player>", event.getPlayer().getName()).replace("<displayname>", event.getPlayer().getDisplayName()).replace("<killstreak>", "" + currentKillstreak));
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		try {
			// Remove kit
			try {
				if (!Utilities.inPvPWorld(event.getPlayer())) {
					if (this.getPlugin().playerKits.containsKey(event.getPlayer().getName()))
						this.getPlugin().playerKits.remove(event.getPlayer().getName());
					if (this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
						this.getPlugin().usingKits.remove(event.getPlayer().getName());
						event.getPlayer().setMaxHealth(20D);
						if (!this.getPlugin().configValues.multiInvs && !this.getPlugin().getServer().getPluginManager().isPluginEnabled(this.getPlugin().configValues.multiInvsPlugin)) {
							event.getPlayer().getInventory().clear();
							event.getPlayer().getInventory().setArmorContents(null);
							event.getPlayer().updateInventory();

							for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects())
								event.getPlayer().removePotionEffect(potionEffectOnPlayer.getType());
						}
					}
				} else if (this.getPlugin().configValues.pvpWorlds.contains("All") || (!this.getPlugin().configValues.pvpWorlds.contains(event.getFrom().getName()) && this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName()))) {
					if (!this.getPlugin().configValues.multiInvs && !this.getPlugin().getServer().getPluginManager().isPluginEnabled(this.getPlugin().configValues.multiInvsPlugin)) {
						event.getPlayer().getInventory().clear();
						event.getPlayer().getInventory().setArmorContents(null);

						event.getPlayer().updateInventory();
						for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects())
							event.getPlayer().removePotionEffect(potionEffectOnPlayer.getType());
					}
					if (this.getPlugin().configValues.listKitsOnJoin) this.listKitsOnJoin(event.getPlayer());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChangeGamemode(PlayerGameModeChangeEvent event) {
		try {
			// Disable gamemode changing
			try {
				if (this.getPlugin().configValues.disableGamemode) {
					if (event.getNewGameMode() == GameMode.CREATIVE) {
						if (!(this.getPlugin().configValues.opBypass && event.getPlayer().isOp())) {
							if (Utilities.inPvPWorld(event.getPlayer()))
								event.setCancelled(true);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		try {
			// Weapons unbreakable
			try {
				if (event.getDamager() instanceof Player) {
					final Player player = (Player) event.getDamager();
					if (player.getGameMode() == GameMode.SURVIVAL) {
						if (player.getItemInHand() != null && this.getPlugin().usingKits.containsKey(player.getName())) {
							boolean repair = false;
							if (this.isTool(player.getItemInHand().getType()) || player.getItemInHand().getType() == Material.FISHING_ROD || player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
								if (this.getPlugin().configValues.disableItemBreaking) {
									repair = true;
								} else {
									Kit kit = KingKitsAPI.getKitByName(this.getPlugin().usingKits.get(player.getName()), player.getUniqueId());
									repair = kit != null && !kit.canItemsBreak();
								}
							}
							if (repair) {
								player.getServer().getScheduler().runTask(this.getPlugin(), new Runnable() {
									@Override
									public void run() {
										if (player.isOnline() && getPlugin().usingKits.containsKey(player.getName()) && player.getItemInHand() != null && (isTool(player.getItemInHand().getType()) || player.getItemInHand().getType() == Material.FISHING_ROD || player.getItemInHand().getType() == Material.FLINT_AND_STEEL)) {
											ItemStack item = player.getItemInHand();
											item.setDurability((short) 0);
											player.setItemInHand(item);
											player.updateInventory();
										}
									}
								});
							}
						}
					}
				}
				if (event.getEntity() instanceof Player) {
					Player player = (Player) event.getEntity();
					if (player.getGameMode() == GameMode.SURVIVAL) {
						if (this.getPlugin().usingKits.containsKey(player.getName())) {
							boolean repair = false;
							if (this.getPlugin().configValues.disableItemBreaking) {
								repair = true;
							} else {
								Kit kit = KingKitsAPI.getKitByName(this.getPlugin().usingKits.get(player.getName()), player.getUniqueId());
								repair = kit != null && !kit.canItemsBreak();
							}
							if (repair) {
								ItemStack[] armour = player.getInventory().getArmorContents();
								for (ItemStack i : armour)
									if (i != null && this.isArmour(i.getType())) i.setDurability((short) 0);
								player.getInventory().setArmorContents(armour);
								player.updateInventory();
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBowShoot(EntityShootBowEvent event) {
		try {
			// Unbreakable bow
			try {
				if (event.getEntity() instanceof Player) {
					Player player = (Player) event.getEntity();
					if (this.getPlugin().usingKits.containsKey(player.getName())) {
						boolean repair = false;
						if (this.getPlugin().configValues.disableItemBreaking) {
							repair = true;
						} else {
							Kit kit = KingKitsAPI.getKitByName(this.getPlugin().usingKits.get(player.getName()), player.getUniqueId());
							repair = kit != null && !kit.canItemsBreak();
						}
						if (repair) event.getBow().setDurability((short) 0);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRunCommand(PlayerCommandPreprocessEvent event) {
		try {
			// Kit command alias
			try {
				if (!event.isCancelled() && Utilities.inPvPWorld(event.getPlayer())) {
					String strCommand = event.getMessage().contains(" ") ? event.getMessage().split(" ")[0].substring(1) : event.getMessage().substring(1);
					Kit targetKit = KingKitsAPI.getKitByName(strCommand, true);
					if (targetKit != null && !targetKit.isUserKit() && targetKit.hasAlias()) {
						event.setCancelled(true);
						event.getPlayer().performCommand("pvpkit " + targetKit.getName());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		try {
			// Give items back to a player when they respawn
			try {
				if (this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
					Kit playerKit = KingKitsAPI.getKitByName(this.getPlugin().usingKits.get(event.getPlayer().getName()), event.getPlayer().getUniqueId());
					boolean removeKit = true;
					if (playerKit != null && Utilities.inPvPWorld(event.getRespawnLocation()))
						removeKit = !SetKit.setKit(event.getPlayer(), playerKit.getRealName(), false, false);
					if (removeKit) {
						this.getPlugin().usingKits.remove(event.getPlayer().getName());
						this.getPlugin().playerKits.remove(event.getPlayer().getName());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// Show GUI on respawn
			try {
				if (this.getPlugin().configValues.guiOnRespawn && !this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
					if (Utilities.inPvPWorld(event.getPlayer())) {
						final Player player = event.getPlayer();
						player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
							@Override
							public void run() {
								if (player.isOnline() && !GuiKingKits.guiKitMenuMap.containsKey(player.getName()) && !GuiKingKits.guiPreviewKitMap.containsKey(player.getName()) && Utilities.inPvPWorld(player))
									KingKitsAPI.showKitMenu(player, 1, true);
							}
						}, 5L);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Gets the plugin instance *
	 */
	private KingKits getPlugin() {
		return KingKits.getInstance();
	}

	private void listKitsOnJoin(final Player p) {
		p.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
			public void run() {
				if (p != null && p.isOnline()) {
					List<String> kitList = getPlugin().getKitList();
					if (getPlugin().configValues.sortAlphabetically)
						Collections.sort(kitList, Utilities.ALPHANUMERICAL_ORDER);
					StringBuilder sbKits = new StringBuilder();
					for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
						String kit = kitList.get(kitPos);
						ChatColor col = ChatColor.GREEN;
						boolean ignoreKit = false;
						if (!p.hasPermission("kingkits.kits." + kit.toLowerCase())) {
							if (!getPlugin().configValues.kitListPermissionsJoin) ignoreKit = true;
							else col = ChatColor.DARK_RED;
						}
						if (!ignoreKit) {
							if (kitPos == kitList.size() - 1) sbKits.append(col).append(kit);
							else sbKits.append(col).append(kit).append(", ");
						} else {
							if (kitPos == kitList.size() - 1)
								sbKits = new StringBuilder().append(replaceLast(sbKits.toString(), ",", ""));
						}
					}
					if (sbKits.toString().trim().isEmpty())
						sbKits = new StringBuilder().append(Lang.GEN_NO_KITS_AVAILABLE.getMessage());
					Lang.sendMessage(p, Lang.GEN_KIT_LIST, sbKits.toString());
				}
			}
		}, 25L);
	}

	/**
	 * Returns if a material is an armour piece
	 */
	private boolean isArmour(Material material) {
		return material.name().endsWith("HELMET") || material.name().endsWith("CHESTPLATE") || material.name().endsWith("LEGGINGS") || material.name().endsWith("BOOTS");
	}

	/**
	 * Returns if a material is a tool/sword *
	 */
	private boolean isTool(Material material) {
		return material.name().endsWith("SWORD") || material.name().endsWith("PICKAXE") || material.name().endsWith("AXE") || material.name().endsWith("SPADE") || material.name().endsWith("SHOVEL") || material.name().endsWith("HOE");
	}

	/**
	 * Replaces the last occurrence of a string in a string *
	 */
	private String replaceLast(String text, String original, String replacement) {
		String message = text;
		if (message.contains(original)) {
			StringBuilder stringBuilder = new StringBuilder(text);
			stringBuilder.replace(text.lastIndexOf(original), text.lastIndexOf(original) + 1, replacement);
			message = stringBuilder.toString();
		}
		return message;
	}

	/**
	 * Returns a string with the real colours *
	 */
	private String r(String message) {
		return Utilities.replaceChatColour(message);
	}

}
