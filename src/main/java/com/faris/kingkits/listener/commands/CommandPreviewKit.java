package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.controller.GuiController;
import com.faris.kingkits.helper.util.KitUtilities;
import com.faris.kingkits.helper.util.StringUtilities;
import com.faris.kingkits.helper.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.*;

public class CommandPreviewKit extends KingKitsCommand {

	public CommandPreviewKit(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("previewkit")) {
			try {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!ConfigController.getInstance().getCommands(player.getWorld())[7]) {
						Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (!Utilities.isPvPWorld(player.getWorld())) {
						Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (sender.hasPermission(Permissions.COMMAND_KIT_PREVIEW)) {
						if (args.length == 1) {
							String strKit = args[0];
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
							if (kit != null) GuiController.getInstance().openPreviewGUI(player, kit);
						} else if (args.length == 2) {
							Player target = sender.getServer().getPlayer(args[1]);
							if (target != null) {
								if (Utilities.isPvPWorld(target.getWorld())) {
									String strKit = args[0];
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
									if (kit != null) GuiController.getInstance().openPreviewGUI(target, kit);
								}
							} else {
								Messages.sendMessage(sender, Messages.GENERAL_PLAYER_NOT_FOUND);
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "<kit>" + (player.hasPermission(Permissions.COMMAND_KIT_PREVIEW_OTHER) ? " [<player>]" : ""));
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
					}
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
				}
			} catch (Exception ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Failed to execute '/" + label.toLowerCase() + " " + StringUtilities.joinString(args) + "'", ex);
				if (ex.getCause() != null) {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getCause().getClass().getName());
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, ex.getClass().getName());
				}
			}
			return true;
		}
		return false;
	}

}
