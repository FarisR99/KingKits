package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.helper.util.ItemUtilities;
import com.faris.kingkits.helper.util.PlayerUtilities;
import com.faris.kingkits.helper.util.StringUtilities;
import com.faris.kingkits.helper.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.*;

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
						if (ConfigController.getInstance().getCommands(player.getWorld())[8]) {
							if (Utilities.isPvPWorld(player.getWorld())) {
								if (args.length == 0) {
									if (player.hasPermission(Permissions.COMMAND_SOUP_REFILL_SINGLE)) {
										if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() == Material.BOWL) {
											int invContentsSize = 0;
											ItemStack[] invContents = player.getInventory().getStorageContents();
											for (ItemStack invContent : invContents) {
												if (!ItemUtilities.isNull(invContent)) invContentsSize++;
											}
											if (invContentsSize < invContents.length) {
												ItemStack itemInHand = player.getInventory().getItemInMainHand();
												if (ConfigController.getInstance().getCostPerRefill(player.getWorld()) > 0D) {
													double cost = ConfigController.getInstance().getCostPerRefill(player.getWorld());
													if (PlayerUtilities.getBalance(player) >= cost) {
														PlayerUtilities.incrementMoney(player, -cost);
													} else {
														Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
														return true;
													}
												}
												int amount = itemInHand.getAmount();
												if (amount <= 1) {
													player.getInventory().setItemInMainHand(new ItemStack(Material.MUSHROOM_SOUP));
												} else {
													itemInHand.setAmount(amount - 1);
													player.getInventory().setItemInMainHand(itemInHand);
													player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));
												}
												player.updateInventory();
											} else {
												Messages.sendMessage(player, Messages.COMMAND_REFILL_FULL_INV);
											}
										} else if (player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().getType() == Material.BOWL) {
											int invContentsSize = 0;
											ItemStack[] invContents = player.getInventory().getStorageContents();
											for (ItemStack invContent : invContents) {
												if (!ItemUtilities.isNull(invContent)) invContentsSize++;
											}
											if (invContentsSize < invContents.length) {
												ItemStack itemInHand = player.getInventory().getItemInOffHand();
												if (ConfigController.getInstance().getCostPerRefill(player.getWorld()) > 0D) {
													double cost = ConfigController.getInstance().getCostPerRefill(player.getWorld());
													if (PlayerUtilities.getBalance(player) >= cost) {
														PlayerUtilities.incrementMoney(player, -cost);
													} else {
														Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
														return true;
													}
												}
												int amount = itemInHand.getAmount();
												if (amount <= 1) {
													player.getInventory().setItemInOffHand(new ItemStack(Material.MUSHROOM_SOUP));
												} else {
													itemInHand.setAmount(amount - 1);
													player.getInventory().setItemInOffHand(itemInHand);
													player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));
												}
												player.updateInventory();
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
											if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() == Material.BOWL) {
												int invContentsSize = 0;
												ItemStack[] inventoryContents = player.getInventory().getStorageContents();
												for (ItemStack invContent : inventoryContents) {
													if (!ItemUtilities.isNull(invContent)) invContentsSize++;
												}
												if (invContentsSize < inventoryContents.length) {
													int bowlAmount = player.getInventory().getItemInMainHand().getAmount();
													int invSize = 0, bowlsToGive = 0;
													int invMaxSize = inventoryContents.length;
													for (ItemStack invContent : inventoryContents) {
														if (!ItemUtilities.isNull(invContent)) invSize++;
													}
													for (int i = 0; i < bowlAmount; i++) {
														if (invSize + bowlsToGive < invMaxSize) {
															bowlsToGive++;
														}
													}
													if (ConfigController.getInstance().getCostPerRefill(player.getWorld()) > 0D) {
														double cost = ConfigController.getInstance().getCostPerRefill(player.getWorld()) * bowlsToGive;
														if (PlayerUtilities.getBalance(player) >= cost) {
															PlayerUtilities.incrementMoney(player, -cost);
														} else {
															Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
															return true;
														}
													}
													for (int i = 0; i < bowlsToGive; i++)
														player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
													if (player.getInventory().getItemInMainHand().getAmount() - bowlsToGive > 0)
														player.getInventory().setItemInMainHand(new ItemStack(Material.BOWL, player.getInventory().getItemInMainHand().getAmount() - bowlsToGive));
													else
														player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
													player.updateInventory();
												} else {
													if (player.getInventory().getItemInMainHand().getAmount() == 1) {
														if (ConfigController.getInstance().getCostPerRefill(player.getWorld()) > 0D) {
															double cost = ConfigController.getInstance().getCostPerRefill(player.getWorld());
															if (PlayerUtilities.getBalance(player) >= cost) {
																PlayerUtilities.incrementMoney(player, -cost);
															} else {
																Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																return true;
															}
														}
														player.getInventory().setItemInMainHand(new ItemStack(Material.MUSHROOM_SOUP));
														player.updateInventory();
													}
												}
											} else if (player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().getType() == Material.BOWL) {
												int invContentsSize = 0;
												ItemStack[] inventoryContents = player.getInventory().getStorageContents();
												for (ItemStack itemContent : inventoryContents) {
													if (!ItemUtilities.isNull(itemContent)) invContentsSize++;
												}
												if (invContentsSize < inventoryContents.length) {
													int bowlAmount = player.getInventory().getItemInOffHand().getAmount();
													int invSize = 0, bowlsToGive = 0;
													int invMaxSize = inventoryContents.length;
													for (ItemStack invContent : inventoryContents) {
														if (!ItemUtilities.isNull(invContent)) invSize++;
													}
													for (int i = 0; i < bowlAmount; i++) {
														if (invSize + bowlsToGive < invMaxSize) {
															bowlsToGive++;
														}
													}
													if (ConfigController.getInstance().getCostPerRefill(player.getWorld()) > 0D) {
														double cost = ConfigController.getInstance().getCostPerRefill(player.getWorld()) * bowlsToGive;
														if (PlayerUtilities.getBalance(player) >= cost) {
															PlayerUtilities.incrementMoney(player, -cost);
														} else {
															Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
															return true;
														}
													}
													for (int i = 0; i < bowlsToGive; i++)
														player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
													if (player.getInventory().getItemInOffHand().getAmount() - bowlsToGive > 0)
														player.getInventory().setItemInOffHand(new ItemStack(Material.BOWL, player.getInventory().getItemInOffHand().getAmount() - bowlsToGive));
													else
														player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
													player.updateInventory();
												} else {
													if (player.getInventory().getItemInOffHand().getAmount() == 1) {
														if (ConfigController.getInstance().getCostPerRefill(player.getWorld()) > 0D) {
															double cost = ConfigController.getInstance().getCostPerRefill(player.getWorld());
															if (PlayerUtilities.getBalance(player) >= cost) {
																PlayerUtilities.incrementMoney(player, -cost);
															} else {
																Messages.sendMessage(player, Messages.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																return true;
															}
														}
														player.getInventory().setItemInOffHand(new ItemStack(Material.MUSHROOM_SOUP));
														player.updateInventory();
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
				if (ex.getCause() != null) {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getCause().getClass().getName());
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getClass().getName());
				}
			}
			return true;
		}
		return false;
	}

}
