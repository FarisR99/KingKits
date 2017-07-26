package com.faris.kingkits.storage;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.util.FileUtilities;
import com.faris.kingkits.helper.util.ObjectUtilities;
import com.faris.kingkits.helper.util.Utilities;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class FlatFileStorage extends DataStorage {

	@Override
	public KitPlayer loadPlayer(KitPlayer kitPlayer) {
		try {
			File playerDataDirectory = new File(KingKits.getInstance().getDataFolder(), "players");
			if (!(playerDataDirectory.exists() || playerDataDirectory.mkdirs())) {
				Bukkit.getServer().getLogger().warning("Failed to load player " + kitPlayer + ": Could not create players directory.");
				kitPlayer.setLoaded(true);
				return kitPlayer;
			}
			Object[] playerFileResult = getPlayerDataFile(playerDataDirectory, kitPlayer.getUsername());
			if (playerFileResult == null) {
				playerFileResult = new Object[2];
				playerFileResult[0] = new File(playerDataDirectory, kitPlayer.getUniqueId().toString() + ".yml");
				playerFileResult[1] = false;
			}
			File playerFile = (File) playerFileResult[0];
			if ((Boolean) playerFileResult[1]) {
				try {
					File newPlayerFile = new File(playerDataDirectory, kitPlayer.getUniqueId().toString() + ".yml");
					if (newPlayerFile.exists()) newPlayerFile.delete();
					FileUtil.copy(playerFile, newPlayerFile);
					playerFile.delete();
					playerFile = newPlayerFile;
				} catch (Exception ex) {
					Bukkit.getServer().getLogger().log(Level.WARNING, "Could not create player file for " + kitPlayer + ".", ex);
				}
			}
			if (!playerFile.exists()) {
				kitPlayer.setLoaded(true);
				return kitPlayer;
			}

			FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			kitPlayer.setScore(playerConfig.getInt("Score", 0));
			kitPlayer.setUnlockedKits(playerConfig.getStringList("Unlocked kits"));
			if (playerConfig.contains("Kit timestamps")) {
				Map<String, Object> kitTimestampsSection = ObjectUtilities.getMap(playerConfig.get("Kit timestamps"));
				Map<String, Long> kitTimestamps = new HashMap<>();
				for (Map.Entry<String, Object> kitTimestampEntry : kitTimestampsSection.entrySet()) {
					if (Utilities.isNumber(Long.class, kitTimestampEntry.getValue()))
						kitTimestamps.put(kitTimestampEntry.getKey(), Long.parseLong(kitTimestampEntry.getValue().toString()));
				}
				kitPlayer.setKitTimestamps(kitTimestamps);
			}
			if (playerConfig.contains("Kits")) {
				Map<String, Object> kitsSection = ObjectUtilities.getMap(playerConfig.get("Kits"));
				Map<String, Kit> playerKits = new LinkedHashMap<>();
				for (Map.Entry<String, Object> kitEntry : kitsSection.entrySet()) {
					try {
						Kit playerKit = Kit.deserialize(ObjectUtilities.getMap(kitEntry.getValue()));
						if (playerKit != null) playerKits.put(playerKit.getName(), playerKit.setUserKit(true));
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to deserialize the user kit '" + kitEntry.getKey() + "' for '" + kitPlayer.getUsername() + "'", ex);
					}
				}
				kitPlayer.setKits(playerKits);
			}
			kitPlayer.setLoaded(true);
		} catch (Exception exception) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load player " + kitPlayer, exception);
			if (kitPlayer != null) kitPlayer.setLoaded(true);
		}
		return kitPlayer;
	}

	@Override
	public OfflineKitPlayer loadOfflinePlayer(String playerName) {
		OfflineKitPlayer offlineKitPlayer = new OfflineKitPlayer(playerName);
		try {
			String strPlayerUUID = null;

			File playerDataDirectory = new File(KingKits.getInstance().getDataFolder(), "players");
			if (playerDataDirectory.exists()) {
				File[] playerDataFiles = FileUtilities.getFiles(playerDataDirectory);
				FileConfiguration playerConfig = null;
				if (playerDataFiles != null) {
					for (File playerDataFile : playerDataFiles) {
						FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
						if (playerDataConfig.contains("Username") && playerDataConfig.getString("Username").equalsIgnoreCase(playerName)) {
							playerConfig = playerDataConfig;
							if (playerDataFile.getName().endsWith(".yml"))
								strPlayerUUID = playerDataFile.getName().substring(0, playerDataFile.getName().length() - 4);
							break;
						}
					}
				}

				if (playerConfig != null) {
					offlineKitPlayer.setUsername(playerConfig.getString("Username"));
					if (Utilities.isUUID(strPlayerUUID)) offlineKitPlayer.setUniqueId(UUID.fromString(strPlayerUUID));

					offlineKitPlayer.setScore(playerConfig.getInt("Score", 0));
					offlineKitPlayer.setUnlockedKits(playerConfig.getStringList("Unlocked kits"));
					if (playerConfig.contains("Kit timestamps")) {
						Map<String, Object> kitTimestampsSection = ObjectUtilities.getMap(playerConfig.get("Kit timestamps"));
						Map<String, Long> kitTimestamps = new HashMap<>();
						for (Map.Entry<String, Object> kitTimestampEntry : kitTimestampsSection.entrySet()) {
							if (Utilities.isNumber(Long.class, kitTimestampEntry.getValue()))
								kitTimestamps.put(kitTimestampEntry.getKey(), Long.parseLong(kitTimestampEntry.getValue().toString()));
						}
						offlineKitPlayer.setKitTimestamps(kitTimestamps);
					}
					if (playerConfig.contains("Kits")) {
						Map<String, Object> kitsSection = ObjectUtilities.getMap(playerConfig.get("Kits"));
						Map<String, Kit> playerKits = new LinkedHashMap<>();
						for (Map.Entry<String, Object> kitEntry : kitsSection.entrySet()) {
							try {
								Kit playerKit = Kit.deserialize(ObjectUtilities.getMap(kitEntry.getValue()));
								if (playerKit != null) playerKits.put(playerKit.getName(), playerKit.setUserKit(true));
							} catch (Exception ex) {
								Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to deserialize the user kit '" + kitEntry.getKey() + "' for '" + offlineKitPlayer.getUsername() + "'", ex);
							}
						}
						offlineKitPlayer.setKits(playerKits);
					}

					offlineKitPlayer.setLoaded(true);
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load offline player " + offlineKitPlayer, ex);
			offlineKitPlayer.setLoaded(true);
		}
		return offlineKitPlayer;
	}

	@Override
	public OfflineKitPlayer loadOfflinePlayer(UUID playerUUID) {
		OfflineKitPlayer offlineKitPlayer = new OfflineKitPlayer(playerUUID);
		try {
			File playerDataDirectory = new File(KingKits.getInstance().getDataFolder(), "players");
			if (playerDataDirectory.exists()) {
				File playerFile = new File(playerDataDirectory, playerUUID.toString() + ".yml");
				if (playerFile.exists()) {
					FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

					if (playerConfig.contains("Username"))
						offlineKitPlayer.setUsername(playerConfig.getString("Username"));
					offlineKitPlayer.setScore(playerConfig.getInt("Score", 0));
					offlineKitPlayer.setUnlockedKits(playerConfig.getStringList("Unlocked kits"));
					if (playerConfig.contains("Kit timestamps")) {
						Map<String, Object> kitTimestampsSection = ObjectUtilities.getMap(playerConfig.get("Kit timestamps"));
						Map<String, Long> kitTimestamps = new HashMap<>();
						for (Map.Entry<String, Object> kitTimestampEntry : kitTimestampsSection.entrySet()) {
							if (Utilities.isNumber(Long.class, kitTimestampEntry.getValue()))
								kitTimestamps.put(kitTimestampEntry.getKey(), Long.parseLong(kitTimestampEntry.getValue().toString()));
						}
						offlineKitPlayer.setKitTimestamps(kitTimestamps);
					}
					if (playerConfig.contains("Kits")) {
						Map<String, Object> kitsSection = ObjectUtilities.getMap(playerConfig.get("Kits"));
						Map<String, Kit> playerKits = new LinkedHashMap<>();
						for (Map.Entry<String, Object> kitEntry : kitsSection.entrySet()) {
							try {
								Kit playerKit = Kit.deserialize(ObjectUtilities.getMap(kitEntry.getValue()));
								if (playerKit != null) playerKits.put(playerKit.getName(), playerKit.setUserKit(true));
							} catch (Exception ex) {
								Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to deserialize the user kit '" + kitEntry.getKey() + "' for '" + (offlineKitPlayer.getUsername() != null ? offlineKitPlayer.getUsername() : offlineKitPlayer.getUniqueId().toString()) + "'", ex);
							}
						}
						offlineKitPlayer.setKits(playerKits);
					}

					offlineKitPlayer.setLoaded(true);
				}
			}
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load offline player " + offlineKitPlayer, ex);
			offlineKitPlayer.setLoaded(true);
		}
		return offlineKitPlayer;
	}

	@Override
	public void savePlayer(OfflineKitPlayer offlineKitPlayer) {
		this.savePlayer(offlineKitPlayer, null);
	}

	@Override
	public void savePlayer(OfflineKitPlayer offlineKitPlayer, Runnable runOnComplete) {
		if (offlineKitPlayer == null) return;
		if (!offlineKitPlayer.hasBeenModified()) return;
		KitPlayer kitPlayer = offlineKitPlayer instanceof KitPlayer ? (KitPlayer) offlineKitPlayer : null;
		try {
			File playerDataDirectory = new File(KingKits.getInstance().getDataFolder(), "players");
			if (!(playerDataDirectory.exists() || playerDataDirectory.mkdirs())) {
				Bukkit.getServer().getLogger().warning("Failed to save player " + offlineKitPlayer + ": Could not create player_data directory.");
				return;
			}
			File playerFile = offlineKitPlayer.getUniqueId() != null ? new File(playerDataDirectory, offlineKitPlayer.getUniqueId().toString() + ".yml") : (File) getPlayerDataFile(playerDataDirectory, offlineKitPlayer.getUsername())[0];
			if (playerFile == null || (!playerFile.exists() && !playerFile.createNewFile())) {
				Bukkit.getServer().getLogger().warning("Failed to save player " + offlineKitPlayer + ": Could not create player file.");
				return;
			}

			FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			if (offlineKitPlayer.getUsername() != null) playerConfig.set("Username", offlineKitPlayer.getUsername());

			playerConfig.set("Score", offlineKitPlayer.getScore());
			if (!offlineKitPlayer.getUnlockedKits().isEmpty()) {
				playerConfig.set("Unlocked kits", offlineKitPlayer.getUnlockedKits());
			}
			if (!offlineKitPlayer.getKitTimestamps().isEmpty()) {
				playerConfig.set("Kit timestamps", offlineKitPlayer.getKitTimestamps());
			}
			if (!offlineKitPlayer.getKits().isEmpty()) {
				Map<String, Map<String, Object>> serializedKits = new LinkedHashMap<>();
				for (Map.Entry<String, Kit> playerKitEntry : offlineKitPlayer.getKits().entrySet()) {
					serializedKits.put(playerKitEntry.getKey(), playerKitEntry.getValue().serialize());
				}
				playerConfig.set("Kits", serializedKits);
			} else {
				if (playerConfig.contains("Kits")) playerConfig.set("Kits", null);
			}

			if (!playerConfig.getValues(false).isEmpty()) playerConfig.save(playerFile);
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to save player " + offlineKitPlayer, ex);
		}
		if (runOnComplete != null) runOnComplete.run();
	}

	private static Object[] getPlayerDataFile(File playerDataDirectory, String playerName) {
		File playerFile = new File(playerDataDirectory, playerName + ".yml");
		boolean isUsingPlayerName = playerFile.exists();
		if (!isUsingPlayerName) {
			playerFile = null;
			File[] playerDatas = playerDataDirectory.listFiles();
			if (playerDatas != null) {
				for (File playerDataFile : playerDatas) {
					try {
						if (!playerDataFile.isDirectory() && playerDataFile.getName().endsWith(".yml")) {
							FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
							if (playerDataConfig.getString("Username", "").equalsIgnoreCase(playerName)) {
								playerName = playerDataConfig.getString("Username", "");
								playerFile = playerDataFile;
								break;
							}
						}
					} catch (Exception ignored) {
					}
				}
			}
			if (playerFile == null || playerName.isEmpty()) return null;
		}
		return new Object[]{playerFile, isUsingPlayerName};
	}

}
