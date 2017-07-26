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
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandDeleteKit extends KingKitsCommand {

	public CommandDeleteKit(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("deletekit")) {
			try {
				if (sender instanceof Player) {
					if (!ConfigController.getInstance().getCommands(((Player) sender).getWorld())[2]) {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (!Utilities.isPvPWorld(((Player) sender).getWorld())) {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
				}
				if (sender.hasPermission(Permissions.COMMAND_KIT_DELETE)) {
					if (args.length == 1) {
						String strKit = args[0];
						if (strKit.equals("*")) {
							int deleteCount = 0;
							for (Kit kit : KitController.getInstance().getKits().values()) {
								try {
									KitController.getInstance().removeKit(kit);
									KitController.getInstance().deleteKit(kit);

									deleteCount++;
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							Messages.sendMessage(sender, Messages.COMMAND_KIT_DELETE_ALL, deleteCount);
						} else {
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
								KitController.getInstance().removeKit(kit);
								KitController.getInstance().deleteKit(kit);

								if (!searchResult.hasOtherKits()) {
									try {
										Bukkit.getServer().getPluginManager().removePermission("kingkits.kits." + kit.getName().toLowerCase());
									} catch (Exception ignored) {
									}
								}

								Messages.sendMessage(sender, Messages.COMMAND_KIT_DELETE, kit.getName());
							}
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "<kit>");
					}
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
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
