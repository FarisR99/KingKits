package com.faris.kingkits.player;

import com.faris.kingkits.Kit;
import com.faris.kingkits.controller.KitController;
import com.faris.kingkits.controller.PlayerController;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitPlayer extends OfflineKitPlayer {

	private final Player player;
	private int loadTaskID = -1;

	private Kit kit = null;

	private int killstreak = 0;

	public KitPlayer(Player player) {
		super(player.getName());
		this.player = player;
		this.playerUUID = player.getUniqueId();
	}

	public Player getBukkitPlayer() {
		return this.player.isOnline() ? this.player : null;
	}

	public int getKillstreak() {
		return this.killstreak;
	}

	public Kit getKit() {
		return this.kit;
	}

	public int getLoadTaskID() {
		return this.loadTaskID;
	}

	@Override
	public String getUsername() {
		if (this.isOnline()) this.playerUsername = this.player.getName();
		return this.playerUsername;
	}

	public boolean hasKit() {
		return this.kit != null;
	}

	public boolean hasPermission(Kit kit) {
		return kit != null && (kit.isUserKit() ? this.playerKits.containsKey(kit.getName()) && this.playerKits.containsValue(kit) : this.player.hasPermission("kingkits.kits." + kit.getName().toLowerCase()));
	}

	public boolean hasUnlocked(Kit kit) {
		return kit != null && this.unlockedKits.contains(kit.getName());
	}

	public void incrementKillstreak() {
		this.killstreak++;
	}

	@Override
	public boolean isOnline() {
		return this.player.isOnline();
	}

	public void onDeath() {
		this.resetKillstreak();
	}

	public void onLeave() {
		this.resetKillstreak();

		for (Map.Entry<String, Long> timestampEntry : new HashMap<>(this.kitTimestamps).entrySet()) {
			Kit targetKit = KitController.getInstance().getKit(timestampEntry.getKey());
			if (targetKit != null) {
				long kitTimestamp = timestampEntry.getValue();
				if (targetKit.hasCooldown()) {
					if (kitTimestamp != -1L) {
						if (System.currentTimeMillis() - kitTimestamp > (long) (targetKit.getCooldown() * 1000D))
							this.setKitTimestamp(targetKit, null);
					}
				}
			}
		}
	}

	public void resetKillstreak() {
		this.killstreak = 0;
	}

	public void setKit(Kit kit) {
		this.kit = kit;
	}

	public void setLoadTaskID(int loadTaskID) {
		this.loadTaskID = loadTaskID;
	}

	@Override
	public void setUniqueId(UUID uuid) {
		throw new UnsupportedOperationException("You cannot modify the UUID of an online player.");
	}

	public void update() {
		PlayerController.getInstance().updatePlayer(this);
	}

}
