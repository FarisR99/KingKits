package com.faris.kingkits.controller;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.api.event.PlayerKitEvent;
import com.faris.kingkits.api.event.PlayerPreKitEvent;
import com.faris.kingkits.helper.util.*;
import com.faris.kingkits.player.KitPlayer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.*;

public class GuiController implements Controller {

	private static GuiController instance = null;

	private GuiListener guiListener = null;
	private Map<UUID, GuiType> guiViewers = null;

	private Inventory kitsMenuInventory = null;
	private Inventory kitsInventory = null;
	private Inventory userKitsInventory = null;
	private Inventory previewInventory = null;

	private Map<UUID, GuiKits> guiKits = null;
	private Map<UUID, GuiKitPreview> guiKitPreview = null;

	public GuiController() {
		this.guiListener = new GuiListener();
		Bukkit.getServer().getPluginManager().registerEvents(this.guiListener, KingKits.getInstance());

		this.guiViewers = new HashMap<>();
		this.guiKits = new HashMap<>();
		this.guiKitPreview = new HashMap<>();
	}

	@Override
	public void shutdownController() {
		if (this.guiListener != null) HandlerList.unregisterAll(this.guiListener);

		if (this.guiViewers != null) {
			for (UUID guiViewerUUID : this.guiViewers.keySet()) {
				Player guiViewer = Bukkit.getServer().getPlayer(guiViewerUUID);
				if (guiViewer != null) guiViewer.closeInventory();
			}
			for (UUID guiViewerUUID : this.guiKits.keySet()) {
				Player guiViewer = Bukkit.getServer().getPlayer(guiViewerUUID);
				if (guiViewer != null) guiViewer.closeInventory();
			}
			for (UUID guiViewerUUID : this.guiKitPreview.keySet()) {
				Player guiViewer = Bukkit.getServer().getPlayer(guiViewerUUID);
				if (guiViewer != null) guiViewer.closeInventory();
			}
			this.guiViewers.clear();
		}

		this.guiKits.clear();
		this.guiKitPreview.clear();

		this.guiListener = null;
		this.guiViewers = null;
		this.kitsMenuInventory = null;
		this.kitsInventory = null;
		this.userKitsInventory = null;
		this.previewInventory = null;
		instance = null;
	}

	public void loadInventories() {
		this.kitsMenuInventory = Bukkit.getServer().createInventory(null, 9, GuiType.GUI_KITS_MENU.getTitle());
		this.kitsInventory = Bukkit.getServer().createInventory(null, ConfigController.getInstance().getGuiSize(), StringUtilities.trimString(GuiType.GUI_KITS.getTitle(), 32));
		this.userKitsInventory = Bukkit.getServer().createInventory(null, ConfigController.getInstance().getGuiSize(), StringUtilities.trimString(GuiType.GUI_USER_KITS.getTitle(), 32));
		this.previewInventory = Bukkit.getServer().createInventory(null, 36 + 9 + 9, GuiType.GUI_PREVIEW_KIT.getTitle());

		this.kitsMenuInventory.setItem(3, ItemUtilities.renameItem(new ItemStack(Material.EMPTY_MAP), Messages.GUI_KITS_MENU_GLOBAL.getMessage()));
		this.kitsMenuInventory.setItem(5, ItemUtilities.renameItem(new ItemStack(Material.MAP), Messages.GUI_KITS_MENU_USER.getMessage()));

		this.previewInventory.setItem(this.previewInventory.getSize() - 6, ItemUtilities.renameItem(new ItemStack(Material.STONE_BUTTON), "&6Select"));
		this.previewInventory.setItem(this.previewInventory.getSize() - 4, ItemUtilities.renameItem(new ItemStack(Material.STONE_BUTTON), "&cExit"));
	}

	public Inventory createKitsMenuInventory(Player player) {
		Inventory kitsMenuInv = Bukkit.getServer().createInventory(player, this.kitsMenuInventory.getSize(), this.kitsMenuInventory.getTitle());
		kitsMenuInv.setContents(this.kitsMenuInventory.getContents());
		return kitsMenuInv;
	}

	public Inventory createKitsInventory(Player player) {
		Inventory kitsInv = null;
		if (player != null) {
			kitsInv = Bukkit.getServer().createInventory(player, this.kitsInventory.getSize(), this.kitsInventory.getTitle());
			kitsInv.setContents(this.kitsInventory.getContents());

			GuiKits guiKits = new GuiKits(KitController.getInstance().getKits().values());
			guiKits.fillInventory(kitsInv);
			this.guiKits.put(player.getUniqueId(), guiKits);
		} else {
			kitsInv = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
			kitsInv.setContents(this.kitsInventory.getContents());
		}
		return kitsInv;
	}

	public Inventory createKitPreviewInventory(Player player, Kit kit) {
		Inventory kitPreviewInv = null;
		if (player != null) {
			kitPreviewInv = Bukkit.getServer().createInventory(player, this.previewInventory.getSize(), StringUtilities.trimString(this.previewInventory.getTitle().replace("<kit>", kit != null ? kit.getName() : "null"), 32));
			kitPreviewInv.setContents(this.previewInventory.getContents());

			if (kit != null) {
				for (Map.Entry<Integer, ItemStack> kitItemEntry : kit.getItems().entrySet()) {
					if (kitItemEntry.getKey() >= 0 && kitItemEntry.getKey() < kitPreviewInv.getSize() - 18)
						kitPreviewInv.setItem(kitItemEntry.getKey(), kitItemEntry.getValue());
				}
				kitPreviewInv.setItem(kitPreviewInv.getSize() - 17, kit.getArmour()[3]);
				kitPreviewInv.setItem(kitPreviewInv.getSize() - 15, kit.getArmour()[2]);
				kitPreviewInv.setItem(kitPreviewInv.getSize() - 13, kit.getArmour()[1]);
				kitPreviewInv.setItem(kitPreviewInv.getSize() - 11, kit.getArmour()[0]);
			}
		} else {
			kitPreviewInv = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
			kitPreviewInv.setContents(this.previewInventory.getContents());
		}
		return kitPreviewInv;
	}

	public Inventory createUserKitsInventory(Player player) {
		Inventory userKitsInv = null;
		KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
		if (kitPlayer != null) {
			userKitsInv = Bukkit.getServer().createInventory(player, this.userKitsInventory.getSize(), this.userKitsInventory.getTitle());
			userKitsInv.setContents(this.userKitsInventory.getContents());

			GuiKits guiKits = new GuiKits(kitPlayer.getKits().values());
			guiKits.fillInventory(userKitsInv);
			this.guiKits.put(player.getUniqueId(), guiKits);
		} else {
			userKitsInv = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
			userKitsInv.setContents(this.userKitsInventory.getContents());
		}
		return userKitsInv;
	}

	public GuiListener getListener() {
		return this.guiListener;
	}

	public void openKitsMenu(Player player) {
		if (player != null && !this.guiViewers.containsKey(player.getUniqueId())) {
			player.closeInventory();
			KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
			if (kitPlayer != null && kitPlayer.getKits().isEmpty()) {
				Inventory inventory = this.createKitsInventory(player);
				if (inventory != null) {
					this.guiViewers.put(player.getUniqueId(), GuiType.GUI_KITS);
					player.openInventory(inventory);
				} else {
					this.guiKits.remove(player.getUniqueId());
				}
			} else {
				Inventory inventory = this.createKitsMenuInventory(player);
				if (inventory != null) {
					this.guiViewers.put(player.getUniqueId(), GuiType.GUI_KITS_MENU);
					player.openInventory(inventory);
				} else {
					this.guiKits.remove(player.getUniqueId());
				}
			}
		}
	}

	public void openPreviewGUI(Player player, Kit kit) {
		if (player != null && !this.guiViewers.containsKey(player.getUniqueId())) {
			player.closeInventory();
			Inventory inventory = this.createKitPreviewInventory(player, kit);
			if (inventory != null) {
				this.guiViewers.put(player.getUniqueId(), GuiType.GUI_PREVIEW_KIT);
				this.guiKitPreview.put(player.getUniqueId(), new GuiKitPreview(kit.getName()));
				player.openInventory(inventory);
			} else {
				this.guiKitPreview.remove(player.getUniqueId());
			}
		}
	}

	private class GuiListener implements Listener {
		@EventHandler
		public void onInventoryClick(InventoryClickEvent event) {
			try {
				if (!(event.getWhoClicked() instanceof Player)) return;
				final Player player = (Player) event.getWhoClicked();
				GuiType guiType = guiViewers.get(player.getUniqueId());
				if (guiType == GuiType.GUI_KITS_MENU) {
					event.setCancelled(true);

					if (event.getRawSlot() == 3) {
						player.closeInventory();
						final UUID playerUUID = player.getUniqueId();
						final Inventory kitsInventory = createKitsInventory(player);
						player.getServer().getScheduler().runTask(KingKits.getInstance(), new Runnable() {
							@Override
							public void run() {
								if (player.isOnline()) {
									player.openInventory(kitsInventory);
									guiViewers.put(player.getUniqueId(), GuiType.GUI_KITS);
								} else {
									guiKits.remove(playerUUID);
								}
							}
						});
					} else if (event.getRawSlot() == 5) {
						player.closeInventory();
						final UUID playerUUID = player.getUniqueId();
						final Inventory userKitsInventory = createUserKitsInventory(player);
						player.getServer().getScheduler().runTask(KingKits.getInstance(), new Runnable() {
							@Override
							public void run() {
								if (player.isOnline()) {
									player.openInventory(userKitsInventory);
									guiViewers.put(player.getUniqueId(), GuiType.GUI_USER_KITS);
								} else {
									guiKits.remove(playerUUID);
								}
							}
						});
					}
				} else if (guiType == GuiType.GUI_KITS) {
					event.setCancelled(true);
					if (guiKits.containsKey(player.getUniqueId())) {
						GuiKits guiKit = guiKits.get(player.getUniqueId());
						Kit clickedKit = guiKit.getKit(event.getRawSlot());
						if (clickedKit != null) {
							KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
							if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return;
							player.closeInventory();

							// TODO: RIGHT CLICK = Preview kits.
							if (event.getClick() != ClickType.RIGHT) {
								setKit(clickedKit, player, kitPlayer, true);
							} else {
								final UUID playerUUID = player.getUniqueId();
								final Inventory previewKitInventory = createKitPreviewInventory(player, clickedKit);
								final Kit finalKit = clickedKit;
								player.getServer().getScheduler().runTask(KingKits.getInstance(), new Runnable() {
									@Override
									public void run() {
										if (player.isOnline()) {
											player.openInventory(previewKitInventory);
											guiViewers.put(player.getUniqueId(), GuiType.GUI_PREVIEW_KIT);
											guiKitPreview.put(player.getUniqueId(), new GuiKitPreview(finalKit.getName()));
										} else {
											guiKits.remove(playerUUID);
										}
									}
								});
							}
						} else {
							if (event.getRawSlot() == event.getInventory().getSize() - 9) {
								if (guiKit.hasPrevious()) {
									guiKit.previous();
									guiKit.fillInventory(event.getInventory());
									guiKits.put(player.getUniqueId(), guiKit);
								}
							} else if (event.getRawSlot() == event.getInventory().getSize() - 1) {
								if (guiKit.hasNext()) {
									guiKit.next();
									guiKit.fillInventory(event.getInventory());
									guiKits.put(player.getUniqueId(), guiKit);
								}
							}
						}
					}
				} else if (guiType == GuiType.GUI_USER_KITS) {
					event.setCancelled(true);
					if (guiKits.containsKey(player.getUniqueId())) {
						GuiKits guiKit = guiKits.get(player.getUniqueId());
						Kit clickedKit = guiKit.getKit(event.getRawSlot());
						if (clickedKit != null) {
							KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
							if (!PlayerUtilities.checkPlayer(player, kitPlayer)) return;
							player.closeInventory();

							setKit(clickedKit, player, kitPlayer, false);
						} else {
							if (event.getRawSlot() == event.getInventory().getSize() - 9) {
								if (guiKit.hasPrevious()) {
									guiKit.previous();
									guiKit.fillInventory(event.getInventory());
									guiKits.put(player.getUniqueId(), guiKit);
								}
							} else if (event.getRawSlot() == event.getInventory().getSize() - 1) {
								if (guiKit.hasNext()) {
									guiKit.next();
									guiKit.fillInventory(event.getInventory());
									guiKits.put(player.getUniqueId(), guiKit);
								}
							}
						}
					}
				} else if (guiType == GuiType.GUI_PREVIEW_KIT) {
					event.setCancelled(true);
					if (event.getRawSlot() == event.getInventory().getSize() - 6) {
						KitPlayer kitPlayer = PlayerController.getInstance().getPlayer(player);
						GuiKitPreview guiKitP = guiKitPreview.get(player.getUniqueId());
						if (kitPlayer != null && guiKitP != null) {
							player.closeInventory();

							if (Utilities.isPvPWorld(player.getWorld())) {
								Kit kit = guiKitP.getKit();
								if (kit != null) {
									setKit(kit, player, kitPlayer, false);
								} else {
									Messages.sendMessage(player, Messages.KIT_NOT_FOUND);
								}
							}
						}
					} else if (event.getRawSlot() == event.getInventory().getSize() - 4) {
						player.closeInventory();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@EventHandler
		public void onInventoryClose(InventoryCloseEvent event) {
			try {
				if (!(event.getPlayer() instanceof Player)) return;
				final Player player = (Player) event.getPlayer();
				GuiType openGuiType = guiViewers.get(player.getUniqueId());
				if (guiViewers.containsKey(player.getUniqueId())) guiViewers.remove(player.getUniqueId());
				if (guiKits.containsKey(player.getUniqueId())) guiKits.remove(player.getUniqueId());
				if (guiKitPreview.containsKey(player.getUniqueId())) guiKitPreview.remove(player.getUniqueId());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void setKit(Kit selectedKit, final Player player, final KitPlayer kitPlayer, final boolean allowKitPreview) {
		if (selectedKit == null || player == null || kitPlayer == null) return;
		try {
			if (!Utilities.isPvPWorld(player.getWorld())) {
				Messages.sendMessage(player, Messages.GENERAL_COMMAND_DISABLED);
				return;
			}
			if (selectedKit.isUserKit() || (kitPlayer.hasPermission(selectedKit) || kitPlayer.hasUnlocked(selectedKit))) {
				if (ConfigController.getInstance().isOneKitPerLife()) {
					if (kitPlayer.hasKit()) {
						Messages.sendMessage(player, Messages.KIT_ONE_PER_LIFE);
						return;
					}
				}

				PlayerPreKitEvent preEvent = new PlayerPreKitEvent(kitPlayer, selectedKit);
				player.getServer().getPluginManager().callEvent(preEvent);
				if (preEvent.isCancelled() || preEvent.getKit() == null) {
					player.sendMessage(ChatColor.RED + "A plugin has cancelled the kit selection.");
					return;
				} else {
					selectedKit = preEvent.getKit();
				}
				long kitTimestamp = selectedKit.isUserKit() || (player.isOp() && ConfigController.getInstance().canOpsBypass()) ? -1L : kitPlayer.getKitTimestamp(selectedKit);
				if (kitTimestamp != -1L) {
					if (selectedKit.hasCooldown()) {
						if (System.currentTimeMillis() - kitTimestamp > (long) (selectedKit.getCooldown() * 1_000D)) {
							kitPlayer.setKitTimestamp(selectedKit, null);
							kitTimestamp = -1L;
						}
					}
				}
				if (kitTimestamp == -1L) {
					if (selectedKit.getCost() > 0D && (!player.isOp() || !ConfigController.getInstance().canOpsBypass())) {
						double playerBalance = PlayerUtilities.getBalance(player);
						if (playerBalance >= selectedKit.getCost()) {
							playerBalance -= selectedKit.getCost();
							PlayerUtilities.setBalance(player, playerBalance);
							Messages.sendMessage(player, Messages.ECONOMY_COST_PER_KIT, selectedKit.getCost());
						} else {
							Messages.sendMessage(player, Messages.KIT_NOT_ENOUGH_MONEY, selectedKit.getCost() - playerBalance);
							return;
						}
					}

					Kit oldKit = kitPlayer.getKit();
					kitPlayer.setKit(selectedKit);
					if (ConfigController.getInstance().shouldSetDefaultGamemodeOnKitSelection())
						player.setGameMode(player.getServer().getDefaultGameMode());
					if (ConfigController.getInstance().shouldClearItemsOnKitSelection()) {
						player.getInventory().clear();
						player.getInventory().setArmorContents(null);
						for (PotionEffect activePotionEffect : player.getActivePotionEffects())
							player.removePotionEffect(activePotionEffect.getType());

						for (Map.Entry<Integer, ItemStack> kitItemEntry : selectedKit.getItems().entrySet()) {
							try {
								player.getInventory().setItem(kitItemEntry.getKey(), kitItemEntry.getValue());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						player.getInventory().setArmorContents(selectedKit.getArmour());
					} else {
						List<ItemStack> itemsToDrop = new ArrayList<>();
						for (ItemStack kitItem : selectedKit.getItems().values()) {
							itemsToDrop.addAll(player.getInventory().addItem(kitItem).values());
						}
						for (ItemStack kitArmour : selectedKit.getArmour()) {
							if (!ItemUtilities.isNull(kitArmour))
								itemsToDrop.addAll(player.getInventory().addItem(kitArmour).values());
						}
						if (ConfigController.getInstance().shouldDropItemsOnFullInventory()) {
							for (ItemStack itemToDrop : itemsToDrop)
								player.getWorld().dropItem(player.getLocation(), itemToDrop);
						}
					}
					player.setWalkSpeed(selectedKit.getWalkSpeed());
					if (player.getHealth() > selectedKit.getMaxHealth())
						player.setHealth(selectedKit.getMaxHealth());
					if (ConfigController.getInstance().shouldSetMaxHealth()) {
						player.setMaxHealth(selectedKit.getMaxHealth());
						if (player.getHealth() >= PlayerUtilities.getDefaultMaxHealth())
							player.setHealth(selectedKit.getMaxHealth());
					}
					player.addPotionEffects(selectedKit.getPotionEffects());

					for (String command : selectedKit.getCommands()) {
						command = command.replace("<player>", player.getName()).replace("<name>", player.getName()).replace("<username>", player.getName());
						command = command.replace("<displayname>", player.getDisplayName());
						command = command.replace("<kit>", selectedKit.getName());
						BukkitUtilities.performCommand(command);
					}

					if (selectedKit.hasCooldown())
						kitPlayer.setKitTimestamp(selectedKit, System.currentTimeMillis());

					player.getServer().getPluginManager().callEvent(new PlayerKitEvent(kitPlayer, oldKit, selectedKit));
				} else {
					PlayerUtilities.sendKitDelayMesasge(player, selectedKit, kitTimestamp);
				}
			} else {
				if (allowKitPreview && ConfigController.getInstance().shouldShowKitPreview()) {
					player.closeInventory();
					final UUID playerUUID = player.getUniqueId();
					final Inventory previewKitInventory = this.createKitPreviewInventory(player, selectedKit);
					final Kit finalKit = selectedKit;
					player.getServer().getScheduler().runTask(KingKits.getInstance(), new Runnable() {
						@Override
						public void run() {
							if (player.isOnline()) {
								player.openInventory(previewKitInventory);
								guiViewers.put(player.getUniqueId(), GuiType.GUI_PREVIEW_KIT);
								guiKitPreview.put(player.getUniqueId(), new GuiKitPreview(finalKit.getName()));
							} else {
								guiKits.remove(playerUUID);
							}
						}
					});
				} else {
					Messages.sendMessage(player, Messages.KIT_NO_PERMISSION, selectedKit.getName());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static GuiController getInstance() {
		if (instance == null) instance = new GuiController();
		return instance;
	}

	public enum GuiType {
		GUI_KITS_MENU(),
		GUI_KITS(),
		GUI_USER_KITS(),
		GUI_PREVIEW_KIT();

		private String messagesPathPrefix = "";

		GuiType() {
			this.messagesPathPrefix = this.name() + "_";
		}

		GuiType(String prefix) {
			this.messagesPathPrefix = prefix;
		}

		public String getTitle() {
			return Messages.getMessageByEnum(this.messagesPathPrefix + "Title");
		}
	}

	private static class GuiKits {
		private int page = 1, maxPage = 1;
		private List<Kit> availableKits = new ArrayList<>();
		private Map<Integer, Kit> kitsSlot = new TreeMap<>();

		public GuiKits(Collection<Kit> kits) {
			this.availableKits = new ArrayList<>(kits);

			this.page = 1;
			this.maxPage = 1;
			for (int i = 0; i < this.availableKits.size(); i++) {
				if (i != 0 && i % (ConfigController.getInstance().getGuiSize() - 9) == 0) this.maxPage++;
			}
		}

		public void fillInventory(Inventory inventory) {
			if (inventory != null) {
				inventory.clear();
				this.kitsSlot.clear();

				Map<Kit, ItemStack> leftOverKits = new LinkedHashMap<>();
				int minPage = (this.page - 1);
				int invSize = (inventory.getSize() - 9);
				int numAvailableKits = this.availableKits.size();
				for (int i = minPage * invSize; i < numAvailableKits && i < minPage * invSize + invSize; i++) {
					Kit availableKit = this.availableKits.get(i);
					if (availableKit == null) continue;
					try {
						ItemStack guiItem = availableKit.getGuiItem().clone();
						if (!ItemUtilities.hasName(guiItem))
							ItemUtilities.renameItem(guiItem, "&a" + availableKit.getName());
						if (availableKit.getGuiPosition() != -1) {
							if (availableKit.getGuiPosition() >= 0 && availableKit.getGuiPosition() < inventory.getSize()) {
								inventory.setItem(availableKit.getGuiPosition(), guiItem);
								this.kitsSlot.put(availableKit.getGuiPosition(), availableKit);
							}
						} else {
							leftOverKits.put(availableKit, guiItem);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				for (Map.Entry<Kit, ItemStack> leftOverKit : leftOverKits.entrySet()) {
					try {
						int freeSlot = inventory.firstEmpty();
						if (freeSlot != -1) {
							inventory.setItem(freeSlot, leftOverKit.getValue());
							this.kitsSlot.put(freeSlot, leftOverKit.getKey());
						} else {
							break;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++) {
					this.kitsSlot.remove(i);
					inventory.setItem(i, null);

					ItemStack button = null;
					if (i == inventory.getSize() - 9) {
						button = ConfigController.getInstance().getGuiPreviousButton().clone();
						inventory.setItem(i, ItemUtilities.hasName(button) ? ItemUtilities.renameItem(button, ItemUtilities.getName(button).replace("<colour>", this.hasPrevious() ? ChatColor.AQUA.toString() : ChatColor.DARK_GRAY.toString())) : button);
					} else if (i == inventory.getSize() - 1) {
						button = ConfigController.getInstance().getGuiNextButton().clone();
						inventory.setItem(i, ItemUtilities.hasName(button) ? ItemUtilities.renameItem(button, ItemUtilities.getName(button).replace("<colour>", this.hasNext() ? ChatColor.AQUA.toString() : ChatColor.DARK_GRAY.toString())) : button);
					}
				}
			}
		}

		public Kit getKit(int slot) {
			return this.kitsSlot.get(slot);
		}

		public List<Kit> getKits() {
			return this.availableKits;
		}

		public boolean hasNext() {
			return this.page < this.maxPage;
		}

		public boolean hasPrevious() {
			return this.page > 1;
		}

		public void next() {
			if (!this.hasNext()) throw new ArrayIndexOutOfBoundsException();
			this.page++;
		}

		public void previous() {
			if (!this.hasPrevious()) throw new ArrayIndexOutOfBoundsException();
			this.page--;
		}
	}


	private static class GuiKitPreview {
		private String kitName = "";

		public GuiKitPreview(String kit) {
			this.kitName = kit;
		}

		public Kit getKit() {
			return KitController.getInstance().getKit(this.kitName);
		}
	}

}
