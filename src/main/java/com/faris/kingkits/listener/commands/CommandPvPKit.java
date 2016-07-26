package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.api.event.PlayerKitEvent;
import com.faris.kingkits.api.event.PlayerPreKitEvent;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.controller.GuiController;
import com.faris.kingkits.controller.PlayerController;
import com.faris.kingkits.helper.util.*;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CommandPvPKit extends KingKitsCommand {

	public CommandPvPKit(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("pvpkit")) {
			try {
				if (sender instanceof Player) {
					if (!ConfigController.getInstance().getCommands(((Player) sender).getWorld())[0]) {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (!Utilities.isPvPWorld(((Player) sender).getWorld())) {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
				}
				if (args.length == 0) {
					if (sender.hasPermission(Permissions.COMMAND_KIT_LIST)) {
						KitUtilities.listKits(sender);
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION, Permissions.COMMAND_KIT_LIST.getName());
					}
				} else if (args.length == 1) {
					if (sender instanceof Player) {
						if (sender.hasPermission(Permissions.COMMAND_KIT)) {
							this.setKit(sender, (Player) sender, args[0], false);
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION, Permissions.COMMAND_KIT.getName());
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
					}
				} else if (args.length == 2) {
					if (sender.hasPermission(Permissions.COMMAND_KIT_OTHER)) {
						Player target = sender.getServer().getPlayer(args[1]);
						if (target != null) {
							this.setKit(sender, target, args[0], true);
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_PLAYER_NOT_FOUND, args[1]);
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION, Permissions.COMMAND_KIT_OTHER.getName());
					}
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase() + " [<name>" + (sender.hasPermission(Permissions.COMMAND_KIT_OTHER) ? "|<name> <player>]" : "]"));
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to execute '/" + label.toLowerCase() + " " + StringUtilities.joinString(args) + "'", ex);
				Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getCause().getClass().getName());
			}
			return true;
		}
		return false;
	}

	private void setKit(CommandSender sender, Player player, String strKit, boolean isOther) {
		if (sender == null || player == null || strKit == null) return;
		if (!Utilities.isPvPWorld(player.getWorld())) {
			Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
			return;
		}
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		if (kitPlayer == null) return;
		Kit kit = null;
		KitUtilities.KitSearchResult searchResult = KitUtilities.getKits(strKit, kitPlayer);
		if (searchResult.hasKit()) {
			kit = searchResult.getKit();
		} else if (searchResult.hasOtherKits()) {
			if (searchResult.getOtherKits().size() == 1) {
				kit = searchResult.getOtherKits().get(0);
			} else {
				Messages.sendMessage(sender, Messages.KIT_MULTIPLE_FOUND, strKit);
			}
		} else {
			searchResult = KitUtilities.getKits(strKit);
			if (searchResult.hasKit()) {
				kit = searchResult.getKit();
			} else if (searchResult.hasOtherKits()) {
				if (searchResult.getOtherKits().size() == 1) {
					kit = searchResult.getOtherKits().get(0);
				} else {
					Messages.sendMessage(sender, Messages.KIT_MULTIPLE_FOUND, strKit);
				}
			} else {
				Messages.sendMessage(sender, Messages.KIT_NOT_FOUND, strKit);
			}
		}
		if (kit != null) {
			strKit = kit.getName();
			if (isOther || kitPlayer.hasPermission(kit) || kitPlayer.hasUnlocked(kit)) {
				if (ConfigController.getInstance().isOneKitPerLife(player.getWorld())) {
					if (kitPlayer.hasKit()) {
						Messages.sendMessage(player, Messages.KIT_ONE_PER_LIFE);
						return;
					}
				}

				PlayerPreKitEvent preEvent = new PlayerPreKitEvent(kitPlayer, kit);
				player.getServer().getPluginManager().callEvent(preEvent);
				if (preEvent.isCancelled() || preEvent.getKit() == null) {
					sender.sendMessage(ChatColor.RED + "A plugin has cancelled the kit selection.");
					return;
				} else {
					kit = preEvent.getKit();
				}
				long kitTimestamp = player.hasPermission(Permissions.ADMIN) && ConfigController.getInstance().canAdminsBypass() ? -1L : kitPlayer.getKitTimestamp(kit);
				if (kit.hasCooldown()) {
					if (kitTimestamp != -1L) {
						if (System.currentTimeMillis() - kitTimestamp > (long) (kit.getCooldown() * 1_000D)) {
							kitPlayer.setKitTimestamp(kit, null);
							kitTimestamp = -1L;
						}
					}
				}
				if (kitTimestamp == -1L) {
					if (kit.getCost() > 0D && (!player.hasPermission(Permissions.ADMIN) || !ConfigController.getInstance().canAdminsBypass())) {
						double playerBalance = PlayerUtilities.getBalance(player);
						if (playerBalance >= kit.getCost()) {
							playerBalance -= kit.getCost();
							PlayerUtilities.setBalance(player, playerBalance);
							Messages.sendMessage(player, Messages.ECONOMY_COST_PER_KIT, kit.getCost());
						} else {
							Messages.sendMessage(player, Messages.KIT_NOT_ENOUGH_MONEY, kit.getCost() - playerBalance);
							return;
						}
					}

					Kit oldKit = kitPlayer.getKit();
					kitPlayer.setKit(kit);
					if (ConfigController.getInstance().shouldClearItemsOnKitSelection(player.getWorld())) {
						player.getInventory().clear();
						player.getInventory().setArmorContents(null);
						for (PotionEffect activePotionEffect : player.getActivePotionEffects())
							player.removePotionEffect(activePotionEffect.getType());

						for (Map.Entry<Integer, ItemStack> kitItemEntry : kit.getItems().entrySet()) {
							try {
								player.getInventory().setItem(kitItemEntry.getKey(), kitItemEntry.getValue());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (!ItemUtilities.isNull(kit.getOffHand()))
							player.getInventory().setItemInOffHand(kit.getOffHand());
						player.getInventory().setArmorContents(kit.getArmour());
					} else {
						List<ItemStack> itemsToDrop = new ArrayList<>();
						for (ItemStack kitItem : kit.getItems().values()) {
							itemsToDrop.addAll(player.getInventory().addItem(kitItem).values());
						}
						if (!ItemUtilities.isNull(kit.getOffHand()))
							itemsToDrop.addAll(player.getInventory().addItem(kit.getOffHand()).values());
						for (ItemStack kitArmour : kit.getArmour()) {
							if (!ItemUtilities.isNull(kitArmour))
								itemsToDrop.addAll(player.getInventory().addItem(kitArmour).values());
						}
						if (ConfigController.getInstance().shouldDropItemsOnFullInventory()) {
							for (ItemStack itemToDrop : itemsToDrop)
								player.getWorld().dropItem(player.getLocation(), itemToDrop);
						}
					}
					if (kit.getHeldItemSlot() != -1) player.getInventory().setHeldItemSlot(kit.getHeldItemSlot());
					player.setWalkSpeed(kit.getWalkSpeed());
					if (player.getHealth() > kit.getMaxHealth())
						player.setHealth(kit.getMaxHealth());
					if (ConfigController.getInstance().shouldSetMaxHealth()) player.setMaxHealth(kit.getMaxHealth());
					if (player.getHealth() >= PlayerUtilities.getDefaultMaxHealth())
						player.setHealth(kit.getMaxHealth());
					player.addPotionEffects(kit.getPotionEffects());

					for (String command : kit.getCommands()) {
						command = command.replace("<player>", player.getName()).replace("<name>", player.getName()).replace("<username>", player.getName());
						command = command.replace("<displayname>", player.getDisplayName());
						command = command.replace("<kit>", kit.getName());
						BukkitUtilities.performCommand(command);
					}

					if (kit.hasCooldown()) kitPlayer.setKitTimestamp(kit, System.currentTimeMillis());

					player.getServer().getPluginManager().callEvent(new PlayerKitEvent(kitPlayer, oldKit, kit));
					Messages.sendMessage(player, Messages.KIT_SET, kit.getName());
				} else {
					if (!isOther) PlayerUtilities.sendKitDelayMessage(player, kit, kitTimestamp);
				}
			} else {
				Messages.sendMessage(sender, Messages.KIT_NO_PERMISSION, strKit);
				if (ConfigController.getInstance().shouldShowKitPreview())
					GuiController.getInstance().openPreviewGUI(player, kit);
			}
		}
	}

}
