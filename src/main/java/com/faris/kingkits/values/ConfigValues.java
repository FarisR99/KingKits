package com.faris.kingkits.values;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

public class ConfigValues {
	// Booleans
	public boolean checkForUpdates = true;
	public boolean automaticUpdates = false;

	public boolean opBypass = true;

	public boolean listKitsOnJoin = true;
	public boolean kitListPermissionsJoin = false;
    public boolean sortAlphabetically = true;

	public boolean kitListPermissions = true;

	public boolean dropItemsOnDeath = false;
	public boolean dropItems = false;
	@SuppressWarnings("deprecation")
	public List<Integer> dropAnimations = Arrays.asList(Material.BOWL.getId());
	public boolean allowPickingUpItems = true;

	public boolean clearInvsOnReload = true;

	public boolean oneKitPerLife = false;

	public boolean removeItemsOnLeave = true;
	public boolean removePotionEffectsOnLeave = true;
	public boolean removeItemsOnCreateKit = true;

	public boolean rightClickCompass = true;

	public boolean quickSoup = true;
	public boolean quickSoupKitOnly = true;

	public boolean banBlockBreakingAndPlacing = false;

	public boolean disableDeathMessages = false;

	public boolean lockHunger = true;
	public int hungerLock = 20;

	public boolean disableGamemode = false;

	public boolean killstreaks = false;

	public boolean disableItemBreaking = true;

	public boolean kitMenuOnJoin = false;

	public boolean kitParticleEffects = false;

	public boolean showKitPreview = false;

	public boolean kitCooldown = false;

	public boolean replaceItems = true;

	// GUI
	public String guiTitle = "<menucolour>PvP Kits";
	public int guiSize = 36;

	// Scores
	public boolean scores = false;
	public String scoreFormat = "&6[&a<score>&6]";
	public int scoreIncrement = 2;
	public int maxScore = Integer.MAX_VALUE;

	// Vault
	public VaultValues vaultValues = new VaultValues();

	public class VaultValues {
		public boolean useEconomy = false;

		public boolean useCostPerKit = false;
		public boolean useCostPerRefill = false;
		public double costPerRefill = 2.5D;

		public boolean useMoneyPerKill = false;
		public double moneyPerKill = 5D;
		public boolean useMoneyPerDeath = false;
		public double moneyPerDeath = 5D;
	}

	// Strings
	public String commandToRun = "";

	public List<String> pvpWorlds = Arrays.asList("All");
	public String multiInvsPlugin = "Multiverse-Inventories";
	public boolean multiInvs = false;

	public String customMessages = "";

	public String kitListMode = "Text";

	public String strKitSign = "[Kit]";
	public String strKitListSign = "[KList]";
	public String strKitSignValid = "[Kit]";
	public String strKitSignInvalid = "[Kit]";
	public String strKitListSignValid = "[KList]";

}
