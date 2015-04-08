package com.faris.kingkits;

import org.bukkit.*;
import org.bukkit.plugin.*;

public class Vault {

	private boolean printed = false;

	public Vault() {
		this.printed = false;
	}

	public Object getEconomy() {
		if (KingKits.getInstance() != null && KingKits.getInstance().configValues.vaultValues.useEconomy) {
			try {
				RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
				if (economyProvider != null) return economyProvider.getProvider();
			} catch (Exception ex) {
				if (!this.printed) {
					System.out.println("Vault could not be found.");
					this.printed = true;
				}
			}
		}
		return null;
	}

}
