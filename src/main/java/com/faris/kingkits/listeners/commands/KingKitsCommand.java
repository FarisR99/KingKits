package com.faris.kingkits.listeners.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helpers.ConfigCommand;
import com.faris.kingkits.helpers.Lang;
import com.faris.kingkits.helpers.Utils;
import com.faris.kingkits.hooks.PvPKits;
import com.faris.kingkits.listeners.KingCommand;
import com.faris.kingkits.listeners.event.custom.KingKitsPreReloadEvent;
import com.faris.kingkits.listeners.event.custom.KingKitsReloadEvent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KingKitsCommand extends KingCommand {

    private List<ConfigCommand> configCommands = new ArrayList<ConfigCommand>();

    public KingKitsCommand(KingKits pluginInstance) {
        super(pluginInstance);

        for (Map.Entry<String, Object> entrySet : this.getPlugin().getConfig().getValues(false).entrySet()) {
            if (!(entrySet.getValue() instanceof Collection) && !(entrySet.getValue() instanceof ConfigurationSection) && !(entrySet.getValue() instanceof Map)) {
                this.configCommands.add(new ConfigCommand(WordUtils.capitalizeFully(entrySet.getKey().toLowerCase()).replace(" ", ""), entrySet.getKey(), "Config"));
            }
        }
        for (Map.Entry<String, Object> entrySet : this.getPlugin().getEconomyConfig().getValues(false).entrySet()) {
            if (!(entrySet.getValue() instanceof Collection) && !(entrySet.getValue() instanceof ConfigurationSection) && !(entrySet.getValue() instanceof Map)) {
                this.configCommands.add(new ConfigCommand(WordUtils.capitalizeFully(entrySet.getKey().toLowerCase()).replace(" ", ""), entrySet.getKey(), "Economy"));
            }
        }
    }

    @Override
    protected boolean onCommand(CommandSender sender, String command, String[] args) {
        if (command.equalsIgnoreCase("kingkits") || command.equalsIgnoreCase("kk")) {
            try {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.GOLD + "KingKits v" + this.getPlugin().getDescription().getVersion());
                } else if (args.length > 0) {
                    String strCommand = args[0];
                    if (strCommand.equalsIgnoreCase("reload")) {
                        if (sender.isOp() || sender.hasPermission(this.getPlugin().permissions.cmdReloadConfig)) {
                            if (args.length == 1) {
                                KingKitsPreReloadEvent kkPreReloadEvent = new KingKitsPreReloadEvent(sender);
                                sender.getServer().getPluginManager().callEvent(kkPreReloadEvent);
                                if (!kkPreReloadEvent.isCancelled()) {
                                    try {
                                        this.getPlugin().reloadAllConfigs();
                                        this.getPlugin().loadConfiguration();
                                        Lang.init(this.getPlugin());
                                        try {
                                            if (sender.getServer().getPluginManager().isPluginEnabled("KingKitsSpecial") && sender.getServer().getPluginCommand("kkspecial") != null) {
                                                sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "kkspecial reload");
                                            }
                                        } catch (Exception ex) {
                                        }

                                        sender.sendMessage(ChatColor.GOLD + "You reloaded KingKits configurations.");
                                        KingKitsReloadEvent kkReloadEvent = new KingKitsReloadEvent(sender);
                                        sender.getServer().getPluginManager().callEvent(kkReloadEvent);
                                    } catch (Exception ex) {
                                        sender.sendMessage(ChatColor.RED + "Failed to reload KingKits configurations.");

                                        KingKitsReloadEvent kkReloadEvent = new KingKitsReloadEvent(sender, ex);
                                        sender.getServer().getPluginManager().callEvent(kkReloadEvent);
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "A plugin has not allowed you to reload the configuration.");
                                }
                            } else {
                                Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " " + strCommand.toLowerCase());
                            }
                        } else {
                            this.sendNoAccess(sender);
                        }
                    } else if (strCommand.equalsIgnoreCase("config")) {
                        if (sender.hasPermission(this.getPlugin().permissions.cmdConfigManagement)) {
                            if (args.length == 2) {
                                String configProperty = args[1];
                                if (configProperty.equalsIgnoreCase("list")) {
                                    sender.sendMessage(ChatColor.GREEN + "KingKits config command property list (" + this.configCommands.size() + "): ");
                                    StringBuilder configListBuilder = new StringBuilder();
                                    for (int i = 0; i < this.configCommands.size(); i++) {
                                        if (i == this.configCommands.size() - 1)
                                            configListBuilder.append(this.configCommands.get(i).getCommand());
                                        else configListBuilder.append(this.configCommands.get(i).getCommand() + ", ");
                                    }
                                    sender.sendMessage(ChatColor.GOLD + configListBuilder.toString().trim());
                                } else {
                                    Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " " + strCommand.toLowerCase() + " <property> <value>");
                                }
                            } else if (args.length == 3) {
                                String configKey = args[1];
                                String configValue = args[2];
                                if (this.containsCommand(this.configCommands, configKey)) {
                                    sender.sendMessage(this.updateConfig(configKey, configValue));
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Invalid config property: " + ChatColor.DARK_RED + configKey);
                                    sender.sendMessage(ChatColor.RED + "To list all the config properties you can edit, type: " + ChatColor.DARK_RED + "/" + command.toLowerCase() + " " + strCommand.toLowerCase() + " list");
                                }
                            } else {
                                Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " " + strCommand.toLowerCase() + " <property> <value>");
                            }
                        } else {
                            this.sendNoAccess(sender);
                        }
                    } else if (strCommand.equalsIgnoreCase("removescoreboards")) {
                        if (sender.isOp()) {
                            if (args.length == 1) {
                                Scoreboard newScoreboard = sender.getServer().getScoreboardManager().getNewScoreboard();
                                for (Player onlinePlayer : sender.getServer().getOnlinePlayers()) {
                                    Scoreboard pScoreboard = onlinePlayer.getScoreboard();
                                    if (pScoreboard != null && pScoreboard.getObjective("KingKits") != null) {
                                        onlinePlayer.setScoreboard(newScoreboard);
                                    }
                                }
                                sender.getServer().dispatchCommand(sender.getServer().getConsoleSender(), "scoreboard objectives remove KingKits");
                            } else {
                                Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " " + strCommand.toLowerCase());
                            }
                        } else {
                            this.sendNoAccess(sender);
                        }
                    } else if (strCommand.equalsIgnoreCase("killstreak")) {
                        if (!this.isConsole(sender)) {
                            Player player = (Player) sender;
                            if (player.hasPermission(this.getPlugin().permissions.killstreak)) {
                                if (args.length == 1) {
                                    long killstreak = PvPKits.getKillstreak(player);
                                    sender.sendMessage(ChatColor.GOLD + "Killstreak: " + ChatColor.DARK_RED + killstreak);
                                } else {
                                    Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " " + strCommand.toLowerCase());
                                }
                            } else {
                                this.sendNoAccess(sender);
                            }
                        } else {
                            Lang.sendMessage(sender, Lang.COMMAND_GEN_IN_GAME);
                        }
                    } else if (strCommand.equalsIgnoreCase("setcooldown")) {
                        if (sender.hasPermission(this.getPlugin().permissions.cmdSetCooldown)) {
                            if (args.length == 3) {
                                String strTargetKit = args[1];
                                List<String> kitList = this.getPlugin().getKitList();
                                List<String> lcKitList = Utils.toLowerCaseList(kitList);
                                if (lcKitList.contains(strTargetKit.toLowerCase())) {
                                    if (!kitList.contains(strTargetKit))
                                        strTargetKit = kitList.get(lcKitList.indexOf(strTargetKit.toLowerCase()));
                                    Kit targetKit = this.getPlugin().kitList.get(strTargetKit);
                                    if (targetKit != null) {
                                        String strCooldown = args[2];
                                        if (Utils.isInteger(strCooldown)) {
                                            int cooldown = Integer.parseInt(strCooldown);
                                            if (cooldown >= 0) {
                                                targetKit.setCooldown(cooldown);
                                                this.getPlugin().kitList.put(strTargetKit, targetKit);
                                                this.getPlugin().getKitsConfig().set(strTargetKit + ".Cooldown", cooldown);
                                                this.getPlugin().saveKitsConfig();

                                                sender.sendMessage(ChatColor.GOLD + "Changed the cooldown for '" + targetKit.getRealName() + "' to " + cooldown + " second(s).");
                                            } else {
                                                sender.sendMessage(ChatColor.RED + "Please enter a valid integer between 0 and " + Integer.MAX_VALUE + " (inclusive) for the kit cooldown.");
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "Please enter a valid integer between 0 and " + Integer.MAX_VALUE + " (inclusive) for the kit cooldown.");
                                        }
                                    } else {
                                        Lang.sendMessage(sender, Lang.KIT_NONEXISTENT);
                                    }
                                } else {
                                    Lang.sendMessage(sender, Lang.KIT_NONEXISTENT);
                                }
                            } else {
                                Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " " + strCommand.toLowerCase() + " <kit> <cooldown>");
                            }
                        } else {
                            this.sendNoAccess(sender);
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Unknown KingKits command: " + ChatColor.DARK_RED + strCommand);
                    }
                }
            } catch (Exception ex) {
                if (Math.random() < 0.25) ex.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * Returns if a list of config commands contains a command (as a string) *
     */
    private boolean containsCommand(List<ConfigCommand> configCmds, String command) {
        for (ConfigCommand cmd : configCmds) {
            if (cmd.getCommand().equalsIgnoreCase(command)) return true;
        }
        return false;
    }

    /**
     * Updates the config with the property key and value *
     */
    public String updateConfig(String propertyKey, Object propertyValue) {
        try {
            String key = "";
            String config = "";
            if (this.containsCommand(this.configCommands, propertyKey)) {
                for (int i = 0; i < this.configCommands.size(); i++) {
                    if (this.configCommands.get(i).getCommand().equalsIgnoreCase(propertyKey)) {
                        ConfigCommand configCommand = this.configCommands.get(i);
                        key = configCommand.getDescription();
                        config = configCommand.getConfig();
                    }
                }
                if (key == "") return ChatColor.RED + "Failed to find the key '" + propertyKey + "' in the config.";
            } else return ChatColor.RED + "Failed to find the key '" + propertyKey + "' in the config.";
            if (config.equalsIgnoreCase("Config")) {
                String value = String.valueOf(propertyValue);
                if (this.isBoolean(value)) {
                    this.getPlugin().getConfig().set(key, Boolean.parseBoolean(value));
                } else if (this.isDouble(value)) {
                    this.getPlugin().getConfig().set(key, Double.parseDouble(value));
                } else if (this.isInteger(value)) {
                    this.getPlugin().getConfig().set(key, Integer.parseInt(value));
                } else {
                    this.getPlugin().getConfig().set(key, propertyValue);
                }
                this.getPlugin().saveConfig();
            } else if (config.equalsIgnoreCase("Economy")) {
                String value = String.valueOf(propertyValue);
                if (this.isBoolean(value)) {
                    this.getPlugin().getEconomyConfig().set(key, Boolean.parseBoolean(value));
                } else if (this.isDouble(value)) {
                    this.getPlugin().getEconomyConfig().set(key, Double.parseDouble(value));
                } else if (this.isInteger(value)) {
                    this.getPlugin().getEconomyConfig().set(key, Integer.parseInt(value));
                } else {
                    this.getPlugin().getEconomyConfig().set(key, propertyValue);
                }
                this.getPlugin().saveEconomyConfig();
            }
            if (!this.getPlugin().checkConfig()) return ChatColor.RED + "Could not update " + propertyKey + ".";
            this.getPlugin().reloadAllConfigs();
            this.getPlugin().loadConfiguration();

            return ChatColor.GOLD + "Successfully updated " + propertyKey + " in the config.";
        } catch (Exception ex) {
            if (Math.random() < 0.25) ex.printStackTrace();
            return ChatColor.RED + "Error: Couldn't update the config with the property.";
        }
    }

}
