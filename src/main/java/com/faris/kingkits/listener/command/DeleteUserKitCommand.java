package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;

public class DeleteUserKitCommand extends PlayerCommand {

	public DeleteUserKitCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("deleteukit")) {
			if (player.hasPermission(this.getPlugin().permissions.kitUDeleteCommand)) {
				if (this.getPlugin().cmdValues.deleteUKits) {
					if (Utilities.inPvPWorld(player)) {
						if (args.length == 0) {
							Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit>");
							Lang.sendMessage(player, Lang.COMMAND_DELETE_UKIT_DESCRIPTION);
						} else if (args.length == 1) {
							String kitName = args[0];
							List<String> listKits = this.getPlugin().getKitList(player.getUniqueId());
							List<String> listKitsLC = Utilities.toLowerCaseList(listKits);
							if (listKitsLC.contains(kitName.toLowerCase())) {
								try {
									kitName = listKits.get(listKitsLC.indexOf(kitName.toLowerCase()));
									this.getPlugin().getUserKitsConfig().set(player.getUniqueId().toString() + "." + kitName, null);
									this.getPlugin().saveUserKitsConfig();
									if (this.getPlugin().userKitList.containsKey(player.getUniqueId())) {
										List<Kit> kitList = this.getPlugin().userKitList.get(player.getUniqueId());
										if (kitList == null) kitList = new ArrayList<Kit>();
										int deleteIndex = -1;
										for (int i = 0; i < kitList.size(); i++) {
											Kit targetKit = kitList.get(i);
											if (targetKit != null && targetKit.getRealName().toLowerCase().equals(kitName.toLowerCase())) {
												deleteIndex = i;
												break;
											}
										}
										if (deleteIndex != -1) kitList.remove(deleteIndex);
										if (kitList.isEmpty()) {
											this.getPlugin().userKitList.remove(player.getUniqueId());
											this.getPlugin().getUserKitsConfig().set(player.getUniqueId().toString(), null);
											this.getPlugin().saveUserKitsConfig();
										} else {
											this.getPlugin().userKitList.put(player.getUniqueId(), kitList);
										}
									}

									Lang.sendMessage(player, Lang.COMMAND_DELETE_DELETED, kitName);
									if (this.getPlugin().usingKits.containsKey(player.getName()) && this.getPlugin().usingKits.get(player.getName()).equalsIgnoreCase(kitName)) {
										this.getPlugin().usingKits.remove(player.getName());
										this.getPlugin().playerKits.remove(player.getName());
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									Lang.sendMessage(player, Lang.COMMAND_DELETE_ERROR, kitName);
								}
							} else {
								Lang.sendMessage(player, Lang.COMMAND_DELETE_UKIT_NONEXISTENT);
							}
						} else {
							Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit>");
						}
					} else {
						Lang.sendMessage(player, Lang.COMMAND_GEN_WORLD);
					}
				} else {
					Lang.sendMessage(player, Lang.COMMAND_GEN_DISABLED);
				}
			} else {
				this.sendNoAccess(player);
			}
			return true;
		}
		return false;
	}
}
