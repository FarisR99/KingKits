package com.faris.kingkits.helpers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * @author 1Rogue
 * @version 1.0.0
 * @since 1.0.0
 */
public enum Lang {
    COMMAND_GEN_INGAME("command.general.in-game", "&cYou must be a player to use that command!"), COMMAND_GEN_USAGE("command.general.usage", "&cUsage: &4/%s"), COMMAND_GEN_NOTONLINE("command.general.not-online", "%s is not online!"), COMMAND_GEN_NOPERM("command.general.no-permission", "&4You do not have access to that command.");

    private static FileConfiguration yaml;
    private final String def;
    private final String path;

    /**
     * {@link Lang} private constructor
     *
     * @param path The path to the value
     * @param def The default value
     * @version 1.0.0
     * @since 1.0.0
     */
    private Lang(String path, String def) {
        this.path = path;
        this.def = def;
    }

    /**
     * Formats a {@link Lang} enum constant with the supplied arguments
     *
     * @param args The arguments to supply
     * @return The formatted string
     * @version 1.0.0
     * @since 1.0.0
     */
    public String format(Object... args) {
        return Lang.__(String.format(yaml.getString(this.path, this.def), args));
    }

    /**
     * Will format a string with "PLURAL" or "PLURALA" tokens in them.
     * <br /><br /><ul>
     * <li> <em>PLURALA</em>: Token that will evaluate gramatically. An int
     * value of 1 will return "is &lt;amount&gt; 'word'", otherwise it will be
     * "are &lt;amount&gt; 'word'".
     * </li><li> <em>PLURAL</em>: Token that will evaluate the word. An int
     * value of 1 will return the first word, value of 2 the second word.
     *
     * @param amount The amount representative of the data token
     * @param args The arguments to replace any other tokens with.
     * @return
     * @version 1.0.0
     * @since 1.0.0
     */
    public String pluralFormat(int amount, Object... args) {
        String repl = yaml.getString(this.path);
        repl = repl.replaceAll("\\{PLURALA (.*)\\|(.*)\\}", amount == 1 ? "is " + amount + " $1" : "are " + amount + " $2");
        repl = repl.replaceAll("\\{PLURAL (.*)\\|(.*)\\}", amount == 1 ? "$1" : "$2");
        return Lang.__(String.format(repl, args));
    }

    /**
     * Converts pre-made strings to have chat colors in them
     *
     * @param color String with unconverted color codes
     * @return string with correct chat colors included
     * @version 1.0.0
     * @since 1.0.0
     */
    public static String __(String color) {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    /**
     * Loads the lang values from the configuration file. Safe to use for
     * reloading.
     *
     * @param plugin The {@link Plugin} to load lang information from
     * @throws IOException If the file cannot be read
     * @version 1.0.0
     * @since 1.0.0
     */
    public static void init(Plugin plugin) {
        File ref = new File(plugin.getDataFolder(), "messages.yml");
        yaml = YamlConfiguration.loadConfiguration(ref);
        for (Lang l : Lang.values()) {
            if (!yaml.isSet(l.getPath())) {
                yaml.set(l.getPath(), l.getDefault());
            }
        }
        try {
            yaml.save(ref);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends a formatted string.
     *
     * @param target The target to send to
     * @param message The message to format and send
     * @version 1.0.0
     * @since 1.0.0
     * @deprecated
     */
    public static void sendMessage(CommandSender target, String message) {
        target.sendMessage(__(String.format("&7[&5KingKits&7] %s", message)));
    }

    /**
     * Sends a raw message without additional formatting aside from translating
     * color codes
     *
     * @param target The target to send to
     * @param message The message to colorize and send
     * @version 1.0.0
     * @since 1.0.0
     * @deprecated
     */
    public static void sendRawMessage(CommandSender target, String message) {
        target.sendMessage(__(message));
    }

    /**
     * Sends a formatted string.
     *
     * @param target The target to send to
     * @param message The message to colorize and send
     * @param args Arguments to supply to the {@link Lang} message
     * @version 1.0.0
     * @since 1.0.0
     */
    public static void sendMessage(CommandSender target, Lang message, Object... args) {
        String s = String.format("&7[&5KingKits&7] %s", message.format(args));
        if (!s.isEmpty()) {
            target.sendMessage(__(s));
        }
    }

    /**
     * Sends a raw message without additional formatting aside from translating
     * color codes
     *
     * @param target The target to send to
     * @param message The message to colorize and send
     * @param args Arguments to supply to the {@link Lang} message
     * @version 1.0.0
     * @since 1.0.0
     */
    public static void sendRawMessage(CommandSender target, Lang message, Object... args) {
        String s = __(message.format(args));
        if (!s.isEmpty()) {
            target.sendMessage(s);
        }
    }

    /**
     * The YAML path to store this value in
     *
     * @return The path to the YAML value
     * @version 1.0.0
     * @since 1.0.0
     */
    private String getPath() {
        return this.path;
    }

    /**
     * The default value of this YAML string
     *
     * @return The default value
     * @version 1.0.0
     * @since 1.0.0
     */
    private String getDefault() {
        return this.def;
    }
}