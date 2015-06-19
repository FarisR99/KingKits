package com.faris.kingkits;

import org.bukkit.*;
import org.bukkit.plugin.*;

public class Vault {

	private static boolean printed = false;

	public static Object getEconomy() {
		if (KingKits.getInstance() != null && hasVault() && KingKits.getInstance().configValues.vaultValues.useEconomy) {
			try {
				RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
				if (economyProvider != null) return economyProvider.getProvider();
			} catch (Exception ex) {
				if (!printed) {
					System.out.println("Vault could not be found.");
					printed = true;
				}
			}
		}
		return null;
	}

	public static Object getPermissions() {
		if (KingKits.getInstance() != null) {
			if (hasVault()) {
				try {
					RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
					if (permissionProvider != null) permissionProvider.getProvider();
				} catch (Exception ex) {
					if (!printed) {
						System.out.println("Vault could not be found.");
						printed = true;
					}
				}
			} else {
				if (!printed) {
					System.out.println("Vault could not be found.");
					printed = true;
				}
			}
		}
		return null;
	}

	public static boolean hasVault() {
		return Bukkit.getServer().getPluginManager().isPluginEnabled("Vault");
	}

}
