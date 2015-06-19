package com.faris.kingkits;

import com.faris.kingkits.gui.GuiKingKits;
import com.faris.kingkits.gui.GuiKitMenu;
import com.faris.kingkits.gui.GuiPreviewKit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.UUIDFetcher;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.helper.value.CommandValues;
import com.faris.kingkits.helper.value.ConfigValues;
import com.faris.kingkits.listener.command.*;
import com.faris.kingkits.listener.event.EventListener;
import com.faris.kingkits.updater.BukkitUpdater;
import com.faris.kingkits.updater.SpigotUpdater;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.serialization.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.permissions.*;
import org.bukkit.plugin.java.*;
import org.bukkit.potion.*;
import org.bukkit.util.*;
import org.mcstats.Metrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

@SuppressWarnings({"unused", "deprecation"})
public class KingKits extends JavaPlugin {

	private static KingKits pluginInstance = null;

	// Class variables
	private KingKitsSQL kingKitsSQL = null;
	public CommandValues cmdValues = new CommandValues();
	public ConfigValues configValues = new ConfigValues();

	// Plugin variables
	public Map<String, String> usingKits = null;
	public Map<String, String> playerKits = null;
	public Map<UUID, Object> playerScores = null;
	public Map<UUID, UUID> compassTargets = null;
	public Map<String, Long> playerKillstreaks = null;

	// Kit lists
	public Map<String, Kit> kitList = null;
	public Map<UUID, List<Kit>> userKitList = null;
	private int cooldownTaskID = -1;

	public void onEnable() {
		pluginInstance = this;

		// Initialise all the collections
		this.usingKits = new HashMap<>();
		this.playerKits = new HashMap<>();
		this.playerScores = new HashMap<>();
		this.compassTargets = new HashMap<>();
		this.playerKillstreaks = new HashMap<>();
		this.kitList = new HashMap<>();
		this.userKitList = new HashMap<>();

		// Initialise variables
		ConfigurationSerialization.registerClass(Kit.class);
		try {
			this.loadConfiguration();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			Lang.init(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Register commands
		this.getCommand("kingkits").setExecutor(new KingKitsCommand(this));
		this.getCommand("kingkits").setAliases(Collections.singletonList("kk"));
		this.getCommand("pvpkit").setExecutor(new KitCommand(this));
		this.getCommand("createkit").setExecutor(new CreateKitCommand(this));
		this.getCommand("deletekit").setExecutor(new DeleteKitCommand(this));
		this.getCommand("refill").setExecutor(new RefillCommand(this));
		this.getCommand("refill").setAliases(Collections.singletonList("soup"));
		this.getCommand("renamekit").setExecutor(new RenameKitCommand(this));
		this.getCommand("createukit").setExecutor(new CreateUserKitCommand(this));
		this.getCommand("deleteukit").setExecutor(new DeleteUserKitCommand(this));
		this.getCommand("renameukit").setExecutor(new RenameUserKitCommand(this));
		this.getCommand("previewkit").setExecutor(new PreviewKitCommand(this));

		// Register permissions
		Permissions.initialisePermissions();
		for (Permission registeredPerm : Permissions.getPermissions()) {
			try {
				this.getServer().getPluginManager().addPermission(registeredPerm);
			} catch (Exception ignored) {
			}
		}
		for (int i = 1; i <= 54; i++) {
			try {
				this.getServer().getPluginManager().addPermission(new Permission("kingkits.kit.limit." + i));
			} catch (Exception ignored) {
			}
		}
		this.getServer().getPluginManager().registerEvents(new EventListener(), this);

		// Check for updates
		if (this.configValues.checkForUpdates) {
			if (this.getServer().getVersion().contains("Spigot")) {
				final int pluginID = 2209;
				SpigotUpdater updater = new SpigotUpdater(this, pluginID, false);
				if (updater.getResult() == SpigotUpdater.UpdateResult.UPDATE_AVAILABLE) {
					String title = "============================================";
					String titleSpace = "                                            ";
					this.getLogger().info(title);
					try {
						this.getLogger().info(titleSpace.substring(0, titleSpace.length() / 2 - "KingKits".length() + 3) + "KingKits" + titleSpace.substring(0, titleSpace.length() / 2 - "KingKits".length()));
					} catch (Exception ex) {
						this.getServer().getConsoleSender().sendMessage("KingKits");
					}
					this.getLogger().info(title);
					this.getLogger().info("A new version is available: KingKits v" + updater.getVersion());
					this.getLogger().info("Your current version: KingKits v" + updater.getCurrentVersion());
					this.getLogger().info((this.configValues.automaticUpdates ? "Automatic updates do not work for Spigot. " : "") + "Download it from: http://www.spigotmc.org/threads/kingkits.37947");
				}
			} else {
				final int pluginID = 56371;
				BukkitUpdater updater = new BukkitUpdater(this, pluginID, this.getFile(), BukkitUpdater.UpdateType.NO_DOWNLOAD, false);
				if (updater.getResult() == BukkitUpdater.UpdateResult.UPDATE_AVAILABLE) {
					String header = "============================================";
					String titleSpace = "                                            ";
					this.getLogger().info(header);
					try {
						this.getLogger().info(titleSpace.substring(0, titleSpace.length() / 2 - "KingKits".length() + 3) + "KingKits" + titleSpace.substring(0, titleSpace.length() / 2 - "KingKits".length()));
					} catch (Exception ex) {
						this.getServer().getConsoleSender().sendMessage("== KingKits ==");
					}
					this.getLogger().info(header);
					this.getLogger().info("A new version is available: " + updater.getLatestName());
					this.getLogger().info("Your current version: KingKits v" + this.getDescription().getVersion());
					if (this.configValues.automaticUpdates) {
						this.getLogger().info("Downloading " + updater.getLatestName() + "...");
						updater = new BukkitUpdater(this, pluginID, this.getFile(), BukkitUpdater.UpdateType.NO_VERSION_CHECK, false);
						BukkitUpdater.UpdateResult updateResult = updater.getResult();
						switch (updateResult) {
							case DISABLED:
								this.getLogger().warning("Plugin updater disabled in the updater's configuration.");
							case FAIL_APIKEY:
								this.getLogger().warning("Download failed: Improperly configured the server's API key in the configuration");
								break;
							case FAIL_DBO:
								this.getLogger().warning("Download failed: Could not connect to BukkitDev.");
								break;
							case FAIL_DOWNLOAD:
								this.getLogger().warning("Download failed: Could not download the file.");
								break;
							case FAIL_NOVERSION:
								this.getLogger().warning("Download failed: The latest version has an incorrect title.");
								break;
							default:
								this.getLogger().info("The latest version of KingKits has been downloaded.");
								break;
						}
					} else {
						this.getLogger().info("Download it from: " + updater.getLatestFileLink());
					}
				}
			}
		}

		try {
			new Metrics(this).start();
		} catch (Exception ex) {
			this.getLogger().log(Level.WARNING, "Could not start metrics due to a(n) " + ex.getClass().getSimpleName() + " error.", ex);
		}
	}

	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);

		if (this.kingKitsSQL != null) {
			this.kingKitsSQL.onDisable();
			this.kingKitsSQL = null;
		}

		// Clear inventories on reload
		if (this.configValues.clearInvOnReload) {
			for (Player target : Utilities.getOnlinePlayers()) {
				if (KingKitsAPI.hasKit(target.getName(), false)) {
					target.getInventory().clear();
					target.getInventory().setArmorContents(null);
					target.resetMaxHealth();
					for (PotionEffect potionEffect : target.getActivePotionEffects())
						target.removePotionEffect(potionEffect.getType());
				}
			}
		}

		ConfigurationSerialization.unregisterClass(Kit.class);

		// Clear all lists
		this.usingKits = null;
		this.playerKits = null;
		this.playerScores = null;
		this.compassTargets = null;
		this.playerKillstreaks = null;
		this.kitList = null;
		this.userKitList = null;

		// Unregister all permissions
		for (Permission registeredPermission : Permissions.getPermissions())
			this.getServer().getPluginManager().removePermission(registeredPermission);
		for (int i = 1; i <= 54; i++) {
			try {
				this.getServer().getPluginManager().removePermission("kingkits.kit.limit." + i);
			} catch (Exception ignored) {
			}
		}

		try {
			for (Entry<String, GuiKitMenu> playerEntry : GuiKingKits.guiKitMenuMap.entrySet()) {
				playerEntry.getValue().closeMenu(true, this.getServer().getPlayerExact(playerEntry.getKey()) != null);
			}
			GuiKingKits.guiKitMenuMap.clear();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			for (Entry<String, GuiPreviewKit> playerEntry : GuiKingKits.guiPreviewKitMap.entrySet()) {
				playerEntry.getValue().closeMenu(true, this.getServer().getPlayerExact(playerEntry.getKey()) != null);
			}
			GuiKingKits.guiPreviewKitMap.clear();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Permissions.clearPermissions();
		pluginInstance = null;
	}

	// Load Configurations
	public void loadConfiguration() throws Exception {
		if (this.cooldownTaskID != -1 && this.getServer().getScheduler().isQueued(this.cooldownTaskID))
			this.getServer().getScheduler().cancelTask(this.cooldownTaskID);
		if (KingKitsSQL.isInitialised() || KingKitsSQL.isOpen()) KingKitsSQL.closeConnection();
		this.getConfig().options().header("KingKits Configuration");
		this.getConfig().addDefault("Op bypass", true);
		this.getConfig().addDefault("MySQL.Enabled", false);
		this.getConfig().addDefault("MySQL.Host", "localhost");
		this.getConfig().addDefault("MySQL.Port", 3306);
		this.getConfig().addDefault("MySQL.Username", "root");
		this.getConfig().addDefault("MySQL.Password", "");
		this.getConfig().addDefault("MySQL.Database", "kingkits");
		this.getConfig().addDefault("MySQL.Table prefix", "kk_");
		this.getConfig().addDefault("GUI.Title", "<menucolour>PvP Kits");
		this.getConfig().addDefault("GUI.Size", 36);
		this.getConfig().addDefault("GUI.Page button.ID", Material.STONE_BUTTON.getId());
		this.getConfig().addDefault("GUI.Page button.Data value", 0);
		this.getConfig().addDefault("GUI.Show on respawn", false);
		this.getConfig().addDefault("PvP Worlds", new ArrayList<String>() {{
			this.add("All");
		}});
		this.getConfig().addDefault("Multiple world inventories plugin", "Multiverse-Inventories");
		this.getConfig().addDefault("Multi-inventories", this.getServer().getPluginManager().isPluginEnabled(this.getConfig().getString("Multiple world inventories plugin")));
		this.getConfig().addDefault("Enable kits command", true);
		this.getConfig().addDefault("Enable create kits command", true);
		this.getConfig().addDefault("Enable delete kits command", true);
		this.getConfig().addDefault("Enable rename kits command", true);
		this.getConfig().addDefault("Enable create user kits command", true);
		this.getConfig().addDefault("Enable delete user kits command", true);
		this.getConfig().addDefault("Enable rename user kits command", true);
		this.getConfig().addDefault("Enable refill command", true);
		this.getConfig().addDefault("Enable preview kit command", true);
		this.getConfig().addDefault("Kit sign", "[Kit]");
		this.getConfig().addDefault("Kit list sign", "[KList]");
		this.getConfig().addDefault("Kit sign valid", "[&1Kit&0]");
		this.getConfig().addDefault("Kit sign invalid", "[&cKit&0]");
		this.getConfig().addDefault("Kit list sign valid", "[&1KList&0]");
		this.getConfig().addDefault("Refill sign", "[KRefill]");
		this.getConfig().addDefault("Refill sign valid", "[&1KRefill&0]");
		this.getConfig().addDefault("Kit cooldown enabled", true);
		if (this.getConfig().contains("Kit cooldown")) this.getConfig().set("Kit cooldown", null);
		this.getConfig().addDefault("List kits on join", true);
		this.getConfig().addDefault("Use permissions on join", true);
		this.getConfig().addDefault("Use permissions for kit list", true);
		this.getConfig().addDefault("Kit list mode", "Text");
		this.getConfig().addDefault("Sort kits alphabetically", true);
		this.getConfig().addDefault("Remove items on leave", true);
		this.getConfig().addDefault("Drop items on death", false);
		this.getConfig().addDefault("Drop items", false);
		this.getConfig().addDefault("Drop animations", new ArrayList<Integer>() {{
			this.add(Material.BOWL.getId());
		}});
		this.getConfig().addDefault("Allow picking up items", true);
		this.getConfig().addDefault("Clear inventories on reload", true);
		this.getConfig().addDefault("One kit per life", false);
		this.getConfig().addDefault("Check for updates", true);
		this.getConfig().addDefault("Automatically update", false);
		this.getConfig().addDefault("Enable score", false);
		this.getConfig().addDefault("Score chat prefix", "&6[&a<score>&6]");
		this.getConfig().addDefault("Score per kill", 2);
		this.getConfig().addDefault("Max score", Integer.MAX_VALUE);
		this.getConfig().addDefault("Remove potion effects on leave", true);
		this.getConfig().addDefault("Set compass target to nearest player", true);
		this.getConfig().addDefault("Quick soup", true);
		this.getConfig().addDefault("Quick soup heal", 2.5D);
		this.getConfig().addDefault("Requires kit to use refill", true);
		this.getConfig().addDefault("Command to run when changing kits", "");
		this.getConfig().addDefault("Disable block placing and breaking", false);
		this.getConfig().addDefault("Disable death messages", false);
		this.getConfig().addDefault("Lock hunger level", true);
		this.getConfig().addDefault("Hunger lock", 20);
		this.getConfig().addDefault("Custom message", "&6You have chosen the kit &c<kit>&6.");
		this.getConfig().addDefault("Disable gamemode while using a kit", false);
		this.getConfig().addDefault("Enable killstreaks", false);
		this.getConfig().addDefault("Disable item breaking", true);
		this.getConfig().addDefault("Kit menu on join", false);
		this.getConfig().addDefault("Clear items on kit creation", true);
		this.getConfig().addDefault("Kit particle effects", false);
		this.getConfig().addDefault("Show kit preview", false);
		this.getConfig().addDefault("Replace items when selecting a kit", true);
		this.getConfig().addDefault("Drop items on full inventory", false);
		this.getConfig().addDefault("Remove kit on death", true);
		this.getConfig().addDefault("Decrease score on auto unlock", false);
		this.getConfig().addDefault("Decrease score on death", false);
		this.getConfig().addDefault("Commands to run on death", new ArrayList<>());
		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().copyHeader(true);

		this.configValues.opBypass = this.getConfig().getBoolean("Op bypass");
		this.configValues.pvpWorlds = this.getConfig().getStringList("PvP Worlds");
		this.configValues.multiInvsPlugin = this.getConfig().getString("Multiple world inventories plugin");
		this.configValues.multiInvs = this.getConfig().getBoolean("Multi-inventories");
		if (!this.configValues.multiInvs && this.getServer().getPluginManager().isPluginEnabled(this.getConfig().getString("Multiple world inventories plugin"))) {
			this.configValues.multiInvs = true;
			this.getConfig().set("Multi-inventories", true);
		}
		this.cmdValues.pvpKits = this.getConfig().getBoolean("Enable kits command");
		this.cmdValues.createKits = this.getConfig().getBoolean("Enable create kits command");
		this.cmdValues.deleteKits = this.getConfig().getBoolean("Enable delete kits command");
		this.cmdValues.renameKits = this.getConfig().getBoolean("Enable rename kits command");
		this.cmdValues.createUKits = this.getConfig().getBoolean("Enable create user kits command");
		this.cmdValues.deleteUKits = this.getConfig().getBoolean("Enable delete user kits command");
		this.cmdValues.renameUKits = this.getConfig().getBoolean("Enable rename user kits command");
		this.cmdValues.refillKits = this.getConfig().getBoolean("Enable refill command");
		this.cmdValues.previewKit = this.getConfig().getBoolean("Enable preview kit command");
		this.configValues.strSignKit = Utilities.replaceChatColour(this.getConfig().getString("Kit sign"));
		this.configValues.strSignKitList = Utilities.replaceChatColour(this.getConfig().getString("Kit list sign"));
		this.configValues.strSignValidKit = Utilities.replaceChatColour(this.getConfig().getString("Kit sign valid"));
		this.configValues.strSignInvalidKit = Utilities.replaceChatColour(this.getConfig().getString("Kit sign invalid"));
		this.configValues.strSignValidKitList = Utilities.replaceChatColour(this.getConfig().getString("Kit list sign valid"));
		this.configValues.strSignRefill = Utilities.replaceChatColour(this.getConfig().getString("Refill sign"));
		this.configValues.strSignRefillValid = Utilities.replaceChatColour(this.getConfig().getString("Refill sign valid"));
		this.configValues.kitCooldown = this.getConfig().getBoolean("Kit cooldown enabled");
		this.configValues.listKitsOnJoin = this.getConfig().getBoolean("List kits on join");
		this.configValues.kitListMode = this.getConfig().getString("Kit list mode");
		this.configValues.sortAlphabetically = this.getConfig().getBoolean("Sort kits alphabetically");
		this.configValues.kitListPermissionsJoin = this.getConfig().getBoolean("Use permissions on join");
		this.configValues.kitListPermissions = this.getConfig().getBoolean("Use permissions for kit list");
		this.configValues.removeItemsOnLeave = this.getConfig().getBoolean("Remove items on leave");
		this.configValues.dropItemsOnDeath = this.getConfig().getBoolean("Drop items on death");
		this.configValues.dropItems = this.getConfig().getBoolean("Drop items");
		this.configValues.dropAnimations = this.getConfig().getIntegerList("Drop animations");
		this.configValues.allowPickingUpItems = this.getConfig().getBoolean("Allow picking up items");
		this.configValues.clearInvOnReload = this.getConfig().getBoolean("Clear inventories on reload");
		this.configValues.oneKitPerLife = this.getConfig().getBoolean("One kit per life");
		this.configValues.checkForUpdates = this.getConfig().getBoolean("Check for updates");
		this.configValues.automaticUpdates = this.getConfig().getBoolean("Automatically update");
		this.configValues.removePotionEffectsOnLeave = this.getConfig().getBoolean("Remove potion effects on leave");
		this.configValues.rightClickCompass = this.getConfig().getBoolean("Set compass target to nearest player");
		this.configValues.quickSoup = this.getConfig().getBoolean("Quick soup");
		this.configValues.quickSoupHeal = Math.ceil(this.getConfig().getDouble("Quick soup heal") * 2) / 2;
		this.configValues.quickSoupKitOnly = this.getConfig().getBoolean("Requires kit to use refill");
		this.configValues.banBlockBreakingAndPlacing = this.getConfig().getBoolean("Disable block placing and breaking");
		this.configValues.disableDeathMessages = this.getConfig().getBoolean("Disable death messages");
		this.configValues.lockHunger = this.getConfig().getBoolean("Lock hunger level");
		this.configValues.hungerLock = this.getConfig().getInt("Hunger lock");
		this.configValues.customMessages = this.getConfig().getString("Custom message");
		this.configValues.commandToRun = this.getConfig().getString("Command to run when changing kits");
		this.configValues.disableGamemode = this.getConfig().getBoolean("Disable gamemode while using a kit");
		this.configValues.killstreaks = this.getConfig().getBoolean("Enable killstreaks");
		this.configValues.disableItemBreaking = this.getConfig().getBoolean("Disable item breaking");
		this.configValues.kitMenuOnJoin = this.getConfig().getBoolean("Kit menu on join");
		this.configValues.removeItemsOnCreateKit = this.getConfig().getBoolean("Clear items on kit creation");
		this.configValues.kitParticleEffects = this.getConfig().getBoolean("Kit particle effects");
		this.configValues.showKitPreview = this.getConfig().getBoolean("Show kit preview");
		this.configValues.replaceItems = this.getConfig().getBoolean("Replace items when selecting a kit");
		this.configValues.dropItemsOnFullInventory = this.getConfig().getBoolean("Drop items on full inventory");
		this.configValues.removeKitOnDeath = this.getConfig().getBoolean("Remove kit on death");
		this.configValues.decreaseScoreOnAutoUnlock = this.getConfig().getBoolean("Decrease score on auto unlock");
		this.configValues.decreaseScoreOnDeath = this.getConfig().getBoolean("Decrease score on death");
		this.configValues.commandsToRunOnDeath = this.getConfig().getStringList("Commands to run on death");

		this.configValues.guiTitle = Utilities.replaceChatColour(this.getConfig().getString("GUI.Title"));
		this.configValues.guiSize = this.getConfig().getInt("GUI.Size");
		if (this.configValues.guiSize <= 0 || this.configValues.guiSize % 9 != 0 || this.configValues.guiSize > 54) {
			this.configValues.guiSize = 54;
			this.getConfig().set("GUI.Size", 54);
		}
		this.configValues.guiItemID = this.getConfig().getInt("GUI.Page button.ID");
		this.configValues.guiItemData = (short) this.getConfig().getInt("GUI.Page button.Data value");
		this.configValues.guiOnRespawn = this.getConfig().getBoolean("GUI.Show on respawn");

		this.configValues.scores = this.getConfig().getBoolean("Enable score");
		this.configValues.scoreIncrement = this.getConfig().getInt("Score per kill");
		this.configValues.scoreFormat = this.getConfig().getString("Score chat prefix");
		this.configValues.maxScore = this.getConfig().getInt("Max score");
		this.saveConfig();

		this.loadMySQL();
		this.loadEconomy();
		this.loadKillstreaks();
		this.loadPvPKits();
		this.loadScores();
		this.loadUnlockedKits();
		this.loadUserKits();
		if (this.configValues.kitCooldown) {
			this.cooldownTaskID = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
				public void run() {
					boolean hasBeenModified = false;
					for (Entry<String, Object> configEntrySet : getCooldownConfig().getValues(false).entrySet()) {
						Map<String, Object> playerMap = null;
						if (configEntrySet.getValue() instanceof ConfigurationSection)
							playerMap = ((ConfigurationSection) configEntrySet.getValue()).getValues(false);
						else if (configEntrySet.getValue() instanceof Map)
							playerMap = (Map<String, Object>) configEntrySet.getValue();
						if (playerMap != null) {
							for (Entry<String, Object> entrySet : playerMap.entrySet()) {
								String strValue = entrySet.getValue().toString();
								if (Utilities.isNumber(Long.class, strValue)) {
									Kit targetKit = kitList.get(entrySet.getKey());
									if (targetKit != null) {
										long kitCooldown = targetKit.getCooldown();
										long playerCooldown = Long.parseLong(strValue);
										if (System.currentTimeMillis() - playerCooldown >= kitCooldown * 1000) {
											getCooldownConfig().set(configEntrySet.getKey() + "." + entrySet.getKey(), null);
											if (playerMap.size() == 1)
												getCooldownConfig().set(configEntrySet.getKey(), null);
											hasBeenModified = true;
										}
									}
								}
							}
						}
					}
					if (hasBeenModified) saveCooldownConfig();
				}
			}, 1200L, 1200L).getTaskId();
		}
		this.convertOldConfigCooldowns();
	}

	private void loadMySQL() {
		if (KingKitsSQL.isOpen()) KingKitsSQL.closeConnection();
		KingKitsSQL.sqlEnabled = this.getConfig().getBoolean("MySQL.Enabled", false);
		this.kingKitsSQL = new KingKitsSQL(this.getConfig().getString("MySQL.Host", "localhost"), this.getConfig().getInt("MySQL.Port", 3306), this.getConfig().getString("MySQL.Username", "root"), this.getConfig().getString("MySQL.Password", ""), this.getConfig().getString("MySQL.Database", "kingkits"), this.getConfig().getString("MySQL.Table prefix", "kk_"));
	}

	private void loadPvPKits() {
		try {
			this.getKitsConfig().options().header("KingKits Kits Configuration.");
			this.getKitsConfig().addDefault("First run", true);
			if (this.getKitsConfig().getBoolean("First run")) {
				Kit defaultKit = new Kit("Default").setRealName("Default").setCommands(Arrays.asList("feed <player>", "tell <player> &6You have been fed for using the default kit."));
				List<ItemStack> defaultKitItems = new ArrayList<>(), defaultKitArmour = new ArrayList<>();

				ItemStack defaultSword = new ItemStack(Material.IRON_SWORD);
				defaultSword.addEnchantment(Enchantment.DURABILITY, 3);
				defaultKitItems.add(Utilities.ItemUtils.setLore(Utilities.ItemUtils.setName(defaultSword, "Default Kit Sword"), new ArrayList<String>() {{
					this.add("&6Slay your enemies!");
				}}));
				defaultKitItems.add(new ItemStack(Material.GOLDEN_APPLE, 2));
				defaultKitArmour.add(new ItemStack(Material.IRON_HELMET));
				defaultKitArmour.add(Utilities.ItemUtils.setDye(new ItemStack(Material.LEATHER_CHESTPLATE), 10040115));
				defaultKitArmour.add(new ItemStack(Material.IRON_LEGGINGS));
				defaultKitArmour.add(new ItemStack(Material.IRON_BOOTS));
				defaultKitItems.add(new ItemStack(Material.POTION, 1, (short) 8201));

				defaultKit.setItems(defaultKitItems);
				defaultKit.setArmour(defaultKitArmour);
				defaultKit.setDescription(new ArrayList<>(Arrays.asList("The default KingKits", "amazingly powerful kit!")));
				defaultKit.setPotionEffects(Collections.singletonList(new PotionEffect(PotionEffectType.SPEED, 200, 1)));

				this.getKitsConfig().addDefault("Default", defaultKit.serialize());
				this.getKitsConfig().set("First run", false);
			}
			this.getKitsConfig().options().copyDefaults(true);
			this.getKitsConfig().options().copyHeader(true);
			this.saveKitsConfig();

			this.kitList.clear();
			List<String> kitList = this.getConfigKitList();
			for (String kitName : kitList) {
				try {
					Object objKitConfigSection = this.getKitsConfig().get(kitName);
					Kit kit = null;
					if (objKitConfigSection instanceof ConfigurationSection)
						kit = Kit.deserialize(((ConfigurationSection) objKitConfigSection).getValues(false));
					else if (objKitConfigSection instanceof Map)
						kit = Kit.deserialize((Map<String, Object>) objKitConfigSection);
					if (kit != null) this.kitList.put(kitName, kit.setRealName(kitName).setUserKit(false));
					else
						this.getLogger().warning("Could not register the kit '" + kitName + "' it has been invalidly defined in the configuration.");
				} catch (Exception ex) {
					this.getLogger().warning("Could not register the kit '" + kitName + "' due to a(n) " + ex.getClass().getSimpleName() + " error:");
					ex.printStackTrace();
				}
			}

			this.setupPermissions(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadUserKits() {
		try {
			this.getUserKitsConfig().options().header("KingKits User Kits Configuration.");
			this.getUserKitsConfig().options().copyHeader(true);
			this.saveUserKitsConfig();

			this.userKitList.clear();
			this.convertOldConfigUserKits();
			List<String> userList = new ArrayList<>(this.getUserKitsConfig().getKeys(false));
			for (String strPlayerUUID : userList) {
				UUID playerUUID = Utilities.isUUID(strPlayerUUID) ? UUID.fromString(strPlayerUUID) : null;
				if (playerUUID == null) continue;
				List<Kit> userKits = new ArrayList<>();
				try {
					for (Map.Entry<String, Object> kitEntry : this.getUserKitsConfig().getConfigurationSection(strPlayerUUID).getValues(false).entrySet()) {
						try {
							String kitName = kitEntry.getKey();
							Object objKitConfigSection = kitEntry.getValue();
							Kit kit = null;
							if (objKitConfigSection instanceof ConfigurationSection)
								kit = Kit.deserialize(((ConfigurationSection) objKitConfigSection).getValues(false));
							else if (objKitConfigSection instanceof Map)
								kit = Kit.deserialize((Map<String, Object>) objKitConfigSection);
							if (kit != null) userKits.add(kit.setRealName(kitName).setUserKit(true));
						} catch (Exception ignored) {
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (!userKits.isEmpty()) this.userKitList.put(playerUUID, userKits);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadScores() {
		try {
			this.getScoresConfig().options().header("KingKits Score Configuration");
			if (!this.getScoresConfig().contains("Scores"))
				this.getScoresConfig().createSection("Scores", Collections.singletonMap(UUID.fromString("f9843dd6-0a5f-4009-b451-847291bda6b3"), 0));
			this.getScoresConfig().options().copyDefaults(true);
			this.getScoresConfig().options().copyHeader(true);
			this.saveScoresConfig();

			Map<String, Object> scoresMap = this.getScoresConfig().getConfigurationSection("Scores").getValues(true);
			List<String> unconvertedScores = new ArrayList<>();
			for (Entry<String, Object> scoreEntry : scoresMap.entrySet()) {
				if (!Utilities.isUUID(scoreEntry.getKey())) unconvertedScores.add(scoreEntry.getKey());
			}
			boolean hasConverted;
			try {
				Map<String, UUID> uuidList = UUIDFetcher.lookupNames(unconvertedScores);
				for (Entry<String, UUID> uuidEntry : uuidList.entrySet()) {
					scoresMap.put(uuidEntry.getValue().toString(), scoresMap.get(uuidEntry.getKey()));
					this.getScoresConfig().set("Scores." + uuidEntry.getValue().toString(), scoresMap.get(uuidEntry.getKey()));
				}
				hasConverted = true;
			} catch (Exception ex) {
				ex.printStackTrace();
				hasConverted = false;
			}
			for (String unconvertedPlayer : unconvertedScores) {
				scoresMap.remove(unconvertedPlayer);
				if (hasConverted) this.getScoresConfig().set("Scores." + unconvertedPlayer, null);
			}
			this.saveScoresConfig();
			this.playerScores = new HashMap<>();
			for (Entry<String, Object> mapEntry : scoresMap.entrySet()) {
				if (Utilities.isUUID(mapEntry.getKey()))
					this.playerScores.put(UUID.fromString(mapEntry.getKey()), mapEntry.getValue());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadEconomy() {
		this.getEconomyConfig().options().header("KingKits Economy Configuration");
		this.getEconomyConfig().addDefault("Use economy", false);
		this.getEconomyConfig().addDefault("Enable cost per kit", false);
		this.getEconomyConfig().addDefault("Enable cost per refill", false);
		this.getEconomyConfig().addDefault("Cost per refill", 2.5D);
		this.getEconomyConfig().addDefault("Currency", "dollars");
		this.getEconomyConfig().addDefault("Message", "&a<money> <currency> was taken from your balance.");
		this.getEconomyConfig().addDefault("Enable money per kill", false);
		this.getEconomyConfig().addDefault("Money per kill", 5D);
		this.getEconomyConfig().addDefault("Money per kill message", "&aYou received <money> <currency> for killing <target>.");
		this.getEconomyConfig().addDefault("Enable money per death", false);
		this.getEconomyConfig().addDefault("Money per death", 5D);
		this.getEconomyConfig().addDefault("Money per death message", "&aYou lost <money> <currency> for being killed by <killer>.");
		this.getEconomyConfig().options().copyDefaults(true);
		this.getEconomyConfig().options().copyHeader(true);
		this.saveEconomyConfig();

		this.configValues.vaultValues.useEconomy = this.getEconomyConfig().getBoolean("Use economy");

		boolean useCostPerKit = this.getEconomyConfig().getBoolean("Enable cost per kit");
		if (useCostPerKit && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable cost per kit", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useCostPerKit = this.getEconomyConfig().getBoolean("Enable cost per kit");

		boolean useCostPerRefill = this.getEconomyConfig().getBoolean("Enable cost per refill");
		if (useCostPerRefill && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable cost per refill", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useCostPerRefill = this.getEconomyConfig().getBoolean("Enable cost per refill");

		this.configValues.vaultValues.costPerRefill = this.getEconomyConfig().getDouble("Cost per refill");

		boolean useMoneyPerKill = this.getEconomyConfig().getBoolean("Enable money per kill");
		if (useMoneyPerKill && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable money per kill", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useMoneyPerKill = this.getEconomyConfig().getBoolean("Enable money per kill");
		this.configValues.vaultValues.moneyPerKill = this.getEconomyConfig().getDouble("Money per kill");

		boolean useMoneyPerDeath = this.getEconomyConfig().getBoolean("Enable money per death");
		if (useMoneyPerDeath && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable money per death", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useMoneyPerDeath = this.getEconomyConfig().getBoolean("Enable money per death");
		this.configValues.vaultValues.moneyPerDeath = this.getEconomyConfig().getDouble("Money per death");
	}

	private void loadKillstreaks() {
		this.getKillstreaksConfig().options().header("KingKits Killstreak Configuration");
		this.getKillstreaksConfig().addDefault("First run", true);
		if (this.getKillstreaksConfig().getBoolean("First run")) {
			this.getKillstreaksConfig().set("Killstreak 9001", Arrays.asList("broadcast &c<player>&a's killstreak is over 9000!", "msg <player> Well done!"));
			this.getKillstreaksConfig().set("First run", false);
		}
		this.getKillstreaksConfig().options().copyDefaults(true);
		this.getKillstreaksConfig().options().copyHeader(true);
		this.saveKillstreaksConfig();
	}

	private void loadUnlockedKits() {
		this.getUnlockedKitsConfig().options().header("KingKits players' unlocked kits");
		this.getUnlockedKitsConfig().options().copyHeader(true);
		this.saveUnlockedKitsConfig();
	}

	public boolean checkConfig() {
		String scoreChatPrefix = this.getConfig().getString("Score chat prefix");
		if (!scoreChatPrefix.contains("<score>")) {
			this.getConfig().set("Score chat prefix", "&6[&a<score>&6]");
			this.saveConfig();
			return false;
		}
		return true;
	}

	private void setupPermissions(boolean unregisterFirst) {
		if (unregisterFirst) {
			try {
				List<String> kitNames = new ArrayList<>(this.getKitsConfig().getKeys(false));
				for (String kit : kitNames) {
					if (kit.contains(" ")) kit = kit.split(" ")[0];
					try {
						this.getServer().getPluginManager().removePermission("kingkits.kits." + kit.toLowerCase());
						this.getServer().getPluginManager().removePermission("kingkits.free." + kit.toLowerCase());
					} catch (Exception ignored) {
					}
				}
			} catch (Exception ignored) {
			}
		}
		try {
			List<String> kitNames = new ArrayList<>(this.getKitsConfig().getKeys(false));
			for (String kit : kitNames) {
				if (kit.contains(" ")) kit = kit.split(" ")[0];
				try {
					this.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kit.toLowerCase()));
				} catch (Exception ex) {
					this.getLogger().log(Level.WARNING, "Couldn't register the permission node: kingkits.kits." + kit.toLowerCase(), ex);
				}
				try {
					this.getServer().getPluginManager().addPermission(new Permission("kingkits.free." + kit.toLowerCase()));
				} catch (Exception ex) {
					this.getLogger().log(Level.WARNING, "Couldn't register the permission node: kingkits.free." + kit.toLowerCase(), ex);
				}
			}
		} catch (Exception ignored) {
		}
	}

	public String getEconomyMessage(double amount) {
		this.reloadEconomyConfig();
		String message = this.getEconomyConfig().getString("Message");
		String currency = this.getEconomyConfig().getString("Currency");
		if (amount == 1) {
			if (currency.contains("s") && currency.lastIndexOf('s') == message.length())
				currency = this.replaceLast(currency, "s", "");
		}
		message = message.replace("<currency>", currency);
		message = message.replace("<money>", String.valueOf(amount));
		return Utilities.replaceChatColour(message);
	}

	public List<String> getConfigKitList() {
		List<String> configKeys = new ArrayList<>(this.getKitsConfig().getKeys(false));
		configKeys.remove("First run");
		return configKeys;
	}

	public long getCooldown(UUID playerUUID, String kitName) {
		if (playerUUID != null && kitName != null && this.kitList.containsKey(kitName) && this.getCooldownConfig().contains(playerUUID.toString())) {
			Object objCooldownPlayer = this.getCooldownConfig().get(playerUUID.toString());
			Map<String, Object> playerKitCooldowns = objCooldownPlayer instanceof ConfigurationSection ? ((ConfigurationSection) objCooldownPlayer).getValues(false) : (objCooldownPlayer instanceof Map ? (Map) objCooldownPlayer : new HashMap<String, Object>());
			if (playerKitCooldowns.containsKey(kitName)) {
				String strPlayerCooldown = playerKitCooldowns.get(kitName) != null ? playerKitCooldowns.get(kitName).toString() : "0";
				if (!Utilities.isNumber(Long.class, strPlayerCooldown)) {
					this.getCooldownConfig().set(playerUUID.toString() + "." + kitName, null);
					this.saveCooldownConfig();
				} else {
					return Long.parseLong(strPlayerCooldown);
				}
			}
		}
		return System.currentTimeMillis();
	}

	public Map<String, Long> getCooldowns(UUID playerUUID) {
		Map<String, Long> kitCooldowns = new HashMap<>();
		if (playerUUID != null && this.getCooldownConfig().contains(playerUUID.toString())) {
			Object objCooldownPlayer = this.getCooldownConfig().get(playerUUID.toString());
			Map<String, Object> configKitCooldowns = objCooldownPlayer instanceof ConfigurationSection ? ((ConfigurationSection) objCooldownPlayer).getValues(false) : (objCooldownPlayer instanceof Map ? (Map) objCooldownPlayer : new HashMap<String, Object>());
			for (Entry<String, Object> kitEntry : configKitCooldowns.entrySet()) {
				String strPlayerCooldown = kitEntry.getValue().toString();
				if (!Utilities.isNumber(Long.class, strPlayerCooldown)) {
					this.getCooldownConfig().set(playerUUID.toString() + "." + strPlayerCooldown, null);
					this.saveCooldownConfig();
				} else {
					kitCooldowns.put(kitEntry.getKey(), Long.parseLong(strPlayerCooldown));
				}
			}
		}
		return kitCooldowns;
	}

	public List<String> getKitList() {
		return new ArrayList<>(this.kitList.keySet());
	}

	public List<String> getKitList(UUID playerUUID) {
		List<Kit> kitList = this.userKitList.get(playerUUID);
		List<String> strKitList = new ArrayList<>();
		if (kitList != null) {
			for (Kit kit : kitList) {
				if (kit != null) strKitList.add(kit.getRealName());
			}
		}
		return strKitList;
	}

	public String getMPKMessage(Player killer, double amount) {
		this.reloadEconomyConfig();
		String message = this.getEconomyConfig().getString("Money per kill message");
		String currency = this.getEconomyConfig().getString("Currency");
		if (amount == 1) {
			if (currency.contains("s") && currency.lastIndexOf('s') == message.length() - 1)
				currency = this.replaceLast(currency, "s", "");
		}
		message = message.replace("<currency>", currency);
		message = message.replace("<money>", String.valueOf(amount));
		message = message.replace("<target>", killer.getName());
		return Utilities.replaceChatColour(message);
	}

	public String getMPDMessage(Player dead, double amount) {
		this.reloadEconomyConfig();
		String message = this.getEconomyConfig().getString("Money per death message");
		String currency = this.getEconomyConfig().getString("Currency");
		if (amount == 1) {
			if (currency.contains("s")) {
				if (currency.lastIndexOf('s') == message.length() - 1) currency = this.replaceLast(currency, "s", "");
			}
		}
		message = message.replace("<currency>", currency);
		message = message.replace("<money>", String.valueOf(amount));
		message = message.replace("<killer>", dead.getKiller().getName());
		return Utilities.replaceChatColour(message);
	}

	private String replaceLast(String s, String character, String targetChar) {
		String string = s;
		if (string.contains(character)) {
			StringBuilder b = new StringBuilder(string);
			b.replace(string.lastIndexOf(character), string.lastIndexOf(character) + 1, targetChar);
			string = b.toString();
		}
		return string;
	}

	private FileConfiguration kitsConfig = null;
	private File kitsConfigFile = null;

	public void reloadKitsConfig() {
		if (this.kitsConfigFile == null) this.kitsConfigFile = new File(this.getDataFolder(), "kits.yml");
		try {
			this.kitsConfig = loadConfigSafely(this.kitsConfigFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			if (this.kitsConfigFile.exists()) {
				File kitDestination = new File(this.getDataFolder(), "kits.broken.yml");
				if (kitDestination.exists()) {
					try {
						kitDestination.delete();
					} catch (Exception ignored) {
					}
				}
				FileUtil.copy(this.kitsConfigFile, kitDestination);
				try {
					this.kitsConfigFile.delete();
				} catch (Exception ignored) {
				}
			}
			try {
				this.kitsConfigFile.createNewFile();
				this.kitsConfig = loadConfigSafely(this.kitsConfigFile);
			} catch (Exception ignored) {
			}
		}
	}

	public FileConfiguration getKitsConfig() {
		if (this.kitsConfig == null || this.kitsConfigFile == null) this.reloadKitsConfig();
		return this.kitsConfig;
	}

	public void saveKitsConfig() {
		if (this.kitsConfig == null || this.kitsConfigFile == null) return;
		try {
			this.getKitsConfig().save(this.kitsConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Kits config as " + this.kitsConfigFile.getName(), ex);
		}
	}

	private FileConfiguration scoresConfig = null;
	private File scoresConfigFile = null;

	public void reloadScoresConfig() {
		if (this.scoresConfigFile == null) this.scoresConfigFile = new File(this.getDataFolder(), "scores.yml");
		this.scoresConfig = loadConfigSafely(this.scoresConfigFile);
	}

	public FileConfiguration getScoresConfig() {
		if (this.scoresConfig == null || this.scoresConfigFile == null) this.reloadScoresConfig();
		return this.scoresConfig;
	}

	public void saveScoresConfig() {
		if (this.scoresConfig == null || this.scoresConfigFile == null) return;
		try {
			this.getScoresConfig().save(this.scoresConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Scores config as " + this.scoresConfigFile.getName(), ex);
		}
	}

	private FileConfiguration economyConfig = null;
	private File economyConfigFile = null;

	public void reloadEconomyConfig() {
		if (this.economyConfigFile == null) this.economyConfigFile = new File(this.getDataFolder(), "economy.yml");
		this.economyConfig = loadConfigSafely(this.economyConfigFile);
	}

	public FileConfiguration getEconomyConfig() {
		if (this.economyConfig == null || this.economyConfigFile == null) this.reloadEconomyConfig();
		return this.economyConfig;
	}

	public void saveEconomyConfig() {
		if (this.economyConfig == null || this.economyConfigFile == null) return;
		try {
			this.getEconomyConfig().save(this.economyConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Economy config as " + this.economyConfigFile.getName(), ex);
		}
	}

	private FileConfiguration killstreaksConfig = null;
	private File killstreaksConfigFile = null;

	public void reloadKillstreaksConfig() {
		if (this.killstreaksConfigFile == null)
			this.killstreaksConfigFile = new File(this.getDataFolder(), "killstreaks.yml");
		this.killstreaksConfig = loadConfigSafely(this.killstreaksConfigFile);
	}

	public FileConfiguration getKillstreaksConfig() {
		if (this.killstreaksConfig == null || this.killstreaksConfigFile == null) this.reloadKillstreaksConfig();
		return this.killstreaksConfig;
	}

	public void saveKillstreaksConfig() {
		if (this.killstreaksConfig == null || this.killstreaksConfigFile == null) return;
		try {
			this.getKillstreaksConfig().save(this.killstreaksConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Killstreaks config as " + this.killstreaksConfigFile.getName(), ex);
		}
	}

	private FileConfiguration cooldownConfig = null;
	private File cooldownConfigFile = null;

	public void reloadCooldownConfig() {
		if (this.cooldownConfigFile == null)
			this.cooldownConfigFile = new File(this.getDataFolder(), "cooldown.yml");
		this.cooldownConfig = loadConfigSafely(this.cooldownConfigFile);
	}

	public FileConfiguration getCooldownConfig() {
		if (this.cooldownConfig == null || this.cooldownConfigFile == null) this.reloadCooldownConfig();
		return this.cooldownConfig;
	}

	public void saveCooldownConfig() {
		if (this.cooldownConfig == null || this.cooldownConfigFile == null) return;
		try {
			this.cooldownConfig.save(this.cooldownConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the cooldown config as " + this.cooldownConfigFile.getName(), ex);
		}
	}

	private FileConfiguration userKitsConfig = null;
	private File userKitsConfigFile = null;

	public void reloadUserKitsConfig() {
		if (this.userKitsConfigFile == null)
			this.userKitsConfigFile = new File(this.getDataFolder(), "userkits.yml");
		this.userKitsConfig = loadConfigSafely(this.userKitsConfigFile);
	}

	public FileConfiguration getUserKitsConfig() {
		if (this.userKitsConfig == null || this.userKitsConfigFile == null) this.reloadUserKitsConfig();
		return this.userKitsConfig;
	}

	public void saveUserKitsConfig() {
		if (this.userKitsConfig == null || this.userKitsConfigFile == null) return;
		try {
			this.userKitsConfig.save(this.userKitsConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the user kits config as " + this.userKitsConfigFile.getName(), ex);
		}
	}

	private FileConfiguration unlockedKitsConfig = null;
	private File unlockedKitsConfigFile = null;

	public void reloadUnlockedKitsConfig() {
		if (this.unlockedKitsConfigFile == null)
			this.unlockedKitsConfigFile = new File(this.getDataFolder(), "unlockedkits.yml");
		this.unlockedKitsConfig = loadConfigSafely(this.unlockedKitsConfigFile);
	}

	public FileConfiguration getUnlockedKitsConfig() {
		if (this.unlockedKitsConfig == null || this.unlockedKitsConfigFile == null) this.reloadUnlockedKitsConfig();
		return this.unlockedKitsConfig;
	}

	public void saveUnlockedKitsConfig() {
		if (this.unlockedKitsConfig == null || this.unlockedKitsConfigFile == null) return;
		try {
			this.unlockedKitsConfig.save(this.unlockedKitsConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the unlocked kits config as " + this.unlockedKitsConfigFile.getName(), ex);
		}
	}

	public void reloadAllConfigs() {
		this.reloadConfig();
		this.reloadKitsConfig();
		this.reloadUserKitsConfig();

		this.reloadScoresConfig();
		this.reloadEconomyConfig();
		this.reloadKillstreaksConfig();
		this.reloadCooldownConfig();
	}

	private void convertOldConfigUserKits() {
		List<String> keyList = new ArrayList<>(this.getUserKitsConfig().getKeys(false));
		Map<String, UUID> addMap = new HashMap<>();
		List<String> checkList = new ArrayList<>();

		for (String strPlayerName : keyList) {
			if (strPlayerName != null && !Utilities.isUUID(strPlayerName)) checkList.add(strPlayerName);
		}
		try {
			addMap.putAll(UUIDFetcher.lookupNames(checkList));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (!addMap.isEmpty()) {
			for (Map.Entry<String, UUID> addEntry : addMap.entrySet()) {
				if (addEntry.getValue() != null)
					this.getUserKitsConfig().set(addEntry.getValue().toString(), this.getUserKitsConfig().get(addEntry.getKey()));
				else continue;
				this.getUserKitsConfig().set(addEntry.getKey(), null);
			}
			addMap.clear();
			this.saveUserKitsConfig();
		}
	}

	private void convertOldConfigCooldowns() {
		List<String> keyList = new ArrayList<>(this.getCooldownConfig().getKeys(false));
		Map<String, UUID> addMap = new HashMap<>();
		List<String> checkList = new ArrayList<>();

		for (String strPlayerName : keyList) {
			if (strPlayerName != null && !Utilities.isUUID(strPlayerName)) checkList.add(strPlayerName);
		}
		try {
			addMap.putAll(UUIDFetcher.lookupNames(checkList));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (!addMap.isEmpty()) {
			for (Map.Entry<String, UUID> addEntry : addMap.entrySet()) {
				if (addEntry.getValue() != null)
					this.getCooldownConfig().set(addEntry.getValue().toString(), this.getCooldownConfig().get(addEntry.getKey()));
				else continue;
				this.getCooldownConfig().set(addEntry.getKey(), null);
			}
			addMap.clear();
			this.saveCooldownConfig();
		}
	}

	public static KingKits getInstance() {
		return pluginInstance;
	}

	private static FileConfiguration loadConfigSafely(File configFile) {
		try {
			return customLoadConfiguration(configFile);
		} catch (Exception ex) {
			if (ex.getClass() != FileNotFoundException.class)
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Cannot load " + configFile, ex);
			if (configFile.exists()) {
				String filePath = configFile.getAbsolutePath();
				String brokenFilePath = (filePath.contains(".yml") ? filePath.substring(0, filePath.indexOf(".yml")) : filePath) + "-" + System.currentTimeMillis() + ".yml.broken";
				File configDestination = new File(brokenFilePath);
				try {
					FileInputStream configFileInputStream = new FileInputStream(configFile);
					Files.copy(configFileInputStream, configDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
					configFileInputStream.close();
					configFile.delete();
				} catch (Exception ignored) {
				}
			}
			try {
				configFile.createNewFile();
				return customLoadConfiguration(configFile);
			} catch (Exception ignored) {
			}
		}
		return YamlConfiguration.loadConfiguration(new File(getInstance().getDataFolder(), "temp" + System.currentTimeMillis() + ".yml"));
	}

	private static YamlConfiguration customLoadConfiguration(File file) throws Exception {
		Validate.notNull(file, "File cannot be null");
		YamlConfiguration config = new YamlConfiguration();
		config.load(file);
		return config;
	}

}
