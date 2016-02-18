package com.faris.kingkits.controller;

import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import com.faris.kingkits.storage.DataStorage;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerController implements Controller {

	private static PlayerController instance;

	private final Map<UUID, KitPlayer> playerRegistry;

	private PlayerController() {
		this.playerRegistry = new HashMap<>();
	}

	@Override
	public void shutdownController() {
		this.playerRegistry.clear();

		instance = null;
	}

	public Collection<KitPlayer> getAllPlayers() {
		return this.playerRegistry.values();
	}

	public OfflineKitPlayer getOfflinePlayer(String playerName) {
		if (playerName != null) {
			try {
				return DataStorage.getInstance().loadOfflinePlayer(playerName);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public OfflineKitPlayer getOfflinePlayer(UUID playerUUID) {
		if (playerUUID != null) {
			try {
				return DataStorage.getInstance().loadOfflinePlayer(playerUUID);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public KitPlayer getPlayer(Player bukkitPlayer) {
		return bukkitPlayer != null ? this.playerRegistry.get(bukkitPlayer.getUniqueId()) : null;
	}

	public KitPlayer getPlayer(UUID playerUUID) {
		return playerUUID != null ? this.playerRegistry.get(playerUUID) : null;
	}

	public KitPlayer registerPlayer(Player player) {
		KitPlayer kitPlayer = null;
		if (!this.playerRegistry.containsKey(player.getUniqueId())) {
			kitPlayer = new KitPlayer(player);
			this.playerRegistry.put(player.getUniqueId(), kitPlayer);
		} else {
			kitPlayer = this.playerRegistry.get(player.getUniqueId());
		}
		return kitPlayer;
	}

	public void saveAllPlayers() {
		try {
			for (KitPlayer kitPlayer : new ArrayList<>(this.playerRegistry.values())) {
				if (kitPlayer.isLoaded()) DataStorage.getInstance().savePlayer(kitPlayer);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void saveOfflinePlayer(OfflineKitPlayer offlineKitPlayer) {
		try {
			DataStorage.getInstance().savePlayer(offlineKitPlayer);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void savePlayer(KitPlayer kitPlayer) {
		try {
			DataStorage.getInstance().savePlayer(kitPlayer);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public KitPlayer unregisterPlayer(Player bukkitPlayer) {
		return this.playerRegistry.remove(bukkitPlayer.getUniqueId());
	}

	public void updatePlayer(KitPlayer kitPlayer) {
		if (kitPlayer != null && this.playerRegistry.containsKey(kitPlayer.getUniqueId())) {
			KitPlayer currentPlayer = this.playerRegistry.get(kitPlayer.getUniqueId());
			if (currentPlayer != null) {
				currentPlayer.setLoaded(kitPlayer.isLoaded());
				this.playerRegistry.put(kitPlayer.getUniqueId(), currentPlayer);
			} else {
				this.playerRegistry.put(kitPlayer.getUniqueId(), kitPlayer);
			}
		}
	}

	public static PlayerController getInstance() {
		if (instance == null) instance = new PlayerController();
		return instance;
	}

}
