package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.controller.KitController;
import com.faris.kingkits.helper.util.KitUtilities;
import com.faris.kingkits.helper.util.StringUtilities;
import com.faris.kingkits.helper.util.Utilities;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.permissions.*;

import java.util.logging.Level;

public class CommandRenameKit extends KingKitsCommand {

	public CommandRenameKit(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("renamekit")) {
			try {
				if (sender instanceof Player) {
					if (!ConfigController.getInstance().getCommands()[3]) {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (!Utilities.isPvPWorld(((Player) sender).getWorld())) {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
				}
				if (sender.hasPermission(Permissions.COMMAND_KIT_RENAME)) {
					if (args.length == 2) {
						String strKit = args[0];
						String newKitName = args[1];

						KitUtilities.KitSearchResult searchResult = KitUtilities.getKits(strKit);
						Kit kit = null;
						if (searchResult.hasKit()) {
							kit = searchResult.getKit();
						} else if (searchResult.hasOtherKits()) {
							if (searchResult.getOtherKits().size() == 1) {
								kit = searchResult.getOtherKits().get(0);
							} else {
								Messages.sendMessage(sender, Messages.KIT_MULTIPLE_FOUND, strKit);
							}
						} else {
							Messages.sendMessage(sender, Messages.KIT_NOT_FOUND, strKit);
						}
						if (kit != null) {
							if (StringUtilities.containsIllegalCharacters(newKitName)) {
								Messages.sendMessage(sender, Messages.KIT_ILLEGAL_CHARACTERS, newKitName);
								return true;
							}
							strKit = kit.getName();

							if (!searchResult.hasOtherKits()) {
								try {
									Bukkit.getServer().getPluginManager().removePermission("kingkits.kits." + kit.getName().toLowerCase());
								} catch (Exception ignored) {
								}
							}

							KitController.getInstance().removeKit(kit);
							KitController.getInstance().deleteKit(kit);
							kit.setName(newKitName);
							KitController.getInstance().addKit(kit);
							KitController.getInstance().saveKit(kit);

							KitUtilities.KitSearchResult newSearchResult = KitUtilities.getKits(newKitName);
							if (!newSearchResult.hasOtherKits()) {
								try {
									Bukkit.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kit.getName().toLowerCase()));
								} catch (Exception ex) {
									this.getPlugin().getLogger().log(Level.WARNING, "Failed to register the kit permission node 'kingkits.kits." + kit.getName().toLowerCase() + "'.", ex);
								}
							}

							Messages.sendMessage(sender, Messages.COMMAND_KIT_RENAME, strKit, kit.getName());
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "<kit> <newkit>");
					}
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to execute '/" + label.toLowerCase() + " " + StringUtilities.joinString(args) + "'", ex);
				Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getCause().getClass().getName());
			}
			return true;
		}
		return false;
	}

}
