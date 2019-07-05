package com.faris.kingkits.listener;

import com.faris.BackwardsCompatibility;
import com.faris.easysql.mysql.MySQLHandler;
import com.faris.easysql.mysql.helper.StatementDropTable;
import com.faris.easysql.mysql.helper.StatementInsertTable;
import com.faris.easysql.mysql.helper.StatementSelectTable;
import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.*;
import com.faris.kingkits.helper.util.*;
import com.faris.kingkits.player.KitPlayer;
import com.faris.kingkits.player.OfflineKitPlayer;
import com.faris.kingkits.storage.DataStorage;
import com.faris.kingkits.storage.FlatFileStorage;
import com.faris.kingkits.storage.SQLStorage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.logging.*;

public class CommandListener implements CommandExecutor {

	private KingKits plugin = null;

	public CommandListener(KingKits pluginInstance) {
		this.plugin = pluginInstance;
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
		if (cmd.getName().equals("kingkits")) {
			try {
				if (args.length > 0) {
					String subCommand = args[0];
					if (subCommand.equalsIgnoreCase("reload")) {
						if (sender.hasPermission(Permissions.COMMAND_RELOAD)) {
							try {
								this.plugin.stopAutoSaveTask();

								if (SQLController.hasInstance()) SQLController.getInstance().closeConnection();
								PlayerController.getInstance().shutdownController();

								ConfigController.getInstance().reloadConfigs();
								ConfigController.getInstance().loadConfiguration();

								KitController.getInstance().loadKits();
								GuiController.getInstance().loadInventories();

								PlayerController.getInstance();
								if (ConfigController.getInstance().getSQLDetails() != null && ConfigController.getInstance().getSQLDetails().isEnabled()) {
									SQLController.getInstance().setHandler(MySQLHandler.newInstance(this.plugin, ConfigController.getInstance().getSQLDetails()));
									SQLController.getInstance().openConnection();
								}
								if (DataStorage.getInstance() == null) {
									if (ConfigController.getInstance().getSQLDetails().isEnabled()) {
										DataStorage.createInstance(DataStorage.DataStorageType.SQL);
									} else {
										DataStorage.createInstance(DataStorage.DataStorageType.FILE);
									}
								}

								try {
									for (Player player : sender.getServer().getOnlinePlayers()) {
										this.plugin.getEventListener().handleJoinEvent(player);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}

								this.plugin.startAutoSaveTask();

								Messages.initMessages(this.plugin);
								Messages.sendMessage(sender, Messages.COMMAND_RELOAD_SUCCESSFUL, this.plugin.getDescription().getVersion());
							} catch (Exception ex) {
								this.plugin.getLogger().log(Level.SEVERE, "Failed to reload the configurations.", ex);
								Messages.sendMessage(sender, Messages.COMMAND_RELOAD_FAILED);
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("convert")) {
						if (sender.isOp()) {
							if (args.length == 2) {
								String strType = args[1];
								if (strType.equalsIgnoreCase("file")) {
									if (SQLController.getInstance().getHandler().testConnection()) {
										final File playerDataFolder = new File(this.plugin.getDataFolder(), "players");
										if (FileUtilities.createDirectory(playerDataFolder)) {
											if (playerDataFolder.exists()) FileUtilities.deleteInside(playerDataFolder);

											sender.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
												private int success = 0, total = 0;

												@Override
												public void run() {
													try {
														if (SQLController.getInstance().getHandler().doesTableExist(SQLController.getInstance().getPlayersTable())) {
															StatementSelectTable selectTable = new StatementSelectTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable());
															StatementSelectTable.Table selectResult = selectTable.executeAsTable();
															if (selectResult.hasColumn("uuid")) {
																this.total = selectResult.getRowCount();
																for (int row = 0; row < selectResult.getRowCount(); row++) {
																	String strUUID = null;
																	try {
																		strUUID = selectResult.getColumn(row, "uuid").asString();
																		if (Utilities.isUUID(strUUID)) {
																			UUID uuid = UUID.fromString(strUUID);
																			File playerDataFile = new File(playerDataFolder, uuid.toString() + ".yml");
																			if (playerDataFile.exists()) {
																				playerDataFile.delete();
																			}

																			FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

																			StatementSelectTable.Table.Column columnUsername = selectResult.getColumn(row, "username");
																			if (!columnUsername.isNull() && !columnUsername.asString().trim().isEmpty()) {
																				playerDataConfig.set("Username", columnUsername.asString());
																			}

																			StatementSelectTable.Table.Column columnScore = selectResult.getColumn(row, "score");
																			if (!columnUsername.isNull()) {
																				playerDataConfig.set("Score", columnScore.asInteger());
																			}

																			StatementSelectTable.Table.Column columnUnlockedKits = selectResult.getColumn(row, "unlocked");
																			if (!columnUnlockedKits.isNull() && !columnUnlockedKits.asString().trim().isEmpty()) {
																				playerDataConfig.set("Unlocked kits", columnUnlockedKits.asString());
																			}

																			StatementSelectTable.Table.Column columnKits = selectResult.getColumn(row, "kits");
																			if (!columnKits.isNull() && !columnKits.asString().trim().isEmpty()) {
																				Map<String, Map<String, Object>> playerKitsSerialized = new LinkedHashMap<>();
																				JsonObject jsonKits = Utilities.getGsonParser().fromJson(columnKits.asString(), JsonObject.class);
																				for (Map.Entry<String, JsonElement> jsonKit : jsonKits.entrySet()) {
																					if (jsonKit.getValue().isJsonPrimitive()) {
																						Kit playerKit = Utilities.getGsonParser().fromJson(jsonKit.getValue().getAsJsonPrimitive(), Kit.class);
																						if (playerKit != null) {
																							playerKitsSerialized.put(playerKit.getName(), playerKit.serialize());
																						}
																					}
																				}
																				if (!playerKitsSerialized.isEmpty()) {
																					playerDataConfig.set("Kits", playerKitsSerialized);
																				}
																			}

																			playerDataConfig.save(playerDataFile);
																			this.success++;
																		}
																	} catch (Exception ex) {
																		Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to convert " + strUUID + " from SQL to flat file.", ex);
																	}
																}
																BukkitUtilities.sendMessageSync(sender, ChatColor.GOLD + "Successfully converted " + this.success + "/" + this.total + " players from SQL to flat file!");
															} else {
																BukkitUtilities.sendMessageSync(sender, ChatColor.GOLD + "Successfully converted 0/0 players from SQL to flat file!");
															}
														} else {
															BukkitUtilities.sendMessageSync(sender, ChatColor.GOLD + "Successfully converted 0/0 players from SQL to flat file!");
														}
													} catch (Exception ex) {
														ex.printStackTrace();
													} finally {
														if (!SQLController.getInstance().isEnabled()) {
															SQLController.getInstance().closeConnection();
														}
													}
												}
											});
										} else {
											sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Failed to create the 'players' directory.");
										}
									} else {
										sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Failed to connect to the SQL database.");
									}
								} else if (strType.equalsIgnoreCase("sql")) {
									if (SQLController.getInstance().getHandler().testConnection()) {
										final File[] playerDataFiles = FileUtilities.getFiles(new File(this.plugin.getDataFolder(), "players"));

										sender.sendMessage(ChatColor.GOLD + "Converting...");
										sender.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
											private int success = 0, total = 0;

											@Override
											public void run() {
												try {
													final List<String> sqlQueries = new ArrayList<>();
													if (SQLController.getInstance().getHandler().doesTableExist(SQLController.getInstance().getPlayersTable())) {
														sqlQueries.add(new StatementDropTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).toSQLString());
													}
													sqlQueries.add(SQLStorage.getDefaultTableCreateQuery().toSQLString());
													for (File playerDataFile : playerDataFiles) {
														if (playerDataFile.getName().endsWith(".yml")) {
															String strUUID = playerDataFile.getName().substring(0, playerDataFile.getName().length() - 4);
															if (Utilities.isUUID(strUUID)) {
																UUID playerUUID = UUID.fromString(strUUID);
																FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

																Map<String, Kit> playerKits = new LinkedHashMap<>();
																if (playerDataConfig.contains("Kits")) {
																	Map<String, Object> kitsSection = ObjectUtilities.getMap(playerDataConfig.get("Kits"));
																	for (Map.Entry<String, Object> kitEntry : kitsSection.entrySet()) {
																		try {
																			Kit playerKit = Kit.deserialize(ObjectUtilities.getMap(kitEntry.getValue()));
																			if (playerKit != null) {
																				playerKits.put(playerKit.getName(), playerKit.setUserKit(true));
																			}
																		} catch (Exception ignored) {
																		}
																	}
																}

																StatementInsertTable statementInsertTable = new StatementInsertTable(SQLController.getInstance().getHandler()).setTable(SQLController.getInstance().getPlayersTable()).setColumns(new StatementInsertTable.Column("uuid", playerUUID.toString()));
																if (playerDataConfig.contains("Username")) {
																	statementInsertTable.addColumns(new StatementInsertTable.Column("username", playerDataConfig.getString("Username")));
																}
																if (playerDataConfig.getInt("Score", 0) != 0) {
																	statementInsertTable.addColumns(new StatementInsertTable.Column("score", playerDataConfig.getInt("Score")));
																}
																if (playerDataConfig.contains("Unlocked kits") && !playerDataConfig.getStringList("Unlocked kits").isEmpty()) {
																	statementInsertTable.addColumns(new StatementInsertTable.Column("unlocked", playerDataConfig.getStringList("Unlocked kits")));
																}
																if (!playerKits.isEmpty()) {
																	statementInsertTable.addColumns(new StatementInsertTable.Column("kits", JSONObject.escape(Utilities.getGsonParser().toJson(JsonUtilities.fromMap(playerKits)))));
																}
																String insertQuery = statementInsertTable.toSQLString();
																if (insertQuery != null) sqlQueries.add(insertQuery);
															}
														}
													}

													for (String sqlQuery : sqlQueries) {
														try {
															if (sqlQuery != null) {
																if (sqlQuery.startsWith("INSERT ")) this.total++;
																PreparedStatement preparedStatement = null;
																try {
																	preparedStatement = SQLController.getInstance().getHandler().getConnection().prepareStatement(sqlQuery);
																	preparedStatement.executeUpdate();
																	if (sqlQuery.startsWith("INSERT ")) this.success++;
																} finally {
																	if (preparedStatement != null) {
																		Utilities.silentlyClose(preparedStatement);
																	}
																}
															}
														} catch (Exception ex) {
															ex.printStackTrace();
														}
													}
													BukkitUtilities.sendMessageSync(sender, ChatColor.GOLD + "Successfully converted " + success + "/" + total + " players from flat file to SQL!");
												} catch (Exception ex) {
													ex.printStackTrace();
													BukkitUtilities.sendMessageSync(sender, Messages.GENERAL_COMMAND_ERROR, ex.getClass().getName());
												} finally {
													if (!SQLController.getInstance().isEnabled()) {
														SQLController.getInstance().closeConnection();
													}
												}
											}
										});
									} else {
										sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Failed to connect to the SQL database.");
									}
								} else {
									Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase() + " <file|sql>");
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase() + " <file|sql>");
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("config")) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							if (player.hasPermission(Permissions.COMMAND_CONFIG)) {
								if (args.length == 1) {
									ItemStack itemInHand = player.getInventory().getItemInMainHand();
									if (!ItemUtilities.isNull(itemInHand) && itemInHand.getType() == Material.WRITTEN_BOOK && itemInHand.getItemMeta() instanceof BookMeta) {
										BookMeta bookMeta = (BookMeta) itemInHand.getItemMeta();
										List<String> bookLines = new ArrayList<>();
										for (String bookPageLine : bookMeta.getPages()) {
											String[] bookPageLines = bookPageLine.split("\n");
											Collections.addAll(bookLines, bookPageLines);
										}

										File tempFolder = new File(this.plugin.getDataFolder(), "temp");
										FileUtilities.createDirectory(tempFolder);

										File tempConfigFile = new File(tempFolder, "config-" + System.currentTimeMillis() + ".yml");
										BufferedWriter tempConfigWriter = null;
										try {
											tempConfigWriter = new BufferedWriter(new FileWriter(tempConfigFile));
											for (String bookLine : bookLines) {
												tempConfigWriter.append(ChatUtilities.stripColour(bookLine.replace("\n", "")));
												tempConfigWriter.newLine();
											}
										} finally {
											Utilities.silentlyClose(tempConfigWriter);
										}

										FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(tempConfigFile);
										for (Map.Entry<String, Object> configEntry : tempConfig.getValues(false).entrySet())
											ConfigController.getInstance().getConfig().set(configEntry.getKey(), configEntry.getValue());
										ConfigController.getInstance().saveConfig();

										if (FileUtilities.getFiles(tempFolder).length <= 1)
											FileUtilities.delete(tempFolder);

										Messages.sendMessage(sender, Messages.COMMAND_CONFIG_SAVED);
									} else {
										ItemStack configBook = new ItemStack(BackwardsCompatibility.getBookAndQuill());
										ItemMeta itemMeta = configBook.getItemMeta();
										if (itemMeta != null) {
											itemMeta.setDisplayName(ChatUtilities.replaceChatCodes("&6KingKits config"));
											itemMeta.setLore(ChatUtilities.replaceChatCodes(new ArrayList<>(Arrays.asList("&cType '&4/" + label.toLowerCase() + " " + subCommand.toLowerCase() + "&c'", "&cto save the contents of this book to the config."))));
											if (itemMeta instanceof BookMeta) {
												BookMeta bookMeta = (BookMeta) itemMeta;

												File configFile = new File(KingKits.getInstance().getDataFolder(), "config.yml");
												List<String> configLines = FileUtilities.readFile(configFile);

												Map<Integer, List<String>> configPages = new LinkedHashMap<>();
												int page = 0, rawLineIndex = 0;
												for (String configLine : configLines) {
													List<String> configPage = configPages.get(page);
													if (configPage == null) configPage = new ArrayList<>();
													if (!configLine.trim().isEmpty() && !configLine.trim().startsWith("#")) {
														configPage.add(configLine);
														configPages.put(page, configPage);
														if (rawLineIndex != 0 && rawLineIndex % 6 == 0) page++;
														rawLineIndex++;
													}
												}

												for (Map.Entry<Integer, List<String>> configPage : configPages.entrySet()) {
													StringBuilder pageContent = new StringBuilder();
													for (int i = 0; i < configPage.getValue().size(); i++) {
														pageContent.append(configPage.getValue().get(i));
														if (i < configPage.getValue().size() - 1) {
															pageContent.append('\n');
														}
													}
													bookMeta.addPage(pageContent.toString());
												}
											}
											configBook.setItemMeta(itemMeta);
										}
										player.getInventory().addItem(configBook);
										Messages.sendMessage(sender, Messages.COMMAND_CONFIG_ADDED);
									}
								} else {
									Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase());
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("killstreak")) {
						if (args.length == 1) {
							if (sender instanceof Player) {
								Player player = (Player) sender;
								if (player.hasPermission(Permissions.COMMAND_KILLSTREAK)) {
									KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
									if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return true;
									Messages.sendMessage(sender, Messages.COMMAND_KILLSTREAK_SELF, kitPlayer.getKillstreak());
								} else {
									Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
							}
						} else if (args.length == 2) {
							if (sender.hasPermission(Permissions.COMMAND_KILLSTREAK_OTHER)) {
								Player target = sender.getServer().getPlayer(args[1]);
								if (target != null) {
									KitPlayer targetKitPlayer = PlayerController.getInstance().getPlayer(target);
									if (!PlayerUtilities.checkPlayer(sender, targetKitPlayer)) return true;
									Messages.sendMessage(sender, Messages.COMMAND_KILLSTREAK_OTHER, target.getName(), targetKitPlayer.getKillstreak());
								} else {
									Messages.sendMessage(sender, Messages.GENERAL_PLAYER_NOT_FOUND, args[1]);
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase() + " " + (sender instanceof Player ? "[" : "") + "<player>" + (sender instanceof Player ? "]" : ""));
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("kit")) {
						if (args.length == 1) {
							if (sender instanceof Player) {
								Player player = (Player) sender;
								KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
								if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return true;
								if (kitPlayer.hasKit()) {
									Messages.sendMessage(sender, Messages.COMMAND_VIEW_KIT_SELF_KIT, kitPlayer.getKit().getName() + (kitPlayer.getKit().isUserKit() ? " (User)" : ""));
								} else {
									Messages.sendMessage(sender, Messages.COMMAND_VIEW_KIT_SELF_NO_KIT);
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
							}
						} else if (args.length == 2) {
							Player target = sender.getServer().getPlayer(args[1]);
							if (target != null) {
								KitPlayer targetKitPlayer = PlayerController.getInstance().getPlayer(target);
								if (!PlayerUtilities.checkPlayer(sender, targetKitPlayer)) return true;
								if (targetKitPlayer.hasKit()) {
									Messages.sendMessage(sender, Messages.COMMAND_VIEW_KIT_OTHER_KIT, target.getName(), targetKitPlayer.getKit().getName() + (targetKitPlayer.getKit().isUserKit() ? " (User)" : ""));
								} else {
									Messages.sendMessage(sender, Messages.COMMAND_VIEW_KIT_OTHER_NO_KIT, target.getName());
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_PLAYER_NOT_FOUND, args[1]);
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase() + " " + (sender instanceof Player ? "[" : "") + "<player>" + (sender instanceof Player ? "]" : ""));
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("score")) {
						if (args.length == 1) {
							if (sender instanceof Player) {
								Player player = (Player) sender;
								KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
								if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return true;
								Messages.sendMessage(sender, Messages.COMMAND_SCORE_SELF, kitPlayer.getScore());
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
							}
						} else if (args.length == 2) {
							Player target = sender.getServer().getPlayer(args[1]);
							if (target != null) {
								KitPlayer targetKitPlayer = PlayerController.getInstance().getPlayer(target);
								if (!PlayerUtilities.checkPlayer(sender, targetKitPlayer)) return true;
								Messages.sendMessage(sender, Messages.COMMAND_SCORE_OTHER, target.getName(), targetKitPlayer.getScore());
							} else {
								if (DataStorage.getInstance() instanceof FlatFileStorage) {
									OfflineKitPlayer offlineKitPlayer = DataStorage.getInstance().loadOfflinePlayer(args[1]);
									Messages.sendMessage(sender, Messages.COMMAND_SCORE_OTHER, offlineKitPlayer.getUsername(), offlineKitPlayer.getScore());
								} else {
									sender.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
										OfflineKitPlayer offlineKitPlayer = DataStorage.getInstance().loadOfflinePlayer(args[1]);
										long startTime = System.currentTimeMillis();
										while (!offlineKitPlayer.isLoaded()) {
											if (System.currentTimeMillis() - startTime > 5_000L) {
												BukkitUtilities.sendMessageSync(sender, "&cServer took too long to respond.");
												return;
											}
										}
										BukkitUtilities.sendMessageSync(sender, Messages.COMMAND_SCORE_OTHER, offlineKitPlayer.getUsername(), offlineKitPlayer.getScore());
									});
								}
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase() + " " + (sender instanceof Player ? "[" : "") + "<player>" + (sender instanceof Player ? "]" : ""));
						}
						return true;
					} else if (subCommand.equalsIgnoreCase("set")) {
						if (sender.hasPermission(Permissions.COMMAND_SET)) {
							if (args.length > 3) {
								KitUtilities.KitSearchResult kitSearchResult = KitUtilities.getKits(args[1]);
								Kit targetKit = null;
								if (kitSearchResult.hasKit()) {
									targetKit = kitSearchResult.getKit();
								} else if (kitSearchResult.hasOtherKits()) {
									if (kitSearchResult.getOtherKits().size() == 1) {
										targetKit = kitSearchResult.getOtherKits().get(0);
									} else {
										StringBuilder sbKits = new StringBuilder();
										for (int i = 0; i < kitSearchResult.getOtherKits().size(); i++) {
											sbKits.append(kitSearchResult.getOtherKits().get(i));
											if (i != kitSearchResult.getOtherKits().size() - 1) {
												sbKits.append(ChatColor.RED).append(", ").append(ChatColor.GREEN);
											}
										}
										sender.sendMessage(ChatColor.GOLD + "Select a kit from: " + ChatColor.GREEN + sbKits);
										return true;
									}
								} else {
									sender.sendMessage(ChatColor.RED + "Unknown kit: " + ChatColor.DARK_RED + args[1]);
									return true;
								}
								String key = args[2];
								String strValue = StringUtilities.joinString(args, 3);
								Object value = strValue;
								if (key.equalsIgnoreCase("displayname")) {
									key = "Display name";
									targetKit.setDisplayName(strValue);
								} else if (key.equalsIgnoreCase("commandalias") || key.equalsIgnoreCase("alias") || key.equalsIgnoreCase("breakableitems") || key.equalsIgnoreCase("breakitems")) {
									int keyType = -1;
									if (key.equalsIgnoreCase("commandalias") || key.equalsIgnoreCase("alias")) {
										key = "Command alias";
										keyType = 1;
									} else if (key.equalsIgnoreCase("breakableitems") || key.equalsIgnoreCase("breakitems")) {
										key = "Breakable items";
										keyType = 2;
									}
									if (strValue.equalsIgnoreCase("true")) {
										value = true;
									} else if (strValue.equalsIgnoreCase("false")) {
										value = false;
									} else {
										sender.sendMessage(ChatColor.RED + "Valid values: true, false");
										return true;
									}
									switch (keyType) {
										case 1:
											targetKit.setAlias((Boolean) value);
											break;
										case 2:
											targetKit.setItemsBreakable((Boolean) value);
											break;
									}
								} else if (key.equalsIgnoreCase("description") || key.equalsIgnoreCase("commands")) {
									int keyType = -1;
									if (key.equalsIgnoreCase("description")) {
										key = "Description";
										keyType = 1;
									} else if (key.equalsIgnoreCase("commands")) {
										key = "Commands";
										keyType = 2;
									}
									if (strValue.startsWith("[") && strValue.endsWith("]")) {
										strValue = strValue.substring(1, strValue.length() - 1);
										if (!strValue.trim().isEmpty()) {
											List<String> description = new ArrayList<>();
											Collections.addAll(description, strValue.split(", "));
											value = description;
										} else {
											value = new ArrayList<String>();
										}
									} else {
										value = new ArrayList<String>();
									}
									switch (keyType) {
										case 1:
											targetKit.setDescription((List<String>) value);
											break;
										case 2:
											targetKit.setCommands((List<String>) value);
											break;
									}
									sender.sendMessage(ChatColor.DARK_RED + "Warning: " + ChatColor.RED + "It is not recommended you use commands to set lists.");
								} else if (key.equalsIgnoreCase("cost") || key.equalsIgnoreCase("cooldown") || key.equalsIgnoreCase("maxhealth")) {
									int keyType = -1;
									if (key.equalsIgnoreCase("cost")) {
										key = "Cost";
										keyType = 1;
									} else if (key.equalsIgnoreCase("cooldown")) {
										key = "Cooldown";
										keyType = 2;
									} else if (key.equalsIgnoreCase("maxhealth")) {
										key = "Max health";
										keyType = 3;
									}
									if (Utilities.isNumber(Double.class, strValue)) {
										value = Double.parseDouble(strValue);
									} else if (Utilities.isNumber(Integer.class, strValue)) {
										value = (double) Integer.parseInt(strValue);
									} else {
										sender.sendMessage(ChatColor.RED + "Valid values: Positive numbers");
										return true;
									}
									if ((Double) value < 0D) {
										sender.sendMessage(ChatColor.RED + "Valid values: Positive numbers");
										return true;
									}
									switch (keyType) {
										case 1:
											targetKit.setCost((Double) value);
											break;
										case 2:
											targetKit.setCooldown((Double) value);
											break;
										case 3:
											targetKit.setMaxHealth((Double) value);
											break;
									}
								} else if (key.equalsIgnoreCase("walkspeed")) {
									key = "Walk speed";
									if (Utilities.isNumber(Float.class, strValue)) {
										value = Float.parseFloat(strValue);
									} else if (Utilities.isNumber(Integer.class, strValue)) {
										value = (Integer.valueOf(strValue)).floatValue();
									} else if (Utilities.isNumber(Double.class, strValue)) {
										value = (Double.valueOf(strValue)).floatValue();
									} else {
										sender.sendMessage(ChatColor.RED + "Valid values: 0.0 - 1.0");
										return true;
									}
									if ((Float) value < 0F || (Float) value > 1F) {
										sender.sendMessage(ChatColor.RED + "Valid values: 0.0 - 1.0");
										return true;
									}
									targetKit.setWalkSpeed((Float) value);
								} else if (key.equalsIgnoreCase("autounlock") || key.equalsIgnoreCase("autounlockscore") || key.equalsIgnoreCase("helditemslot")) {
									int keyType = -1;
									if (key.equalsIgnoreCase("autounlock") || key.equalsIgnoreCase("autounlockscore")) {
										key = "Auto-unlock score";
										keyType = 1;
									} else if (key.equalsIgnoreCase("helditemslot")) {
										key = "Held item slot";
										keyType = 2;
									}
									if (Utilities.isNumber(Integer.class, strValue)) {
										value = Integer.parseInt(strValue);
									} else {
										sender.sendMessage(ChatColor.RED + "Valid values: Positive integers");
										return true;
									}
									if ((Integer) value < 0) {
										sender.sendMessage(ChatColor.RED + "Valid values: Positive integers");
										return true;
									}
									switch (keyType) {
										case 1:
											targetKit.setAutoUnlockScore((Integer) value);
											break;
										case 2:
											targetKit.setHeldItemSlot((Integer) value);
											break;
									}
								} else {
									sender.sendMessage(ChatColor.RED + "Unknown key: " + ChatColor.DARK_RED + key);
									return true;
								}
								try {
									KitController.getInstance().saveKit(targetKit, key, value);
									sender.sendMessage(ChatColor.GOLD + "Successfully set the value of key '" + key + "' to '" + String.valueOf(value) + "'.");
								} catch (Exception ex) {
									ex.printStackTrace();
									Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex instanceof InvocationTargetException ? ((InvocationTargetException) ex).getTargetException().getClass().getName() : ex.getClass().getName());
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), subCommand.toLowerCase() + " <kit> <key> <value>");
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
						}
						return true;
					}
				}
				sender.sendMessage(ChatColor.GOLD + "KingKits v" + this.plugin.getDescription().getVersion() + " by " + ChatColor.RED.toString() + ChatColor.BOLD.toString() + "KingFaris10");
			} catch (Exception ex) {
				ex.printStackTrace();
				Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex instanceof InvocationTargetException ? ((InvocationTargetException) ex).getTargetException().getClass().getName() : ex.getClass().getName());
			}
			return true;
		}
		return false;
	}

}
