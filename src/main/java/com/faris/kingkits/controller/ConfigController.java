package com.faris.kingkits.controller;

import com.comphenix.attributes.Attributes;
import com.faris.easysql.mysql.MySQLDetails;
import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.config.CustomConfiguration;
import com.faris.kingkits.helper.util.*;
import com.faris.kingkits.old.OldKit;
import com.faris.kingkits.player.OfflineKitPlayer;
import com.faris.kingkits.storage.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ConfigController implements Controller {

	private static ConfigController instance = null;
	public static final String CURRENT_CONFIG_VERSION = "3.4";

	private MySQLDetails sqlDetails = null;

	private Map<String, WorldSettings> worldSettingsMap = new HashMap<>();

	/**
	 * Plugin core settings.
	 */

	private boolean updaterEnabled = true;
	private boolean updaterUpdate = false;
	private double autoSavePlayerData = -1D;

	/**
	 * Plugin function settings.
	 */

	private int guiSize = 0;
	private boolean guiShowOnJoin = false;
	private boolean guiShowOnRespawn = false;
	private Material guiItemType = null;
	private short guiItemData = 0;
	private ItemStack guiPreviousButton = new ItemStack(Material.STONE_BUTTON);
	private ItemStack guiNextButton = new ItemStack(Material.STONE_BUTTON);

	private boolean kitDefaultBreakableItems = true;
	private boolean kitDefaultCommandAlias = false;
	private List<String> kitDefaultCommands = new ArrayList<>();
	private double kitDefaultCooldown = 0D;
	private double kitDefaultCost = 0D;
	private Map<Integer, List<String>> kitDefaultKillstreakCommands;
	private double kitDefaultMaxHealth = PlayerUtilities.getDefaultMaxHealth();
	private float kitDefaultWalkSpeed = PlayerUtilities.getDefaultWalkSpeed();

	private boolean multiInventoriesPlugin = false;
	private String multiInventoriesPluginName = "";

	private List<String> pvpWorlds = new ArrayList<>();
	private boolean[] kkCommands = new boolean[9];

	private String[] signKit = new String[3];
	private String[] signKitList = new String[2];
	private String[] signRefill = new String[3];

	private boolean allowBlockPlacingAndBreaking = true;
	private boolean allowDeathMessages = true;
	private boolean allowItemDropping = false;
	private boolean allowItemPicking = false;
	private boolean allowItemDroppingOnDeath = false;
	private boolean allowAdminBypass = true;
	private boolean allowQuickSoup = true;
	private boolean allowRightClickPreview = true;
	private Map<String, String> chatPlaceholders = new HashMap<>();
	private boolean economyEnabled = false;
	private double economyCostPerRefill = 2.5D;
	private double economyMoneyPerKill = 5D, economyMoneyPerDeath = 7.5D;
	private boolean oneKitPerLife = false;
	private boolean scoreEnabled = false;
	private boolean shouldAutoRespawn = false;
	private boolean shouldClearItemsOnKitSelection = true;
	private boolean shouldDecreaseScoreOnAutoUnlock = false;
	private boolean shouldDropItemsOnFullInventory = false;
	private boolean shouldLockFoodLevel = true;
	private boolean shouldPreventCreative = false;
	private boolean shouldRemoveItemsOnLeave = true;
	private boolean shouldRemoveItemsOnReload = true;
	private boolean shouldRemoveKitOnDeath = true;
	private boolean shouldRemovePotionEffectsOnLeave = true;
	private boolean shouldRemovePotionEffectsOnReload = true;
	private boolean shouldShowKitPreview = true;
	private boolean shouldSortKitsAlphanumerically = false;
	private boolean shouldSetCompassToNearestPlayer = false;
	private boolean shouldSetDefaultGamemodeOnKitSelection = true;
	private boolean shouldSetMaxHealth = true;
	private boolean shouldUsePermissionsForKitList = true;

	private double quickSoupHeal = 5D;

	private int foodLevelLock = 20;
	private int scoreMax = Integer.MAX_VALUE;
	private int scorePerKill = 2;
	private int scorePerDeath = 0;

	private List<String> commandsToRunOnDeath = new ArrayList<>();
	private List<Integer> dropAnimationItems = new ArrayList<>();

	private String kitListMode = "GUI";
	private String scoreChatPrefix = "&6[&a<score>&6] &f";

	private ConfigController() {
		File dataFolder = KingKits.getInstance().getDataFolder();

		this.configFile = new File(dataFolder, "config.yml");
		this.config = CustomConfiguration.loadConfiguration(this.configFile);
		this.config.setNewLineAfterHeader(true);
		this.config.setNewLinePerKey(true);
	}

	@Override
	public void shutdownController() {
		this.config = null;
		this.configFile = null;

		instance = null;
	}

	private void saveDefaultConfig() {
		this.getConfig().options().header("KingKits configuration");
		this.getConfig().addDefault("Version", CURRENT_CONFIG_VERSION, "Do NOT modify this, this is for the plugin only.");
		this.getConfig().addDefault("Updater.Enabled", true, "Set to 'false' if you want to disable the checking of new updates.");
		this.getConfig().addDefault("Updater.Update", true, "Set to 'true' if you want to auto-update the plugin if a new version has been found.", "This only works for Bukkit.");
		this.getConfig().addDefault("MySQL", new MySQLDetails().serialize(), "MySQL authentication information and details.");
		this.getConfig().addDefault("Admin bypass", true, "Set to 'true' if you want OPs or players with 'kingkits.admin' node to be able to drop/pickup items and do other activities even if disabled in the config.");
		this.getConfig().addDefault("Auto-save player data", -1D, "Every time the set number of minutes passes, player data is saved asynchronously.", "Set to -1 to disable auto-save.");
		if (!this.getConfig().contains("Allow"))
			this.getConfig().createSection("Allow", "Allow/disallow (enable/disable) various things.");
		this.getConfig().addDefault("Allow.Block modification", true, "Set to 'false' if you want players to not be able to break/place blocks.");
		this.getConfig().addDefault("Allow.Death messages", true, "Set to 'false' if you want to disable vanilla death messages.");
		this.getConfig().addDefault("Allow.Item dropping", true, "Set to 'false' if you want to ban players from dropping items.");
		this.getConfig().addDefault("Allow.Item dropping on death", true, "Set to 'false' if you want to clear all items dropped when a player dies.");
		this.getConfig().addDefault("Allow.Item picking up", true, "Set to 'false' if you want to ban players from picking up items.");
		this.getConfig().addDefault("Allow.Quick soup", true, "Set to 'false' if you want to disable players from using mushroom stew to heal themselves.");
		this.getConfig().addDefault("Allow.Right click preview", true, "Set to 'false' if you do not want players to be able to right click a kit in the GUI to preview it.");
		this.getConfig().addDefault("Chat placeholders.Has kit text", "&a<kit>");
		this.getConfig().addDefault("Chat placeholders.No kit text", "&cNo");
		if (!this.getConfig().contains("Command")) {
			this.getConfig().set("Command", new LinkedHashMap<String, Boolean>() {{
				this.put("Kit", true);
				this.put("Kit create", true);
				this.put("Kit delete", true);
				this.put("Kit rename", true);
				this.put("User kit create", true);
				this.put("User kit delete", true);
				this.put("User kit rename", true);
				this.put("Preview kit", true);
				this.put("Refill", true);
			}}, "Enable/disable commands.");
		}
		if (!this.getConfig().contains("Economy"))
			this.getConfig().createSection("Economy", "Economy settings", "Note: Vault is required if economy is enabled.");
		this.getConfig().addDefault("Economy.Enabled", false);
		this.getConfig().addDefault("Economy.Cost per refill", 2.5D, "The cost of refilling each empty bowl.");
		this.getConfig().addDefault("Economy.Money per kill", 5D, "The amount of money to give to a player when they kill another player.");
		this.getConfig().addDefault("Economy.Money per death", 7.5D, "The amount of money to take when a player dies");
		if (!this.getConfig().contains("Kit defaults"))
			this.getConfig().createSection("Kit defaults", "Default kit options");
		this.getConfig().addDefault("Kit defaults.Breakable items", true, "The value of this is the default value for \"Breakable items\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Command alias", false, "The value of this is the default value for \"Command alias\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Commands", new ArrayList<>(), "The value of this is the default value for \"Commands\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Cooldown", 0D, "The value of this is the default value for \"Cooldown\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Cost", 0D, "The value of this is the default value for \"Cost\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Killstreak commands", new LinkedHashMap<>(), "The value of this is the default value for \"Killstreak commands\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Max health", PlayerUtilities.getDefaultMaxHealth(), "The value of this is the default value for \"Max health\" of a new kit.");
		this.getConfig().addDefault("Kit defaults.Walk speed", PlayerUtilities.getDefaultWalkSpeed(), "The value of this is the default value for \"Walk speed\" of a new kit.");
		if (!this.getConfig().contains("Kit GUI")) this.getConfig().createSection("Kit GUI", "Kit GUI options");
		this.getConfig().addDefault("Kit GUI.Size", 36, "The size of the GUI inventory.");
		this.getConfig().addDefault("Kit GUI.Show on join", false, "Set to 'true' if the Kit GUI should open when a player joins.");
		this.getConfig().addDefault("Kit GUI.Show on respawn", false, "Set to 'true' if the Kit GUI should open when a player respawns.");
		this.getConfig().addDefault("Kit GUI.GUI Item.Type", "Nothing", "The material of the item that opens the Kit GUI when right clicked.");
		this.getConfig().addDefault("Kit GUI.GUI Item.Data", (short) 0, "The data value/durability of the item that opens the Kit GUI when right clicked. Set to -1 if the data value should be ignored.");
		this.getConfig().addDefault("Kit GUI.Next button", ItemUtilities.serializeItem(ItemUtilities.renameItem(new ItemStack(Material.STONE_BUTTON), "<colour>Next")));
		this.getConfig().addDefault("Kit GUI.Previous button", ItemUtilities.serializeItem(ItemUtilities.renameItem(new ItemStack(Material.STONE_BUTTON), "<colour>Back")));
		if (!this.getConfig().contains("Multi-inventories")) this.getConfig().createSection("Multi-inventories");
		this.getConfig().addDefault("Multi-inventories.Enabled", true, "Set this to 'false' if this plugin should handle player inventory clearing when a player changes worlds. Set to 'true' if this plugin should not handle player inventories when changing worlds.");
		this.getConfig().addDefault("Multi-inventories.Plugin", "Multiverse-Inventories", "The name of the plugin that handles inventories per world.");
		this.getConfig().addDefault("Score.Chat prefix", "&6[&a%d&6] &f");
		this.getConfig().addDefault("Score.Enabled", false);
		this.getConfig().addDefault("Score.Max", Integer.MAX_VALUE);
		this.getConfig().addDefault("Score.Per death", 0);
		this.getConfig().addDefault("Score.Per kill", 2);
		this.getConfig().addDefault("Should.Auto-respawn", false, "If a player should automatically respawn when they die, without the respawn GUI showing up.");
		this.getConfig().addDefault("Should.Clear items on kit selection", true, "If the plugin to clear a player's inventory when choosing a kit.");
		this.getConfig().addDefault("Should.Decrease score on auto-unlock", false, "If a player's score should be reset to 0 when they auto-unlock a kit.");
		this.getConfig().addDefault("Should.Drop items on full inventory", false, "If items should drop when a player has a full inventory.");
		this.getConfig().addDefault("Should.Lock food level", true, "If this plugin should lock the food level at a specific amount.");
		this.getConfig().addDefault("Should.Prevent creative", false, "If players should be prevented from going into Creative mode when using a kit.");
		this.getConfig().addDefault("Should.Remove kit on death", true, "If this plugin should clear a player's inventory and their kit status when they die.");
		this.getConfig().addDefault("Should.Remove kit on leave", true, "If a player's inventory should be cleared when they leave in a PvP world.");
		this.getConfig().addDefault("Should.Remove items on reload", false, "If this plugin should clear all online players' inventories when this plugin is unloaded.");
		this.getConfig().addDefault("Should.Remove potion effects on leave", true, "If all potion effects should be removed when a player leaves.");
		this.getConfig().addDefault("Should.Remove potion effects on reload", true, "If all potion effects should be removed when this plugin is unloaded.");
		this.getConfig().addDefault("Should.Set compass to nearest player", false, "If right clicking a compass targets the nearest player.");
		this.getConfig().addDefault("Should.Set max health to kit max health", true, "If this plugin should set the player's max health to the max health set in the kit config.");
		this.getConfig().addDefault("Should.Set to default gamemode on kit selection", true, "Whether or not to set the player's gamemode to the server's default when they choose a kit.");
		this.getConfig().addDefault("Should.Show kit preview", true, "If a GUI opens up with a list of all the items in a kit when a player chooses a kit that they do not have permission to use.");
		this.getConfig().addDefault("Should.Sort kits alphanumerically", false, "If the list of kits should be sorted alphanumerically.");
		this.getConfig().addDefault("Should.Use permissions for kit list", false, "Whether or not to list kits players don't have access to.");
		this.getConfig().addDefault("Sign.Kit.Unregistered", "[Kit]", "The text on the first line of a kit sign.", "Set to '' to disable kit signs.");
		this.getConfig().addDefault("Sign.Kit.Valid", "[&1Kit&0]", "The text on the first line of a sign required to be registered as a kit sign.");
		this.getConfig().addDefault("Sign.Kit.Invalid", "[&cKit&0]", "The text on the first line of a sign shown when a kit sign is broken/invalid.");
		this.getConfig().addDefault("Sign.Kit list.Unregistered", "[KList]", "The text on the first line of a kit list sign.", "Set to '' to disable kit list signs.");
		this.getConfig().addDefault("Sign.Kit list.Valid", "[&1KList&0]", "The text on the first line of a sign required to be registered as a kit list sign.");
		this.getConfig().addDefault("Sign.Refill sign.Unregistered", "[KRefill]", "The text on the first line of a refill sign.", "Set to '' to disable refill signs.");
		this.getConfig().addDefault("Sign.Refill sign.Valid", "[&1KRefill&0]", "The text on the first line of a sign required to be registered as a refill sign.");
		this.getConfig().addDefault("PvP Worlds", new ArrayList<>(Collections.singletonList("All")), "A list of world names where this plugin works on.");
		this.getConfig().addDefault("Kit list mode", "GUI", "The way the kits are listed, there are three options:", "Text - List of chat messages sent with the kit names.", "Fancy - List of clickable messages sent with the kit names.", "GUI - A menu shows up with all the kits and their icons.");
		this.getConfig().addDefault("One kit per life", false);
		this.getConfig().addDefault("Commands to run on death", new ArrayList<>(), "A list of commands to run when a player dies. <player> is automatically replaced with the dead player's username.");
		this.getConfig().addDefault("Drop animation IDs", new ArrayList<>(Arrays.asList(Material.MUSHROOM_SOUP.getId(), Material.GLASS_BOTTLE.getId())), "A list of item IDs that, when dropped and dropping items is disabled, are dropped but get removed when on the floor.");
		this.getConfig().addDefault("Food level lock", 20, "The food level to lock a player's food level at.");
		this.getConfig().addDefault("Quick soup heal", 5D, "The amount of health to heal a player by when they quick soup. 1 heart = 2 health.");
		this.getConfig().options().copyHeader(true);
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public void loadConfiguration() {
		this.saveDefaultConfig();

		try {
			this.updaterEnabled = this.getConfig().getBoolean("Updater.Enabled", true);
			this.updaterUpdate = this.getConfig().getBoolean("Updater.Update", false);
			this.autoSavePlayerData = this.getConfig().getDouble("Auto-save player data", -1D);

			this.sqlDetails = MySQLDetails.deserialize(ObjectUtilities.getMap(this.getConfig().get("MySQL")));
			this.allowAdminBypass = this.getConfig().getBoolean("Admin bypass", true);
			this.allowBlockPlacingAndBreaking = this.getConfig().getBoolean("Allow.Block modification", true);
			this.allowDeathMessages = this.getConfig().getBoolean("Allow.Death messages", true);
			this.allowItemDropping = this.getConfig().getBoolean("Allow.Item dropping", false);
			this.allowItemDroppingOnDeath = this.getConfig().getBoolean("Allow.Item dropping on death", true);
			this.allowItemPicking = this.getConfig().getBoolean("Allow.Item picking up", true);
			this.allowQuickSoup = this.getConfig().getBoolean("Allow.Quick soup", true);
			this.allowRightClickPreview = this.getConfig().getBoolean("Allow.Right click preview", true);
			this.chatPlaceholders.clear();
			for (Map.Entry<String, Object> placeholderEntry : ObjectUtilities.getMap(this.getConfig().get("Chat placeholders")).entrySet()) {
				if (placeholderEntry.getValue() instanceof String) {
					this.chatPlaceholders.put(placeholderEntry.getKey(), String.valueOf(placeholderEntry.getValue()));
				}
			}
			this.kkCommands = new boolean[]{
					this.getConfig().getBoolean("Command.Kit", true),
					this.getConfig().getBoolean("Command.Kit create", true),
					this.getConfig().getBoolean("Command.Kit delete", true),
					this.getConfig().getBoolean("Command.Kit rename", true),
					this.getConfig().getBoolean("Command.User kit create", true),
					this.getConfig().getBoolean("Command.User kit delete", true),
					this.getConfig().getBoolean("Command.User kit rename", true),
					this.getConfig().getBoolean("Command.Preview kit", true),
					this.getConfig().getBoolean("Command.Refill", true)
			};
			this.economyEnabled = this.getConfig().getBoolean("Economy.Enabled", false);
			this.economyCostPerRefill = this.getConfig().getDouble("Economy.Cost per refill", 2.5D);
			this.economyMoneyPerDeath = this.getConfig().getDouble("Economy.Money per death", 5D);
			this.economyMoneyPerKill = this.getConfig().getDouble("Economy.Money per kill", 7.5D);
			this.guiSize = this.getConfig().getInt("Kit GUI.Size", 36);
			this.guiShowOnJoin = this.getConfig().getBoolean("Kit GUI.Show on join", false);
			this.guiShowOnRespawn = this.getConfig().getBoolean("Kit GUI.Show on respawn", false);
			this.guiItemType = Material.matchMaterial(this.getConfig().getString("Kit GUI.GUI Item.Type", "None"));
			this.guiItemData = (short) this.getConfig().getInt("Kit GUI.GUI Item.Data", 0);
			this.guiNextButton = ItemUtilities.deserializeItem(ObjectUtilities.getMap(this.getConfig().get("Kit GUI.Next button")), new ItemStack(Material.STONE_BUTTON));
			this.guiPreviousButton = ItemUtilities.deserializeItem(ObjectUtilities.getMap(this.getConfig().get("Kit GUI.Previous button")), new ItemStack(Material.STONE_BUTTON));
			this.kitDefaultBreakableItems = this.getConfig().getBoolean("Kit defaults.Breakable items", true);
			this.kitDefaultCommandAlias = this.getConfig().getBoolean("Kit defaults.Command alias", false);
			this.kitDefaultCommands = this.getConfig().getStringList("Kit defaults.Commands");
			this.kitDefaultCooldown = this.getConfig().getDouble("Kit defaults.Cooldown", 0D);
			this.kitDefaultCost = this.getConfig().getDouble("Kit defaults.Cost", 0D);
			this.kitDefaultKillstreakCommands = this.convertKillstreaksCommands(ObjectUtilities.getMap(this.getConfig().get("Kit defaults.Killstreak commands")));
			this.kitDefaultMaxHealth = this.getConfig().getDouble("Kit defaults.Max health", PlayerUtilities.getDefaultMaxHealth());
			this.kitDefaultWalkSpeed = (float) this.getConfig().getDouble("Kit defaults.Walk speed", (double) PlayerUtilities.getDefaultWalkSpeed());
			this.multiInventoriesPlugin = this.getConfig().getBoolean("Multi-inventories.Enabled", false);
			this.multiInventoriesPluginName = this.getConfig().getString("Multi-inventories.Plugin", "Multiverse-Inventories");
			this.scoreChatPrefix = ChatUtilities.replaceChatCodes(this.getConfig().getString("Score.Chat prefix", "&6[&a%d&6] &f"));
			this.scoreEnabled = this.getConfig().getBoolean("Score.Enabled", false);
			this.scoreMax = this.getConfig().getInt("Score.Max", Integer.MAX_VALUE);
			this.scorePerDeath = this.getConfig().getInt("Score.Per death", 0);
			this.scorePerKill = this.getConfig().getInt("Score.Per kill", 2);
			this.shouldAutoRespawn = this.getConfig().getBoolean("Should.Auto-respawn", false);
			this.shouldClearItemsOnKitSelection = this.getConfig().getBoolean("Should.Clear items on kit selection", true);
			this.shouldDecreaseScoreOnAutoUnlock = this.getConfig().getBoolean("Should.Decrease score on auto-unlock", false);
			this.shouldDropItemsOnFullInventory = this.getConfig().getBoolean("Should.Drop items on full inventory", false);
			this.shouldLockFoodLevel = this.getConfig().getBoolean("Should.Lock food level", true);
			this.shouldPreventCreative = this.getConfig().getBoolean("Should.Prevent creative", true);
			this.shouldRemoveKitOnDeath = this.getConfig().getBoolean("Should.Remove kit on death", true);
			this.shouldRemoveItemsOnLeave = this.getConfig().getBoolean("Should.Remove kit on leave", true);
			this.shouldRemoveItemsOnReload = this.getConfig().getBoolean("Should.Remove items on reload", true);
			this.shouldRemovePotionEffectsOnLeave = this.getConfig().getBoolean("Should.Remove potion effects on leave", true);
			this.shouldRemovePotionEffectsOnReload = this.getConfig().getBoolean("Should.Remove potion effects on reload", true);
			this.shouldSetCompassToNearestPlayer = this.getConfig().getBoolean("Should.Set compass to nearest player", false);
			this.shouldSetDefaultGamemodeOnKitSelection = this.getConfig().getBoolean("Should.Set to default gamemode on kit selection", true);
			this.shouldSetMaxHealth = this.getConfig().getBoolean("Should.Set max health to kit max health", true);
			this.shouldShowKitPreview = this.getConfig().getBoolean("Should.Show kit preview", true);
			this.shouldSortKitsAlphanumerically = this.getConfig().getBoolean("Should.Sort kits alphanumerically", false);
			this.shouldUsePermissionsForKitList = this.getConfig().getBoolean("Should.Use permissions for kit list", true);
			this.signKit = ChatUtilities.replaceChatCodes(this.getConfig().getString("Sign.Kit.Unregistered", "[Kit]"), this.getConfig().getString("Sign.Kit.Valid", "[&1Kit&0]"), this.getConfig().getString("Sign.Kit.Invalid", "[&cKit&0]"));
			this.signKitList = ChatUtilities.replaceChatCodes(this.getConfig().getString("Sign.Kit list.Unregistered", "[KList]"), this.getConfig().getString("Sign.Kit list.Valid", "[&1KList&0]"));
			this.signRefill = ChatUtilities.replaceChatCodes(this.getConfig().getString("Sign.Refill sign.Unregistered", "[KRefill]"), this.getConfig().getString("Sign.Refill sign.Valid", "[&1KRefill&0]"));
			this.pvpWorlds = this.getConfig().getStringList("PvP Worlds");
			this.kitListMode = this.getConfig().getString("Kit list mode", "GUI");
			this.oneKitPerLife = this.getConfig().getBoolean("One kit per life", false);
			this.commandsToRunOnDeath = this.getConfig().getStringList("Commands to run on death");
			this.dropAnimationItems = this.getConfig().getIntegerList("Drop animation IDs");
			this.foodLevelLock = this.getConfig().getInt("Food level lock", 20);
			this.quickSoupHeal = this.getConfig().getDouble("Quick soup heal", 2.5D);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			this.worldSettingsMap.clear();
			File worldsFolder = new File(KingKits.getInstance().getDataFolder(), "worlds");
			File[] worldsFiles = FileUtilities.getFiles(worldsFolder);
			for (File worldFile : worldsFiles) {
				if (worldFile.getName().endsWith(".yml")) {
					String strWorld = worldFile.getName().substring(0, worldFile.getName().length() - 4);
					CustomConfiguration worldConfig = CustomConfiguration.loadConfiguration(worldFile);
					WorldSettings worldSettings = new WorldSettings();

					worldSettings.allowBlockPlacingAndBreaking = worldConfig.getBoolean("Allow.Block modification", this.getConfig().getBoolean("Allow.Block modification", true));
					worldSettings.allowDeathMessages = worldConfig.getBoolean("Allow.Death messages", this.getConfig().getBoolean("Allow.Death messages", true));
					worldSettings.allowItemDropping = worldConfig.getBoolean("Allow.Item dropping", this.getConfig().getBoolean("Allow.Item dropping", false));
					worldSettings.allowItemDroppingOnDeath = worldConfig.getBoolean("Allow.Item dropping on death", this.getConfig().getBoolean("Allow.Item dropping on death", true));
					worldSettings.allowItemPicking = worldConfig.getBoolean("Allow.Item picking up", this.getConfig().getBoolean("Allow.Item picking up", true));
					worldSettings.allowQuickSoup = worldConfig.getBoolean("Allow.Quick soup", this.getConfig().getBoolean("Allow.Quick soup", true));
					worldSettings.kkCommands = new boolean[]{
							worldConfig.getBoolean("Command.Kit", this.getConfig().getBoolean("Command.Kit", true)),
							worldConfig.getBoolean("Command.Kit create", this.getConfig().getBoolean("Command.Kit create", true)),
							worldConfig.getBoolean("Command.Kit delete", this.getConfig().getBoolean("Command.Kit delete", true)),
							worldConfig.getBoolean("Command.Kit rename", this.getConfig().getBoolean("Command.Kit rename", true)),
							worldConfig.getBoolean("Command.User kit create", this.getConfig().getBoolean("Command.User kit create", true)),
							worldConfig.getBoolean("Command.User kit delete", this.getConfig().getBoolean("Command.User kit delete", true)),
							worldConfig.getBoolean("Command.User kit rename", this.getConfig().getBoolean("Command.User kit rename", true)),
							worldConfig.getBoolean("Command.Preview kit", this.getConfig().getBoolean("Command.Preview kit", true)),
							worldConfig.getBoolean("Command.Refill", this.getConfig().getBoolean("Command.Refill", true))
					};
					worldSettings.economyEnabled = worldConfig.getBoolean("Economy.Enabled", this.getConfig().getBoolean("Economy.Enabled", false));
					worldSettings.economyCostPerRefill = worldConfig.getDouble("Economy.Cost per refill", this.getConfig().getDouble("Economy.Cost per refill", 2.5D));
					worldSettings.economyMoneyPerDeath = worldConfig.getDouble("Economy.Money per death", this.getConfig().getDouble("Economy.Money per death", 5D));
					worldSettings.economyMoneyPerKill = worldConfig.getDouble("Economy.Money per kill", this.getConfig().getDouble("Economy.Money per kill", 7.5D));
					worldSettings.oneKitPerLife = worldConfig.getBoolean("One kit per life", this.getConfig().getBoolean("One kit per life", false));
					worldSettings.shouldClearItemsOnKitSelection = worldConfig.getBoolean("Should.Clear items on kit selection", this.getConfig().getBoolean("Should.Clear items on kit selection", true));
					worldSettings.shouldLockFoodLevel = worldConfig.getBoolean("Should.Lock food level", this.getConfig().getBoolean("Should.Lock food level", true));
					worldSettings.shouldRemoveItemsOnLeave = worldConfig.getBoolean("Should.Remove kit on leave", this.getConfig().getBoolean("Should.Remove kit on leave", true));
					worldSettings.shouldRemoveItemsOnReload = worldConfig.getBoolean("Should.Remove items on reload", this.getConfig().getBoolean("Should.Remove items on reload", true));
					worldSettings.shouldRemovePotionEffectsOnLeave = worldConfig.getBoolean("Should.Remove potion effects on leave", this.getConfig().getBoolean("Should.Remove potion effects on leave", true));
					worldSettings.shouldRemovePotionEffectsOnReload = worldConfig.getBoolean("Should.Remove potion effects on reload", this.getConfig().getBoolean("Should.Remove potion effects on reload", true));
					worldSettings.shouldSetCompassToNearestPlayer = worldConfig.getBoolean("Should.Set compass to nearest player", this.getConfig().getBoolean("Should.Set compass to nearest player", false));
					worldSettings.signKit = ChatUtilities.replaceChatCodes(worldConfig.getString("Sign.Kit.Unregistered", this.getConfig().getString("Sign.Kit.Unregistered", "[Kit]")), worldConfig.getString("Sign.Kit.Valid", this.getConfig().getString("Sign.Kit.Valid", "[&1Kit&0]")), worldConfig.getString("Sign.Kit.Invalid", this.getConfig().getString("Sign.Kit.Invalid", "[&cKit&0]")));
					worldSettings.signKitList = ChatUtilities.replaceChatCodes(worldConfig.getString("Sign.Kit list.Unregistered", this.getConfig().getString("Sign.Kit list.Unregistered", "[KList]")), worldConfig.getString("Sign.Kit list.Valid", this.getConfig().getString("Sign.Kit list.Valid", "[&1KList&0]")));
					worldSettings.signRefill = ChatUtilities.replaceChatCodes(worldConfig.getString("Sign.Refill sign.Unregistered", this.getConfig().getString("Sign.Refill sign.Unregistered", "[KRefill]")), worldConfig.getString("Sign.Refill sign.Valid", this.getConfig().getString("Sign.Refill sign.Valid", "[&1KRefill&0]")));
					worldSettings.dropAnimationItems = worldConfig.contains("Drop animation IDs") ? worldConfig.getIntegerList("Drop animation IDs") : this.getConfig().getIntegerList("Drop animation IDs");

					this.worldSettingsMap.put(strWorld.toLowerCase(), worldSettings);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.checkConfig();
	}

	private void checkConfig() {
		if (!BukkitUtilities.hasPlugin("Vault")) {
			boolean showWarning = false;
			try {
				if (this.economyEnabled) {
					showWarning = true;
					this.economyEnabled = false;
					this.getConfig().set("Economy.Enabled", false);
				}
				for (Map.Entry<String, WorldSettings> worldSettingsEntry : new HashMap<>(this.worldSettingsMap).entrySet()) {
					if (worldSettingsEntry.getValue().economyEnabled) {
						showWarning = true;
						worldSettingsEntry.getValue().economyEnabled = false;
						this.worldSettingsMap.put(worldSettingsEntry.getKey(), worldSettingsEntry.getValue());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (showWarning) {
				Bukkit.getServer().getLogger().warning("Economy is enabled in the config but Vault is not enabled.");
				Bukkit.getServer().getLogger().warning("You can download Vault at: http://dev.bukkit.org/bukkit-plugins/vault/");
			}
		}

		this.saveConfig();
	}

	private Map<Integer, List<String>> convertKillstreaksCommands(Map<String, Object> rawKillstreakCommands) {
		Map<Integer, List<String>> killstreaksCommands = new LinkedHashMap<>();
		for (Map.Entry<String, Object> killstreakEntry : rawKillstreakCommands.entrySet()) {
			try {
				if (killstreakEntry.getValue() instanceof List) {
					String strKillstreak = killstreakEntry.getKey().startsWith("Killstreak ") ? killstreakEntry.getKey().substring(11) : killstreakEntry.getKey();
					if (Utilities.isNumber(Integer.class, strKillstreak)) {
						int killstreak = Integer.parseInt(strKillstreak);
						List<String> commands = Utilities.toStringList((List) killstreakEntry.getValue());
						if (commands != null && !commands.isEmpty())
							killstreaksCommands.put(killstreak, commands);
					}
				} else if (killstreakEntry.getValue() instanceof String) {
					String strKillstreak = killstreakEntry.getKey().startsWith("Killstreak ") ? killstreakEntry.getKey().substring(11) : killstreakEntry.getKey();
					if (Utilities.isNumber(Integer.class, strKillstreak)) {
						int killstreak = Integer.parseInt(strKillstreak);
						String command = (String) killstreakEntry.getValue();
						if (command != null && !command.isEmpty())
							killstreaksCommands.put(killstreak, new ArrayList<>(Collections.singletonList(command)));
					}
				}
			} catch (Exception ignored) {
			}
		}
		return killstreaksCommands;
	}

	public boolean canDropItems(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.allowItemDropping : this.worldSettingsMap.get(lowerCaseWorld).allowItemDropping;
	}

	public boolean canModifyBlocks(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.allowBlockPlacingAndBreaking : this.worldSettingsMap.get(lowerCaseWorld).allowBlockPlacingAndBreaking;
	}

	public boolean canAdminsBypass() {
		return this.allowAdminBypass;
	}

	public boolean canPickupItems(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.allowItemPicking : this.worldSettingsMap.get(lowerCaseWorld).allowItemPicking;
	}

	public boolean canQuickSoup(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.allowQuickSoup : this.worldSettingsMap.get(lowerCaseWorld).allowQuickSoup;
	}

	public double getAutoSavePlayerDataTime() {
		return this.autoSavePlayerData;
	}

	public String getChatPlaceholder(String configKey, String defaultValue) {
		return this.chatPlaceholders.containsKey(configKey) ? this.chatPlaceholders.get(configKey) : defaultValue;
	}

	public boolean[] getCommands(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.kkCommands : this.worldSettingsMap.get(lowerCaseWorld).kkCommands;
	}

	public List<String> getCommandsToRunOnDeath() {
		return this.commandsToRunOnDeath;
	}

	public double getCostPerRefill(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.economyCostPerRefill : this.worldSettingsMap.get(lowerCaseWorld).economyCostPerRefill;
	}

	public List<Integer> getDropAnimationItems(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.dropAnimationItems : this.worldSettingsMap.get(lowerCaseWorld).dropAnimationItems;
	}

	public int getFoodLevelLock() {
		return this.foodLevelLock;
	}

	public short getGuiItemData() {
		return this.guiItemData;
	}

	public Material getGuiItemType() {
		return this.guiItemType;
	}

	public ItemStack getGuiNextButton() {
		return this.guiNextButton;
	}

	public ItemStack getGuiPreviousButton() {
		return this.guiPreviousButton;
	}

	public int getGuiSize() {
		return this.guiSize;
	}

	public boolean getKitDefaultBreakableItems() {
		return this.kitDefaultBreakableItems;
	}

	public boolean getKitDefaultCommandAlias() {
		return this.kitDefaultCommandAlias;
	}

	public List<String> getKitDefaultCommands() {
		return this.kitDefaultCommands;
	}

	public double getKitDefaultCooldown() {
		return this.kitDefaultCooldown;
	}

	public double getKitDefaultCost() {
		return this.kitDefaultCost;
	}

	public Map<Integer, List<String>> getKitDefaultKillstreakCommands() {
		return this.kitDefaultKillstreakCommands;
	}

	public double getKitDefaultMaxHealth() {
		return this.kitDefaultMaxHealth;
	}

	public float getKitDefaultWalkSpeed() {
		return this.kitDefaultWalkSpeed;
	}

	public String getKitListMode() {
		return this.kitListMode;
	}

	public int getMaxScore() {
		return this.scoreMax;
	}

	public double getMoneyPerDeath(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.economyMoneyPerDeath : this.worldSettingsMap.get(lowerCaseWorld).economyMoneyPerDeath;
	}

	public double getMoneyPerKill(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.economyMoneyPerKill : this.worldSettingsMap.get(lowerCaseWorld).economyMoneyPerKill;
	}

	public String getMultiInventoriesPluginName() {
		return this.multiInventoriesPluginName;
	}

	public List<String> getPvPWorlds() {
		return this.pvpWorlds;
	}

	public double getQuickSoupHeal() {
		return this.quickSoupHeal;
	}

	public String getScoreChatPrefix() {
		return this.scoreChatPrefix;
	}

	public int getScorePerDeath() {
		return this.scorePerDeath;
	}

	public int getScorePerKill() {
		return this.scorePerKill;
	}

	public String[] getSignsKit(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.signKit : this.worldSettingsMap.get(lowerCaseWorld).signKit;
	}

	public String[] getSignsKitList(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.signKitList : this.worldSettingsMap.get(lowerCaseWorld).signKitList;
	}

	public String[] getSignsRefill(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.signRefill : this.worldSettingsMap.get(lowerCaseWorld).signRefill;
	}

	public MySQLDetails getSQLDetails() {
		return this.sqlDetails;
	}

	public boolean isMultiInventoriesPluginEnabled() {
		return this.multiInventoriesPlugin;
	}

	public boolean isOneKitPerLife(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.oneKitPerLife : this.worldSettingsMap.get(lowerCaseWorld).oneKitPerLife;
	}

	public boolean isEconomyEnabled(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.economyEnabled : this.worldSettingsMap.get(lowerCaseWorld).economyEnabled;
	}

	public boolean isScoreEnabled() {
		return this.scoreEnabled;
	}

	public void setSQLDetails(MySQLDetails details) {
		this.sqlDetails = details;
	}

	public boolean shouldAllowRightClickPreview() {
		return this.allowRightClickPreview;
	}

	public boolean shouldAutomaticallyUpdate() {
		return this.updaterUpdate;
	}

	public boolean shouldAutoRespawn() {
		return this.shouldAutoRespawn;
	}

	public boolean shouldCheckForUpdates() {
		return this.updaterEnabled;
	}

	public boolean shouldClearItemsOnKitSelection(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldClearItemsOnKitSelection : this.worldSettingsMap.get(lowerCaseWorld).shouldClearItemsOnKitSelection;
	}

	public boolean shouldDecreaseScoreOnAutoUnlock() {
		return this.shouldDecreaseScoreOnAutoUnlock;
	}

	public boolean shouldDropItemsOnFullInventory() {
		return this.shouldDropItemsOnFullInventory;
	}

	public boolean shouldDropItemsOnDeath(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.allowItemDroppingOnDeath : this.worldSettingsMap.get(lowerCaseWorld).allowItemDroppingOnDeath;
	}

	public boolean shouldLockFoodLevel(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldLockFoodLevel : this.worldSettingsMap.get(lowerCaseWorld).shouldLockFoodLevel;
	}

	public boolean shouldPreventCreative() {
		return this.shouldPreventCreative;
	}

	public boolean shouldRemoveKitOnDeath() {
		return this.shouldRemoveKitOnDeath;
	}

	public boolean shouldRemoveItemsOnLeave(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldRemoveItemsOnLeave : this.worldSettingsMap.get(lowerCaseWorld).shouldRemoveItemsOnLeave;
	}

	public boolean shouldRemoveItemsOnReload(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldRemoveItemsOnReload : this.worldSettingsMap.get(lowerCaseWorld).shouldRemoveItemsOnReload;
	}

	public boolean shouldRemovePotionEffectsOnLeave(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldRemovePotionEffectsOnLeave : this.worldSettingsMap.get(lowerCaseWorld).shouldRemovePotionEffectsOnLeave;
	}

	public boolean shouldRemovePotionEffectsOnReload(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldRemovePotionEffectsOnReload : this.worldSettingsMap.get(lowerCaseWorld).shouldRemovePotionEffectsOnReload;
	}

	public boolean shouldSetCompassToNearestPlayer(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.shouldSetCompassToNearestPlayer : this.worldSettingsMap.get(lowerCaseWorld).shouldSetCompassToNearestPlayer;
	}

	public boolean shouldSetDefaultGamemodeOnKitSelection() {
		return this.shouldSetDefaultGamemodeOnKitSelection;
	}

	public boolean shouldSetMaxHealth() {
		return this.shouldSetMaxHealth;
	}

	public boolean shouldShowDeathMessages(World world) {
		String lowerCaseWorld = world == null ? "" : world.getName().toLowerCase();
		return !this.worldSettingsMap.containsKey(lowerCaseWorld) ? this.allowDeathMessages : this.worldSettingsMap.get(lowerCaseWorld).allowDeathMessages;
	}

	public boolean shouldShowGuiOnJoin() {
		return this.guiShowOnJoin;
	}

	public boolean shouldShowGuiOnRespawn() {
		return this.guiShowOnRespawn;
	}

	public boolean shouldShowKitPreview() {
		return this.shouldShowKitPreview;
	}

	public boolean shouldSortKitsAlphanumerically() {
		return this.shouldSortKitsAlphanumerically;
	}

	public boolean shouldUsePermissionsForKitList() {
		return this.shouldUsePermissionsForKitList;
	}

	private File configFile = null;

	private CustomConfiguration config = null;

	public CustomConfiguration getConfig() {
		if (this.config == null || this.configFile == null) this.reloadConfig();
		return this.config;
	}

	public void reloadConfig() {
		if (this.configFile == null) this.configFile = new File(KingKits.getInstance().getDataFolder(), "config.yml");
		this.config = CustomConfiguration.loadConfigurationSafely(this.configFile);
		this.config.setNewLineAfterHeader(true);
		this.config.setNewLinePerKey(true);
	}

	public void saveConfig() {
		try {
			this.config.save(this.configFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private File playersFile = null;

	private FileConfiguration playersConfig = null;

	public void deletePlayersConfig() {
		if (this.playersFile != null) this.playersFile.delete();
	}

	public FileConfiguration getPlayersConfig() {
		if (this.playersFile == null)
			this.playersFile = new File(KingKits.getInstance().getDataFolder(), "players.yml");
		if (this.playersConfig == null) this.playersConfig = YamlConfiguration.loadConfiguration(this.playersFile);
		return this.playersConfig;
	}

	public void savePlayersConfig() {
		if (this.playersFile == null || this.playersConfig == null) return;
		try {
			this.playersConfig.save(this.playersFile);
		} catch (Exception ignored) {
		}
	}

	public void reloadConfigs() {
		this.reloadConfig();
	}

	public void migrateOldConfigs() {
		KingKits plugin = KingKits.getInstance();
		File dataFolder = KingKits.getInstance().getDataFolder();
		if (dataFolder != null && dataFolder.exists()) {
			final String migrationFailedMessage = "Failed to convert old KingKits %s: %s";
			if (!this.getConfig().contains("Version")) {
				plugin.getLogger().log(Level.INFO, "Converting old configurations to KingKits v5.0.0...");

				File oldFolder = new File(dataFolder, "old");
				try {
					if (!oldFolder.exists()) oldFolder.mkdirs();
				} catch (Exception ex) {
					plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "(config.yml, messages.yml, economy.yml, kits.yml, userkits.yml, cooldown.yml, killstreaks.yml, scores.yml)", ""), ex);
					return;
				}

				try {
					File killstreaksFile = new File(dataFolder, "killstreaks.yml");
					File oldKillstreaksFile = new File(oldFolder, "killstreaks.yml");
					if (killstreaksFile.exists()) {
						if (FileUtil.copy(killstreaksFile, oldKillstreaksFile)) {
							FileUtilities.delete(killstreaksFile);
						}
					}

					File configFile = new File(dataFolder, "config.yml");
					if (configFile.exists()) {
						File oldConfigFile = new File(oldFolder, "config.yml");
						if (FileUtil.copy(configFile, oldConfigFile)) {
							if (configFile.delete()) {
								CustomConfiguration newConfig = CustomConfiguration.loadConfiguration(configFile);
								final FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);

								newConfig.set("Version", "3.0");
								newConfig.set("Updater.Enabled", oldConfig.getBoolean("Check for updates", true));
								newConfig.set("Updater.Update", oldConfig.getBoolean("Automatically update", true));
								newConfig.set("MySQL", MySQLDetails.deserialize(ObjectUtilities.getMap(oldConfig.get("MySQL"))).serialize());
								newConfig.set("OP bypass", oldConfig.getBoolean("Op bypass", true));
								newConfig.set("Allow.Block modification", !oldConfig.getBoolean("Disable block placing and breaking", false));
								newConfig.set("Allow.Death messages", !oldConfig.getBoolean("Disable death messages", false));
								newConfig.set("Allow.Item dropping", oldConfig.getBoolean("Drop items", false));
								newConfig.set("Allow.Item dropping on death", oldConfig.getBoolean("Drop items on death", false));
								newConfig.set("Allow.Item picking up", oldConfig.getBoolean("Allow picking up items", true));
								newConfig.set("Allow.Quick soup", oldConfig.getBoolean("Quick soup", true));
								Map<String, Object> existingKeys = ObjectUtilities.getMap(newConfig.get("Command"));
								newConfig.set("Command", new LinkedHashMap<String, Boolean>() {{
									this.put("Kit", oldConfig.getBoolean("Enable kits command", true));
									this.put("Kit create", oldConfig.getBoolean("Enable create kits command", true));
									this.put("Kit delete", oldConfig.getBoolean("Enable delete kits command", true));
									this.put("Kit rename", oldConfig.getBoolean("Enable rename kits command", true));
									this.put("User kit create", oldConfig.getBoolean("Enable create user kits command", true));
									this.put("User kit delete", oldConfig.getBoolean("Enable delete user kits command", true));
									this.put("User kit rename", oldConfig.getBoolean("Enable rename user kits command", true));
									this.put("Preview kit", oldConfig.getBoolean("Enable preview kit command", true));
									this.put("Refill", oldConfig.getBoolean("Enable refill command", true));
								}}, "Enable/disable commands.");
								for (Map.Entry<String, Object> existingKey : existingKeys.entrySet()) {
									if (!newConfig.isSet("Command." + existingKey.getKey()))
										newConfig.set("Command." + existingKey.getKey(), existingKey.getValue());
								}
								newConfig.set("Kit GUI.Size", oldConfig.getInt("GUI.Size", 36));
								String strKitGuiPageButtonType = StringUtilities.capitalizeFully(Material.matchMaterial(String.valueOf(oldConfig.getInt("GUI.Page button.ID"))).name().replace('_', ' '));
								short kitGuiPageButtonData = (short) oldConfig.getInt("GUI.Page button.Data value", 0);
								newConfig.set("Kit GUI.Next button.Type", strKitGuiPageButtonType);
								newConfig.set("Kit GUI.Previous button.Type", strKitGuiPageButtonType);
								if (kitGuiPageButtonData != 0)
									newConfig.set("Kit GUI.Next button.Data", kitGuiPageButtonData);
								if (kitGuiPageButtonData != 0)
									newConfig.set("Kit GUI.Previous button.Data", kitGuiPageButtonData);
								newConfig.set("Kit GUI.Show on join", (oldConfig.getBoolean("List kits on join") && !oldConfig.getString("Kit list mode").equalsIgnoreCase("Text")) || oldConfig.getBoolean("Kit menu on join", false));
								newConfig.set("Kit GUI.Show on respawn", oldConfig.getBoolean("GUI.Show on respawn", false));
								newConfig.set("Multi-inventories.Enabled", oldConfig.getBoolean("Multi-inventories", false));
								newConfig.set("Multi-inventories.Plugin", oldConfig.getString("Multiple world inventories plugin", "Multiverse-Inventories"));
								newConfig.set("Score.Chat prefix", oldConfig.getString("Score chat prefix", "&6[&a%d&6] &f").replace("<score>", "%d"));
								newConfig.set("Score.Enabled", oldConfig.getBoolean("Enable score", false));
								newConfig.set("Score.Max", oldConfig.getInt("Max score", Integer.MAX_VALUE));
								newConfig.set("Score.Per kill", oldConfig.getInt("Score per kill", 0));
								newConfig.set("Should.Clear items on kit selection", oldConfig.getBoolean("Replace items when selecting a kit", true));
								newConfig.set("Should.Decrease score on auto-unlock", oldConfig.getBoolean("Decrease score on auto unlock", false));
								newConfig.set("Should.Drop items on full inventory", oldConfig.getBoolean("Drop items on full inventory", false));
								newConfig.set("Should.Lock food level", oldConfig.getBoolean("Lock hunger level", true));
								newConfig.set("Should.Prevent creative", oldConfig.getBoolean("Disable gamemode while using a kit", false));
								newConfig.set("Should.Remove items on reload", oldConfig.getBoolean("Clear inventories on reload", true));
								newConfig.set("Should.Remove kit on death", oldConfig.getBoolean("Remove items on death", true));
								newConfig.set("Should.Remove kit on leave", oldConfig.getBoolean("Remove items on leave", true));
								newConfig.set("Should.Remove potion effects on leave", oldConfig.getBoolean("Remove potion effects on leave", true));
								newConfig.set("Should.Set compass to nearest player", oldConfig.getBoolean("Set compass target to nearest player", false));
								newConfig.set("Should.Show kit preview", oldConfig.getBoolean("Show kit preview", true));
								newConfig.set("Should.Sort kits alphanumerically", oldConfig.getBoolean("Sort kits alphabetically", true));
								newConfig.set("Should.Use permissions for kit list", oldConfig.getBoolean("Use permissions for kit list", true));
								newConfig.set("Sign.Kit.Unregistered", oldConfig.getString("Kit sign", "[Kit]"));
								newConfig.set("Sign.Kit.Valid", oldConfig.getString("Kit sign valid", "[&1Kit&0]"));
								newConfig.set("Sign.Kit.Invalid", oldConfig.getString("Kit sign invalid", "[&cKit&0]"));
								newConfig.set("Sign.Kit list.Unregistered", oldConfig.getString("Kit list sign", "[KLit]"));
								newConfig.set("Sign.Kit list.Valid", oldConfig.getString("Kit list sign valid", "[&1KList&0]"));
								newConfig.set("Sign.Refill sign.Unregistered", oldConfig.getString("Refill sign", "[KRefill]"));
								newConfig.set("Sign.Refill sign.Valid", oldConfig.getString("Refill sign valid", "[&1KRefill&0]"));
								newConfig.set("PvP Worlds", oldConfig.getStringList("PvP Worlds"));
								newConfig.set("Kit list mode", oldConfig.getString("Kit list mode", "GUI"));
								newConfig.set("One kit per life", oldConfig.getBoolean("One kit per life", false));
								newConfig.set("Commands to run on death", oldConfig.getStringList("Commands to run on death"));
								newConfig.set("Drop animation IDs", oldConfig.getIntegerList("Drop animations"));
								newConfig.set("Food level lock", oldConfig.getInt("Hunger lock", 20));
								newConfig.set("Quick soup heal", oldConfig.getDouble("Quick soup heal", 5D));

								newConfig.save(configFile);

								plugin.getLogger().info("Successfully converted old config.yml into new config.yml.");

								File economyConfigFile = new File(dataFolder, "economy.yml");
								if (economyConfigFile.exists()) {
									File oldEconomyConfigFile = new File(oldFolder, "economy.yml");
									if (FileUtil.copy(economyConfigFile, oldEconomyConfigFile)) {
										if (economyConfigFile.delete()) {
											FileConfiguration oldEconomyConfig = YamlConfiguration.loadConfiguration(oldEconomyConfigFile);

											newConfig.set("Economy.Enabled", oldEconomyConfig.getBoolean("Use economy", false));
											newConfig.set("Economy.Cost per refill", oldEconomyConfig.getDouble("Cost per refill", 2.5D));
											newConfig.set("Economy.Money per kill", oldEconomyConfig.getDouble("Money per kill", 5D));
											newConfig.set("Economy.Money per death", oldEconomyConfig.getDouble("Money per death", 7.5D));

											newConfig.save(configFile);

											plugin.getLogger().info("Successfully converted old economy.yml.");
										} else {
											plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "economy.yml", "Could not delete old config.yml."));
										}
									} else {
										plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "economy.yml", "Could not move old economy.yml to the old folder."));
									}
								}
								this.reloadConfig();
							} else {
								plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "config.yml", "Could not delete old config.yml."));
							}
						} else {
							plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "config.yml", "Could not move old config.yml to the old folder."));
						}
					}
				} catch (Exception ex) {
					plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "config.yml", "Could not convert old config.yml to new config.yml."), ex);
				}
				try {
					File messagesFile = new File(dataFolder, "messages.yml");
					if (messagesFile.exists()) {
						File oldMessagesFile = new File(oldFolder, "messages.yml");
						if (FileUtil.copy(messagesFile, oldMessagesFile)) {
							if (messagesFile.delete()) {
								FileConfiguration newMessagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
								FileConfiguration oldMessagesConfig = YamlConfiguration.loadConfiguration(oldMessagesFile);

								newMessagesConfig.set("General.Command error", oldMessagesConfig.getString("Command.General.Error"));
								newMessagesConfig.set("General.Command permission", oldMessagesConfig.getString("Command.General.No permission"));
								newMessagesConfig.set("General.Command usage", oldMessagesConfig.getString("Command.General.Usage").replace("%s", "%s %s"));
								newMessagesConfig.set("General.Player not found", oldMessagesConfig.getString("Command.General.Not online"));
								newMessagesConfig.set("General.Player command", oldMessagesConfig.getString("Command.General.In-game"));
								newMessagesConfig.set("Command.Create kit.Created", oldMessagesConfig.getString("Command.Create kit.Created"));
								newMessagesConfig.set("Command.Create kit.Overwrote", oldMessagesConfig.getString("Command.Create kit.Overwrite"));
								newMessagesConfig.set("Command.Create user kit.Created", oldMessagesConfig.getString("Command.Create kit.Created"));
								newMessagesConfig.set("Command.Create user kit.Maximum kits", oldMessagesConfig.getString("Command.Create kit.User.Maximum personal kits"));
								newMessagesConfig.set("Command.Create user kit.Overwrote", oldMessagesConfig.getString("Command.Create kit.Overwrite"));
								newMessagesConfig.set("Command.Delete kit.Deleted", oldMessagesConfig.getString("Command.Delete kit.Deleted"));
								newMessagesConfig.set("Command.Delete user kit.Deleted", oldMessagesConfig.getString("Command.Delete kit.Deleted"));
								newMessagesConfig.set("Command.Kit.List.Title", oldMessagesConfig.getString("General.Kit list title").replace("%s", "%d"));
								newMessagesConfig.set("Command.Kit.List.No kits", oldMessagesConfig.getString("General.No kits available"));
								newMessagesConfig.set("Command.Rename kit.Renamed", oldMessagesConfig.getString("Command.Rename kit.Renamed"));
								newMessagesConfig.set("Command.Rename user kit.Renamed", oldMessagesConfig.getString("Command.Rename kit.Renamed"));
								newMessagesConfig.set("Command.Refill.Bowl", oldMessagesConfig.getString("Command.Refill.Bowl"));
								newMessagesConfig.set("Command.Refill.Not enough money", oldMessagesConfig.getString("Command.Refill.Not enough money"));
								newMessagesConfig.set("Command.Refill.Full inventory", oldMessagesConfig.getString("Command.Refill.Full inventory"));
								newMessagesConfig.set("Compass.Player", oldMessagesConfig.getString("Compass.Player"));
								newMessagesConfig.set("Compass.Spawn", oldMessagesConfig.getString("Compass.Spawn"));
								newMessagesConfig.set("Kit.Delay", oldMessagesConfig.getString("Kit.Delay"));
								newMessagesConfig.set("Kit.Illegal characters", oldMessagesConfig.getString("Command.Create kit.Illegal characters"));
								newMessagesConfig.set("Kit.No permission", oldMessagesConfig.getString("Kit.No permission"));
								newMessagesConfig.set("Kit.Not enough money", oldMessagesConfig.getString("Kit.Not enough money"));
								newMessagesConfig.set("Kit.Not found", oldMessagesConfig.getString("Kit.Non-existent"));
								newMessagesConfig.set("Kit.One per life", oldMessagesConfig.getString("Kit.Already chosen"));
								newMessagesConfig.set("Kit.Unlocked", oldMessagesConfig.getString("Kit.Unlocked"));
								newMessagesConfig.set("Sign.Create.No permission", oldMessagesConfig.getString("Sign.Create.No permission"));
								newMessagesConfig.set("Sign.Create.Incorrectly setup", oldMessagesConfig.getString("Sign.General.Incorrectly set up"));
								newMessagesConfig.set("Sign.Use.No permission", oldMessagesConfig.getString("Sign.Use.No permission"));
								newMessagesConfig.set("Time.Second", oldMessagesConfig.getString("Time.Second"));
								newMessagesConfig.set("Time.Seconds", oldMessagesConfig.getString("Time.Seconds"));
								newMessagesConfig.set("Time.Minute", oldMessagesConfig.getString("Time.Minute"));
								newMessagesConfig.set("Time.Minutes", oldMessagesConfig.getString("Time.Minutes"));
								newMessagesConfig.set("Time.Hour", oldMessagesConfig.getString("Time.Hour"));
								newMessagesConfig.set("Time.Hours", oldMessagesConfig.getString("Time.Hours"));
								newMessagesConfig.set("Time.Day", oldMessagesConfig.getString("Time.Day"));
								newMessagesConfig.set("Time.Days", oldMessagesConfig.getString("Time.Days"));

								File oldConfigFile = new File(oldFolder, "config.yml");
								if (oldConfigFile.exists()) {
									FileConfiguration oldConfigYml = YamlConfiguration.loadConfiguration(oldConfigFile);
									newMessagesConfig.set("Kit.Set", oldConfigYml.getString("Custom message"));
								}
								File oldEconomyFile = new File(oldFolder, "economy.yml");
								if (oldEconomyFile.exists()) {
									FileConfiguration oldEconomyYml = YamlConfiguration.loadConfiguration(oldEconomyFile);
									if (oldEconomyYml.contains("Message"))
										newMessagesConfig.set("Economy.Kit cost", oldEconomyYml.getString("Message").replace(" <currency>", "").replace("<currency>", "").replace("<money>", "%.2f"));
									if (oldEconomyYml.contains("Money per death message"))
										newMessagesConfig.set("Economy.Money per death", oldEconomyYml.getString("Money per death message").replace(" <currency>", "").replace("<currency>", "").replace("<money>", "%.2f").replace("<killer>", "%s"));
									if (oldEconomyYml.contains("Money per kill message"))
										newMessagesConfig.set("Economy.Money per kill", oldEconomyYml.getString("Money per kill message").replace(" <currency>", "").replace("<currency>", "").replace("<money>", "%.2f").replace("<target>", "%s"));
								}

								newMessagesConfig.save(messagesFile);

								plugin.getLogger().info("Successfully converted old messages.yml into new messages.yml.");
							} else {
								plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "messages.yml", "Could not delete old messages.yml."));
							}
						} else {
							plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "messages.yml", "Could not move old messages.yml to the old folder."));
						}
					}
				} catch (Exception ex) {
					plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "messages.yml", "Could not convert old messages.yml to new messages.yml."), ex);
				}
				try {
					File configFile = new File(dataFolder, "kits.yml");
					if (configFile.exists()) {
						File oldConfigFile = new File(oldFolder, "kits.yml");
						if (FileUtil.copy(configFile, oldConfigFile)) {
							if (configFile.delete()) {
								FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);

								File kitsFolder = new File(dataFolder, "kits");
								FileUtilities.createDirectory(kitsFolder);

								for (Map.Entry<String, Object> kitEntry : oldConfig.getValues(false).entrySet()) {
									final OldKit oldKit = OldKit.deserialize(ObjectUtilities.getMap(kitEntry.getValue()));
									if (oldKit != null) {
										File kitFile = new File(kitsFolder, kitEntry.getKey() + ".yml");
										if (kitFile.exists()) kitFile.delete();
										CustomConfiguration kitConfig = CustomConfiguration.loadConfiguration(kitFile);
										kitConfig.setNewLinePerKey(true);

										final Kit kit = new Kit(kitEntry.getKey(), oldKit.getItemsWithSlot(), null);

										ItemStack[] kitArmour = new ItemStack[4];
										List<ItemStack> armourItems = oldKit.getArmour();
										for (ItemStack armourItem : armourItems) {
											if (armourItem != null) {
												String strArmourType = armourItem.getType().toString().toLowerCase();
												if (strArmourType.endsWith("helmet"))
													kitArmour[3] = armourItem;
												else if (strArmourType.endsWith("chestplate"))
													kitArmour[2] = armourItem;
												else if (strArmourType.endsWith("leggings") || strArmourType.endsWith("pants"))
													kitArmour[1] = armourItem;
												else if (strArmourType.endsWith("boots"))
													kitArmour[0] = armourItem;
											}
										}
										kit.setArmour(kitArmour);

										kit.setAlias(oldKit.hasAlias());
										kit.setAutoUnlockScore(oldKit.getUnlockScore());
										kit.setCommands(oldKit.getCommands());
										kit.setCooldown(oldKit.getCooldown());
										kit.setCost(oldKit.getCost());
										kit.setDescription(oldKit.getDescription());
										kit.setGuiItem(oldKit.getGuiItem());
										kit.setGuiPosition(oldKit.getGuiPosition());
										kit.setItemsBreakable(oldKit.canItemsBreak());
										kit.setKillstreakCommands(new LinkedHashMap<Integer, List<String>>() {{
											for (Map.Entry<Long, List<String>> killstreakEntry : oldKit.getKillstreaks().entrySet()) {
												this.put((int) Math.min(Math.max(killstreakEntry.getKey(), (long) Integer.MIN_VALUE), (long) Integer.MAX_VALUE), killstreakEntry.getValue());
											}
										}});
										kit.setMaxHealth(oldKit.getMaxHealth());
										kit.setPotionEffects(oldKit.getPotionEffects());

										Map<String, Object> serializedKit = kit.serialize();
										for (Map.Entry<String, Object> serializationEntry : serializedKit.entrySet())
											kitConfig.set(serializationEntry.getKey(), serializationEntry.getValue());
										kitConfig.save(kitFile);
									}
								}

								plugin.getLogger().info("Successfully converted old kits.yml.");
							} else {
								plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "kits.yml", "Could not delete old kits.yml."));
							}
						} else {
							plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "kits.yml", "Could not move old kits.yml to the old folder."));
						}
					}
				} catch (Exception ex) {
					plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "kits.yml", "Could not convert old kits.yml."), ex);
				}

				this.reloadConfigs();
				this.loadConfiguration();
				if (ConfigController.getInstance().getSQLDetails().isEnabled()) {
					SQLController.getInstance();
					DataStorage.setInstance(DataStorage.DataStorageType.SQL);
				} else {
					DataStorage.setInstance(DataStorage.DataStorageType.FILE);
				}

				try {
					Map<UUID, OfflineKitPlayer> cachedPlayers = new HashMap<>();

					File cooldownFile = new File(dataFolder, "cooldown.yml");
					if (cooldownFile.exists()) {
						File oldCooldownFile = new File(oldFolder, "cooldown.yml");
						if (FileUtil.copy(cooldownFile, oldCooldownFile)) {
							if (cooldownFile.delete()) {
								FileConfiguration cooldownConfig = YamlConfiguration.loadConfiguration(oldCooldownFile);
								for (Map.Entry<String, Object> uuidEntry : cooldownConfig.getValues(false).entrySet()) {
									if (Utilities.isUUID(uuidEntry.getKey())) {
										Map<String, Object> configKitCooldowns = ObjectUtilities.getMap(uuidEntry.getValue());
										if (!configKitCooldowns.isEmpty()) {
											Map<String, Long> kitCooldowns = new LinkedHashMap<>();
											for (Map.Entry<String, Object> configKitCooldown : configKitCooldowns.entrySet()) {
												if (Utilities.isNumber(Long.class, configKitCooldown.getValue())) {
													kitCooldowns.put(configKitCooldown.getKey(), Long.parseLong(configKitCooldown.getValue().toString()));
												}
											}
											if (!kitCooldowns.isEmpty()) {
												OfflineKitPlayer offlineKitPlayer = DataStorage.getInstance().loadOfflinePlayer(UUID.fromString(uuidEntry.getKey()));
												long currentTime = System.currentTimeMillis();
												while (true) {
													if (offlineKitPlayer.isLoaded() || System.currentTimeMillis() - currentTime > 1_000L)
														break;
												}
												offlineKitPlayer.setKitTimestamps(kitCooldowns);
												cachedPlayers.put(offlineKitPlayer.getUniqueId(), offlineKitPlayer);
											}
										}
									}
								}
							}
						}
					}

					File scoresFile = new File(dataFolder, "scores.yml");
					if (scoresFile.exists()) {
						File oldScoresFile = new File(oldFolder, "scores.yml");
						if (FileUtil.copy(scoresFile, oldScoresFile)) {
							if (scoresFile.delete()) {
								FileConfiguration scoresConfig = YamlConfiguration.loadConfiguration(oldScoresFile);
								if (scoresConfig.contains("Scores")) {
									Map<String, Object> configScores = ObjectUtilities.getMap(scoresConfig.get("Scores"));
									if (!configScores.isEmpty()) {
										for (Map.Entry<String, Object> configScore : configScores.entrySet()) {
											if (Utilities.isNumber(Integer.class, configScore.getValue())) {
												if (Utilities.isUUID(configScore.getKey())) {
													UUID offlinePlayerUUID = UUID.fromString(configScore.getKey());
													OfflineKitPlayer offlineKitPlayer = cachedPlayers.get(offlinePlayerUUID);
													if (offlineKitPlayer == null) {
														offlineKitPlayer = DataStorage.getInstance().loadOfflinePlayer(offlinePlayerUUID);
														long currentTime = System.currentTimeMillis();
														while (true) {
															if (offlineKitPlayer.isLoaded() || System.currentTimeMillis() - currentTime > 1_000L)
																break;
														}
													}
													offlineKitPlayer.setScore(Math.max(Integer.parseInt(configScore.getValue().toString()), 0));
													cachedPlayers.put(offlineKitPlayer.getUniqueId(), offlineKitPlayer);
												}
											}
										}
									}
								}
							}
						}
					}

					File unlockedKitsFile = new File(dataFolder, "unlockedkits.yml");
					if (unlockedKitsFile.exists()) {
						File oldUnlockedKitsFile = new File(oldFolder, "unlockedkits.yml");
						if (FileUtil.copy(unlockedKitsFile, oldUnlockedKitsFile)) {
							if (unlockedKitsFile.delete()) {
								FileConfiguration unlockedKitsConfig = YamlConfiguration.loadConfiguration(oldUnlockedKitsFile);
								for (String strUUIDEntry : unlockedKitsConfig.getKeys(false)) {
									if (Utilities.isUUID(strUUIDEntry)) {
										UUID offlinePlayerUUID = UUID.fromString(strUUIDEntry);
										OfflineKitPlayer offlineKitPlayer = cachedPlayers.get(offlinePlayerUUID);
										if (offlineKitPlayer == null) {
											offlineKitPlayer = DataStorage.getInstance().loadOfflinePlayer(offlinePlayerUUID);
											long currentTime = System.currentTimeMillis();
											while (true) {
												if (offlineKitPlayer.isLoaded() || System.currentTimeMillis() - currentTime > 1_000L)
													break;
											}
										}
										offlineKitPlayer.setUnlockedKits(unlockedKitsConfig.getStringList(strUUIDEntry));
										cachedPlayers.put(offlineKitPlayer.getUniqueId(), offlineKitPlayer);
									}
								}
							}
						}
					}

					File userKitsFile = new File(dataFolder, "userkits.yml");
					if (userKitsFile.exists()) {
						File oldUserKitsFile = new File(oldFolder, "userkits.yml");
						if (FileUtil.copy(userKitsFile, oldUserKitsFile)) {
							if (userKitsFile.delete()) {
								FileConfiguration userKitsConfig = YamlConfiguration.loadConfiguration(oldUserKitsFile);
								for (Map.Entry<String, Object> playerEntry : userKitsConfig.getValues(false).entrySet()) {
									if (Utilities.isUUID(playerEntry.getKey())) {
										Map<String, Object> configPlayerKits = ObjectUtilities.getMap(playerEntry.getValue());
										if (!configPlayerKits.isEmpty()) {
											Map<String, Kit> playerKits = new LinkedHashMap<>();
											for (Map.Entry<String, Object> configPlayerKit : configPlayerKits.entrySet()) {
												final OldKit oldPlayerKit = OldKit.deserialize(ObjectUtilities.getMap(configPlayerKit.getValue()));
												if (oldPlayerKit != null) {
													final Kit kit = new Kit(configPlayerKit.getKey(), oldPlayerKit.getItemsWithSlot(), null);

													ItemStack[] kitArmour = new ItemStack[4];
													List<ItemStack> armourItems = oldPlayerKit.getArmour();
													for (ItemStack armourItem : armourItems) {
														if (armourItem != null) {
															String strArmourType = armourItem.getType().toString().toLowerCase();
															if (strArmourType.endsWith("helmet"))
																kitArmour[3] = armourItem;
															else if (strArmourType.endsWith("chestplate"))
																kitArmour[2] = armourItem;
															else if (strArmourType.endsWith("leggings") || strArmourType.endsWith("pants"))
																kitArmour[1] = armourItem;
															else if (strArmourType.endsWith("boots"))
																kitArmour[0] = armourItem;
														}
													}
													kit.setArmour(kitArmour);

													kit.setAlias(oldPlayerKit.hasAlias());
													kit.setAutoUnlockScore(oldPlayerKit.getUnlockScore());
													kit.setCommands(oldPlayerKit.getCommands());
													kit.setCooldown(oldPlayerKit.getCooldown());
													kit.setCost(oldPlayerKit.getCost());
													kit.setDescription(oldPlayerKit.getDescription());
													kit.setGuiItem(oldPlayerKit.getGuiItem());
													kit.setGuiPosition(oldPlayerKit.getGuiPosition());
													kit.setItemsBreakable(oldPlayerKit.canItemsBreak());
													kit.setKillstreakCommands(new LinkedHashMap<Integer, List<String>>() {{
														for (Map.Entry<Long, List<String>> killstreakEntry : oldPlayerKit.getKillstreaks().entrySet()) {
															this.put((int) Math.min(Math.max(killstreakEntry.getKey(), (long) Integer.MIN_VALUE), (long) Integer.MAX_VALUE), killstreakEntry.getValue());
														}
													}});
													kit.setMaxHealth(oldPlayerKit.getMaxHealth());
													kit.setPotionEffects(oldPlayerKit.getPotionEffects());

													playerKits.put(kit.getName(), kit);
												}
											}
											if (!playerKits.isEmpty()) {
												UUID offlinePlayerUUID = UUID.fromString(playerEntry.getKey());
												OfflineKitPlayer offlineKitPlayer = cachedPlayers.get(offlinePlayerUUID);
												if (offlineKitPlayer == null) {
													offlineKitPlayer = DataStorage.getInstance().loadOfflinePlayer(offlinePlayerUUID);
													long currentTime = System.currentTimeMillis();
													while (true) {
														if (offlineKitPlayer.isLoaded() || System.currentTimeMillis() - currentTime > 1_000L)
															break;
													}
												}
												offlineKitPlayer.setKits(playerKits);
												cachedPlayers.put(offlineKitPlayer.getUniqueId(), offlineKitPlayer);
											}
										}
									}
								}
							}
						}
					}

					int successful = 0;
					boolean printedError = false;
					for (OfflineKitPlayer cachedPlayer : cachedPlayers.values()) {
						try {
							cachedPlayer.setModified(true);
							PlayerController.getInstance().saveOfflinePlayer(cachedPlayer);
							successful++;
						} catch (Exception ex) {
							if (!printedError) {
								ex.printStackTrace();
								printedError = true;
							}
						}
					}
					plugin.getLogger().info("Successfully migrated " + successful + "/" + cachedPlayers.size() + " old player data.");
				} catch (Exception ex) {
					plugin.getLogger().log(Level.WARNING, "Failed to migrate old player data.", ex);
				}
			}
			if (this.getConfig().getString("Version", "").startsWith("3.0")) {
				// 1.9 changes
				String oldVersion = this.getConfig().getString("Version");
				plugin.getLogger().log(Level.INFO, "Converting config v" + oldVersion + " to v3.1...");

				this.getConfig().set("Version", "3.1");
				this.saveConfig();

				this.reloadConfigs();
				this.loadConfiguration();
				try {
					if (ConfigController.getInstance().getSQLDetails().isEnabled()) {
						SQLController.getInstance();
						DataStorage.setInstance(DataStorage.DataStorageType.SQL);
					} else {
						DataStorage.setInstance(DataStorage.DataStorageType.FILE);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				try {
					File kitsFolder = new File(KingKits.getInstance().getDataFolder(), "kits");
					if (kitsFolder.exists()) {
						File[] kitFiles = FileUtilities.getFiles(kitsFolder);
						if (kitFiles.length > 0) {
							File oldFolder = new File(dataFolder, "old" + oldVersion);
							try {
								if (!oldFolder.exists()) oldFolder.mkdirs();
							} catch (Exception ex) {
								plugin.getLogger().log(Level.WARNING, String.format(migrationFailedMessage, "(kits)", ""), ex);
								oldFolder = null;
							}

							for (File kitFile : kitFiles) {
								if (kitFile.getName().endsWith(".yml")) {
									CustomConfiguration kitConfig = CustomConfiguration.loadConfiguration(kitFile);
									kitConfig.setNewLineAfterHeader(true);
									kitConfig.setNewLinePerKey(true);

									boolean save = false;
									if (kitConfig.contains("Items")) {
										Map<String, Object> itemsMap = ObjectUtilities.getMap(kitConfig.get("Items"));
										boolean saveItems = false;
										for (Map.Entry<String, Object> serializedItemEntry : new LinkedHashMap<>(itemsMap).entrySet()) {
											Map<String, Object> serializedItem = ObjectUtilities.getMap(serializedItemEntry.getValue());
											boolean saveItem = false;
											if (!serializedItem.isEmpty()) {
												if (serializedItem.containsKey("Data")) {
													saveItem = true;
													serializedItem.remove("Data");
												}
												if (oldVersion.equals("3.0") && ObjectUtilities.getObject(serializedItem, String.class, "Type", "").toLowerCase().endsWith("potion")) {
													if (serializedItem.containsKey("Potion")) {
														saveItem = true;
														serializedItem.remove("Potion");
													}
													if (serializedItem.containsKey("Durability")) {
														short durability = ObjectUtilities.getObject(serializedItem, Number.class, "Durability", (short) 0).shortValue();
														String potionType = "";
														if (durability == 16) {
															potionType = "minecraft:awkward"; // Awkward
														} else if (durability == 32) {
															potionType = "minecraft:thick"; // Thick
														} else if (durability == 64) {
															potionType = "minecraft:mundane"; // Mundane (extended)
														} else if (durability == 8192) {
															potionType = "minecraft:mundane"; // Mundane
														} else if (isPotion(durability, 8193)) {
															potionType = "minecraft:regeneration"; // Regeneration
														} else if (isPotion(durability, 8257)) {
															potionType = "minecraft:long_regeneration"; // Regeneration (extended)
														} else if (isPotion(durability, 8225)) {
															potionType = "minecraft:strong_regeneration"; // Regeneration II
														} else if (isPotion(durability, 8194)) {
															potionType = "minecraft:swiftness"; // Swiftness
														} else if (isPotion(durability, 8258)) {
															potionType = "minecraft:long_swiftness"; // Swiftness (extended)
														} else if (isPotion(durability, 8226)) {
															potionType = "minecraft:strong_swiftness"; // Swiftness II
														} else if (isPotion(durability, 8195)) {
															potionType = "minecraft:fire_resistance"; // Fire Resistance
														} else if (isPotion(durability, 8259)) {
															potionType = "minecraft:long_fire_resistance"; // Fire Resistance (extended)
														} else if (isPotion(durability, 8197)) {
															potionType = "minecraft:healing"; // Healing
														} else if (isPotion(durability, 8229)) {
															potionType = "minecraft:strong_healing"; // Healing II
														} else if (isPotion(durability, 8198)) {
															potionType = "minecraft:night_vision"; // Night Vision
														} else if (isPotion(durability, 8262)) {
															potionType = "minecraft:long_night_vision"; // Night Vision (extended)
														} else if (isPotion(durability, 8201)) {
															potionType = "minecraft:strength"; // Strength
														} else if (isPotion(durability, 8265)) {
															potionType = "minecraft:long_strength"; // Strength (extended)
														} else if (isPotion(durability, 8233)) {
															potionType = "minecraft:strong_strength"; // Strength II
														} else if (isPotion(durability, 8203)) {
															potionType = "minecraft:leaping"; // Leaping
														} else if (isPotion(durability, 8267)) {
															potionType = "minecraft:long_leaping"; // Leaping (extended)
														} else if (isPotion(durability, 8235)) {
															potionType = "minecraft:strong_leaping"; // Leaping II
														} else if (isPotion(durability, 8205)) {
															potionType = "minecraft:water_breathing"; // Water Breathing
														} else if (isPotion(durability, 8269)) {
															potionType = "minecraft:long_water_breathing"; // Water Breathing (extended)
														} else if (isPotion(durability, 8206)) {
															potionType = "minecraft:invisibility"; // Invisibility
														} else if (isPotion(durability, 8270)) {
															potionType = "minecraft:long_invisibility"; // Invisibility (extended)
														} else if (isPotion(durability, 8196)) {
															potionType = "minecraft:poison"; // Poison
														} else if (isPotion(durability, 8260)) {
															potionType = "minecraft:long_poison"; // Poison (extended)
														} else if (isPotion(durability, 8228)) {
															potionType = "minecraft:strong_poison"; // Poison II
														} else if (isPotion(durability, 8200)) {
															potionType = "minecraft:weakness"; // Weakness
														} else if (isPotion(durability, 8264)) {
															potionType = "minecraft:long_weakness"; // Weakness (extended)
														} else if (isPotion(durability, 8202)) {
															potionType = "minecraft:slowness"; // Slowness
														} else if (isPotion(durability, 8266)) {
															potionType = "minecraft:long_slowness"; // Slowness (extended)
														} else if (isPotion(durability, 8204)) {
															potionType = "minecraft:harming"; // Harming
														} else if (isPotion(durability, 8236)) {
															potionType = "minecraft:strong_harming"; // Harming II
														} else if (isPotion(durability, 8289)) {
															potionType = "minecraft:strong_regeneration"; // Regeneration II
														} else if (isPotion(durability, 8290)) {
															potionType = "minecraft:strong_swiftness"; // Swiftness II
														} else if (isPotion(durability, 8297)) {
															potionType = "minecraft:strong_strength"; // Strength II
														} else if (isPotion(durability, 8292)) {
															potionType = "minecraft:strong_poison"; // Poison II
														} else if (durability == 373) {
															potionType = "minecraft:empty"; // Empty
														}
														if (durability > 16000)
															serializedItem.put("Type", "Splash Potion");

														saveItem = true;
														serializedItem.remove("Durability");
														if (!potionType.isEmpty())
															serializedItem.put("Potion", potionType);
													}
												}
												if (saveItem) {
													saveItems = true;
													itemsMap.put(serializedItemEntry.getKey(), serializedItem);
												}
											}
										}
										if (saveItems) {
											kitConfig.set("Items", itemsMap);
											save = true;
										}
									}
									if (oldVersion.equals("3.0") && kitConfig.contains("Potion effects")) {
										Map<String, Object> serializedPotionEffects = ObjectUtilities.getMap(kitConfig.get("Potion effects"));
										boolean savePotionEffects = false;
										for (Map.Entry<String, Object> serializedPotionEffectEntry : new LinkedHashMap<>(serializedPotionEffects).entrySet()) {
											Map<String, Object> serializedPotionEffect = ObjectUtilities.getMap(serializedPotionEffectEntry.getValue());
											if (serializedPotionEffect.containsKey("Duration")) {
												double duration = ObjectUtilities.getObject(serializedPotionEffect, Number.class, "Duration").doubleValue();
												if (duration >= 0L) {
													duration = duration / 20D;
													savePotionEffects = true;
													serializedPotionEffect.put("Duration", duration);
													serializedPotionEffects.put(serializedPotionEffectEntry.getKey(), serializedPotionEffect);
												}
											}
										}
										if (savePotionEffects) {
											kitConfig.set("Potion effects", serializedPotionEffects);
											save = true;
										}
									}
									if (save) {
										if (oldFolder == null) {
											kitConfig.save(kitFile);
											continue;
										}
										FileUtil.copy(kitFile, new File(oldFolder, kitFile.getName()));
										FileUtilities.delete(kitFile);
										kitConfig.save(kitFile);
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (this.getConfig().getString("Version", "").equals("3.1")) {
				// 1.9 changes
				plugin.getLogger().log(Level.INFO, "Converting config v3.1 to v3.2...");

				this.getConfig().set("Version", "3.2");
				this.saveConfig();

				try {
					File kitsFolder = new File(KingKits.getInstance().getDataFolder(), "kits");
					if (kitsFolder.exists()) {
						File[] kitFiles = FileUtilities.getFiles(kitsFolder);
						if (kitFiles.length > 0) {
							File oldFolder = new File(dataFolder, "old3.1");
							try {
								if (!oldFolder.exists()) oldFolder.mkdirs();
							} catch (Exception ex) {
								plugin.getLogger().log(Level.WARNING, "Could not create 'old' folder", ex);
								oldFolder = null;
							}

							for (File kitFile : kitFiles) {
								if (kitFile.getName().endsWith(".yml")) {
									CustomConfiguration kitConfig = CustomConfiguration.loadConfiguration(kitFile);
									kitConfig.setNewLineAfterHeader(true);
									kitConfig.setNewLinePerKey(true);

									boolean save = false;
									if (kitConfig.contains("Items")) {
										Map<String, Object> itemsMap = ObjectUtilities.getMap(kitConfig.get("Items"));
										boolean saveItems = false;
										for (Map.Entry<String, Object> serializedItemEntry : new LinkedHashMap<>(itemsMap).entrySet()) {
											Map<String, Object> serializedItem = ObjectUtilities.getMap(serializedItemEntry.getValue());
											boolean saveItem = false;
											if (!serializedItem.isEmpty()) {
												if (serializedItem.containsKey("Potion")) {
													String strPotionType = ObjectUtilities.getObject(serializedItem, String.class, "Potion");
													if (strPotionType != null) {
														PotionData potionData = null;
														if (strPotionType.equalsIgnoreCase("minecraft:awkward")) {
															potionData = new PotionData(PotionType.AWKWARD);
														} else if (strPotionType.equalsIgnoreCase("minecraft:thick")) {
															potionData = new PotionData(PotionType.THICK);
														} else if (strPotionType.equalsIgnoreCase("minecraft:mundane")) {
															potionData = new PotionData(PotionType.MUNDANE, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:mundane")) {
															potionData = new PotionData(PotionType.MUNDANE);
														} else if (strPotionType.equalsIgnoreCase("minecraft:regeneration")) {
															potionData = new PotionData(PotionType.REGEN);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_regeneration")) {
															potionData = new PotionData(PotionType.REGEN, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_regeneration")) {
															potionData = new PotionData(PotionType.REGEN, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:swiftness")) {
															potionData = new PotionData(PotionType.SPEED);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_swiftness")) {
															potionData = new PotionData(PotionType.SPEED, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_swiftness")) {
															potionData = new PotionData(PotionType.SPEED, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:fire_resistance")) {
															potionData = new PotionData(PotionType.FIRE_RESISTANCE);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_fire_resistance")) {
															potionData = new PotionData(PotionType.FIRE_RESISTANCE, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:healing")) {
															potionData = new PotionData(PotionType.INSTANT_HEAL);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_healing")) {
															potionData = new PotionData(PotionType.INSTANT_HEAL, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:night_vision")) {
															potionData = new PotionData(PotionType.NIGHT_VISION);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_night_vision")) {
															potionData = new PotionData(PotionType.NIGHT_VISION, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strength")) {
															potionData = new PotionData(PotionType.STRENGTH);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_strength")) {
															potionData = new PotionData(PotionType.STRENGTH, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_strength")) {
															potionData = new PotionData(PotionType.STRENGTH, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:leaping")) {
															potionData = new PotionData(PotionType.JUMP);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_leaping")) {
															potionData = new PotionData(PotionType.JUMP, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_leaping")) {
															potionData = new PotionData(PotionType.JUMP, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:water_breathing")) {
															potionData = new PotionData(PotionType.WATER_BREATHING);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_water_breathing")) {
															potionData = new PotionData(PotionType.WATER_BREATHING, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:invisibility")) {
															potionData = new PotionData(PotionType.INVISIBILITY);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_invisibility")) {
															potionData = new PotionData(PotionType.INVISIBILITY, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:poison")) {
															potionData = new PotionData(PotionType.POISON);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_poison")) {
															potionData = new PotionData(PotionType.POISON, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_poison")) {
															potionData = new PotionData(PotionType.POISON, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:weakness")) {
															potionData = new PotionData(PotionType.WEAKNESS);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_weakness")) {
															potionData = new PotionData(PotionType.WEAKNESS, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:slowness")) {
															potionData = new PotionData(PotionType.SLOWNESS);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_slowness")) {
															potionData = new PotionData(PotionType.SLOWNESS, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:harming")) {
															potionData = new PotionData(PotionType.INSTANT_DAMAGE);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_harming")) {
															potionData = new PotionData(PotionType.INSTANT_DAMAGE, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_regeneration")) {
															potionData = new PotionData(PotionType.REGEN, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_swiftness")) {
															potionData = new PotionData(PotionType.SPEED, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_strength")) {
															potionData = new PotionData(PotionType.STRENGTH, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_poison")) {
															potionData = new PotionData(PotionType.POISON, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:empty")) {
															potionData = new PotionData(PotionType.WATER);
														}
														if (potionData != null) {
															serializedItem.put("Potion", ItemUtilities.serializePotionData(potionData));
															saveItem = true;
														}
													}
												}
												if (saveItem) {
													saveItems = true;
													itemsMap.put(serializedItemEntry.getKey(), serializedItem);
												}
											}
										}
										if (saveItems) {
											kitConfig.set("Items", itemsMap);
											save = true;
										}
									}
									if (save) {
										if (oldFolder == null) {
											kitConfig.save(kitFile);
											continue;
										}
										FileUtil.copy(kitFile, new File(oldFolder, kitFile.getName()));
										FileUtilities.delete(kitFile);
										kitConfig.save(kitFile);
									}
								}
							}

							if (FileUtilities.getFiles(oldFolder).length == 0) FileUtilities.delete(oldFolder);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				this.reloadConfigs();
				this.loadConfiguration();
				try {
					if (ConfigController.getInstance().getSQLDetails().isEnabled()) {
						SQLController.getInstance();
						DataStorage.setInstance(DataStorage.DataStorageType.SQL);
					} else {
						DataStorage.setInstance(DataStorage.DataStorageType.FILE);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (this.getConfig().getString("Version", "").equals("3.2")) {
				// 1.9 changes
				plugin.getLogger().log(Level.INFO, "Converting config v3.2 to v3.3...");

				this.getConfig().set("Version", "3.3");
				this.saveConfig();

				try {
					File kitsFolder = new File(KingKits.getInstance().getDataFolder(), "kits");
					if (kitsFolder.exists()) {
						File[] kitFiles = FileUtilities.getFiles(kitsFolder);
						if (kitFiles.length > 0) {
							File oldFolder = new File(dataFolder, "old3.2");
							try {
								if (!oldFolder.exists()) oldFolder.mkdirs();
							} catch (Exception ex) {
								plugin.getLogger().log(Level.WARNING, "Could not create 'old' folder", ex);
								oldFolder = null;
							}

							for (File kitFile : kitFiles) {
								if (kitFile.getName().endsWith(".yml")) {
									CustomConfiguration kitConfig = CustomConfiguration.loadConfiguration(kitFile);
									kitConfig.setNewLineAfterHeader(true);
									kitConfig.setNewLinePerKey(true);

									boolean save = false;
									if (kitConfig.contains("Items")) {
										Map<String, Object> itemsMap = ObjectUtilities.getMap(kitConfig.get("Items"));
										boolean saveItems = false;
										for (Map.Entry<String, Object> serializedItemEntry : new LinkedHashMap<>(itemsMap).entrySet()) {
											Map<String, Object> serializedItem = ObjectUtilities.getMap(serializedItemEntry.getValue());
											boolean saveItem = false;
											if (!serializedItem.isEmpty()) {
												String strType = ObjectUtilities.getObject(serializedItem, String.class, "Type", "");
												if (strType.toUpperCase().replace(' ', '_').equalsIgnoreCase("TIPPED_ARROW") && serializedItem.get("Potion") instanceof String) {
													String strPotionType = ObjectUtilities.getObject(serializedItem, String.class, "Potion");
													if (strPotionType != null) {
														PotionData potionData = null;
														if (strPotionType.equalsIgnoreCase("minecraft:awkward")) {
															potionData = new PotionData(PotionType.AWKWARD);
														} else if (strPotionType.equalsIgnoreCase("minecraft:thick")) {
															potionData = new PotionData(PotionType.THICK);
														} else if (strPotionType.equalsIgnoreCase("minecraft:mundane")) {
															potionData = new PotionData(PotionType.MUNDANE, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:mundane")) {
															potionData = new PotionData(PotionType.MUNDANE);
														} else if (strPotionType.equalsIgnoreCase("minecraft:regeneration")) {
															potionData = new PotionData(PotionType.REGEN);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_regeneration")) {
															potionData = new PotionData(PotionType.REGEN, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_regeneration")) {
															potionData = new PotionData(PotionType.REGEN, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:swiftness")) {
															potionData = new PotionData(PotionType.SPEED);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_swiftness")) {
															potionData = new PotionData(PotionType.SPEED, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_swiftness")) {
															potionData = new PotionData(PotionType.SPEED, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:fire_resistance")) {
															potionData = new PotionData(PotionType.FIRE_RESISTANCE);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_fire_resistance")) {
															potionData = new PotionData(PotionType.FIRE_RESISTANCE, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:healing")) {
															potionData = new PotionData(PotionType.INSTANT_HEAL);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_healing")) {
															potionData = new PotionData(PotionType.INSTANT_HEAL, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:night_vision")) {
															potionData = new PotionData(PotionType.NIGHT_VISION);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_night_vision")) {
															potionData = new PotionData(PotionType.NIGHT_VISION, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strength")) {
															potionData = new PotionData(PotionType.STRENGTH);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_strength")) {
															potionData = new PotionData(PotionType.STRENGTH, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_strength")) {
															potionData = new PotionData(PotionType.STRENGTH, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:leaping")) {
															potionData = new PotionData(PotionType.JUMP);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_leaping")) {
															potionData = new PotionData(PotionType.JUMP, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_leaping")) {
															potionData = new PotionData(PotionType.JUMP, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:water_breathing")) {
															potionData = new PotionData(PotionType.WATER_BREATHING);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_water_breathing")) {
															potionData = new PotionData(PotionType.WATER_BREATHING, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:invisibility")) {
															potionData = new PotionData(PotionType.INVISIBILITY);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_invisibility")) {
															potionData = new PotionData(PotionType.INVISIBILITY, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:poison")) {
															potionData = new PotionData(PotionType.POISON);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_poison")) {
															potionData = new PotionData(PotionType.POISON, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_poison")) {
															potionData = new PotionData(PotionType.POISON, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:weakness")) {
															potionData = new PotionData(PotionType.WEAKNESS);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_weakness")) {
															potionData = new PotionData(PotionType.WEAKNESS, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:slowness")) {
															potionData = new PotionData(PotionType.SLOWNESS);
														} else if (strPotionType.equalsIgnoreCase("minecraft:long_slowness")) {
															potionData = new PotionData(PotionType.SLOWNESS, true, false);
														} else if (strPotionType.equalsIgnoreCase("minecraft:harming")) {
															potionData = new PotionData(PotionType.INSTANT_DAMAGE);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_harming")) {
															potionData = new PotionData(PotionType.INSTANT_DAMAGE, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_regeneration")) {
															potionData = new PotionData(PotionType.REGEN, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_swiftness")) {
															potionData = new PotionData(PotionType.SPEED, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_strength")) {
															potionData = new PotionData(PotionType.STRENGTH, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:strong_poison")) {
															potionData = new PotionData(PotionType.POISON, false, true);
														} else if (strPotionType.equalsIgnoreCase("minecraft:empty")) {
															potionData = new PotionData(PotionType.WATER);
														}
														if (potionData != null) {
															serializedItem.put("Potion", ItemUtilities.serializePotionData(potionData));
															saveItem = true;
														}
													}
												}
												if (saveItem) {
													saveItems = true;
													itemsMap.put(serializedItemEntry.getKey(), serializedItem);
												}
											}
										}
										if (saveItems) {
											kitConfig.set("Items", itemsMap);
											save = true;
										}
									}
									if (save) {
										if (oldFolder == null) {
											kitConfig.save(kitFile);
											continue;
										}
										FileUtil.copy(kitFile, new File(oldFolder, kitFile.getName()));
										FileUtilities.delete(kitFile);
										kitConfig.save(kitFile);
									}
								}
							}

							if (FileUtilities.getFiles(oldFolder).length == 0) FileUtilities.delete(oldFolder);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				this.reloadConfigs();
				this.loadConfiguration();
				try {
					if (ConfigController.getInstance().getSQLDetails().isEnabled()) {
						SQLController.getInstance();
						DataStorage.setInstance(DataStorage.DataStorageType.SQL);
					} else {
						DataStorage.setInstance(DataStorage.DataStorageType.FILE);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (this.getConfig().getString("Version", "").equals("3.3")) {
				plugin.getLogger().log(Level.INFO, "Converting config v3.3 to v3.4...");

				this.getConfig().set("Version", "3.4");
				this.saveConfig();

				try {
					if (this.getConfig().contains("OP bypass")) {
						this.getConfig().set("Admin bypass", this.getConfig().getBoolean("OP bypass"));
						this.getConfig().set("OP bypass", null);
						this.saveConfig();
					}

					File kitsFolder = new File(KingKits.getInstance().getDataFolder(), "kits");
					if (kitsFolder.exists()) {
						File[] kitFiles = FileUtilities.getFiles(kitsFolder);
						if (kitFiles.length > 0) {
							File oldFolder = new File(dataFolder, "old3.3");
							try {
								if (!oldFolder.exists()) oldFolder.mkdirs();
							} catch (Exception ex) {
								plugin.getLogger().log(Level.WARNING, "Could not create 'old' folder", ex);
								oldFolder = null;
							}

							for (File kitFile : kitFiles) {
								if (kitFile.getName().endsWith(".yml")) {
									CustomConfiguration kitConfig = CustomConfiguration.loadConfiguration(kitFile);
									kitConfig.setNewLineAfterHeader(true);
									kitConfig.setNewLinePerKey(true);

									boolean save = false;
									if (kitConfig.contains("Items")) {
										Map<String, Object> itemsMap = ObjectUtilities.getMap(kitConfig.get("Items"));
										for (Map.Entry<String, Object> serializedItemEntry : new LinkedHashMap<>(itemsMap).entrySet()) {
											Map<String, Object> serializedItem = ObjectUtilities.getMap(serializedItemEntry.getValue());
											if (!serializedItem.isEmpty()) {
												if (serializedItem.containsKey("Attributes")) {
													Map<String, Object> serializedAttributes = ObjectUtilities.getMap(serializedItem.get("Attributes"));
													for (Map.Entry<String, Object> serializedAttributesEntry : serializedAttributes.entrySet()) {
														Map<String, Object> serializedAttribute = ObjectUtilities.getMap(serializedAttributesEntry.getValue());
														if (serializedAttribute.containsKey("Type")) {
															String strKeyPrefix = "Items." + serializedItemEntry.getKey() + ".Attributes." + serializedAttributesEntry.getKey();
															String strAttrType = ObjectUtilities.getObject(serializedAttribute, String.class, "Type", "");
															save = true;
															kitConfig.set(strKeyPrefix + ".Name", strAttrType.replace(' ', '_').toUpperCase());
															Attributes.AttributeType deserializedAttrType = Attributes.AttributeType.valueOf(strAttrType);
															if (deserializedAttrType != null) {
																kitConfig.set(strKeyPrefix + ".Type", deserializedAttrType.getMinecraftId());
															}
														}
													}
												}
											}
										}
									}
									if (save) {
										if (oldFolder == null) {
											kitConfig.save(kitFile);
											continue;
										}
										FileUtil.copy(kitFile, new File(oldFolder, kitFile.getName()));
										FileUtilities.delete(kitFile);
										kitConfig.save(kitFile);
									}
								}
							}

							if (FileUtilities.getFiles(oldFolder).length == 0) FileUtilities.delete(oldFolder);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				this.reloadConfigs();
				this.loadConfiguration();
				try {
					if (ConfigController.getInstance().getSQLDetails().isEnabled()) {
						SQLController.getInstance();
						DataStorage.setInstance(DataStorage.DataStorageType.SQL);
					} else {
						DataStorage.setInstance(DataStorage.DataStorageType.FILE);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static ConfigController getInstance() {
		if (instance == null) instance = new ConfigController();
		return instance;
	}

	private static boolean isPotion(short durability, int dataValue) {
		return durability == dataValue || durability == (short) dataValue + (short) 8192;
	}

	private static class WorldSettings {
		private boolean[] kkCommands = new boolean[9];

		private String[] signKit = new String[3];
		private String[] signKitList = new String[2];
		private String[] signRefill = new String[3];

		private boolean allowBlockPlacingAndBreaking = true;
		private boolean allowDeathMessages = true;
		private boolean allowItemDropping = false;
		private boolean allowItemPicking = false;
		private boolean allowItemDroppingOnDeath = false;
		private boolean allowQuickSoup = true;
		private boolean economyEnabled = false;
		private double economyCostPerRefill = 2.5D;
		private double economyMoneyPerKill = 5D, economyMoneyPerDeath = 7.5D;
		private boolean oneKitPerLife = false;
		private boolean shouldClearItemsOnKitSelection = true;
		private boolean shouldLockFoodLevel = true;
		private boolean shouldRemoveItemsOnLeave = true;
		private boolean shouldRemoveItemsOnReload = true;
		private boolean shouldRemovePotionEffectsOnLeave = true;
		private boolean shouldRemovePotionEffectsOnReload = true;
		private boolean shouldSetCompassToNearestPlayer = false;

		private List<Integer> dropAnimationItems = new ArrayList<>();
	}

}
