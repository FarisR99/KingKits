package com.faris.kingkits.listener.command;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.PlayerCommand;
import org.bukkit.entity.*;
import org.bukkit.potion.*;

import java.util.List;

public class DeleteKitCommand extends PlayerCommand {

	public DeleteKitCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player player, String command, String[] args) {
		if (command.equalsIgnoreCase("deletekit")) {
			if (player.hasPermission(this.getPlugin().permissions.kitDeleteCommand)) {
				if (this.getPlugin().cmdValues.deleteKits) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(player.getWorld().getName())) {
						if (args.length == 0) {
							Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <kit>");
							Lang.sendMessage(player, Lang.COMMAND_DELETE_KIT_DESCRIPTION);
						} else if (args.length == 1) {
							String kitName = args[0];
							List<String> listKits = this.getPlugin().getKitList();
							List<String> listKitsLC = Utilities.toLowerCaseList(listKits);
							if (listKitsLC.contains(kitName.toLowerCase())) {
								try {
									kitName = listKits.get(listKitsLC.indexOf(kitName.toLowerCase()));
									this.getPlugin().getKitsConfig().set(kitName, null);
									this.getPlugin().saveKitsConfig();
									if (this.getPlugin().kitList.containsKey(kitName))
										this.getPlugin().kitList.remove(kitName);

									Lang.sendMessage(player, Lang.COMMAND_DELETE_DELETED, kitName);
									for (Player target : Utilities.getOnlinePlayers()) {
										if (target != null) {
											if (this.getPlugin().usingKits.get(target.getName()) != null) {
												String targetKit = this.getPlugin().usingKits.get(target.getName());
												if (targetKit.equalsIgnoreCase(kitName)) {
													KingKitsAPI.removePlayer(target.getName());
													if (!player.getName().equals(target.getName())) {
														target.getInventory().clear();
														target.getInventory().setArmorContents(null);
														for (PotionEffect potionEffect : target.getActivePotionEffects())
															target.removePotionEffect(potionEffect.getType());
														target.setMaxHealth(20D);
														Lang.sendMessage(target, Lang.COMMAND_DELETE_PLAYER, player.getName());
													}
												}
											}
										}
									}
									if (this.getPlugin().usingKits.containsKey(player.getName()) && this.getPlugin().usingKits.get(player.getName()).equalsIgnoreCase(kitName)) {
										this.getPlugin().usingKits.remove(player.getName());
										this.getPlugin().playerKits.remove(player.getName());
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									Lang.sendMessage(player, Lang.COMMAND_DELETE_ERROR, kitName);
								}
							} else {
								Lang.sendMessage(player, Lang.COMMAND_DELETE_KIT_NONEXISTENT);
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
