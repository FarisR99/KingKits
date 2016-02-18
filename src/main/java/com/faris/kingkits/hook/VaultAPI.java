package com.faris.kingkits.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultAPI {

	private static Object objEconomy = null;

	private VaultAPI() {
	}

	public static double getBalance(Player player) {
		if (player != null) {
			try {
				if (setupEconomy()) {
					net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) objEconomy;
					return economy.hasAccount(player) ? economy.getBalance(player) : 0D;
				}
			} catch (Exception ignored) {
			}
		}
		return 0D;
	}

	public static void giveMoney(Player player, double amount) {
		if (player != null && amount != 0D) {
			try {
				if (setupEconomy()) {
					net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) objEconomy;
					if (amount > 0D) {
						if (!economy.hasAccount(player)) economy.createPlayerAccount(player);
						economy.depositPlayer(player, amount);
					} else {
						amount *= -1D;

						double playerBalance = 0D;
						if (!economy.hasAccount(player)) economy.createPlayerAccount(player);
						else playerBalance = economy.getBalance(player);

						amount = Math.max(Math.min(playerBalance, amount), 0);
						if (amount != 0D) economy.withdrawPlayer(player, amount);
					}
				}
			} catch (Exception ignored) {
			}
		}
	}

	public static void setBalance(Player player, double balance) {
		if (player != null) {
			try {
				if (setupEconomy()) {
					net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) objEconomy;
					double currentBalance = economy.getBalance(player);
					if (currentBalance < balance) {
						economy.depositPlayer(player, balance - currentBalance);
					} else if (currentBalance > balance) {
						economy.withdrawPlayer(player, currentBalance - balance);
					}
				}
			} catch (Exception ignored) {
			}
		}
	}

	private static boolean setupEconomy() throws Exception {
		if (objEconomy == null) {
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) objEconomy = economyProvider.getProvider();
		}
		return objEconomy != null;
	}

}
