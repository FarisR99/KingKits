package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;

public class RenameUserKitCommand extends PlayerCommand {

	public RenameUserKitCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("renameukit")) {
			try {
				if (player.hasPermission(this.getPlugin().permissions.kitURenameCommand)) {
					if (this.getPlugin().cmdValues.renameUKits) {
						if (Utilities.inPvPWorld(player)) {
							if (args.length == 0) {
								Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
								Lang.sendMessage(player, Lang.COMMAND_RENAME_KIT_DESCRIPTION);
							} else if (args.length == 2) {
								String strKit = args[0];
								String strNewKit = args[1];
								List<String> currentKits = this.getPlugin().getKitList(player.getUniqueId());
								List<String> currentKitsLC = Utilities.toLowerCaseList(currentKits);
								if (currentKitsLC.contains(strKit.toLowerCase()))
									strKit = currentKits.get(currentKitsLC.indexOf(strKit.toLowerCase()));
								if (!this.getPlugin().getUserKitsConfig().contains(player.getName() + "." + strNewKit) && !currentKitsLC.contains(strNewKit.toLowerCase())) {
									if (!this.containsIllegalCharacters(strNewKit)) {
										if (KingKitsAPI.isUserKit(strKit, player.getUniqueId())) {
											final Kit kit = KingKitsAPI.getKitByName(strKit, player.getUniqueId());
											strKit = kit.getRealName();
											kit.setName(strNewKit);
											kit.setRealName(strNewKit);
											this.getPlugin().getUserKitsConfig().set(player.getName() + "." + strKit, null);
											this.getPlugin().getUserKitsConfig().set(player.getName() + "." + strNewKit, kit.serialize());
											this.getPlugin().saveUserKitsConfig();

											if (this.getPlugin().userKitList.containsKey(player.getUniqueId())) {
												List<Kit> kitList = this.getPlugin().userKitList.get(player.getUniqueId());
												if (kitList == null) kitList = new ArrayList<Kit>();
												int deleteIndex = -1;
												for (int i = 0; i < kitList.size(); i++) {
													Kit targetKit = kitList.get(i);
													if (targetKit != null && targetKit.getRealName().toLowerCase().equals(strKit)) {
														deleteIndex = i;
														break;
													}
												}
												if (deleteIndex != -1) {
													kitList.remove(deleteIndex);
													if (kitList.isEmpty()) {
														this.getPlugin().userKitList.remove(player.getUniqueId());
														this.getPlugin().getUserKitsConfig().set(player.getUniqueId().toString(), null);
														this.getPlugin().saveUserKitsConfig();
													} else {
														this.getPlugin().userKitList.put(player.getUniqueId(), kitList);
													}
												}
											}
											List<Kit> kitList = this.getPlugin().userKitList.get(player.getUniqueId());
											if (kitList == null) kitList = new ArrayList<Kit>();
											if (!kitList.contains(kit)) kitList.add(kit);
											this.getPlugin().userKitList.put(player.getUniqueId(), kitList);

											if (this.getPlugin().usingKits.containsKey(player.getName()) && this.getPlugin().usingKits.get(player.getName()).equalsIgnoreCase(strKit))
												this.getPlugin().usingKits.put(player.getName(), strNewKit);
											if (this.getPlugin().playerKits.containsKey(player.getName()) && this.getPlugin().playerKits.get(player.getName()).equalsIgnoreCase(strKit))
												this.getPlugin().playerKits.put(player.getName(), strNewKit);

											Lang.sendMessage(player, Lang.COMMAND_RENAME_RENAMED, strKit, strNewKit);
										} else {
											Lang.sendMessage(player, Lang.KIT_NONEXISTENT, strKit);
										}
									} else {
										Lang.sendMessage(player, Lang.COMMAND_RENAME_ILLEGAL_CHARACTERS);
									}
								} else {
									Lang.sendMessage(player, Lang.COMMAND_RENAME_ALREADY_EXISTS, strNewKit);
								}
							} else {
								Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<name> <newname>]");
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
			} catch (Exception ex) {
				Lang.sendMessage(player, Lang.COMMAND_GEN_ERROR);
			}
			return true;
		}
		return false;
	}
}
