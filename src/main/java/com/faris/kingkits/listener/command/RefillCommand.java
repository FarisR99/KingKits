package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class RefillCommand extends PlayerCommand {

	public RefillCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("refill") || command.equalsIgnoreCase("soup")) {
			try {
				if (player.hasPermission(this.getPlugin().permissions.refillSoupSingle) || player.hasPermission(this.getPlugin().permissions.refillSoupAll)) {
					if (this.getPlugin().cmdValues.refillKits) {
						if (Utilities.inPvPWorld(player)) {
							if (this.getPlugin().configValues.quickSoupKitOnly) {
								if (!this.getPlugin().usingKits.containsKey(player.getName())) {
									Lang.sendMessage(player, Lang.GEN_NO_KIT_SELECTED);
									return true;
								}
							}
							if (args.length == 0) {
								if (player.hasPermission(this.getPlugin().permissions.refillSoupSingle)) {
									if (player.getInventory().getItemInHand() != null) {
										if (player.getInventory().getItemInHand().getType() == Material.BOWL) {
											int invContentsSize = 0;
											ItemStack[] itemContents = player.getInventory().getContents();
											for (ItemStack itemContent : itemContents) {
												if (itemContent != null) {
													if (itemContent.getType() != Material.AIR) invContentsSize++;
												}
											}
											if (invContentsSize < player.getInventory().getSize()) {
												ItemStack itemInHand = player.getInventory().getItemInHand();
												int amount = itemInHand.getAmount();
												if (amount <= 1) {
													player.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP, 1));
												} else {
													itemInHand.setAmount(amount - 1);
													player.getInventory().setItemInHand(itemInHand);
													player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
												}
												if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
													try {
														net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
														if (economy.hasAccount(player)) {
															double cost = this.getPlugin().configValues.vaultValues.costPerRefill;
															if (economy.getBalance(player) >= cost) {
																economy.withdrawPlayer(player, cost);
																if (cost != 0)
																	player.sendMessage(this.getPlugin().getEconomyMessage(cost));
															} else {
																Lang.sendMessage(player, Lang.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																return true;
															}
														} else {
															Lang.sendMessage(player, Lang.COMMAND_REFILL_NOT_ENOUGH_MONEY);
															return true;
														}
													} catch (Exception ex) {
													}
												}
											} else {
												Lang.sendMessage(player, Lang.COMMAND_REFILL_FULL_INV);
											}
										} else {
											Lang.sendMessage(player, Lang.COMMAND_REFILL_BOWL);
										}
									} else {
										Lang.sendMessage(player, Lang.COMMAND_REFILL_BOWL);
									}
								} else {
									this.sendNoAccess(player);
								}
							} else if (args.length == 1) {
								if (args[0].equalsIgnoreCase("all")) {
									if (player.hasPermission(this.getPlugin().permissions.refillSoupAll)) {
										if (player.getInventory().getItemInHand() != null) {
											if (player.getInventory().getItemInHand().getType() == Material.BOWL) {
												int invContentsSize = 0;
												ItemStack[] inventoryContents = player.getInventory().getContents();
												for (ItemStack itemContent : inventoryContents) {
													if (itemContent != null) {
														if (itemContent.getType() != Material.AIR) invContentsSize++;
													}
												}
												if (invContentsSize < player.getInventory().getSize()) {
													int bowlAmount = player.getInventory().getItemInHand().getAmount();
													int invSize = 0;
													int bowlsGiven = 0;
													ItemStack[] itemContents = player.getInventory().getContents();
													int invMaxSize = player.getInventory().getSize();
													for (int i = 0; i < itemContents.length; i++) {
														if (itemContents[i] != null) {
															if (itemContents[i].getType() != Material.AIR) invSize++;
														}
													}
													for (int i = 0; i < bowlAmount; i++) {
														if (invSize < invMaxSize) {
															invSize++;
															bowlsGiven++;
														}
													}
													if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
														try {
															net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
															if (economy.hasAccount(player)) {
																double cost = this.getPlugin().configValues.vaultValues.costPerRefill * bowlsGiven;
																if (economy.getBalance(player) >= cost) {
																	economy.withdrawPlayer(player, cost);
																	if (cost != 0)
																		player.sendMessage(this.getPlugin().getEconomyMessage(cost));
																} else {
																	Lang.sendMessage(player, Lang.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																	return true;
																}
															} else {
																Lang.sendMessage(player, Lang.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																return true;
															}
														} catch (Exception ex) {
														}
													}
													for (int i = 0; i < bowlsGiven; i++) {
														player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
													}
													if (player.getInventory().getItemInHand().getAmount() - bowlsGiven > 0)
														player.getInventory().setItemInHand(new ItemStack(Material.BOWL, player.getInventory().getItemInHand().getAmount() - bowlsGiven));
													else
														player.getInventory().setItemInHand(new ItemStack(Material.AIR));
												} else {
													if (player.getInventory().getItemInHand().getAmount() == 1) {
														if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
															try {
																net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
																if (economy.hasAccount(player)) {
																	double cost = this.getPlugin().configValues.vaultValues.costPerRefill;
																	if (economy.getBalance(player) >= cost) {
																		economy.withdrawPlayer(player, cost);
																		if (cost != 0)
																			player.sendMessage(this.getPlugin().getEconomyMessage(cost));
																	} else {
																		Lang.sendMessage(player, Lang.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																		return true;
																	}
																} else {
																	Lang.sendMessage(player, Lang.COMMAND_REFILL_NOT_ENOUGH_MONEY);
																	return true;
																}
															} catch (Exception ex) {
															}
															player.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP));
														}
													}
												}
											} else {
												Lang.sendMessage(player, Lang.COMMAND_REFILL_BOWL);
											}
										} else {
											Lang.sendMessage(player, Lang.COMMAND_REFILL_BOWL);
										}
									} else {
										this.sendNoAccess(player);
									}
								} else {
									Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<all>]");
								}
							} else {
								Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<all>]");
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
			} catch (Exception ex) {
				ex.printStackTrace();
				Lang.sendMessage(player, Lang.COMMAND_GEN_ERROR);
			}
			return true;
		}
		return false;
	}

}
