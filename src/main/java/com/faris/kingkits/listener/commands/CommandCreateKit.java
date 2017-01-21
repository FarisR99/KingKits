package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.controller.KitController;
import com.faris.kingkits.controller.PlayerController;
import com.faris.kingkits.helper.util.*;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class CommandCreateKit extends KingKitsCommand {

	public CommandCreateKit(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("createkit")) {
			try {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!ConfigController.getInstance().getCommands(player.getWorld())[1]) {
						Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (!Utilities.isPvPWorld(player.getWorld())) {
						Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}

					KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
					if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return true;

					if (player.hasPermission(Permissions.COMMAND_KIT_CREATE)) {
						if (args.length == 1 || args.length == 2) {
							String strKit = args[0];
							if (StringUtilities.containsIllegalCharacters(strKit)) {
								Messages.sendMessage(sender, Messages.KIT_ILLEGAL_CHARACTERS, strKit);
								return true;
							}
							KitUtilities.KitSearchResult searchResult = KitUtilities.getKits(strKit);

							Map<Integer, ItemStack> kitItems = new TreeMap<>();
							ItemStack[] kitArmour = player.getInventory().getArmorContents();
							ItemStack offHand = ItemUtilities.isNull(player.getInventory().getItemInOffHand()) ? new ItemStack(Material.AIR) : player.getInventory().getItemInOffHand();
							List<PotionEffect> kitEffects = new ArrayList<>();
							if (player.getInventory().getContents() != null) {
								ItemStack[] playerInventoryContents = player.getInventory().getStorageContents();
								for (int i = 0; i < playerInventoryContents.length; i++) {
									if (!ItemUtilities.isNull(playerInventoryContents[i])) {
										kitItems.put(i, playerInventoryContents[i]);
									}
								}
							}
							for (PotionEffect potionEffect : player.getActivePotionEffects()) {
								PotionEffect newEffect = new PotionEffect(potionEffect.getType(), potionEffect.getDuration(), potionEffect.getAmplifier(), potionEffect.isAmbient(), potionEffect.hasParticles());
								kitEffects.add(newEffect);
							}

							ItemStack guiItem = null;
							if (args.length == 2) {
								String strGuiItem = args[1];
								if (strGuiItem.contains(":")) {
									String[] strGuiItemSplit = strGuiItem.split(":");
									Material guiItemType = Material.matchMaterial(strGuiItemSplit[0]);
									if (guiItemType != null) {
										short guiItemData = Utilities.isNumber(Short.class, strGuiItemSplit[1]) ? Short.parseShort(strGuiItemSplit[1]) : 0;
										guiItem = new ItemStack(guiItemType, 1, guiItemData);
									}
								} else {
									Material guiItemType = Material.matchMaterial(strGuiItem);
									if (guiItemType != null) guiItem = new ItemStack(guiItemType);
								}
							}

							if (searchResult.hasKit()) {
								Kit kit = searchResult.getKit();
								if (!ItemUtilities.isNull(guiItem)) kit.setGuiItem(guiItem);
								kit.setItems(kitItems);
								kit.setOffHand(offHand);
								kit.setArmour(kitArmour);
								kit.setPotionEffects(kitEffects);

								KitController.getInstance().addKit(kit);
								KitController.getInstance().saveKit(kit);
							} else {
								Kit kit = new Kit(strKit, kitItems, offHand, kitArmour, kitEffects);
								if (!ItemUtilities.isNull(guiItem)) kit.setGuiItem(guiItem);
								kit.setWalkSpeed(player.getWalkSpeed());
								kit.setMaxHealth(player.getMaxHealth());

								KitController.getInstance().addKit(kit);
								KitController.getInstance().saveKit(kit);

								if (!searchResult.hasOtherKits()) {
									try {
										player.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kit.getName().toLowerCase()));
									} catch (Exception ex) {
										this.getPlugin().getLogger().log(Level.WARNING, "Failed to register the kit permission node 'kingkits.kits." + kit.getName().toLowerCase() + "'.", ex);
									}
								}
							}

							Messages.sendMessage(player, searchResult.hasKit() ? Messages.COMMAND_KIT_CREATE_OVERWROTE : Messages.COMMAND_KIT_CREATE_CREATED, strKit);
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "<name> [<guiItem>]");
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
					}
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to execute '/" + label.toLowerCase() + " " + StringUtilities.joinString(args) + "'", ex);
				Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getCause().getClass().getName());
			}
			return true;
		}
		return false;
	}

}
