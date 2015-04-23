package com.faris.kingkits.helper.value;

import org.bukkit.*;

import java.util.Arrays;
import java.util.List;

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
	public List<Integer> dropAnimations = Arrays.asList(Material.BOWL.getId());
	public boolean allowPickingUpItems = true;
	public boolean clearInvOnReload = true;
	public boolean oneKitPerLife = false;
	public boolean removeItemsOnLeave = true;
	public boolean removePotionEffectsOnLeave = true;
	public boolean removeItemsOnCreateKit = true;
	public boolean rightClickCompass = true;
	public boolean quickSoup = true;
	public boolean quickSoupKitOnly = true;
	public double quickSoupHeal = 2.5;
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
	public boolean dropItemsOnFullInventory = false;
	public boolean removeKitOnDeath = true;

	// GUI
	public String guiTitle = "<menucolour>PvP Kits";
	public int guiSize = 36;
	public int guiItemID = Material.STONE_BUTTON.getId();
	public short guiItemData = 0;

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

	public String strSignKit = "[Kit]";
	public String strSignKitList = "[KList]";
	public String strSignValidKit = "[Kit]";
	public String strSignInvalidKit = "[Kit]";
	public String strSignValidKitList = "[KList]";
	public String strSignRefill = "[KRefill]";
	public String strSignRefillValid = "[KRefill]";

}
