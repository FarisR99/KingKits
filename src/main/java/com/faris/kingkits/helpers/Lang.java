package com.faris.kingkits.helpers;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author 1Rogue
 * @version 1.0.0
 * @since 1.0.0
 */
public enum Lang {
    PREFIX("Prefix.Plugin", "&6[&4KingKits&6] &a"),
    COMMAND_GEN_IN_GAME("Command.General.In-game", "&cYou must be a player to use that command!"),
    COMMAND_GEN_USAGE("Command.General.Usage", "&cUsage: &4/%s"),
    COMMAND_GEN_NOT_ONLINE("Command.General.Not online", "%s is not online!"),
    COMMAND_GEN_NO_PERMISSION("Command.General.No permission", "&4You do not have access to that command.");

    private static YamlConfiguration config = null;
    private static File configFile = null;

    private String key = "";
    private String defaultValue = "";

    private Lang(String key, String defValue) {
        this.key = key;
        this.defaultValue = defValue;
    }

    public String getMessage() {
        return replaceChatColours(this.getRawMessage());
    }

    public String getMessage(Object... format) {
        return replaceChatColours(String.format(this.getRawMessage(), format));
    }

    public String getRawMessage() {
        return config != null ? config.getString(this.key, this.defaultValue) : this.defaultValue;
    }

    public String getReplacedMessage(Object... objects) {
        String langMessage = this.getRawMessage();
        if (objects != null) {
            Object firstObject = "";
            for (int i = 0; i < objects.length; i++) {
                if (i == 0 || i % 2 == 0) {
                    firstObject = objects[i] != null ? objects[i].toString() : "null";
                } else {
                    if (firstObject != null)
                        langMessage = langMessage.replace(firstObject.toString(), (objects[i] != null ? objects[i].toString() : "null"));
                }
            }
        }
        return replaceChatColours(langMessage);
    }

    public static void sendMessage(CommandSender sender, Lang lang) {
        sender.sendMessage(PREFIX.getMessage() + lang.getMessage());
    }

    public static void sendMessage(CommandSender sender, Lang lang, Object... objects) {
        sender.sendMessage(PREFIX.getMessage() + lang.getMessage(objects));
    }

    public static void sendReplacedMessage(CommandSender sender, Lang lang, Object... objects) {
        sender.sendMessage(lang.getReplacedMessage(objects));
    }

    public static void sendRawMessage(CommandSender sender, Lang lang) {
        sender.sendMessage(lang.getMessage());
    }

    public static void sendRawMessage(CommandSender sender, Lang lang, Object... objects) {
        sender.sendMessage(lang.getMessage(objects));
    }

    public static void init(JavaPlugin plugin) {
        configFile = new File(plugin.getDataFolder(), "messages.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        for (Lang value : values()) {
            if (!config.isSet(value.key)) config.set(value.key, value.defaultValue);
        }
        try {
            config.save(configFile);
        } catch (Exception ex) {
        }
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public static String saveString(String path, String value) {
        if (!config.isSet(path)) {
            config.set(path, value);
            try {
                config.save(configFile);
            } catch (Exception ex) {
            }
        }
        return config.isSet(path) ? config.getString(value) : value;
    }

    private static String replaceChatColours(String aString) {
        return Utils.replaceChatColour(aString);
    }
}