package com.faris.kingkits.helpers;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Based off 1Rogue's Lang class.
 */
public enum Lang {
    PREFIX("Prefix.Plugin", "&6[&4KingKits&6] &a"),

    COMMAND_GEN_IN_GAME("Command.General.In-game", "&cYou must be a player to use that command!"),
    COMMAND_GEN_ERROR("Command.General.Error", "&cAn error occurred."),
    COMMAND_GEN_NO_PERMISSION("Command.General.No permission", "&4You do not have access to that command."),
    COMMAND_GEN_USAGE("Command.General.Usage", "&cUsage: &4/%s"),
    COMMAND_GEN_DISABLED("Command.General.Disabled", "&cThis command is disabled in the configuration."),
    COMMAND_GEN_WORLD("Command.General.World", "&cYou cannot use this command in the world you are in."),
    COMMAND_GEN_NOT_ONLINE("Command.General.Not online", "%s does not exist or is not online."),

    COMMAND_CREATE_ILLEGAL_CHARACTERS("Command.Create kit.Illegal characters", "&6The kit name must only consist of letters, numbers and underscores."),
    COMMAND_CREATE_DENIED("Command.Create kit.Denied", "&cA plugin has not allowed you to create this kit."),
    COMMAND_CREATE_EMPTY_INV("Command.Create kit.Empty inventory", "&cYou have nothing in your inventory!"),
    COMMAND_CREATE_OVERWRITTEN("Command.Create kit.Overwrite", "&4%s &6has been overwritten."),
    COMMAND_CREATE_CREATED("Command.Create kit.Created", "&4%s &6has been created."),
    COMMAND_CREATE_KIT_DESCRIPTION("Command.Create kit.Global.Description", "&cDescription: &4Create your own PvP kit with every item in your inventory."),
    COMMAND_CREATE_UKIT_DESCRIPTION("Command.Create kit.User.Description", "&cDescription: &4Create your own personal PvP kit with every item in your inventory."),
    COMMAND_CREATE_UKIT_EXISTS("Command.Create kit.User.Global kit exists", "&cA PvP kit already exists with that name!"),
    COMMAND_CREATE_UKIT_MAX_PERSONAL_KITS("Command.Create kit.User.Maximum personal kits", "&cYou have reached the maximum number of personal kits you can create."),

    COMMAND_DELETE_ERROR("Command.Delete kit.Error", "&4%s&6's deletion was unsuccessful."),
    COMMAND_DELETE_DELETED("Command.Delete kit.Deleted", "&4%s &6was successfully deleted."),
    COMMAND_DELETE_PLAYER("Command.Delete kit.Player message", "&4%s &cdeleted the kit you were using!"),
    COMMAND_DELETE_KIT_NONEXISTENT("Command.Delete kit.Global.Non-existent", "&4That kit does not exist."),
    COMMAND_DELETE_KIT_DESCRIPTION("Command.Delete kit.Global.Description", "&cDescription: &4Delete a PvP Kit."),
    COMMAND_DELETE_UKIT_NONEXISTENT("Command.Delete kit.User.Non-existent", "&4That user kit does not exist."),
    COMMAND_DELETE_UKIT_DESCRIPTION("Command.Delete kit.User.Description", "&cDescription: &4Delete a personal PvP Kit."),

    COMMAND_KIT_LIST_NO_PERMISSION("Command.Kit.List.No permission", "&4You do not have permission to list the kits."),
    COMMAND_KIT_OTHER_PLAYER("Command.Kit.Other player", "&6You set %s's kit. This may not have been successful if you typed an invalid kit name, if they already have a kit, if they do not have permission to use that kit or they do not have enough money."),

    COMMAND_REFILL_BOWL("Command.Refill.Bowl", "&cYou must have a bowl in your hand."),
    COMMAND_REFILL_NOT_ENOUGH_MONEY("Command.Refill.Not enough money", "&aYou do not have enough money to refill your bowl(s)."),
    COMMAND_REFILL_FULL_INV("Command.Refill.Full inventory", "&cYou have a full inventory!"),

    COMMAND_RENAME_ILLEGAL_CHARACTERS("Command.Rename kit.Illegal characters", "&6The new kit name must only consist of letters, numbers and underscores."),
    COMMAND_RENAME_ALREADY_EXISTS("Command.Rename kit.Exists", "&c%s already exists."),
    COMMAND_RENAME_RENAMED("Command.Rename kit.Renamed", "&6Successfully renamed %s to %s."),
    COMMAND_RENAME_KIT_DESCRIPTION("Command.Rename kit.Global.Description", "&cDescription: &4Rename a PvP kit."),
    COMMAND_RENAME_UKIT_DESCRIPTION("Command.Rename kit.User.Description", "&cDescription: &4Rename a user PvP kit."),

    COMPASS_POINTING_PLAYER("Compass.Player", "&eYour compass is pointing to %s."),
    COMPASS_POINTING_SPAWN("Compass.Spawn", "Your compass is pointing to spawn."),

    GEN_KIT_LIST("General.Kit list", "&6PvP Kits: &a%s"),
    GEN_KIT_LIST_TITLE("General.Kit list title", "&aKits (%s):"),
    GEN_NO_KIT_SELECTED("General.No kit selected", "&cYou have not chosen a kit."),
    GEN_NO_KITS("General.No kits", "&4There are no kits."),
    GEN_NO_KITS_AVAILABLE("General.No kits available", "&cNo kits available"),
    GEN_ITEM_DROP("General.Item drop", "&cYou cannot drop this item whilst using a kit."),
    GEN_ITEM_PICKUP("General.Item pickup", "&cYou cannot drop this item whilst using a kit."),

    GUI_PREVIEW_TITLE("GUI.Preview.Title", "&c%s &7kit preview"),
    GUI_PREVIEW_BACK("GUI.Preview.Back", "&bBack"),

    KIT_NO_PERMISSION("Kit.No permission", "&cYou do not have permission to use the kit &4%s&c."),
    KIT_DELAY("Kit.Delay", "&cYou must wait %s second(s) before using this kit again."),
    KIT_ALREADY_CHOSEN("Kit.Already chosen", "&6You have already chosen a kit!"),
    KIT_NONEXISTENT("Kit.Non-existent", "&4%s &6does not exist."),
    KIT_NOT_ENOUGH_MONEY("Kit.Not enough money", "&aYou do not have enough money to change kits."),

    SIGN_CREATE_NO_PERMISSION("Sign.Create.No permission", "&4You do not have access to create a KingKits %s sign."),
    SIGN_CREATE_SECOND_LINE("Sign.Create.Second line empty", "&cPlease enter a kit name on the second line."),

    SIGN_GENERAL_INCORRECTLY_SETUP("Sign.General.Incorrectly set up", "&cThat sign has incorrectly been set up."),

    SIGN_USE_NO_PERMISSION("Sign.Use.No permission", "&cYou do not have permission to use this sign.");

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

    private static String replaceChatColours(String aString) {
        return Utils.replaceChatColour(aString);
    }
}