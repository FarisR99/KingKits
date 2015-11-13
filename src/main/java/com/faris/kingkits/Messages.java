package com.faris.kingkits;

import com.faris.kingkits.helper.util.ChatUtilities;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public enum Messages {
	GENERAL_COMMAND_ERROR("General.Command error", "&cA(n) %s error occurred whilst trying to execute that command. Please contact a server administrator."),
	GENERAL_COMMAND_NO_PERMISSION("General.Command permission", "&4You do not have access to that command."),
	GENERAL_COMMAND_USAGE("General.Command usage", "&cUsage: &4/%s %s"),
	GENERAL_COMMAND_DISABLED("General.Command disabled", "&cThat command has been disabled."),
	GENERAL_PLAYER_NOT_FOUND("General.Player not found", "&4That player is not online or does not exist!"),
	GENERAL_PLAYER_COMMAND("General.Player command", "&cYou must be a player to use that command."),

	COMMAND_RELOAD_SUCCESSFUL("Command.Reload.Successful", "&6Successfully reloaded KingKits v%s."),
	COMMAND_RELOAD_FAILED("Command.Reload.Failed", "&cFailed to reload KingKits' configurations."),

	COMMAND_CONFIG_ADDED("Command.Config.Added", "&6You have received a book and a quill with the config in it. Type '/kk config' with a signed book to save it."),
	COMMAND_CONFIG_SAVED("Command.Config.Saved", "&6Successfully saved the book's contents to the config."),

	COMMAND_KIT_CREATE_CREATED("Command.Create kit.Created", "&6Successfully created the kit &c%s&6."),
	COMMAND_KIT_CREATE_OVERWROTE("Command.Create kit.Overwrote", "&6Successfully overwrote the kit &c%s&6."),
	COMMAND_KIT_CREATE_USER_CREATED("Command.Create user kit.Created", "&6Successfully created the user kit &c%s&6."),
	COMMAND_KIT_CREATE_USER_MAX_KITS("Command.Create user kit.Maximum kits", "&cYou have reached the maximum number of user kits you can create."),
	COMMAND_KIT_CREATE_USER_OVERWROTE("Command.Create user kit.Overwrote", "&6Successfully overwrote the user kit &c%s&6."),

	COMMAND_KIT_DELETE("Command.Delete kit.Deleted", "&6Successfully deleted &c%s&6."),
	COMMAND_KIT_DELETE_ALL("Command.Delete kit.Deleted all", "&6Successfully deleted &c%d &6kit(s)."),
	COMMAND_KIT_DELETE_USER("Command.Delete user kit.Deleted", "&6Successfully deleted &c%s&6."),
	COMMAND_KIT_DELETE_ALL_USER("Command.Delete user kit.Deleted all", "&6Successfully deleted &c%d &6user kit(s)."),

	COMMAND_KILLSTREAK_SELF("Command.Killstreak.Self", "&6You are on a(n) &b%d &6killstreak."),
	COMMAND_KILLSTREAK_OTHER("Command.Killstreak.Other", "&6%s is on a(n) &b%d &6killstreak."),

	COMMAND_KIT_LIST_TITLE("Command.Kit.List.Title", "&6Kits (%d):"),
	COMMAND_KIT_LIST_KITS("Command.Kit.List.Message", "<colour>%s"),
	COMMAND_KIT_LIST_NONE("Command.Kit.List.No kits", "&4There are no kits available."),

	COMMAND_KIT_RENAME("Command.Rename kit.Renamed", "&6Successfully renamed &c%s &6to &c%s&6."),
	COMMAND_KIT_RENAME_USER("Command.Rename user kit.Renamed", "&6Successfully renamed &c%s &6to &c%s&6."),

	COMMAND_REFILL_BOWL("Command.Refill.Bowl", "&cYou must have a bowl in your hand."),
	COMMAND_REFILL_NOT_ENOUGH_MONEY("Command.Refill.Not enough money", "&aYou do not have enough money to refill your bowl(s)."),
	COMMAND_REFILL_FULL_INV("Command.Refill.Full inventory", "&cYou have a full inventory!"),

	COMMAND_SCORE_SELF("Command.Score.Self", "&6Score: &b%d"),
	COMMAND_SCORE_OTHER("Command.Score.Other", "&6%s's score: &b%d"),

	COMMAND_VIEW_KIT_SELF_KIT("Command.View kit.Self.Has kit", "&6You are using the kit &c%s&6."),
	COMMAND_VIEW_KIT_SELF_NO_KIT("Command.View kit.Self.No kit", "&6You are not using a kit."),
	COMMAND_VIEW_KIT_OTHER_KIT("Command.View kit.Other.Has kit", "&6%s is using the kit &c%s&6."),
	COMMAND_VIEW_KIT_OTHER_NO_KIT("Command.View kit.Other.No kit", "&6%s is using not using a kit."),

	COMPASS_POINTING_PLAYER("Compass.Player", "&eYour compass is pointing at %s."),
	COMPASS_POINTING_SPAWN("Compass.Spawn", "&eYour compass is pointing to spawn."),

	EVENT_BLOCK_BREAK("Event.Block.Break", "&cYou cannot break blocks here!"),
	EVENT_BLOCK_PLACE("Event.Block.Place", "&cYou cannot place blocks here!"),

	GUI_KITS_MENU_TITLE("Gui.Kits menu.Title", "&aKits"),
	GUI_KITS_MENU_GLOBAL("Gui.Kits menu.Global", "&cGlobal kits"),
	GUI_KITS_MENU_USER("Gui.Kits menu.User", "&eUser kits"),
	GUI_KITS_TITLE("Gui.Kits.Title", "&aPvP Kits"),
	GUI_USER_KITS_TITLE("Gui.User kits.Title", "&aUser Kits"),
	GUI_PREVIEW_KIT_TITLE("Gui.Preview kit.Title", "&aPreview - &b<kit>"),

	KIT_DELAY("Kit.Delay", "&cYou must wait %s %s before using this kit again."),
	KIT_ILLEGAL_CHARACTERS("Kit.Illegal characters", "&cThe kit name can only consist of A-Z, 0-1 and underscores."),
	KIT_MULTIPLE_FOUND("Kit.Multiple found", "&cMultiple kits exist with that name. The kit name is case sensitive."),
	KIT_NO_PERMISSION("Kit.No permission", "&cYou do not have permission to use &4%s&c."),
	KIT_NOT_ENOUGH_MONEY("Kit.Not enough money", "&cYou require $%f more to use that kit."),
	KIT_NOT_FOUND("Kit.Not found", "&cA kit with that name does not exist."),
	KIT_ONE_PER_LIFE("Kit.One per life", "&cYou have already chosen a kit!"),
	KIT_SET("Kit.Set", "&6You have chosen &4%s&6."),
	KIT_UNLOCK("Kit.Unlocked", "&6Congratulations, you auto-unlocked &c%s&6!"),

	SIGN_CREATE_NO_PERMISSION("Sign.Create.No permission", "&cYou do not have permission to create a %s sign."),
	SIGN_CREATE_INCORRECTLY_SETUP("Sign.Create.Incorrectly setup", "&cThat sign was incorrectly set up."),
	SIGN_USE_NO_PERMISSION("Sign.Use.No permission", "&cYou do not have permission to use that %s sign."),

	TIME_SECOND_SINGULAR("Time.Second", "second"),
	TIME_SECOND_PLURAL("Time.Seconds", "seconds"),
	TIME_MINUTE_SINGULAR("Time.Minute", "minute"),
	TIME_MINUTE_PLURAL("Time.Minutes", "minutes"),
	TIME_HOUR_SINGULAR("Time.Hour", "hour"),
	TIME_HOUR_PLURAL("Time.Hours", "hours"),
	TIME_DAY_SINGULAR("Time.Day", "day"),
	TIME_DAY_PLURAL("Time.Days", "days");


	private static File messagesFile = null;
	private static FileConfiguration messagesConfig = null;

	private String path = "";
	private String defaultValue = "";

	Messages(String path, String defaultValue) {
		if (path == null || path.isEmpty())
			throw new IllegalArgumentException("The path specified cannot be " + (path == null ? "null" : "empty") + "!");
		this.path = path;
		this.defaultValue = defaultValue != null ? defaultValue : "";
	}

	public String getMessage() {
		return ChatUtilities.replaceChatCodes(messagesConfig.getString(this.path, this.defaultValue));
	}

	public String getMessage(Object... format) {
		return ChatUtilities.replaceChatCodes(String.format(messagesConfig.getString(this.path, this.defaultValue), format));
	}

	public String getRawMessage() {
		return messagesConfig.getString(this.path, this.defaultValue);
	}

	private void sendMessage(CommandSender sender) {
		String message = this.getMessage();
		if (!message.isEmpty()) sender.sendMessage(message);
	}

	private void sendMessage(CommandSender sender, Object... format) {
		String message = "";
		if (format != null && format.length > 0) message = this.getMessage(format);
		else message = this.getMessage();
		if (!message.isEmpty()) sender.sendMessage(message);
	}

	public void setMessage(String message) {
		messagesConfig.set(this.path, message);
		try {
			messagesConfig.save(messagesFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void initMessages(JavaPlugin plugin) throws Exception {
		if (messagesFile == null) messagesFile = new File(plugin.getDataFolder(), "messages.yml");
		messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
		boolean modifiedMessages = false;
		for (Messages message : values()) {
			if (!messagesConfig.isSet(message.path) || !messagesConfig.isString(message.path)) {
				messagesConfig.set(message.path, message.defaultValue);
				modifiedMessages = true;
			}
		}
		if (modifiedMessages) {
			try {
				messagesConfig.save(messagesFile);
			} catch (Exception ex) {
				plugin.getLogger().log(Level.WARNING, "Failed to save the messages configuration", ex);
			}
		}
	}

	public static String getMessageByEnum(String enumName) {
		try {
			return Messages.valueOf(enumName.toUpperCase().replace(' ', '_')).getMessage();
		} catch (Exception ex) {
			return "";
		}
	}

	public static void sendMessage(CommandSender sender, Messages message) {
		if (message != null && sender != null) message.sendMessage(sender);
	}

	public static void sendMessage(CommandSender sender, Messages message, Object... format) {
		if (message != null && sender != null) message.sendMessage(sender, format);
	}

}
