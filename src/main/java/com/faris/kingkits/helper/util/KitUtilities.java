package com.faris.kingkits.helper.util;

import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.api.event.PlayerKitEvent;
import com.faris.kingkits.api.event.PlayerPreKitEvent;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.controller.GuiController;
import com.faris.kingkits.controller.KitController;
import com.faris.kingkits.controller.PlayerController;
import com.faris.kingkits.player.KitPlayer;
import mkremins.fanciful.FancyMessage;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.*;

public class KitUtilities {

	private KitUtilities() {
	}

	public static KitSearchResult getKits(String kitName) {
		Kit exactKit = null;
		List<Kit> kitList = new ArrayList<>();
		if (kitName != null) {
			for (Map.Entry<String, Kit> kitEntry : KitController.getInstance().getKits().entrySet()) {
				if (kitName.equals(kitEntry.getKey())) exactKit = kitEntry.getValue();
				else if (kitName.equalsIgnoreCase(kitEntry.getKey())) kitList.add(kitEntry.getValue());
			}
		}
		return new KitSearchResult(exactKit, kitList);
	}

	public static KitSearchResult getKits(String kitName, KitPlayer kitPlayer) {
		Kit exactKit = null;
		List<Kit> kitList = new ArrayList<>();
		if (kitName != null && kitPlayer != null) {
			for (Map.Entry<String, Kit> kitEntry : kitPlayer.getKits().entrySet()) {
				if (kitName.equals(kitEntry.getValue().getName())) exactKit = kitEntry.getValue();
				else if (kitName.equalsIgnoreCase(kitEntry.getValue().getName())) kitList.add(kitEntry.getValue());
			}
		}
		return new KitSearchResult(exactKit, kitList);
	}

	public static void listKits(CommandSender sender) {
		boolean isPlayer = sender instanceof Player;
		if (sender != null && (!isPlayer || ((Player) sender).isOnline())) {
			if (isPlayer && (ConfigController.getInstance().getKitListMode().equalsIgnoreCase("GUI") || ConfigController.getInstance().getKitListMode().equalsIgnoreCase("Menu"))) {
				Player player = (Player) sender;
				GuiController.getInstance().openKitsMenu(player);
			} else if (isPlayer && ConfigController.getInstance().getKitListMode().equalsIgnoreCase("Fancy")) {
				Player player = (Player) sender;
				KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
				if (kitPlayer != null) {
					List<Kit> kitList = ConfigController.getInstance().shouldSortKitsAlphanumerically() ? sortAlphabetically(KitController.getInstance().getKits().values()) : new ArrayList<>(KitController.getInstance().getKits().values());
					if (ConfigController.getInstance().shouldUsePermissionsForKitList()) {
						List<Kit> kitsToRemove = new ArrayList<>();
						for (Kit kit : kitList) {
							if (!kitPlayer.hasPermission(kit) && !kitPlayer.hasUnlocked(kit)) kitsToRemove.add(kit);
						}
						kitList.removeAll(kitsToRemove);
					}
					Messages.sendMessage(sender, Messages.COMMAND_KIT_LIST_TITLE, kitList.size());
					if (!kitList.isEmpty()) {
						for (Kit kit : kitList) {
							String kitMessage = Messages.COMMAND_KIT_LIST_KITS.getMessage();
							if (kitMessage.contains("%s")) {
								ChatColor kitColour = (!ConfigController.getInstance().shouldUsePermissionsForKitList() || (kitPlayer.hasPermission(kit) || kitPlayer.hasUnlocked(kit))) ? ChatColor.GREEN : ChatColor.DARK_RED;
								FancyMessage fancyKitMessage = new FancyMessage(kitMessage.substring(0, kitMessage.indexOf("%s")).replace("<colour>", kitColour.toString()).replace("<color>", kitColour.toString()));
								String afterKit = kitMessage.substring(kitMessage.indexOf("%s") + 2);
								if (kitMessage.contains("<colour>") || kitMessage.contains("<color>"))
									fancyKitMessage.then(kit.getName()).color(kitColour);
								fancyKitMessage.command("/pvpkit " + kit.getName());

								if (kit.hasDescription()) {
									final List<String> kitDescription = new ArrayList<>();
									for (String descriptionLine : kit.getDescription()) {
										descriptionLine = ChatUtilities.replaceChatCodes(descriptionLine);
										descriptionLine = descriptionLine.replace("<player>", player.getName());
										descriptionLine = descriptionLine.replace("<name>", kit.getName());
										descriptionLine = descriptionLine.replace("<cost>", String.valueOf(kit.getCost()));
										descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(kit.getCooldown()));
										descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(kit.getMaxHealth()));
										descriptionLine = descriptionLine.replace("<walkspeed>", String.valueOf(kit.getWalkSpeed()));
										kitDescription.add(descriptionLine);
									}
									if (!kitDescription.isEmpty())
										fancyKitMessage.tooltip(kitDescription);
								}

								fancyKitMessage.then(afterKit);
								fancyKitMessage.send(player);
							} else {
								sender.sendMessage(Messages.COMMAND_KIT_LIST_KITS.getMessage(kit.getName()).replace("<colour>", (ConfigController.getInstance().shouldUsePermissionsForKitList() || (kitPlayer.hasPermission(kit) || kitPlayer.hasUnlocked(kit))) ? ChatColor.GREEN.toString() : ChatColor.DARK_RED.toString()));
							}
						}
					} else {
						Messages.sendMessage(sender, Messages.COMMAND_KIT_LIST_NONE);
					}
				}
			} else {
				List<Kit> kitList = ConfigController.getInstance().shouldSortKitsAlphanumerically() ? sortAlphabetically(KitController.getInstance().getKits().values()) : new ArrayList<>(KitController.getInstance().getKits().values());
				KitPlayer kitPlayer = sender instanceof Player ? PlayerController.getInstance().getPlayer((Player) sender) : null;
				if (sender instanceof Player && ConfigController.getInstance().shouldUsePermissionsForKitList()) {
					if (kitPlayer != null) {
						List<Kit> kitsToRemove = new ArrayList<>();
						for (Kit kit : kitList) {
							if (!kitPlayer.hasPermission(kit) && !kitPlayer.hasUnlocked(kit)) kitsToRemove.add(kit);
						}
						kitList.removeAll(kitsToRemove);
					} else {
						kitList.clear();
					}
				}
				Messages.sendMessage(sender, Messages.COMMAND_KIT_LIST_TITLE, kitList.size());
				if (!kitList.isEmpty()) {
					for (Kit kit : kitList)
						sender.sendMessage(Messages.COMMAND_KIT_LIST_KITS.getMessage(kit.getName()).replace("<colour>", (!(sender instanceof Player) || ConfigController.getInstance().shouldUsePermissionsForKitList() || (kitPlayer != null && (kitPlayer.hasPermission(kit) || kitPlayer.hasUnlocked(kit)))) ? ChatColor.GREEN.toString() : ChatColor.DARK_RED.toString()));
				} else {
					Messages.sendMessage(sender, Messages.COMMAND_KIT_LIST_NONE);
				}
			}
		}
	}

	public static boolean setKit(final Player player, Kit kit) {
		return setKit(player, kit, false, player != null && player.isOp() && ConfigController.getInstance().canOpsBypass(), player != null && player.isOp() && ConfigController.getInstance().canOpsBypass());
	}

	public static boolean setKit(final Player player, Kit kit, boolean ignoreOneKitPerLife, boolean ignoreCooldown, boolean ignoreCost) {
		if (player == null || kit == null) return false;
		final KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		if (kitPlayer == null) return false;
		try {
			if (!Utilities.isPvPWorld(player.getWorld())) {
				Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
				return false;
			}
			if (kit.isUserKit() || (kitPlayer.hasPermission(kit) || kitPlayer.hasUnlocked(kit))) {
				if (!ignoreOneKitPerLife && ConfigController.getInstance().isOneKitPerLife()) {
					if (kitPlayer.hasKit()) {
						Messages.sendMessage(player, Messages.KIT_ONE_PER_LIFE);
						return false;
					}
				}

				PlayerPreKitEvent preEvent = new PlayerPreKitEvent(kitPlayer, kit);
				player.getServer().getPluginManager().callEvent(preEvent);
				if (preEvent.isCancelled() || preEvent.getKit() == null) {
					player.sendMessage(ChatColor.RED + "A plugin has cancelled the kit selection.");
					return false;
				} else {
					kit = preEvent.getKit();
				}
				long kitTimestamp = kit.isUserKit() ? -1L : kitPlayer.getKitTimestamp(kit);
				if (!ignoreCooldown && kitTimestamp != -1L) {
					if (kit.hasCooldown()) {
						if (System.currentTimeMillis() - kitTimestamp > (long) (kit.getCooldown() * 1_000D)) {
							kitPlayer.setKitTimestamp(kit, null);
							kitTimestamp = -1L;
						}
					}
				}
				if (ignoreCooldown || kitTimestamp == -1L) {
					if (!ignoreCost && kit.getCost() > 0D) {
						double playerBalance = PlayerUtilities.getBalance(player);
						if (playerBalance >= kit.getCost()) {
							playerBalance -= kit.getCost();
							PlayerUtilities.setBalance(player, playerBalance);
							Messages.sendMessage(player, Messages.ECONOMY_COST_PER_KIT, kit.getCost());
						} else {
							Messages.sendMessage(player, Messages.KIT_NOT_ENOUGH_MONEY, kit.getCost() - playerBalance);
							return false;
						}
					}

					Kit oldKit = kitPlayer.getKit();
					kitPlayer.setKit(kit);
					if (ConfigController.getInstance().shouldSetDefaultGamemodeOnKitSelection())
						player.setGameMode(player.getServer().getDefaultGameMode());
					if (ConfigController.getInstance().shouldClearItemsOnKitSelection()) {
						player.getInventory().clear();
						player.getInventory().setArmorContents(null);
						for (PotionEffect activePotionEffect : player.getActivePotionEffects())
							player.removePotionEffect(activePotionEffect.getType());

						for (Map.Entry<Integer, ItemStack> kitItemEntry : kit.getItems().entrySet()) {
							try {
								player.getInventory().setItem(kitItemEntry.getKey(), kitItemEntry.getValue());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						player.getInventory().setArmorContents(kit.getArmour());
					} else {
						List<ItemStack> itemsToDrop = new ArrayList<>();
						for (ItemStack kitItem : kit.getItems().values()) {
							itemsToDrop.addAll(player.getInventory().addItem(kitItem).values());
						}
						for (ItemStack kitArmour : kit.getArmour()) {
							if (!ItemUtilities.isNull(kitArmour))
								itemsToDrop.addAll(player.getInventory().addItem(kitArmour).values());
						}
						if (ConfigController.getInstance().shouldDropItemsOnFullInventory()) {
							for (ItemStack itemToDrop : itemsToDrop)
								player.getWorld().dropItem(player.getLocation(), itemToDrop);
						}
					}
					player.setWalkSpeed(kit.getWalkSpeed());
					if (player.getHealth() > kit.getMaxHealth())
						player.setHealth(kit.getMaxHealth());
					if (ConfigController.getInstance().shouldSetMaxHealth()) {
						player.setMaxHealth(kit.getMaxHealth());
						if (player.getHealth() >= PlayerUtilities.getDefaultMaxHealth())
							player.setHealth(kit.getMaxHealth());
					}
					player.addPotionEffects(kit.getPotionEffects());

					for (String command : kit.getCommands()) {
						command = command.replace("<player>", player.getName()).replace("<name>", player.getName()).replace("<username>", player.getName());
						command = command.replace("<displayname>", player.getDisplayName());
						command = command.replace("<kit>", kit.getName());
						BukkitUtilities.performCommand(command);
					}

					if (kit.hasCooldown())
						kitPlayer.setKitTimestamp(kit, System.currentTimeMillis());

					player.getServer().getPluginManager().callEvent(new PlayerKitEvent(kitPlayer, oldKit, kit));
					Messages.sendMessage(player, Messages.KIT_SET, kit.getName());
				} else {
					PlayerUtilities.sendKitDelayMessage(player, kit, kitTimestamp);
				}
			} else {
				if (ConfigController.getInstance().shouldShowKitPreview()) {
					GuiController.getInstance().openPreviewGUI(player, kit);
				} else {
					Messages.sendMessage(player, Messages.KIT_NO_PERMISSION, kit.getName());
					return false;
				}
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static List<Kit> sortAlphabetically(Collection<Kit> kitList) {
		List<Kit> sortedKits = new ArrayList<>();
		if (kitList != null) {
			Map<String, Kit> kitMap = new LinkedHashMap<>();
			for (Kit kit : kitList) {
				if (kit != null) kitMap.put(kit.getName(), kit);
			}
			List<String> sortedKitNames = new ArrayList<>(kitMap.keySet());
			Collections.sort(sortedKitNames, Utilities.getAlphanumericalComparator());
			for (String sortedKitName : sortedKitNames) {
				Kit sortedKit = kitMap.get(sortedKitName);
				if (sortedKit != null) sortedKits.add(sortedKit);
			}
		}
		return sortedKits;
	}

	public static class KitSearchResult {
		private Kit exactKit = null;
		private List<Kit> otherKits = null;

		public KitSearchResult(Kit exactKit, List<Kit> otherKits) {
			this.exactKit = exactKit;
			this.otherKits = otherKits;
		}

		public Kit getKit() {
			return this.exactKit;
		}

		public List<Kit> getOtherKits() {
			return this.otherKits;
		}

		public boolean hasKit() {
			return this.exactKit != null;
		}

		public boolean hasOtherKits() {
			return !this.otherKits.isEmpty();
		}
	}

}
