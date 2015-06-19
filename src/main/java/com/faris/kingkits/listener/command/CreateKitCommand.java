package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import com.faris.kingkits.listener.event.custom.PlayerCreateKitEvent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.permissions.*;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateKitCommand extends PlayerCommand {

	public CreateKitCommand(KingKits instance) {
		super(instance);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("createkit")) {
			if (player.hasPermission(Permissions.COMMAND_KIT_CREATE)) {
				if (this.getPlugin().cmdValues.createKits) {
					if (Utilities.inPvPWorld(player)) {
						if (args.length == 0) {
							Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <guiitem>]");
							Lang.sendMessage(player, Lang.COMMAND_CREATE_KIT_DESCRIPTION);
						} else if (args.length > 0 && args.length < 3) {
							String kitName = args[0];

							boolean containsKit = this.getPlugin().getKitsConfig().contains(kitName);
							if (!containsKit) {
								List<String> currentKits = this.getPlugin().getKitList();
								List<String> currentKitsLC = Utilities.toLowerCaseList(currentKits);
								if (currentKitsLC.contains(kitName.toLowerCase()))
									kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));
								containsKit = this.getPlugin().getKitsConfig().contains(kitName);
							}

							if (!this.containsIllegalCharacters(kitName)) {
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

								Map<Integer, ItemStack> itemsInInv = new HashMap<>();
								List<ItemStack> armourInInv = new ArrayList<>();
								ItemStack[] pContents = player.getInventory().getContents();
								if (pContents == null) pContents = new ItemStack[player.getInventory().getSize()];
								for (int i = 0; i < player.getInventory().getSize(); i++) {
									if (pContents.length > i && pContents[i] != null) itemsInInv.put(i, pContents[i]);
									else itemsInInv.put(i, new ItemStack(Material.AIR));
								}
								for (ItemStack armour : player.getInventory().getArmorContents())
									if (armour != null && armour.getType() != Material.AIR) armourInInv.add(armour);
								PlayerCreateKitEvent createKitEvent = new PlayerCreateKitEvent(player, kitName, itemsInInv, armourInInv, false);
								player.getServer().getPluginManager().callEvent(createKitEvent);

								if (!createKitEvent.isCancelled()) {
									itemsInInv = createKitEvent.getKitContentsWithSlots();
									armourInInv = createKitEvent.getKitArmour();
									if (itemsInInv.size() > 0 || armourInInv.size() > 0) {
										if (containsKit) {
											this.getPlugin().getKitsConfig().set(kitName, null);
											this.getPlugin().saveKitsConfig();
											if (this.getPlugin().kitList.containsKey(kitName))
												this.getPlugin().kitList.remove(kitName);
										}

										final Kit kit = new Kit(kitName, itemsInInv).setRealName(kitName).setArmour(armourInInv);
										if (args.length == 2) {
											ItemStack guiItem = null;
											try {
												guiItem = new ItemStack(Integer.parseInt(args[1]));
											} catch (Exception ignored) {
											}
											try {
												if (args[1].contains(":")) {
													String[] guiSplit = args[1].split(":");
													guiItem = new ItemStack(Integer.parseInt(guiSplit[0]));
													guiItem.setDurability(Short.parseShort(guiSplit[1]));
												}
											} catch (Exception ignored) {
											}
											if (guiItem != null) {
												if (guiItem.getType() != Material.AIR) {
													kit.setGuiItem(guiItem);
												}
											}
										}

										List<PotionEffect> kitPotionEffects = new ArrayList<>();
										for (PotionEffect potionEffect : player.getActivePotionEffects()) {
											if (potionEffect != null) kitPotionEffects.add(potionEffect);
										}
										if (!kitPotionEffects.isEmpty()) kit.setPotionEffects(kitPotionEffects);

										kit.setMaxHealth((int) player.getMaxHealth());

										this.getPlugin().getKitsConfig().set(kitName, kit.serialize());
										this.getPlugin().kitList.put(kitName, kit);
										this.getPlugin().saveKitsConfig();

										try {
											player.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
											player.getServer().getPluginManager().addPermission(new Permission("kingkits.free." + kitName.toLowerCase()));
										} catch (Exception ignored) {
										}
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
								Lang.sendMessage(player, Lang.COMMAND_CREATE_ILLEGAL_CHARACTERS);
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
