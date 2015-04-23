package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import com.faris.kingkits.listener.event.custom.PlayerCreateKitEvent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateUserKitCommand extends PlayerCommand {

	public CreateUserKitCommand(KingKits instance) {
		super(instance);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("createukit")) {
			if (player.hasPermission(this.getPlugin().permissions.kitUCreateCommand)) {
				if (this.getPlugin().cmdValues.createUKits) {
					if (Utilities.inPvPWorld(player)) {
						if (args.length == 0) {
							Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
							Lang.sendMessage(player, Lang.COMMAND_CREATE_UKIT_DESCRIPTION);
						} else if (args.length > 0 && args.length < 3) {
							String kitName = args[0];

							boolean containsRealKit = this.getPlugin().getKitsConfig().contains(kitName);
							if (!containsRealKit) {
								List<String> currentKits = this.getPlugin().getKitList();
								List<String> currentKitsLC = Utilities.toLowerCaseList(currentKits);
								containsRealKit = currentKitsLC.contains(kitName.toLowerCase());
							}

							if (!containsRealKit) {
								List<String> currentKits = this.getPlugin().getKitList(player.getUniqueId());
								boolean containsKit = this.getPlugin().getUserKitsConfig().contains(player.getUniqueId().toString() + "." + kitName);
								if (!containsKit) {
									List<String> currentKitsLC = Utilities.toLowerCaseList(currentKits);
									if (currentKitsLC.contains(kitName.toLowerCase()))
										kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));
									containsKit = currentKits.contains(kitName);
								}

								if (!this.containsIllegalCharacters(kitName)) {
									List<Kit> playerKits = this.getPlugin().userKitList.get(player.getUniqueId());
									if (playerKits == null) playerKits = new ArrayList<Kit>();
									int maxSizePerm = 0;
									for (int i = 1; i <= 54; i++) {
										if (maxSizePerm < i && player.hasPermission("kingkits.kit.limit." + i))
											maxSizePerm = i;
									}
									if (maxSizePerm > playerKits.size()) {
										if (args.length == 2) {
											if (args[1].contains(":")) {
												String[] guiSplit = args[1].split(":");
												if (guiSplit.length == 2) {
													if (!this.isInteger(guiSplit[0]) || !this.isInteger(guiSplit[1])) {
														Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
														return true;
													}
												} else {
													if (!this.isInteger(args[1])) {
														Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
														return true;
													}
												}
											} else {
												if (!this.isInteger(args[1])) {
													Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
													return true;
												}
											}
										}

										Map<Integer, ItemStack> itemsInInv = new HashMap<Integer, ItemStack>();
										List<ItemStack> armourInInv = new ArrayList<ItemStack>();
										ItemStack[] pContents = player.getInventory().getContents();
										if (pContents == null)
											pContents = new ItemStack[player.getInventory().getSize()];
										for (int i = 0; i < player.getInventory().getSize(); i++) {
											if (pContents.length > i && pContents[i] != null)
												itemsInInv.put(i, pContents[i]);
											else itemsInInv.put(i, new ItemStack(Material.AIR));
										}
										for (ItemStack armour : player.getInventory().getArmorContents())
											if (armour != null && armour.getType() != Material.AIR)
												armourInInv.add(armour);
										PlayerCreateKitEvent createKitEvent = new PlayerCreateKitEvent(player, kitName, itemsInInv, armourInInv, true);
										player.getServer().getPluginManager().callEvent(createKitEvent);

										if (!createKitEvent.isCancelled()) {
											itemsInInv = createKitEvent.getKitContentsWithSlots();
											armourInInv = createKitEvent.getKitArmour();
											if (itemsInInv.size() > 0 || armourInInv.size() > 0) {
												if (containsKit) {
													this.getPlugin().getUserKitsConfig().set(player.getUniqueId().toString() + "." + kitName, null);
													this.getPlugin().saveUserKitsConfig();
													if (playerKits != null) {
														List<Kit> newKits = new ArrayList<Kit>();
														for (Kit playerKit : playerKits) {
															if (playerKit != null && !playerKit.getRealName().toLowerCase().equals(kitName.toLowerCase()))
																newKits.add(playerKit);
														}
														this.getPlugin().userKitList.put(player.getUniqueId(), newKits);
													}
												}

												final Kit kit = new Kit(kitName, itemsInInv).setRealName(kitName).setArmour(armourInInv).setUserKit(true);
												if (args.length == 2) {
													ItemStack guiItem = null;
													try {
														guiItem = new ItemStack(Integer.parseInt(args[1]));
													} catch (Exception ex) {
													}
													try {
														if (args[1].contains(":")) {
															String[] guiSplit = args[1].split(":");
															guiItem = new ItemStack(Integer.parseInt(guiSplit[0]));
															guiItem.setDurability(Short.parseShort(guiSplit[1]));
														}
													} catch (Exception ex) {
													}
													if (guiItem != null) {
														if (guiItem.getType() != Material.AIR) {
															kit.setGuiItem(guiItem);
														}
													}
												}

												List<PotionEffect> kitPotionEffects = new ArrayList<PotionEffect>();
												for (PotionEffect potionEffect : player.getActivePotionEffects()) {
													if (potionEffect != null) kitPotionEffects.add(potionEffect);
												}
												if (!kitPotionEffects.isEmpty()) kit.setPotionEffects(kitPotionEffects);
												kit.setMaxHealth((int) player.getMaxHealth());

												this.getPlugin().getUserKitsConfig().set(player.getUniqueId().toString() + "." + kitName, kit.serialize());
												if (playerKits == null) playerKits = new ArrayList<Kit>();
												playerKits.add(kit);
												this.getPlugin().userKitList.put(player.getUniqueId(), playerKits);
												this.getPlugin().saveUserKitsConfig();

												Lang.sendMessage(player, containsKit ? Lang.COMMAND_CREATE_OVERWRITTEN : Lang.COMMAND_CREATE_CREATED, kitName);

												if (this.getPlugin().configValues.removeItemsOnCreateKit) {
													player.getInventory().clear();
													player.getInventory().setArmorContents(null);
												}
											} else {
												Lang.sendMessage(player, Lang.COMMAND_CREATE_EMPTY_INV);
											}
										} else {
											Lang.sendMessage(player, Lang.COMMAND_CREATE_DENIED);
										}
									} else {
										Lang.sendMessage(player, Lang.COMMAND_CREATE_UKIT_MAX_PERSONAL_KITS);
									}
								} else {
									Lang.sendMessage(player, Lang.COMMAND_CREATE_ILLEGAL_CHARACTERS);
								}
							} else {
								Lang.sendMessage(player, Lang.COMMAND_CREATE_UKIT_EXISTS);
							}
						} else {
							Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
						}
					} else {
						Lang.sendMessage(player, Lang.COMMAND_GEN_WORLD);
					}
				} else {
					Lang.sendMessage(player, Lang.COMMAND_GEN_DISABLED);
				}
			} else {
				this.sendNoAccess(player);
			}
			return true;
		}
		return false;
	}

}
