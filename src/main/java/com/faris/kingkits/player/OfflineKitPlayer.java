package com.faris.kingkits.player;

import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.util.ObjectUtilities;
import com.faris.kingkits.helper.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

public class OfflineKitPlayer implements ConfigurationSerializable {

	protected boolean loaded = false;
	protected boolean modified = false;

	protected UUID playerUUID = null;
	protected String playerUsername = null;

	protected List<String> unlockedKits = new ArrayList<>();
	protected Map<String, Kit> playerKits = new LinkedHashMap<>();
	protected Map<String, Long> kitTimestamps = new HashMap<>();

	protected int score = 0;

	public OfflineKitPlayer(KitPlayer kitPlayer) {
		this.playerUUID = kitPlayer.getUniqueId();
	}

	public OfflineKitPlayer(String username) {
		this.playerUsername = username;
	}

	public OfflineKitPlayer(UUID uuid) {
		this.playerUUID = uuid;
	}

	public void addKit(Kit kit) {
		if (kit != null) {
			if (this.loaded && (!this.playerKits.containsKey(kit.getName()) || !kit.equals(this.playerKits.get(kit.getName())))) {
				this.modified = true;
			}
			this.playerKits.put(kit.getName(), kit);
		}
	}

	public Player getBukkitPlayer() {
		return Bukkit.getServer().getPlayer(this.playerUUID);
	}

	public Map<String, Kit> getKits() {
		return new LinkedHashMap<>(this.playerKits);
	}

	public long getKitTimestamp(Kit kit) {
		return kit != null ? (this.kitTimestamps.containsKey(kit.getName()) ? this.kitTimestamps.get(kit.getName()) : -1L) : 0L;
	}

	public Map<String, Long> getKitTimestamps() {
		return new HashMap<>(this.kitTimestamps);
	}

	public int getScore() {
		return this.score;
	}

	public UUID getUniqueId() {
		return this.playerUUID;
	}

	public List<String> getUnlockedKits() {
		return this.unlockedKits;
	}

	public String getUsername() {
		return this.playerUsername;
	}

	public boolean hasBeenModified() {
		return this.modified;
	}

	public boolean isLoaded() {
		return this.loaded;
	}

	public boolean isOnline() {
		return false;
	}

	public void removeKit(Kit kit) {
		if (kit != null) {
			if (this.loaded && this.playerKits.containsKey(kit.getName())) this.modified = true;
			this.playerKits.remove(kit.getName());
		}
	}

	public void setKits(Map<String, Kit> kits) {
		if (this.loaded && !this.playerKits.equals(kits)) this.modified = true;
		this.playerKits = kits == null ? new HashMap<String, Kit>() : kits;
	}

	public void setKitTimestamp(Kit kit, Long timestamp) {
		if (kit != null) {
			if (timestamp != null) {
				this.kitTimestamps.put(kit.getName(), timestamp);
				if (this.loaded && !this.kitTimestamps.containsKey(kit.getName())) this.modified = true;
			} else {
				if (this.loaded && this.kitTimestamps.containsKey(kit.getName())) this.modified = true;
				this.kitTimestamps.remove(kit.getName());
			}
		}
	}

	public void setKitTimestamps(Map<String, Long> kitTimestamps) {
		if (kitTimestamps != null) {
			if (this.loaded && !this.kitTimestamps.equals(kitTimestamps)) this.modified = true;
			this.kitTimestamps = kitTimestamps;
		} else {
			if (this.loaded && !this.kitTimestamps.isEmpty()) this.modified = true;
			this.kitTimestamps.clear();
		}
	}

	public void setLoaded(boolean isLoaded) {
		this.loaded = isLoaded;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public void setScore(int score) {
		if (this.loaded && this.score != score) this.modified = true;
		this.score = score;
	}

	public void setUniqueId(UUID uuid) {
		this.playerUUID = uuid;
	}

	public void setUnlockedKit(String kitName, boolean flag) {
		if (kitName != null) {
			if (flag) {
				if (!this.unlockedKits.contains(kitName)) {
					this.unlockedKits.add(kitName);
					if (this.loaded) this.modified = true;
				}
			} else {
				if (this.unlockedKits.contains(kitName)) {
					this.unlockedKits.remove(kitName);
					if (this.loaded) this.modified = true;
				}
			}
		}
	}

	public void setUnlockedKits(List<String> unlockedKits) {
		if (unlockedKits != null) {
			if (this.loaded && !this.unlockedKits.equals(unlockedKits)) this.modified = true;
			this.unlockedKits = unlockedKits;
		} else {
			if (this.loaded && !this.unlockedKits.isEmpty()) this.modified = true;
			this.unlockedKits.clear();
		}
	}

	public void setUsername(String username) {
		if (username != null) {
			if (!username.equals(this.playerUsername)) this.modified = true;
			this.playerUsername = username;
		}
	}

	@Override
	public String toString() {
		return this.getUsername() == null ? this.playerUUID.toString() : this.getUsername();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serializedPlayer = new LinkedHashMap<>();
		if (this.playerUUID != null) serializedPlayer.put("UUID", this.playerUUID.toString());
		if (this.playerUsername != null) serializedPlayer.put("Username", this.playerUsername);
		serializedPlayer.put("Score", this.score);
		serializedPlayer.put("Kit timestamps", this.kitTimestamps);
		serializedPlayer.put("Player kits", new LinkedHashMap<String, Map<String, Object>>() {{
			for (Map.Entry<String, Kit> playerKit : playerKits.entrySet()) {
				this.put(playerKit.getKey(), playerKit.getValue().serialize());
			}
		}});
		serializedPlayer.put("Unlocked kits", this.unlockedKits);
		return serializedPlayer;
	}

	public static OfflineKitPlayer deserialize(Map<String, Object> serializedPlayer) {
		OfflineKitPlayer deserializedPlayer = null;
		if (serializedPlayer != null) {
			try {
				if (serializedPlayer.containsKey("UUID")) {
					deserializedPlayer = new OfflineKitPlayer(UUID.fromString(ObjectUtilities.getObject(serializedPlayer, String.class, "UUID")));
					if (serializedPlayer.containsKey("Username"))
						deserializedPlayer.setUsername(ObjectUtilities.getObject(serializedPlayer, String.class, "Username"));
				} else if (serializedPlayer.containsKey("Username")) {
					deserializedPlayer = new OfflineKitPlayer(ObjectUtilities.getObject(serializedPlayer, String.class, "Username"));
				}
				if (deserializedPlayer != null) {
					if (serializedPlayer.containsKey("Score"))
						deserializedPlayer.setScore(ObjectUtilities.getObject(serializedPlayer, Integer.class, "Score"));
					if (serializedPlayer.containsKey("Kit timestamps")) {
						Map<String, Object> serializedKitTimestamps = ObjectUtilities.getMap(serializedPlayer.get("Kit timestamps"));
						Map<String, Long> deserializedKitTimestamps = new HashMap<>();
						for (Map.Entry<String, Object> serializedKitTimestamp : serializedKitTimestamps.entrySet()) {
							if (Utilities.isNumber(Long.class, serializedKitTimestamp.getValue()))
								deserializedKitTimestamps.put(serializedKitTimestamp.getKey(), Long.parseLong(serializedKitTimestamp.getValue().toString()));
						}
						if (!deserializedKitTimestamps.isEmpty())
							deserializedPlayer.setKitTimestamps(deserializedKitTimestamps);
					}
					if (serializedPlayer.containsKey("Player kits")) {
						Map<String, Object> serializedPlayerKits = ObjectUtilities.getMap(serializedPlayer.get("Player kits"));
						Map<String, Kit> deserializedPlayerKits = new LinkedHashMap<>();
						for (Map.Entry<String, Object> serializedPlayerKit : serializedPlayerKits.entrySet()) {
							Kit deserializedPlayerKit = Kit.deserialize(ObjectUtilities.getMap(serializedPlayerKit.getValue()));
							if (deserializedPlayerKit != null) {
								deserializedPlayerKit.setUserKit(true);
								deserializedPlayerKits.put(serializedPlayerKit.getKey(), deserializedPlayerKit);
							}
						}
						if (!deserializedPlayerKits.isEmpty()) deserializedPlayer.setKits(deserializedPlayerKits);
					}
					if (serializedPlayer.containsKey("Unlocked kits"))
						deserializedPlayer.setUnlockedKits(Utilities.toStringList(ObjectUtilities.getObject(serializedPlayer, List.class, "Unlocked kits")));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return deserializedPlayer;
	}

}
