package me.faris.kingkits.listeners.commands;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RefillCommand extends PlayerCommand {

	public RefillCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("refill") || command.equalsIgnoreCase("soup")) {
			try {
				if (p.hasPermission(this.getPlugin().permissions.refillSoupSingle) || p.hasPermission(this.getPlugin().permissions.refillSoupAll)) {
					if (this.getPlugin().cmdValues.refillKits) {
						if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
							if (this.getPlugin().configValues.quickSoupKitOnly) {
								if (!this.getPlugin().usingKits.containsKey(p.getName())) {
									p.sendMessage(ChatColor.RED + "You have not chosen a kit.");
									return true;
								}
							}
							if (args.length == 0) {
								if (p.hasPermission(this.getPlugin().permissions.refillSoupSingle)) {
									if (p.getInventory().getItemInHand() != null) {
										if (p.getInventory().getItemInHand().getType() == Material.BOWL) {
											int invContentsSize = 0;
											ItemStack[] itemContents = p.getInventory().getContents();
											for (ItemStack itemContent : itemContents) {
												if (itemContent != null) {
													if (itemContent.getType() != Material.AIR) invContentsSize++;
												}
											}
											if (invContentsSize < p.getInventory().getSize()) {
												ItemStack itemInHand = p.getInventory().getItemInHand();
												int amount = itemInHand.getAmount();
												if (amount <= 1) {
													p.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP, 1));
												} else {
													itemInHand.setAmount(amount - 1);
													p.getInventory().setItemInHand(itemInHand);
													p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
												}
												if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
													try {
														net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
														if (economy.hasAccount(p.getName())) {
															double cost = this.getPlugin().configValues.vaultValues.costPerRefill;
															if (economy.getBalance(p.getName()) >= cost) {
																economy.withdrawPlayer(p.getName(), cost);
																if (cost != 0) p.sendMessage(this.getPlugin().getEconomyMessage(cost));
															} else {
																p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill your bowl.");
																return true;
															}
														} else {
															p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill your bowl.");
															return true;
														}
													} catch (Exception ex) {
													}
												}
											} else {
												p.sendMessage(ChatColor.RED + "You have a full inventory.");
											}
										} else {
											p.sendMessage(ChatColor.RED + "You must have a bowl in your hand.");
										}
									} else {
										p.sendMessage(ChatColor.RED + "You must have a bowl in your hand.");
									}
								} else {
									this.sendNoAccess(p);
								}
							} else if (args.length == 1) {
								if (args[0].equalsIgnoreCase("all")) {
									if (p.hasPermission(this.getPlugin().permissions.refillSoupAll)) {
										if (p.getInventory().getItemInHand() != null) {
											if (p.getInventory().getItemInHand().getType() == Material.BOWL) {
												int invContentsSize = 0;
												ItemStack[] itemContentz = p.getInventory().getContents();
												for (ItemStack itemContent : itemContentz) {
													if (itemContent != null) {
														if (itemContent.getType() != Material.AIR) invContentsSize++;
													}
												}
												if (invContentsSize < p.getInventory().getSize()) {
													int bowlAmount = p.getInventory().getItemInHand().getAmount();
													int invSize = 0;
													int bowlsGiven = 0;
													ItemStack[] itemContents = p.getInventory().getContents();
													int invMaxSize = p.getInventory().getSize();
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
															if (economy.hasAccount(p.getName())) {
																double cost = this.getPlugin().configValues.vaultValues.costPerRefill * bowlsGiven;
																if (economy.getBalance(p.getName()) >= cost) {
																	economy.withdrawPlayer(p.getName(), cost);
																	if (cost != 0) p.sendMessage(this.getPlugin().getEconomyMessage(cost));
																} else {
																	p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill all your bowls.");
																	return true;
																}
															} else {
																p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill all your bowls.");
																return true;
															}
														} catch (Exception ex) {
														}
													}
													for (int i = 0; i < bowlsGiven; i++) {
														p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
													}
													if (p.getInventory().getItemInHand().getAmount() - bowlsGiven > 0) p.getInventory().setItemInHand(new ItemStack(Material.BOWL, p.getInventory().getItemInHand().getAmount() - bowlsGiven));
													else p.getInventory().setItemInHand(new ItemStack(Material.AIR));
												} else {
													if (p.getInventory().getItemInHand().getAmount() == 1) {
														if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
															try {
																net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
																if (economy.hasAccount(p.getName())) {
																	double cost = this.getPlugin().configValues.vaultValues.costPerRefill;
																	if (economy.getBalance(p.getName()) >= cost) {
																		economy.withdrawPlayer(p.getName(), cost);
																		if (cost != 0) p.sendMessage(this.getPlugin().getEconomyMessage(cost));
																	} else {
																		p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill your bowl.");
																		return true;
																	}
																} else {
																	p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill your bowl.");
																	return true;
																}
															} catch (Exception ex) {
															}
															p.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP));
														}
													}
												}
											} else {
												p.sendMessage(ChatColor.RED + "You must have a bowl in your hand.");
											}
										} else {
											p.sendMessage(ChatColor.RED + "You must have a bowl in your hand.");
										}
									} else {
										this.sendNoAccess(p);
									}
								} else {
									p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<all>]")));
								}
							} else {
								p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<all>]")));
							}
						} else {
							p.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
						}
					} else {
						p.sendMessage(ChatColor.RED + "This command is disabled in the configuration.");
					}
				} else {
					this.sendNoAccess(p);
				}
			} catch (Exception ex) {
				p.sendMessage(ChatColor.RED + "An unexpected error occured.");
			}
			return true;
		}
		return false;
	}

}
