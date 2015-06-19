/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.faris.kingkits.helper;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Utilities to lookup player usernames and UUIDs from mojang.
 * This caches results so you won't have issues with the rate limit.
 *
 * @author Techcable
 */
public class UUIDFetcher {

	private static Cache<String, PlayerProfile> nameCache = new Cache<>();

	/**
	 * Lookup a profile with the given name.
	 *
	 * @param name Look for a profile with this name.
	 * @return A profile with the given name
	 */
	public static PlayerProfile lookupName(String name) {
		if (name != null) {
			try {
				if (nameCache.contains(name)) return nameCache.get(name);
				List<PlayerProfile> response = postNames(Collections.singletonList(name));
				if (response == null || response.isEmpty()) return null;
				return response.get(0);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Lookup a profile with the given name.
	 *
	 * @param names Look for a profile with this name.
	 * @return A profile with the given name
	 */
	public static Map<String, UUID> lookupNames(List<String> names) {
		Map<String, UUID> playerProfiles = new HashMap<>();
		if (names != null && !names.isEmpty()) {
			List<Integer> removeIndexes = new ArrayList<>();
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				if (nameCache.contains(name)) {
					PlayerProfile playerProfile = nameCache.get(name);
					playerProfiles.put(playerProfile.getName(), playerProfile.getId());
					removeIndexes.add(i);
				}
			}
			for (Integer removeIndex : removeIndexes) names.remove(removeIndex.intValue());
			try {
				List<PlayerProfile> response = postNames(names);
				if (response != null) {
					for (PlayerProfile playerProfile : response) {
						playerProfiles.put(playerProfile.getName(), playerProfile.getId());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return playerProfiles;
	}

	/**
	 * Lookup a profile with the given UUID.
	 *
	 * @param playerUUID Look for a profile with this UUID.
	 * @return A profile with the given UUID.
	 */
	public static PlayerProfile lookupID(UUID playerUUID) {
		return playerUUID != null && Bukkit.getPlayer(playerUUID) != null ? fromPlayer(Bukkit.getPlayer(playerUUID)) : lookupProperties(playerUUID);
	}

	/**
	 * Lookup the player's properties
	 *
	 * @param playerUUID Player's UUID to lookup.
	 * @return The player's profile with properties
	 */
	private static PlayerProfile lookupProperties(UUID playerUUID) {
		try {
			if (idCache.contains(playerUUID)) return idCache.get(playerUUID);
			Object rawResponse = getJson("https://sessionserver.mojang.com/session/minecraft/profile/" + toString(playerUUID));
			if (rawResponse == null || !(rawResponse instanceof JSONObject)) return null;
			JSONObject response = (JSONObject) rawResponse;
			PlayerProfile profile = deserializeProfile(response);
			if (profile == null) return null;
			idCache.put(playerUUID, profile);
			return profile;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Represents a player.
	 *
	 * @author Techcable
	 */
	public static class PlayerProfile {
		public PlayerProfile(UUID id, String name) {
			this.id = id;
			this.name = name;
		}

		private JSONArray properties;
		private final UUID id;
		private final String name;

		/**
		 * Get this player's UUID.
		 *
		 * @return This player's UUID
		 */
		public UUID getId() {
			return this.id;
		}

		/**
		 * Get this player's name.
		 *
		 * @return This player's name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Get a JSON array with this player's properties.
		 * Returns null if this players properties haven't been retrieved.
		 *
		 * @return A JSON array with this player's properties or null if not retrieved
		 */
		public JSONArray getProperties() {
			return this.properties != null ? this.properties : (this.properties = new JSONArray());
		}
	}

	private static Cache<UUID, PlayerProfile> idCache = new Cache<>();

	private static List<PlayerProfile> postNames(List<String> names) throws Exception {
		try {
			JSONArray request = new JSONArray();
			for (String name : names) request.add(name);
			Object rawResponse = postJson("https://api.mojang.com/profiles/minecraft", request);
			if (!(rawResponse instanceof JSONArray)) return null;
			JSONArray response = (JSONArray) rawResponse;
			List<PlayerProfile> profiles = new ArrayList<>();
			for (Object rawEntry : response) {
				if (!(rawEntry instanceof JSONObject)) return null;
				JSONObject entry = (JSONObject) rawEntry;
				PlayerProfile profile = deserializeProfile(entry);
				if (profile != null) profiles.add(profile);
			}
			return profiles;
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static PlayerProfile deserializeProfile(JSONObject jsonObject) {
		if (jsonObject == null || !jsonObject.containsKey("name") || !jsonObject.containsKey("id")) return null;
		if (!(jsonObject.get("name") instanceof String) || !(jsonObject.get("id") instanceof String)) return null;
		String name = (String) jsonObject.get("name");
		UUID playerUUID = toUUID((String) jsonObject.get("id"));
		if (playerUUID == null) return null;
		PlayerProfile profile = new PlayerProfile(playerUUID, name);
		if (jsonObject.containsKey("properties") && jsonObject.get("properties") instanceof JSONArray)
			profile.properties = (JSONArray) jsonObject.get("properties");
		return profile;
	}

	/*
	 * Utilities
	 */

	private static String toString(UUID uuid) {
		return uuid.toString().replace("-", "");
	}

	private static UUID toUUID(String raw) {
		if (raw == null) return null;
		String dashed;
		if (raw.length() == 32) {
			dashed = raw.substring(0, 8) + "-" + raw.substring(8, 12) + "-" + raw.substring(12, 16) + "-" + raw.substring(16, 20) + "-" + raw.substring(20, 32);
		} else {
			dashed = raw;
		}
		return UUID.fromString(dashed);
	}

	private static JSONParser JSON_PARSER = new JSONParser();

	private static Object getJson(String rawUrl) {
		BufferedReader reader = null;
		try {
			URL url = new URL(rawUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) result.append(line);

			return JSON_PARSER.parse(result.toString());
		} catch (Exception ex) {
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	private static String post(String rawUrl, String body) {
		BufferedReader reader = null;
		OutputStream out = null;

		try {
			URL url = new URL(rawUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			out = connection.getOutputStream();
			out.write(body.getBytes());

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) result.append(line);

			return result.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
				if (reader != null) reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	private static Object postJson(String url, JSONArray body) {
		String rawResponse = post(url, body.toJSONString());
		if (rawResponse == null) return null;
		try {
			return JSON_PARSER.parse(rawResponse);
		} catch (Exception e) {
			return null;
		}
	}

	private static class Cache<K, V> {
		private long expireTime = 1000 * 60 * 5;
		private Map<K, CachedEntry<V>> map = new HashMap<>();

		public boolean contains(K key) {
			return this.map.containsKey(key) && this.get(key) != null;
		}

		public V get(K key) {
			CachedEntry<V> entry = this.map.get(key);
			if (entry == null) return null;
			if (entry.isExpired()) {
				this.map.remove(key);
				return null;
			} else {
				return entry.getValue();
			}
		}

		public void put(K key, V value) {
			this.map.put(key, new CachedEntry(value, this.expireTime));
		}

		private static class CachedEntry<V> {
			private final SoftReference<V> value;
			private final long expires;

			public CachedEntry(V value, long expireTime) {
				this.value = new SoftReference<V>(value);
				this.expires = expireTime + System.currentTimeMillis();
			}

			public V getValue() {
				return this.isExpired() ? null : this.value.get();
			}

			public boolean isExpired() {
				return this.value.get() == null || (this.expires != -1 && this.expires > System.currentTimeMillis());
			}
		}
	}

	private static PlayerProfile fromPlayer(Player player) {
		return new PlayerProfile(player.getUniqueId(), player.getName());
	}

}