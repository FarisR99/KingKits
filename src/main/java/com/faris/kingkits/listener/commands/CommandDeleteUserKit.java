package com.faris.kingkits.listener.commands;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.controller.PlayerController;
import com.faris.kingkits.helper.util.KitUtilities;
import com.faris.kingkits.helper.util.PlayerUtilities;
import com.faris.kingkits.helper.util.StringUtilities;
import com.faris.kingkits.helper.util.Utilities;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandDeleteUserKit extends KingKitsCommand {

	public CommandDeleteUserKit(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("deleteukit")) {
			try {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!ConfigController.getInstance().getCommands()[5]) {
						Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}
					if (!Utilities.isPvPWorld(player.getWorld())) {
						Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
						return true;
					}

					KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
					if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return true;

					if (player.hasPermission(Permissions.COMMAND_UKIT_DELETE)) {
						if (args.length == 1) {
							String strKit = args[0];
							if (strKit.equals("*")) {
								int deleteCount = 0;
								for (Kit kit : kitPlayer.getKits().values()) {
									try {
										kitPlayer.removeKit(kit);

										deleteCount++;
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}

								Messages.sendMessage(sender, Messages.COMMAND_KIT_DELETE_ALL_USER, deleteCount);
							} else {
								KitUtilities.KitSearchResult searchResult = KitUtilities.getKits(strKit, kitPlayer);
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
									kitPlayer.removeKit(kit);

									Messages.sendMessage(sender, Messages.COMMAND_KIT_DELETE_USER, kit.getName());
								}
							}
						} else {
							Messages.sendMessage(sender, Messages.GENERAL_COMMAND_USAGE, label.toLowerCase(), "<kit>");
						}
					} else {
						Messages.sendMessage(sender, Messages.GENERAL_COMMAND_NO_PERMISSION);
					}
				} else {
					Messages.sendMessage(sender, Messages.GENERAL_PLAYER_COMMAND);
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
