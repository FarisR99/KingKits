package com.faris.kingkits.controller;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.config.CustomConfiguration;
import com.faris.kingkits.helper.util.FileUtilities;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class KitController implements Controller {

	private static KitController instance = null;

	private final Map<String, Kit> kitMap;

	private File kitsFolder = null;

	private KitController() {
		this.kitMap = new LinkedHashMap<>();
		this.kitsFolder = new File(KingKits.getInstance().getDataFolder(), "kits");
		FileUtilities.createDirectory(this.kitsFolder);
	}

	@Override
	public void shutdownController() {
		this.clearKits();

		instance = null;
	}

	public boolean addKit(Kit kit) {
		if (kit != null) {
			this.kitMap.put(kit.getName(), kit);
			return true;
		} else {
			return false;
		}
	}

	public void clearKits() {
		this.kitMap.clear();
	}

	public void deleteKit(Kit kit) {
		if (kit != null) {
			File directKitFile = new File(this.kitsFolder, kit.getName());
			if (directKitFile.exists()) {
				FileUtilities.delete(directKitFile);
			} else {
				File[] kitFiles = FileUtilities.getFiles(this.kitsFolder);
				if (kitFiles.length > 0) {
					for (File kitFile : kitFiles) {
						if (kitFile.getName().endsWith(".yml")) {
							FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
							if (kitConfig.getString("Name", "").equals(kit.getName())) {
								FileUtilities.delete(kitFile);
								break;
							}
						}
					}
				}
			}
		}
	}

	public Kit getKit(String kitName) {
		return kitName != null ? this.kitMap.get(kitName) : null;
	}

	public Map<String, Kit> getKits() {
		return new LinkedHashMap<>(this.kitMap);
	}

	public void loadKits() {
		this.clearKits();

		try {
			FileUtilities.createDirectory(this.kitsFolder);
			File[] kitsFile = FileUtilities.getFiles(this.kitsFolder);
			if (kitsFile.length == 0) {
				kitsFile = new File[]{new File(this.kitsFolder, "Default.yml")};
				KingKits.getInstance().saveResource("defaultkit.yml", false, kitsFile[0]);
				if (!kitsFile[0].exists()) kitsFile = new File[0];
			}
			if (kitsFile.length > 0) {
				for (File kitFile : kitsFile) {
					try {
						CustomConfiguration kitConfig = CustomConfiguration.loadConfiguration(kitFile);
						Kit kit = Kit.deserialize(kitConfig.getValues(false));
						if (kit != null)
							this.addKit(kit);
						else
							Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to deserialize the kit '" + kitFile.getName() + "' because it is null.");
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to deserialize the kit '" + kitFile.getName() + "'", ex);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void removeKit(Kit kit) {
		if (kit != null) this.kitMap.remove(kit.getName());
	}

	public void saveKit(Kit kit) {
		if (kit != null && !kit.isUserKit()) {
			FileUtilities.createDirectory(this.kitsFolder);

			String kitName = kit.getName();
			Map<String, Object> serializedKit = kit.serialize();

			File kitFile = new File(this.kitsFolder, kitName + ".yml");
			if (kitFile.exists()) FileUtilities.delete(kitFile);

			CustomConfiguration configKit = CustomConfiguration.loadConfiguration(kitFile);
			configKit.setNewLinePerKey(true);
			for (Map.Entry<String, Object> serializationEntry : serializedKit.entrySet()) {
				configKit.set(serializationEntry.getKey(), serializationEntry.getValue());
			}
			try {
				configKit.save(kitFile);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void saveKit(Kit kit, UUID playerUUID) {
		if (kit != null && playerUUID != null && kit.isUserKit()) {
			KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(playerUUID);
			if (kitPlayer != null) {
				kitPlayer.addKit(kit);
			} else {
				OfflineKitPlayer offlineKitPlayer = PlayerController.getInstance().getOfflinePlayer(playerUUID);
				if (offlineKitPlayer != null) {
					long currentTime = System.currentTimeMillis();
					while (true) {
						if (offlineKitPlayer.isLoaded()) break;
						else if (System.currentTimeMillis() - currentTime > 1_000L) break;
					}
					offlineKitPlayer.addKit(kit);
					PlayerController.getInstance().saveOfflinePlayer(offlineKitPlayer);
				}
			}
		}
	}

	public static KitController getInstance() {
		if (instance == null) instance = new KitController();
		return instance;
	}

}
