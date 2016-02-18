package com.faris.kingkits.storage;

import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

public abstract class DataStorage {

	private static DataStorage instance = null;

	public abstract KitPlayer loadPlayer(KitPlayer kitPlayer);

	public abstract OfflineKitPlayer loadOfflinePlayer(String playerName);

	public abstract OfflineKitPlayer loadOfflinePlayer(UUID playerUUID);

	public abstract void savePlayer(OfflineKitPlayer offlineKitPlayer);

	public abstract void savePlayer(OfflineKitPlayer offlineKitPlayer, Runnable runOnComplete);

	public static DataStorage getInstance() {
		return instance;
	}

	public static void setInstance(DataStorageType storageType) {
		if (storageType != null) {
			switch (storageType) {
				case FILE:
					instance = new FlatFileStorage();
					break;
				case SQL:
					instance = new SQLStorage();
					break;
				default:
					instance = new FlatFileStorage();
					Bukkit.getServer().getLogger().warning("An invalid storage type was provided! Using default storage type: Flat");
					break;
			}
		} else {
			instance = new FlatFileStorage();
			Bukkit.getServer().getLogger().warning("An invalid storage type was provided! Using default storage type: Flat");
		}
	}

	public enum DataStorageType {
		FILE,
		SQL;

		public static DataStorageType getByName(String name) {
			if (name != null) {
				for (DataStorageType storageType : values()) {
					if (storageType.name().equalsIgnoreCase(name)) return storageType;
				}
			}
			return FILE;
		}
	}

}
