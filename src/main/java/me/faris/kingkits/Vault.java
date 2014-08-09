package me.faris.kingkits;

import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
	private KingKits plugin = null;
	private boolean printed = false;

	public Vault(KingKits pluginInstance) {
		this.plugin = pluginInstance;
		this.printed = false;
	}

	public Object getEconomy() {
		if (this.plugin.configValues.vaultValues.useEconomy) {
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) return economyProvider.getProvider();
		}
		if (!this.printed) {
			System.out.println("Vault could not be found.");
			this.printed = true;
		}
		return null;
	}

}
