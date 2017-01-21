package com.faris.kingkits.listener;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.*;
import com.faris.kingkits.helper.util.*;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.storage.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EventListener implements Listener {

	private final KingKits plugin;

	private Map<UUID, Integer> joinTasks = new HashMap<>();
	private Map<UUID, Integer> updateInventoryTasks = new HashMap<>();

	public EventListener(KingKits pluginInstance) {
		this.plugin = pluginInstance;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		try {
			if (!this.plugin.allowJoining()) {
				event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
				event.setKickMessage(ChatColor.RED + "Sorry! Uploading player data..." + System.lineSeparator() + "Please wait patiently.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		try {
			this.handleJoinEvent(event.getPlayer());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		try {
			final Player player = event.getPlayer();
			final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);

			if (this.joinTasks.containsKey(player.getUniqueId())) {
				BukkitUtilities.cancelTask(this.joinTasks.remove(player.getUniqueId()));
			}
			if (this.updateInventoryTasks.containsKey(player.getUniqueId())) {
				BukkitUtilities.cancelTask(this.updateInventoryTasks.remove(player.getUniqueId()));
			}

			boolean inPvPWorld = Utilities.isPvPWorld(player.getWorld()) || (kitPlayer != null && kitPlayer.hasKit());
			try {
				if (inPvPWorld && ConfigController.getInstance().shouldRemoveItemsOnLeave(player.getWorld())) {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
				}
				if (inPvPWorld && ConfigController.getInstance().shouldRemovePotionEffectsOnLeave(player.getWorld())) {
					for (PotionEffect activePotionEffect : player.getActivePotionEffects())
						player.removePotionEffect(activePotionEffect.getType());
				}
				if (kitPlayer != null && kitPlayer.hasKit()) {
					if (player.getHealth() > PlayerUtilities.getDefaultMaxHealth())
						player.setHealth(PlayerUtilities.getDefaultMaxHealth());
					if (ConfigController.getInstance().shouldSetMaxHealth())
						player.setMaxHealth(PlayerUtilities.getDefaultMaxHealth());
					if (kitPlayer.getKit().getWalkSpeed() != PlayerUtilities.getDefaultWalkSpeed())
						player.setWalkSpeed(PlayerUtilities.getDefaultWalkSpeed());
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to reset " + player.getName() + (player.getName().endsWith("s") ? "'" : "'s") + " inventory and/or potion effects.", ex);
			}

			CompassController.getInstance().removeTarget(player.getUniqueId());
			CompassController.getInstance().removeTargeter(player.getUniqueId());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeaveHighest(PlayerQuitEvent event) {
		try {
			final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(event.getPlayer());
			if (kitPlayer != null) {
				if (kitPlayer.getLoadTaskID() != -1) BukkitUtilities.cancelTask(kitPlayer.getLoadTaskID());
				kitPlayer.onLeave();
				if (kitPlayer.isLoaded()) PlayerController.getInstance().savePlayer(kitPlayer);
				PlayerController.getInstance().unregisterPlayer(event.getPlayer());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		try {
			final Player player = event.getPlayer();
			final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);

			try {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (event.getItem() != null) {
						if (event.getItem().getType() == ConfigController.getInstance().getGuiItemType() && (ConfigController.getInstance().getGuiItemData() == -1 || event.getItem().getDurability() == ConfigController.getInstance().getGuiItemData())) {
							if (Utilities.isPvPWorld(player.getWorld())) {
								GuiController.getInstance().openKitsMenu(player);
								event.setCancelled(true);
								return;
							}
						} else if (event.getItem().getType() == Material.MUSHROOM_SOUP) {
							if (ConfigController.getInstance().canQuickSoup(player.getWorld())) {
								if (player.hasPermission(Permissions.SOUP_QUICKSOUP)) {
									if (Utilities.isPvPWorld(player.getWorld())) {
										int soupAmount = event.getItem().getAmount();
										if (soupAmount > 0) {
											boolean valid = true;
											if (player.getHealth() < player.getMaxHealth()) {
												player.setHealth(Math.min(player.getHealth() + ConfigController.getInstance().getQuickSoupHeal(), player.getMaxHealth()));
											} else if (player.getFoodLevel() < 20) {
												player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
											} else {
												valid = false;
											}
											if (valid) {
												event.setCancelled(true);
												if (soupAmount == 1) {
													if (event.getHand() == EquipmentSlot.HAND)
														player.getInventory().setItemInMainHand(new ItemStack(Material.BOWL));
													else if (event.getHand() == EquipmentSlot.OFF_HAND)
														player.getInventory().setItemInOffHand(new ItemStack(Material.BOWL));
												} else {
													ItemStack newItem = event.getItem();
													newItem.setAmount(soupAmount - 1);
													if (event.getHand() == EquipmentSlot.HAND)
														player.getInventory().setItemInMainHand(newItem);
													else if (event.getHand() == EquipmentSlot.OFF_HAND)
														player.getInventory().setItemInOffHand(newItem);
													player.getInventory().addItem(new ItemStack(Material.BOWL));
												}
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to execute the item detection code.", ex);
			}

			try {
				if (event.getItem() != null) {
					if (Utilities.isPvPWorld(player.getWorld())) {
						if (kitPlayer != null && kitPlayer.hasKit() && !kitPlayer.getKit().canItemsBreak()) {
							if (ItemUtilities.getDamageableMaterials().contains(event.getItem().getType())) {
								event.getItem().setDurability((short) 0);
							}
						}
					}
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to repair an unbreakable item.", ex);
			}

			try {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (Utilities.isPvPWorld(player.getWorld())) {
						if (event.getClickedBlock().getState() instanceof Sign) {
							Sign clickedSign = (Sign) event.getClickedBlock().getState();
							String firstLine = clickedSign.getLine(0);

							if (firstLine.equals(ConfigController.getInstance().getSignsKit(player.getWorld())[1])) {
								event.setCancelled(true);
								if (player.hasPermission(Permissions.SIGN_KIT_USE)) {
									String strKit = ChatUtilities.stripColour(clickedSign.getLine(1));

									Kit kit = null;
									KitUtilities.KitSearchResult kitResult = clickedSign.getLine(3).equalsIgnoreCase("User") ? KitUtilities.getKits(strKit, kitPlayer) : KitUtilities.getKits(strKit);
									if (kitResult.hasKit()) {
										kit = kitResult.getKit();
									} else if (kitResult.hasOtherKits()) {
										if (kitResult.getOtherKits().size() == 1) {
											kit = kitResult.getOtherKits().get(0);
										} else {
											Messages.sendMessage(player, Messages.KIT_MULTIPLE_FOUND, strKit);
										}
									} else {
										Messages.sendMessage(player, Messages.KIT_NOT_FOUND, strKit);
									}
									if (kit != null) KitUtilities.setKit(player, kit);
								} else {
									Messages.sendMessage(player, Messages.SIGN_USE_NO_PERMISSION, "kit");
								}
								return;
							} else if (firstLine.equals(ConfigController.getInstance().getSignsKitList(player.getWorld())[1])) {
								event.setCancelled(true);
								if (player.hasPermission(Permissions.SIGN_KIT_LIST_USE)) {
									KitUtilities.listKits(player);
								} else {
									Messages.sendMessage(player, Messages.SIGN_USE_NO_PERMISSION, "kit list");
								}
								return;
							} else if (firstLine.equals(ConfigController.getInstance().getSignsRefill(player.getWorld())[1])) {
								event.setCancelled(true);
								if (player.hasPermission(Permissions.SIGN_REFILL_USE)) {
									String strType = clickedSign.getLine(1);
									if (strType != null && strType.equalsIgnoreCase("All")) {
										player.performCommand("refill all");
									} else {
										player.performCommand("refill");
									}
								} else {
									Messages.sendMessage(player, Messages.SIGN_USE_NO_PERMISSION, "refill");
								}
								return;
							}
						}
					}
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to check block for any valid signs.", ex);
			}

			try {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (event.getItem() != null) {
						if (event.getItem().getType() == Material.COMPASS) {
							if (ConfigController.getInstance().shouldSetCompassToNearestPlayer(player.getWorld())) {
								if (player.hasPermission(Permissions.COMPASS)) {
									if (Utilities.isPvPWorld(player.getWorld())) {
										Player nearestPlayer = null;
										double distance = -1D;
										for (Player target : player.getWorld().getPlayers()) {
											if (!target.getUniqueId().equals(player.getUniqueId())) {
												if (distance == -1D) {
													distance = player.getLocation().distanceSquared(target.getLocation());
													nearestPlayer = target;
												} else {
													double distanceSquared = player.getLocation().distanceSquared(target.getLocation());
													if (distanceSquared <= distance) {
														distance = distanceSquared;
														nearestPlayer = target;
													}
												}
											}
										}
										if (nearestPlayer != null) {
											player.setCompassTarget(nearestPlayer.getLocation());
											Messages.sendMessage(player, Messages.COMPASS_POINTING_PLAYER, nearestPlayer.getName());
											CompassController.getInstance().setTarget(player.getUniqueId(), nearestPlayer.getUniqueId());
										} else {
											player.setCompassTarget(player.getWorld().getSpawnLocation());
											Messages.sendMessage(player, Messages.COMPASS_POINTING_SPAWN);
											CompassController.getInstance().removeTarget(player.getUniqueId());
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to execute the compass code.", ex);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		try {
			final Player player = event.getEntity();
			final Player killer = event.getEntity().getKiller();
			final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
			if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return;

			boolean deathInPvPWorld = Utilities.isPvPWorld(player.getWorld());
			if (deathInPvPWorld || kitPlayer.hasKit()) {
				if (!ConfigController.getInstance().shouldShowDeathMessages(player.getWorld()))
					event.setDeathMessage("");
				if (!ConfigController.getInstance().shouldDropItemsOnDeath(player.getWorld())) event.getDrops().clear();
				if (ConfigController.getInstance().shouldRemoveKitOnDeath()) {
					kitPlayer.setKit(null);

					for (PotionEffect activeEffect : player.getActivePotionEffects())
						player.removePotionEffect(activeEffect.getType());

					if (player.getHealth() > PlayerUtilities.getDefaultMaxHealth())
						player.setHealth(PlayerUtilities.getDefaultMaxHealth());
					if (ConfigController.getInstance().shouldSetMaxHealth())
						player.setMaxHealth(PlayerUtilities.getDefaultMaxHealth());
					player.setWalkSpeed(PlayerUtilities.getDefaultWalkSpeed());
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.updateInventory();
				} else {
					event.setKeepInventory(true);
				}
				kitPlayer.onDeath();
				if (killer != null && !player.getUniqueId().equals(killer.getUniqueId())) {
					kitPlayer.setScore(Math.min(Math.max(kitPlayer.getScore() + ConfigController.getInstance().getScorePerDeath(), 0), ConfigController.getInstance().getMaxScore()));
					final KitPlayer killerPlayer = PlayerController.getInstance().getPlayer(killer);
					if (killerPlayer != null) {
						killerPlayer.setScore(Math.min(Math.max(killerPlayer.getScore() + ConfigController.getInstance().getScorePerKill(), 0), ConfigController.getInstance().getMaxScore()));
						try {
							if (killerPlayer.getKit() != null) {
								killerPlayer.incrementKillstreak();
								if (killerPlayer.getKit().getKillstreaks().containsKey(killerPlayer.getKillstreak())) {
									List<String> killstreakCmds = killerPlayer.getKit().getKillstreaks().get(killerPlayer.getKillstreak());
									if (killstreakCmds != null) {
										for (String killstreakCmd : killstreakCmds) {
											try {
												killer.getServer().dispatchCommand(killer.getServer().getConsoleSender(), killstreakCmd.replace("<player>", killer.getName()).replace("<username>", killer.getName()).replace("<name>", killer.getName()).replace("<killstreak>", String.valueOf(killerPlayer.getKillstreak())));
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										}
									}
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						try {
							for (Kit kit : KitController.getInstance().getKits().values()) {
								if (kit.getAutoUnlockScore() != -1) {
									if (kit.getAutoUnlockScore() <= killerPlayer.getScore()) {
										try {
											if (!killerPlayer.hasUnlocked(kit) && !killer.hasPermission("kingkits.kits." + kit.getName().toLowerCase())) {
												killerPlayer.setUnlockedKit(kit.getName(), true);
												if (ConfigController.getInstance().shouldDecreaseScoreOnAutoUnlock()) {
													killerPlayer.setScore(Math.max(killerPlayer.getScore() - kit.getAutoUnlockScore(), 0));
												}
												Messages.sendMessage(killer, Messages.KIT_UNLOCK, kit.getName());
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
					}
				}
				for (String commandToRun : ConfigController.getInstance().getCommandsToRunOnDeath()) {
					try {
						event.getEntity().getServer().dispatchCommand(event.getEntity().getServer().getConsoleSender(), commandToRun.replace("<player>", player.getName()).replace("<killer>", killer != null ? killer.getName() : ""));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (ConfigController.getInstance().isEconomyEnabled(player.getWorld())) {
					if (ConfigController.getInstance().getMoneyPerDeath(player.getWorld()) != 0D) {
						PlayerUtilities.incrementMoney(player, -ConfigController.getInstance().getMoneyPerDeath(player.getWorld()));
						Messages.sendMessage(player, Messages.ECONOMY_MONEY_PER_DEATH, ConfigController.getInstance().getMoneyPerDeath(player.getWorld()), killer != null ? killer.getName() : "unknown");
					}
					if (killer != null && !player.getUniqueId().equals(killer.getUniqueId()) && ConfigController.getInstance().getMoneyPerKill(killer.getWorld()) != 0D) {
						PlayerUtilities.incrementMoney(killer, ConfigController.getInstance().getMoneyPerKill(killer.getWorld()));
						Messages.sendMessage(killer, Messages.ECONOMY_MONEY_PER_KILL, ConfigController.getInstance().getMoneyPerKill(killer.getWorld()), player.getName());
					}
				}
				if (ConfigController.getInstance().shouldAutoRespawn()) {
					player.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
						@Override
						public void run() {
							if (player.isOnline() && player.isDead()) {
								try {
									PlayerUtilities.respawnPlayer(player);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						}
					}, 1L);
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle " + event.getEntity().getName() + "'s death.", ex);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		try {
			final Player player = event.getPlayer();
			boolean deathInPvPWorld = Utilities.isPvPWorld(event.getRespawnLocation().getWorld());
			if (deathInPvPWorld) {
				KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
				if (kitPlayer != null) {
					boolean showGUI = true;
					if (!ConfigController.getInstance().shouldRemoveKitOnDeath()) {
						if (kitPlayer.getKit() != null) showGUI = false;
					}
					if (showGUI && ConfigController.getInstance().shouldShowGuiOnRespawn()) {
						kitPlayer.setKit(null);
						player.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
							@Override
							public void run() {
								if (player.isOnline() && Utilities.isPvPWorld(player.getWorld())) {
									GuiController.getInstance().openKitsMenu(player);
								}
							}
						}, 5L);
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle " + event.getPlayer().getName() + "'s respawn.", ex);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		try {
			final Player player = event.getPlayer();
			boolean inPvPWorld = Utilities.isPvPWorld(player.getWorld());
			KitPlayer kitPlayer = null;
			if (ConfigController.getInstance().isScoreEnabled()) {
				if (inPvPWorld) {
					kitPlayer = PlayerController.getInstance().getPlayer(player);
					event.setFormat(ChatUtilities.replaceChatCodes(String.format(ConfigController.getInstance().getScoreChatPrefix(), kitPlayer != null ? kitPlayer.getScore() : 0)) + ChatColor.WHITE + event.getFormat());
				}
			}
			String chatFormat = event.getFormat();
			if (chatFormat.contains("{kingkits_")) {
				if (kitPlayer == null) kitPlayer = PlayerController.getInstance().getPlayer(player);
				chatFormat = chatFormat.replace("{kingkits_score}", String.valueOf(kitPlayer != null ? kitPlayer.getScore() : 0));
				chatFormat = chatFormat.replace("{kingkits_kit}", ChatUtilities.replaceChatCodes(String.valueOf(kitPlayer != null && kitPlayer.getKit() != null ? ConfigController.getInstance().getChatPlaceholder("Has kit text", "").replace("<kit>", kitPlayer.getKit().getName()) : ConfigController.getInstance().getChatPlaceholder("No kit text", ""))));
				chatFormat = chatFormat.replace("{kingkits_killstreak}", String.valueOf(kitPlayer != null ? kitPlayer.getKillstreak() : 0));
				event.setFormat(chatFormat);
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to modify the chat format.", ex);
		}
	}

	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		try {
			final Player player = event.getPlayer();
			if (!ConfigController.getInstance().canModifyBlocks(event.getBlock().getWorld())) {
				if (Utilities.isPvPWorld(player.getWorld())) {
					if (!ConfigController.getInstance().canAdminsBypass() || !player.hasPermission(Permissions.ADMIN)) {
						event.setCancelled(true);
						Messages.sendMessage(player, Messages.EVENT_BLOCK_BREAK);
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle a block break at (" + event.getBlock().getLocation().toString() + ").", ex);
		}
	}

	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		try {
			final Player player = event.getPlayer();
			if (!ConfigController.getInstance().canModifyBlocks(event.getBlock().getWorld())) {
				if (Utilities.isPvPWorld(player.getWorld())) {
					if (!ConfigController.getInstance().canAdminsBypass() || !player.hasPermission(Permissions.ADMIN)) {
						event.setCancelled(true);
						Messages.sendMessage(player, Messages.EVENT_BLOCK_PLACE);
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle a block place at (" + event.getBlock().getLocation().toString() + ").", ex);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		try {
			final Player player = event.getPlayer();
			final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
			if (Utilities.isPvPWorld(player.getWorld()) || (kitPlayer != null && kitPlayer.hasKit())) {
				if (!ConfigController.getInstance().canDropItems(player.getWorld()) && (!player.hasPermission(Permissions.ADMIN) || ConfigController.getInstance().canAdminsBypass())) {
					if (!ConfigController.getInstance().getDropAnimationItems(player.getWorld()).contains(event.getItemDrop().getItemStack().getTypeId())) {
						event.setCancelled(true);
						final UUID playerUUID = player.getUniqueId();
						if (this.updateInventoryTasks.containsKey(playerUUID)) {
							BukkitUtilities.cancelTask(this.updateInventoryTasks.remove(playerUUID));
						}
						this.updateInventoryTasks.put(playerUUID, player.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
							@Override
							public void run() {
								updateInventoryTasks.remove(playerUUID);
								if (player.isOnline()) {
									player.updateInventory();
								}
							}
						}, 2L).getTaskId());
					} else {
						event.getItemDrop().remove();
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle an item dropping.", ex);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		try {
			final Player player = event.getPlayer();
			final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
			if (Utilities.isPvPWorld(player.getWorld()) || (kitPlayer != null && kitPlayer.hasKit())) {
				if (!ConfigController.getInstance().canPickupItems(player.getWorld()) && (!player.hasPermission(Permissions.ADMIN) || ConfigController.getInstance().canAdminsBypass())) {
					event.setCancelled(true);
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle an item being picked up.", ex);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChangeSign(SignChangeEvent event) {
		try {
			final Player player = event.getPlayer();
			if (player == null) return;
			if (Utilities.isPvPWorld(player.getWorld())) {
				final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
				if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return;

				String firstLine = event.getLine(0);
				if (firstLine.equals(ConfigController.getInstance().getSignsKit(player.getWorld())[0])) {
					if (player.hasPermission(Permissions.SIGN_KIT_CREATE)) {
						String strKit = event.getLine(1);

						Kit kit = null;
						KitUtilities.KitSearchResult kitResult = event.getLine(3).equalsIgnoreCase("User") ? KitUtilities.getKits(strKit, kitPlayer) : KitUtilities.getKits(strKit);
						if (kitResult.hasKit()) {
							kit = kitResult.getKit();
						} else if (kitResult.hasOtherKits()) {
							if (kitResult.getOtherKits().size() == 1) {
								kit = kitResult.getOtherKits().get(0);
							} else {
								Messages.sendMessage(player, Messages.KIT_MULTIPLE_FOUND, strKit);
								event.setLine(0, ConfigController.getInstance().getSignsKit(player.getWorld())[2]);
							}
						} else {
							Messages.sendMessage(player, Messages.KIT_NOT_FOUND, strKit);
							event.setLine(0, ConfigController.getInstance().getSignsKit(player.getWorld())[2]);
						}
						if (kit != null)
							event.setLine(0, ConfigController.getInstance().getSignsKit(player.getWorld())[1]);
					} else {
						Messages.sendMessage(player, Messages.SIGN_CREATE_NO_PERMISSION, "kit");
						event.setCancelled(true);
					}
				} else if (firstLine.equals(ConfigController.getInstance().getSignsKitList(player.getWorld())[0])) {
					if (player.hasPermission(Permissions.SIGN_KIT_LIST_CREATE)) {
						event.setLine(0, ConfigController.getInstance().getSignsKitList(player.getWorld())[1]);
					} else {
						Messages.sendMessage(player, Messages.SIGN_CREATE_NO_PERMISSION, "kit list");
						event.setCancelled(true);
					}
				} else if (firstLine.equals(ConfigController.getInstance().getSignsRefill(player.getWorld())[0])) {
					if (player.hasPermission(Permissions.SIGN_REFILL_CREATE)) {
						event.setLine(0, ConfigController.getInstance().getSignsRefill(player.getWorld())[1]);
					} else {
						Messages.sendMessage(player, Messages.SIGN_CREATE_NO_PERMISSION, "refill");
						event.setCancelled(true);
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle a sign change.", ex);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		try {
			final Player player = event.getPlayer();
			if (!Utilities.isPvPWorld(player.getWorld())) {
				final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
				if (kitPlayer != null) {
					if (kitPlayer.hasKit()) {
						if (player.getHealth() > PlayerUtilities.getDefaultMaxHealth())
							player.setHealth(PlayerUtilities.getDefaultMaxHealth());
						if (ConfigController.getInstance().shouldSetMaxHealth())
							player.setMaxHealth(PlayerUtilities.getDefaultMaxHealth());
					}
					kitPlayer.setKit(null);

					if (!ConfigController.getInstance().isMultiInventoriesPluginEnabled() && !player.getServer().getPluginManager().isPluginEnabled(ConfigController.getInstance().getMultiInventoriesPluginName())) {
						player.getInventory().clear();
						player.getInventory().setArmorContents(null);
						player.updateInventory();
						for (PotionEffect activePotionEffect : player.getActivePotionEffects())
							player.removePotionEffect(activePotionEffect.getType());
					}
				}
			} else if (ConfigController.getInstance().getPvPWorlds().contains("All") || !Utilities.isPvPWorld(event.getFrom())) {
				if (!ConfigController.getInstance().isMultiInventoriesPluginEnabled() && !player.getServer().getPluginManager().isPluginEnabled(ConfigController.getInstance().getMultiInventoriesPluginName())) {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.updateInventory();
					for (PotionEffect activePotionEffect : player.getActivePotionEffects())
						player.removePotionEffect(activePotionEffect.getType());
				}
				if (ConfigController.getInstance().shouldShowGuiOnJoin()) {
					player.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
						@Override
						public void run() {
							if (player.isOnline() && Utilities.isPvPWorld(player.getWorld())) {
								Runnable openMenuRunnable = new Runnable() {
									@Override
									public void run() {
										if (player.isOnline() && Utilities.isPvPWorld(player.getWorld()))
											GuiController.getInstance().openKitsMenu(player);
									}
								};
								if (player.getOpenInventory() != null) {
									player.closeInventory();
									Bukkit.getServer().getScheduler().runTask(plugin, openMenuRunnable);
								} else {
									openMenuRunnable.run();
								}
							}
						}
					}, 5L);
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle " + event.getPlayer().getName() + " changing worlds.", ex);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		try {
			final Player player = event.getPlayer();
			if (Utilities.isPvPWorld(player.getWorld())) {
				String strCommand = event.getMessage().contains(" ") ? event.getMessage().split(" ")[0].substring(1) : event.getMessage().substring(1);
				KitUtilities.KitSearchResult searchResult = KitUtilities.getKits(strCommand);
				Kit targetKit = null;
				if (searchResult.hasKit()) {
					targetKit = searchResult.getKit();
				} else if (searchResult.hasOtherKits()) {
					if (searchResult.getOtherKits().size() == 1) {
						targetKit = searchResult.getOtherKits().get(0);
					}
				}
				if (targetKit != null && targetKit.hasCommandAlias()) {
					event.setCancelled(true);
					player.performCommand("pvpkit " + targetKit.getName());
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle processing a command.", ex);
		}
	}


	@EventHandler(ignoreCancelled = true)
	public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
		try {
			if (ConfigController.getInstance().shouldLockFoodLevel(event.getEntity().getWorld())) {
				if (Utilities.isPvPWorld(event.getEntity().getWorld())) {
					event.setFoodLevel(Math.min(Math.abs(ConfigController.getInstance().getFoodLevelLock()), 20));
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle a change in food level.", ex);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChangeGamemode(PlayerGameModeChangeEvent event) {
		try {
			final Player player = event.getPlayer();
			if (ConfigController.getInstance().shouldPreventCreative()) {
				if (event.getNewGameMode() == GameMode.CREATIVE) {
					if (!ConfigController.getInstance().canAdminsBypass() || !player.hasPermission(Permissions.ADMIN)) {
						if (Utilities.isPvPWorld(player.getWorld())) {
							event.setCancelled(true);
						}
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to handle a change in gamemode.", ex);
		}
	}

	@EventHandler
	public void onPlayerShootBow(EntityShootBowEvent event) {
		try {
			if (event.getEntity() instanceof Player && event.getBow() != null) {
				Player player = (Player) event.getEntity();
				if (player.getGameMode() != GameMode.CREATIVE && Utilities.isPvPWorld(player.getWorld())) {
					KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
					if (kitPlayer != null && kitPlayer.hasKit() && !kitPlayer.getKit().canItemsBreak()) {
						event.getBow().setDurability((short) 0);
					}
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to repair an unbreakable item.", ex);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		try {
			if (!(event instanceof EntityDamageByEntityEvent) && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
				if (event.getEntity() instanceof Player) {
					final Player damaged = (Player) event.getEntity();
					if (damaged.getGameMode() == GameMode.SURVIVAL || damaged.getGameMode() == GameMode.ADVENTURE) {
						if (Utilities.isPvPWorld(damaged.getWorld())) {
							final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(damaged);
							if (kitPlayer != null && kitPlayer.hasKit() && !kitPlayer.getKit().canItemsBreak()) {
								boolean updateHelmet = false, updateChestplate = false, updateLeggings = false, updateBoots = false, updateOffhand = false;
								final ItemStack helmet = damaged.getInventory().getHelmet(), chestplate = damaged.getInventory().getChestplate(), leggings = damaged.getInventory().getLeggings(), boots = damaged.getInventory().getBoots(), offhand = damaged.getInventory().getItemInOffHand();
								if (helmet != null && ItemUtilities.getDamageableMaterials().contains(helmet.getType())) {
									helmet.setDurability((short) 0);
									updateHelmet = true;
								}
								if (chestplate != null && ItemUtilities.getDamageableMaterials().contains(chestplate.getType())) {
									chestplate.setDurability((short) 0);
									updateChestplate = true;
								}
								if (leggings != null && ItemUtilities.getDamageableMaterials().contains(leggings.getType())) {
									leggings.setDurability((short) 0);
									updateLeggings = true;
								}
								if (boots != null && ItemUtilities.getDamageableMaterials().contains(boots.getType())) {
									boots.setDurability((short) 0);
									updateBoots = true;
								}
								if (offhand != null && ItemUtilities.getDamageableMaterials().contains(offhand.getType())) {
									offhand.setDurability((short) 0);
									updateOffhand = true;
								}
								if (updateHelmet || updateChestplate || updateLeggings || updateBoots || updateOffhand) {
									final boolean finalUpdateHelmet = updateHelmet, finalUpdateChestplate = updateChestplate, finalUpdateLeggings = updateLeggings, finalUpdateBoots = updateBoots, finalUpdateOffhand = updateOffhand;
									damaged.getServer().getScheduler().runTask(this.plugin, new Runnable() {
										@Override
										public void run() {
											if (damaged.isOnline() && kitPlayer.hasKit() && !kitPlayer.getKit().canItemsBreak()) {
												if (finalUpdateHelmet) damaged.getInventory().setHelmet(helmet);
												if (finalUpdateChestplate)
													damaged.getInventory().setChestplate(chestplate);
												if (finalUpdateLeggings) damaged.getInventory().setLeggings(leggings);
												if (finalUpdateBoots) damaged.getInventory().setBoots(boots);
												if (finalUpdateOffhand)
													damaged.getInventory().setItemInOffHand(offhand);
											}
										}
									});
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

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		try {
			if (event.getDamager() instanceof Player) {
				final Player damager = (Player) event.getDamager();
				if (damager.getGameMode() == GameMode.SURVIVAL || damager.getGameMode() == GameMode.ADVENTURE) {
					if (Utilities.isPvPWorld(damager.getWorld())) {
						final KitPlayer damagerKitPlayer = PlayerController.getInstance().getPlayer(damager);
						if (damagerKitPlayer != null && damagerKitPlayer.hasKit() && !damagerKitPlayer.getKit().canItemsBreak()) {
							if (damager.getInventory().getItemInMainHand() != null) {
								if (ItemUtilities.getDamageableMaterials().contains(damager.getInventory().getItemInMainHand().getType())) {
									damager.getServer().getScheduler().runTask(this.plugin, new Runnable() {
										@Override
										public void run() {
											if (damager.isOnline() && damagerKitPlayer.hasKit() && damager.getInventory().getItemInMainHand() != null && ItemUtilities.getDamageableMaterials().contains(damager.getInventory().getItemInMainHand().getType())) {
												ItemStack itemInHand = damager.getInventory().getItemInMainHand();
												itemInHand.setDurability((short) 0);
												damager.getInventory().setItemInMainHand(itemInHand);
												damager.updateInventory();
											}
										}
									});
								}
							}
							if (damager.getInventory().getItemInOffHand() != null) {
								if (ItemUtilities.getDamageableMaterials().contains(damager.getInventory().getItemInOffHand().getType())) {
									damager.getServer().getScheduler().runTask(this.plugin, new Runnable() {
										@Override
										public void run() {
											if (damager.isOnline() && damagerKitPlayer.hasKit() && damager.getInventory().getItemInOffHand() != null && ItemUtilities.getDamageableMaterials().contains(damager.getInventory().getItemInOffHand().getType())) {
												ItemStack itemInHand = damager.getInventory().getItemInOffHand();
												itemInHand.setDurability((short) 0);
												damager.getInventory().setItemInOffHand(itemInHand);
												damager.updateInventory();
											}
										}
									});
								}
							}
						}
					}
				}
			}
			if (event.getEntity() instanceof Player) {
				final Player damaged = (Player) event.getEntity();
				if (damaged.getGameMode() == GameMode.SURVIVAL || damaged.getGameMode() == GameMode.ADVENTURE) {
					if (Utilities.isPvPWorld(damaged.getWorld())) {
						final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(damaged);
						if (kitPlayer != null && kitPlayer.hasKit() && !kitPlayer.getKit().canItemsBreak()) {
							boolean updateHelmet = false, updateChestplate = false, updateLeggings = false, updateBoots = false, updateOffhand = false;
							final ItemStack helmet = damaged.getInventory().getHelmet(), chestplate = damaged.getInventory().getChestplate(), leggings = damaged.getInventory().getLeggings(), boots = damaged.getInventory().getBoots(), offhand = damaged.getInventory().getItemInOffHand();
							if (helmet != null && ItemUtilities.getDamageableMaterials().contains(helmet.getType())) {
								helmet.setDurability((short) 0);
								updateHelmet = true;
							}
							if (chestplate != null && ItemUtilities.getDamageableMaterials().contains(chestplate.getType())) {
								chestplate.setDurability((short) 0);
								updateChestplate = true;
							}
							if (leggings != null && ItemUtilities.getDamageableMaterials().contains(leggings.getType())) {
								leggings.setDurability((short) 0);
								updateLeggings = true;
							}
							if (boots != null && ItemUtilities.getDamageableMaterials().contains(boots.getType())) {
								boots.setDurability((short) 0);
								updateBoots = true;
							}
							if (offhand != null && ItemUtilities.getDamageableMaterials().contains(offhand.getType())) {
								offhand.setDurability((short) 0);
								updateOffhand = true;
							}
							if (updateHelmet || updateChestplate || updateLeggings || updateBoots || updateOffhand) {
								final boolean finalUpdateHelmet = updateHelmet, finalUpdateChestplate = updateChestplate, finalUpdateLeggings = updateLeggings, finalUpdateBoots = updateBoots, finalUpdateOffhand = updateOffhand;
								damaged.getServer().getScheduler().runTask(this.plugin, new Runnable() {
									@Override
									public void run() {
										if (damaged.isOnline() && kitPlayer.hasKit() && !kitPlayer.getKit().canItemsBreak()) {
											if (finalUpdateHelmet) damaged.getInventory().setHelmet(helmet);
											if (finalUpdateChestplate) damaged.getInventory().setChestplate(chestplate);
											if (finalUpdateLeggings) damaged.getInventory().setLeggings(leggings);
											if (finalUpdateBoots) damaged.getInventory().setBoots(boots);
											if (finalUpdateOffhand) damaged.getInventory().setItemInOffHand(offhand);
										}
									}
								});
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void handleJoinEvent(final Player player) throws Exception {
		this.handleJoinEvent(player, false);
	}

	public void handleJoinEvent(final Player player, final boolean onlyReload) throws Exception {
		final KitPlayer kitPlayer = PlayerController.getInstance().registerPlayer(player);

		final long currentTime = System.currentTimeMillis();
		final Runnable joinTask = new Runnable() {
			@Override
			public void run() {
				if (player.isOnline()) {
					try {
						if (ConfigController.getInstance().shouldShowGuiOnJoin() && Utilities.isPvPWorld(player.getWorld())) {
							EventListener.this.joinTasks.put(player.getUniqueId(), player.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
								@SuppressWarnings("deprecation")
								public void run() {
									if (player.isOnline()) GuiController.getInstance().openKitsMenu(player);
								}
							}, System.currentTimeMillis() - currentTime > 1_000L ? 0L : 15L).getTaskId());
						}
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to show the kit menu on join.", ex);
					}
				}
			}
		};

		kitPlayer.setLoadTaskID(player.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
			@Override
			public void run() {
				try {
					DataStorage.getInstance().loadPlayer(PlayerController.getInstance().getPlayer(kitPlayer.getUniqueId()));
					while (true) {
						if (PlayerController.getInstance().getPlayer(kitPlayer.getUniqueId()).isLoaded()) {
							break;
						} else if (System.currentTimeMillis() - currentTime > 7_500L) {
							Bukkit.getServer().getScheduler().runTask(plugin, new Runnable() {
								@Override
								public void run() {
									if (player.isOnline()) {
										player.getServer().broadcast(ChatColor.GOLD + "[" + ChatColor.BOLD + ChatColor.AQUA + "KingKits" + ChatColor.GOLD + "] " + ChatColor.RED + "The server took too long to load " + player.getName() + "'s data. They have been kicked from the server.", Permissions.ADMIN.getName());
										player.kickPlayer(ChatColor.RED + "[KingKits] Server took too long to respond!\n" + ChatColor.RED + "Could not load your data.");
									}
								}
							});
							return;
						}
					}
					if (!onlyReload) Bukkit.getServer().getScheduler().runTask(plugin, joinTask);
				} catch (Exception ex) {
					ex.printStackTrace();

					Bukkit.getServer().getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							if (player.isOnline()) {
								player.getServer().broadcast(ChatColor.GOLD + "[" + ChatColor.BOLD + ChatColor.AQUA + "KingKits" + ChatColor.GOLD + "] " + ChatColor.RED + "An error occurred whilst loading " + player.getName() + "'s data. They have been kicked from the server.", Permissions.ADMIN.getName());
								player.kickPlayer(ChatColor.RED + "[KingKits] An error occurred!\n" + ChatColor.RED + "Could not load your data.");
							}
						}
					});
				}
			}
		}).getTaskId());
	}

}
