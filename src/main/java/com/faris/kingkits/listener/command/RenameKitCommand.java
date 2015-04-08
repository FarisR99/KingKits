package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import org.bukkit.entity.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameKitCommand extends PlayerCommand {

	public RenameKitCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("renamekit")) {
			try {
				if (p.hasPermission(this.getPlugin().permissions.kitRenameCommand)) {
					if (this.getPlugin().cmdValues.renameKits) {
						if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
							if (args.length == 0) {
								Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
								Lang.sendMessage(p, Lang.COMMAND_RENAME_KIT_DESCRIPTION);
							} else if (args.length == 2) {
								String strKit = args[0];
								String strNewKit = args[1];
								List<String> currentKits = this.getPlugin().getKitList();
								List<String> currentKitsLC = Utilities.toLowerCaseList(currentKits);
								if (currentKitsLC.contains(strKit.toLowerCase()))
									strKit = currentKits.get(currentKitsLC.indexOf(strKit.toLowerCase()));
								if (!this.getPlugin().getKitsConfig().contains(strNewKit) && !currentKitsLC.contains(strNewKit.toLowerCase())) {
									if (!this.containsIllegalCharacters(strNewKit)) {
										final Kit kit = this.getPlugin().kitList.get(strKit);
										if (kit != null) {
											this.getPlugin().getKitsConfig().set(strKit, null);
											kit.setName(strNewKit);
											kit.setRealName(strNewKit);
											this.getPlugin().getKitsConfig().set(strNewKit, kit.serialize());
											this.getPlugin().saveKitsConfig();

											Map<String, String> newKits = new HashMap<String, String>();
											for (Map.Entry<String, String> entrySet : this.getPlugin().usingKits.entrySet()) {
												if (entrySet.getValue() != null && entrySet.getValue().equals(strKit))
													newKits.put(entrySet.getKey(), strNewKit);
											}
											this.getPlugin().usingKits.putAll(newKits);

											newKits = new HashMap<String, String>();
											for (Map.Entry<String, String> entrySet : this.getPlugin().playerKits.entrySet()) {
												if (entrySet.getValue() != null && entrySet.getValue().equals(strKit))
													newKits.put(entrySet.getKey(), strNewKit);
											}
											this.getPlugin().playerKits.putAll(newKits);

											this.getPlugin().kitList.remove(strKit);
											this.getPlugin().kitList.put(strNewKit, kit);

											Lang.sendMessage(p, Lang.COMMAND_RENAME_RENAMED, strKit, strNewKit);
										} else {
											Lang.sendMessage(p, Lang.KIT_NONEXISTENT, strKit);
										}
									} else {
										Lang.sendMessage(p, Lang.COMMAND_RENAME_ILLEGAL_CHARACTERS);
									}
								} else {
									Lang.sendMessage(p, Lang.COMMAND_RENAME_ALREADY_EXISTS, strNewKit);
								}
							} else {
								Lang.sendMessage(p, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
							}
						} else {
							Lang.sendMessage(p, Lang.COMMAND_GEN_WORLD);
						}
					} else {
						Lang.sendMessage(p, Lang.COMMAND_GEN_DISABLED);
					}
				} else {
					this.sendNoAccess(p);
				}
			} catch (Exception ex) {
				Lang.sendMessage(p, Lang.COMMAND_GEN_ERROR);
			}
			return true;
		}
		return false;
	}

}
