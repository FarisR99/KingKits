package com.faris.kingkits.storage;

import com.faris.easysql.mysql.helper.StatementCreateTable;
import com.faris.easysql.mysql.helper.StatementInsertTable;
import com.faris.easysql.mysql.helper.StatementSelectTable;
import com.faris.easysql.mysql.helper.StatementUpdateTable;
import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.controller.SQLController;
import com.faris.kingkits.helper.util.JsonUtilities;
import com.faris.kingkits.helper.util.Utilities;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Level;

public class SQLStorage extends DataStorage {

	@Override
	public KitPlayer loadPlayer(final KitPlayer kitPlayer) {
		if (kitPlayer == null) throw new IllegalArgumentException("Kit player cannot be null!");
		try {
			Bukkit.getServer().getScheduler().runTaskAsynchronously(KingKits.getInstance(), new Runnable() {
				@Override
				public void run() {
					try {
						if (SQLController.getInstance().isEnabled()) {
							SQLController.getInstance().openConnection();

							createDefaultTable();

							StatementSelectTable.Table table = new StatementSelectTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setWhere("uuid", kitPlayer.getUniqueId()).setRowLimit(1).executeAsTable();
							if (!table.isEmpty()) {
								StatementSelectTable.Table.Column columnScore = table.getColumn(0, "score");
								if (columnScore.getValue() instanceof Number)
									kitPlayer.setScore(columnScore.asNumber().intValue());

								try {
									StatementSelectTable.Table.Column columnUnlockedKits = table.getColumn(0, "unlocked");
									if (!columnUnlockedKits.isNull() && !columnUnlockedKits.asString().trim().isEmpty()) {
										List<String> unlockedKits = new ArrayList<>();
										JsonArray jsonUnlockedKits = Utilities.getGsonParser().fromJson(columnUnlockedKits.asString(), JsonArray.class);
										for (JsonElement jsonUnlockedKit : jsonUnlockedKits)
											unlockedKits.add(jsonUnlockedKit.getAsString());
										kitPlayer.setUnlockedKits(unlockedKits);
									}
								} catch (Exception ex) {
									Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load player " + kitPlayer + "'s unlocked kits", ex);
								}

								try {
									StatementSelectTable.Table.Column columnTimestamps = table.getColumn(0, "timestamps");
									if (!columnTimestamps.isNull() && !columnTimestamps.asString().trim().isEmpty()) {
										String strTimestamps = columnTimestamps.asString();
										Map<String, Long> kitTimestamps = new HashMap<>();
										JsonObject jsonTimestamps = Utilities.getGsonParser().fromJson(strTimestamps, JsonObject.class);
										for (Map.Entry<String, JsonElement> jsonKit : jsonTimestamps.entrySet()) {
											if (jsonKit.getValue().isJsonPrimitive()) {
												kitTimestamps.put(jsonKit.getKey(), jsonKit.getValue().getAsLong());
											}
										}
										kitPlayer.setKitTimestamps(kitTimestamps);
									}
								} catch (Exception ex) {
									Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load player " + kitPlayer + "'s kit timestamps", ex);
								}

								try {
									StatementSelectTable.Table.Column columnKits = table.getColumn(0, "kits");
									if (!columnKits.isNull() && !columnKits.asString().trim().isEmpty()) {
										String strColumnKits = columnKits.asString();
										Map<String, Kit> playerKits = new LinkedHashMap<>();
										JsonObject jsonKits = Utilities.getGsonParser().fromJson(strColumnKits, JsonObject.class);
										for (Map.Entry<String, JsonElement> jsonKit : jsonKits.entrySet()) {
											if (jsonKit.getValue().isJsonPrimitive()) {
												// JsonObject jsonPlayerKit = Utilities.getGsonParser().fromJson(jsonKit.getValue().getAsJsonPrimitive().getAsString(), JsonObject.class);
												// Kit playerKit = Kit.deserialize(JsonUtilities.toMap(jsonPlayerKit)); // Utilities.getGsonParser().fromJson(jsonKit.getValue().getAsJsonPrimitive(), Kit.class);
												Kit playerKit = Kit.deserializeFromJson(jsonKit.getValue().getAsJsonPrimitive().getAsString());
												if (playerKit != null) playerKits.put(playerKit.getName(), playerKit);
											}
										}
										kitPlayer.setKits(playerKits);
									}
								} catch (Exception ex) {
									Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load player " + kitPlayer + "'s kits", ex);
								}
							}
							kitPlayer.setLoaded(true);
							kitPlayer.update();
						} else {
							setInstance(DataStorageType.FILE);
							getInstance().loadPlayer(kitPlayer);
						}
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load player " + kitPlayer, ex);
						kitPlayer.setLoaded(true);
						kitPlayer.update();
					}
				}
			});
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load player " + kitPlayer, ex);
			kitPlayer.setLoaded(true);
			kitPlayer.update();
		}
		return kitPlayer;
	}

	@Override
	public OfflineKitPlayer loadOfflinePlayer(final String playerName) {
		if (playerName == null) throw new IllegalArgumentException("Player username cannot be null!");
		final OfflineKitPlayer offlineKitPlayer = new OfflineKitPlayer(playerName);
		try {
			Bukkit.getServer().getScheduler().runTaskAsynchronously(KingKits.getInstance(), new Runnable() {
				@Override
				public void run() {
					try {
						if (SQLController.getInstance().isEnabled()) {
							SQLController.getInstance().openConnection();

							createDefaultTable();
							StatementSelectTable.Table table = new StatementSelectTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setWhere("username", offlineKitPlayer.getUsername(), true).setRowLimit(1).executeAsTable();
							if (!table.isEmpty()) {
								StatementSelectTable.Table.Column columnUUID = table.getColumn(0, "uuid");
								if (Utilities.isUUID(columnUUID.getValue()))
									offlineKitPlayer.setUniqueId(UUID.fromString(columnUUID.asString()));

								StatementSelectTable.Table.Column columnScore = table.getColumn(0, "score");
								if (columnScore.getValue() instanceof Number)
									offlineKitPlayer.setScore(columnScore.asNumber().intValue());

								StatementSelectTable.Table.Column columnUnlockedKits = table.getColumn(0, "unlocked");
								if (!columnUnlockedKits.isNull() && !columnUnlockedKits.asString().trim().isEmpty()) {
									List<String> unlockedKits = new ArrayList<>();
									JsonArray jsonUnlockedKits = Utilities.getGsonParser().fromJson(columnUnlockedKits.asString(), JsonArray.class);
									for (JsonElement jsonUnlockedKit : jsonUnlockedKits)
										unlockedKits.add(jsonUnlockedKit.getAsString());
									offlineKitPlayer.setUnlockedKits(unlockedKits);
								}

								StatementSelectTable.Table.Column columnTimestamps = table.getColumn(0, "timestamps");
								if (!columnTimestamps.isNull() && !columnTimestamps.asString().trim().isEmpty()) {
									String strTimestamps = columnTimestamps.asString();
									Map<String, Long> kitTimestamps = new HashMap<>();
									JsonObject jsonTimestamps = Utilities.getGsonParser().fromJson(strTimestamps, JsonObject.class);
									for (Map.Entry<String, JsonElement> jsonKit : jsonTimestamps.entrySet()) {
										if (jsonKit.getValue().isJsonPrimitive()) {
											kitTimestamps.put(jsonKit.getKey(), jsonKit.getValue().getAsLong());
										}
									}
									offlineKitPlayer.setKitTimestamps(kitTimestamps);
								}

								StatementSelectTable.Table.Column columnKits = table.getColumn(0, "kits");
								if (!columnKits.isNull() && !columnKits.asString().trim().isEmpty()) {
									String strColumnKits = columnKits.asString();
									Map<String, Kit> playerKits = new LinkedHashMap<>();
									JsonObject jsonKits = Utilities.getGsonParser().fromJson(strColumnKits, JsonObject.class);
									for (Map.Entry<String, JsonElement> jsonKit : jsonKits.entrySet()) {
										if (jsonKit.getValue().isJsonPrimitive()) {
											JsonObject jsonPlayerKit = Utilities.getGsonParser().fromJson(jsonKit.getValue().getAsJsonPrimitive().getAsString(), JsonObject.class);
											Kit playerKit = Kit.deserialize(JsonUtilities.toMap(jsonPlayerKit));
											if (playerKit != null) playerKits.put(playerKit.getName(), playerKit);
										}
									}
									offlineKitPlayer.setKits(playerKits);
								}
							}
							offlineKitPlayer.setLoaded(true);
						} else {
							setInstance(DataStorageType.FILE);
							getInstance().loadOfflinePlayer(playerName);
						}
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load offline player " + offlineKitPlayer, ex);
						offlineKitPlayer.setLoaded(true);
					}
				}
			});
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load offline player " + offlineKitPlayer, ex);
			offlineKitPlayer.setLoaded(true);
		}
		return offlineKitPlayer;
	}

	@Override
	public OfflineKitPlayer loadOfflinePlayer(final UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null!");
		final OfflineKitPlayer offlineKitPlayer = new OfflineKitPlayer(playerUUID);
		try {
			Bukkit.getServer().getScheduler().runTaskAsynchronously(KingKits.getInstance(), new Runnable() {
				@Override
				public void run() {
					try {
						if (SQLController.getInstance().isEnabled()) {
							SQLController.getInstance().openConnection();

							createDefaultTable();
							StatementSelectTable.Table table = new StatementSelectTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setWhere("uuid", offlineKitPlayer.getUniqueId()).setRowLimit(1).executeAsTable();
							if (!table.isEmpty()) {
								StatementSelectTable.Table.Column columnUsername = table.getColumn(0, "username");
								if (!columnUsername.isNull())
									offlineKitPlayer.setUsername(columnUsername.asString());

								StatementSelectTable.Table.Column columnScore = table.getColumn(0, "score");
								if (columnScore.getValue() instanceof Number)
									offlineKitPlayer.setScore(columnScore.asNumber().intValue());

								StatementSelectTable.Table.Column columnUnlockedKits = table.getColumn(0, "unlocked");
								if (!columnUnlockedKits.isNull() && !columnUnlockedKits.asString().trim().isEmpty()) {
									List<String> unlockedKits = new ArrayList<>();
									JsonArray jsonUnlockedKits = Utilities.getGsonParser().fromJson(columnUnlockedKits.asString(), JsonArray.class);
									for (JsonElement jsonUnlockedKit : jsonUnlockedKits)
										unlockedKits.add(jsonUnlockedKit.getAsString());
									offlineKitPlayer.setUnlockedKits(unlockedKits);
								}

								StatementSelectTable.Table.Column columnTimestamps = table.getColumn(0, "timestamps");
								if (!columnTimestamps.isNull() && !columnTimestamps.asString().trim().isEmpty()) {
									String strTimestamps = columnTimestamps.asString();
									Map<String, Long> kitTimestamps = new HashMap<>();
									JsonObject jsonTimestamps = Utilities.getGsonParser().fromJson(strTimestamps, JsonObject.class);
									for (Map.Entry<String, JsonElement> jsonKit : jsonTimestamps.entrySet()) {
										if (jsonKit.getValue().isJsonPrimitive()) {
											kitTimestamps.put(jsonKit.getKey(), jsonKit.getValue().getAsLong());
										}
									}
									offlineKitPlayer.setKitTimestamps(kitTimestamps);
								}

								StatementSelectTable.Table.Column columnKits = table.getColumn(0, "kits");
								if (!columnKits.isNull() && !columnKits.asString().trim().isEmpty()) {
									String strColumnKits = columnKits.asString();
									Map<String, Kit> playerKits = new LinkedHashMap<>();
									JsonObject jsonKits = Utilities.getGsonParser().fromJson(strColumnKits, JsonObject.class);
									for (Map.Entry<String, JsonElement> jsonKit : jsonKits.entrySet()) {
										if (jsonKit.getValue().isJsonPrimitive()) {
											JsonObject jsonPlayerKit = Utilities.getGsonParser().fromJson(jsonKit.getValue().getAsJsonPrimitive().getAsString(), JsonObject.class);
											Kit playerKit = Kit.deserialize(JsonUtilities.toMap(jsonPlayerKit));
											if (playerKit != null) playerKits.put(playerKit.getName(), playerKit);
										}
									}
									offlineKitPlayer.setKits(playerKits);
								}
							}
							offlineKitPlayer.setLoaded(true);
						} else {
							setInstance(DataStorageType.FILE);
							getInstance().loadOfflinePlayer(playerUUID);
						}
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load offline player " + offlineKitPlayer, ex);
						offlineKitPlayer.setLoaded(true);
					}
				}
			});
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to load offline player " + offlineKitPlayer, ex);
			offlineKitPlayer.setLoaded(true);
		}
		return offlineKitPlayer;
	}

	@Override
	public void savePlayer(final OfflineKitPlayer offlineKitPlayer) {
		this.savePlayer(offlineKitPlayer, null);
	}

	@Override
	public void savePlayer(final OfflineKitPlayer offlineKitPlayer, final Runnable runOnComplete) {
		if (offlineKitPlayer == null || offlineKitPlayer.getUniqueId() == null) return;
		if (!offlineKitPlayer.hasBeenModified()) return;
		final KitPlayer kitPlayer = offlineKitPlayer instanceof KitPlayer ? (KitPlayer) offlineKitPlayer : null;
		try {
			Bukkit.getServer().getScheduler().runTaskAsynchronously(KingKits.getInstance(), new Runnable() {
				@Override
				public void run() {
					try {
						if (SQLController.getInstance().isEnabled()) {
							SQLController.getInstance().openConnection();

							createDefaultTable();

							String strUUID = offlineKitPlayer.getUniqueId().toString();
							String strUsername = offlineKitPlayer.getUsername() != null ? offlineKitPlayer.getUsername() : "";
							int score = offlineKitPlayer.getScore();
							List<String> unlockedKits = offlineKitPlayer.getUnlockedKits();
							Map<String, Long> kitTimestamps = offlineKitPlayer.getKitTimestamps();
							Map<String, Kit> playerKits = offlineKitPlayer.getKits();

							if (offlineKitPlayer.getUniqueId() != null && new StatementSelectTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setWhere("uuid", offlineKitPlayer.getUniqueId()).setRowLimit(1).executeAsTable().isEmpty()) {
								StatementInsertTable statementInsertTable = new StatementInsertTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setColumns(new StatementInsertTable.Column("uuid", strUUID), new StatementInsertTable.Column("username", strUsername));
								statementInsertTable.addColumns(new StatementInsertTable.Column("score", score));
								if (!unlockedKits.isEmpty())
									statementInsertTable.addColumns(new StatementInsertTable.Column("unlocked", JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromArray(unlockedKits)))));
								if (!kitTimestamps.isEmpty())
									statementInsertTable.addColumns(new StatementInsertTable.Column("timestamps", JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromMap(kitTimestamps)))));
								if (!playerKits.isEmpty())
									statementInsertTable.addColumns(new StatementInsertTable.Column("kits", JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromMap(playerKits)))));
								statementInsertTable.execute();
							} else {
								StatementUpdateTable statementUpdateTable = new StatementUpdateTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable());
								if (offlineKitPlayer.getUniqueId() != null) {
									statementUpdateTable.setWhere("uuid", strUUID);
									if (offlineKitPlayer.getUsername() != null)
										statementUpdateTable.setColumns(new StatementUpdateTable.Column("username", offlineKitPlayer.getUsername()));
								} else if (offlineKitPlayer.getUsername() != null) {
									statementUpdateTable.setWhere("username", strUsername);
								} else {
									return;
								}
								statementUpdateTable.addColumns(new StatementUpdateTable.Column("score", score));
								statementUpdateTable.addColumns(new StatementUpdateTable.Column("unlocked", !unlockedKits.isEmpty() ? JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromArray(unlockedKits))) : ""));
								statementUpdateTable.addColumns(new StatementUpdateTable.Column("timestamps", !kitTimestamps.isEmpty() ? JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromMap(kitTimestamps))) : ""));
								statementUpdateTable.addColumns(new StatementUpdateTable.Column("kits", !playerKits.isEmpty() ? JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromMap(playerKits))) : ""));
								statementUpdateTable.execute();
							}
							if (runOnComplete != null) runOnComplete.run();
						} else {
							setInstance(DataStorageType.FILE);
							getInstance().savePlayer(offlineKitPlayer);
							if (runOnComplete != null) runOnComplete.run();
						}
					} catch (Exception ex) {
						Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to save" + (offlineKitPlayer instanceof KitPlayer ? " " : " offline ") + "player " + offlineKitPlayer, ex);
						if (runOnComplete != null) runOnComplete.run();
					} finally {
						if (SQLController.getInstance().isEnabled()) SQLController.getInstance().closeConnection();
					}
				}
			});
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to save" + (offlineKitPlayer instanceof KitPlayer ? " " : " offline ") + "player " + offlineKitPlayer, ex);
			if (runOnComplete != null) runOnComplete.run();
		}
	}

	private static void createDefaultTable() {
		getDefaultTableCreateQuery().execute();
	}

	public static StatementCreateTable getDefaultTableCreateQuery() {
		return new StatementCreateTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setIfNotExists(true).setColumns(new StatementCreateTable.Column("uuid", "VARCHAR(36)", true), new StatementCreateTable.Column("username", "VARCHAR(16)", ""), new StatementCreateTable.Column("score", "INT(10)", 0).addAttribute("UNSIGNED"), new StatementCreateTable.Column("unlocked", "VARCHAR(8000)", ""), new StatementCreateTable.Column("timestamps", "VARCHAR(8000)", ""), new StatementCreateTable.Column("kits", "VARCHAR(8000)", "")).setPrimaryColumn("uuid");
	}

}
