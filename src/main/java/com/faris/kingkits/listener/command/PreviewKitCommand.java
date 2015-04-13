package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.gui.GuiKingKits;
import com.faris.kingkits.gui.GuiPreviewKit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.KingCommand;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class PreviewKitCommand extends KingCommand {

	public PreviewKitCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String command, String[] args) {
		if (command.equalsIgnoreCase("previewkit")) {
			if (this.getPlugin().cmdValues.previewKit) {
				if (args.length == 1) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (player.hasPermission(this.getPlugin().permissions.kitPreviewCommand)) {
							String strKit = args[0];
							Kit kit = KingKitsAPI.getKitByName(strKit, false);
							if (kit != null) {
								if (!GuiKingKits.guiKitMenuMap.containsKey(player.getName())) {
									new GuiPreviewKit(player, kit).openMenu();
								}
							} else {
								Lang.sendMessage(sender, Lang.KIT_NONEXISTENT, strKit);
							}
						} else {
							this.sendNoAccess(player);
						}
					} else {
						Lang.sendMessage(sender, Lang.COMMAND_GEN_IN_GAME);
					}
				} else if (args.length == 2) {
					if (sender.hasPermission(this.getPlugin().permissions.kitPreviewOtherCommand)) {
						String strKit = args[0];
						Kit kit = KingKitsAPI.getKitByName(strKit, false);
						if (kit != null) {
							Player target = sender.getServer().getPlayer(args[1]);
							if (target != null && target.isOnline()) {
								if (!GuiKingKits.guiKitMenuMap.containsKey(target.getName())) {
									new GuiPreviewKit(target, kit).openMenu();
								}
								Lang.sendMessage(sender, Lang.COMMAND_PREVIEW_OPEN_OTHER, target.getName(), ChatColor.stripColor(Utilities.replaceChatColour(kit.getName())));
							} else {
								Lang.sendMessage(sender, Lang.COMMAND_GEN_NOT_ONLINE, args[1]);
							}
						} else {
							Lang.sendMessage(sender, Lang.KIT_NONEXISTENT, strKit);
						}
					} else {
						this.sendNoAccess(sender);
					}
				} else {
					if (sender.hasPermission(this.getPlugin().permissions.kitPreviewCommand) || sender.hasPermission(this.getPlugin().permissions.kitPreviewOtherCommand)) {
						Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit> [<player>]");
					} else {
						this.sendNoAccess(sender);
					}
				}
			} else {
				Lang.sendMessage(sender, Lang.COMMAND_GEN_DISABLED);
			}
			return true;
		}
		return false;
	}

}
