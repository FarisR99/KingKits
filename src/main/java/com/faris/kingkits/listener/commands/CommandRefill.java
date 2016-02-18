package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.helper.util.PlayerUtilities;
import com.faris.kingkits.helper.util.StringUtilities;
import com.faris.kingkits.helper.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class CommandRefill extends KingKitsCommand {

	public CommandRefill(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("refill")) {
			try {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (player.hasPermission(Permissions.COMMAND_SOUP_REFILL_SINGLE) || player.hasPermission(Permissions.COMMAND_SOUP_REFILL_ALL)) {
						if (ConfigController.getInstance().getCommands()[8]) {
							if (Utilities.isPvPWorld(player.getWorld())) {
								if (args.length == 0) {
									if (player.hasPermission(Permissions.COMMAND_SOUP_REFILL_SINGLE)) {
										if (player.getInventory().getItemInHand() != null && player.getInventory().getItemInHand().getType() == Material.BOWL) {
											int invContentsSize = 0;
											ItemStack[] itemContents = player.getInventory().getContents();
											for (ItemStack itemContent : itemContents) {
												if (itemContent != null) {
													if (itemContent.getType() != Material.AIR) invContentsSize++;
												}
											}
											if (invContentsSize < player.getInventory().getSize()) {
												ItemStack itemInHand = player.getInventory().getItemInHand();
												if (ConfigController.getInstance().getCostPerRefill() > 0D) {
													double cost = ConfigController.getInstance().getCostPerRefill();
													if (PlayerUtilities.getBalance(player) >= cost) {
														PlayerUtilities.incrementMoney(player, -cost);
													} else {
														Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
														return true;
													}
												}
												int amount = itemInHand.getAmount();
												if (amount <= 1) {
													player.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP));
												} else {
													itemInHand.setAmount(amount - 1);
													player.getInventory().setItemInHand(itemInHand);
													player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));
												}
											} else {
												Messages.sendMessage(player, Messages.COMMAND_REFILL_FULL_INV);
											}
										} else {
											Messages.sendMessage(player, Messages.COMMAND_REFILL_BOWL);
										}
									} else {
										Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
									}
								} else if (args.length == 1) {
									if (args[0].equalsIgnoreCase("all")) {
										if (player.hasPermission(Permissions.COMMAND_SOUP_REFILL_ALL)) {
											if (player.getInventory().getItemInHand() != null && player.getInventory().getItemInHand().getType() == Material.BOWL) {
												int invContentsSize = 0;
												ItemStack[] inventoryContents = player.getInventory().getContents();
												for (ItemStack itemContent : inventoryContents) {
													if (itemContent != null && itemContent.getType() != Material.AIR)
														invContentsSize++;
												}
												if (invContentsSize < player.getInventory().getSize()) {
													int bowlAmount = player.getInventory().getItemInHand().getAmount();
													int invSize = 0, bowlsToGive = 0;
													ItemStack[] itemContents = player.getInventory().getContents();
													int invMaxSize = player.getInventory().getSize();
													for (ItemStack itemContent : itemContents) {
														if (itemContent != null) {
															if (itemContent.getType() != Material.AIR) invSize++;
														}
													}
													for (int i = 0; i < bowlAmount; i++) {
														if (invSize + bowlsToGive < invMaxSize) {
															bowlsToGive++;
														}
													}
													if (ConfigController.getInstance().getCostPerRefill() > 0D) {
														double cost = ConfigController.getInstance().getCostPerRefill() * bowlsToGive;
														if (PlayerUtilities.getBalance(player) >= cost) {
															PlayerUtilities.incrementMoney(player, -cost);
														} else {
															Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
															return true;
														}
													}
													for (int i = 0; i < bowlsToGive; i++)
														player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
													if (player.getInventory().getItemInHand().getAmount() - bowlsToGive > 0)
														player.getInventory().setItemInHand(new ItemStack(Material.BOWL, player.getInventory().getItemInHand().getAmount() - bowlsToGive));
													else
														player.getInventory().setItemInHand(new ItemStack(Material.AIR));
												} else {
													if (player.getInventory().getItemInHand().getAmount() == 1) {
														if (ConfigController.getInstance().getCostPerRefill() > 0D) {
															double cost = ConfigController.getInstance().getCostPerRefill();
															if (PlayerUtilities.getBalance(player) >= cost) {
																PlayerUtilities.incrementMoney(player, -cost);
															} else {
																Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																return true;
															}
														}
														player.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP));
													}
												}
											} else {
												Messages.sendMessage(player, Messages.COMMAND_REFILL_BOWL);
											}
										} else {
											Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
										}
									} else {
										Messages.sendMessage(player, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "[<all>]");
									}
								} else {
									Messages.sendMessage(player, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "[<all>]");
								}
							} else {
								Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
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
