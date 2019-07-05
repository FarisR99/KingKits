package com.faris.kingkits.old;

import com.faris.BackwardsCompatibility;
import com.faris.kingkits.helper.util.ChatUtilities;
import com.faris.kingkits.helper.util.ItemUtilities;
import com.faris.kingkits.helper.util.StringUtilities;
import com.faris.kingkits.helper.util.Utilities;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class OldKit implements Iterable<ItemStack>, ConfigurationSerializable {

	private String kitName = "";
	private String realName = "";
	private boolean userKit = false;
	private double kitCost = 0D;
	private long kitCooldown = 0;

	private List<String> kitCommands = new ArrayList<>();
	private boolean commandAlias = false;

	private ItemStack guiItem = null;
	private int guiPosition = -1;

	private Map<Integer, ItemStack> kitItems = new HashMap<>();
	private List<ItemStack> kitArmour = new ArrayList<>();
	private List<PotionEffect> potionEffects = new ArrayList<>();
	private Map<Long, List<String>> killstreakCommands = new HashMap<>();
	private List<String> kitDescription = new ArrayList<>();

	// Misc
	private boolean itemBreaking = true;
	private int maxHealth = 20;
	private int requiredScoreToUnlock = -1;

	public OldKit(String kitName) {
		Validate.notNull(kitName);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
	}

	public OldKit(String kitName, double kitCost) {
		Validate.notNull(kitName);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitCost = kitCost;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
	}

	public OldKit(String kitName, Map<Integer, ItemStack> kitItems) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
	}

	public OldKit(String kitName, Map<Integer, ItemStack> kitItems, List<PotionEffect> potionEffects) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notNull(potionEffects);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.potionEffects = potionEffects;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
	}

	public OldKit(String kitName, double kitCost, Map<Integer, ItemStack> kitItems) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.kitCost = kitCost;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
	}

	public OldKit(String kitName, double kitCost, Map<Integer, ItemStack> kitItems, List<PotionEffect> potionEffects) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notNull(potionEffects);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.kitCost = kitCost;
		this.potionEffects = potionEffects;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
	}

	public boolean canBeUnlocked() {
		return this.requiredScoreToUnlock != -1;
	}

	public boolean canItemsBreak() {
		return this.itemBreaking;
	}

	public List<ItemStack> getArmour() {
		return Collections.unmodifiableList(this.kitArmour);
	}

	public List<String> getCommands() {
		return this.kitCommands;
	}

	public long getCooldown() {
		return this.kitCooldown;
	}

	public double getCost() {
		return this.kitCost;
	}

	public List<String> getDescription() {
		return this.kitDescription;
	}

	private int getFreeSlot() {
		for (int i = 0; i < 36; i++) {
			if (!this.kitItems.containsKey(i)) return i;
		}
		return this.kitItems.size() + 1;
	}

	public ItemStack getGuiItem() {
		return this.guiItem;
	}

	public int getGuiPosition() {
		return this.guiPosition > 0 ? this.guiPosition : -1;
	}

	public List<ItemStack> getItems() {
		return new ArrayList<>(this.kitItems.values());
	}

	public Map<Integer, ItemStack> getItemsWithSlot() {
		return Collections.unmodifiableMap(this.kitItems);
	}

	public Map<Long, List<String>> getKillstreaks() {
		return this.killstreakCommands;
	}

	public int getMaxHealth() {
		return this.maxHealth;
	}

	public List<ItemStack> getMergedItems() {
		List<ItemStack> kitItems = new ArrayList<>(this.kitItems.values());
		kitItems.addAll(this.kitArmour);
		return Collections.unmodifiableList(kitItems);
	}

	public String getName() {
		return this.kitName;
	}

	public List<PotionEffect> getPotionEffects() {
		return Collections.unmodifiableList(this.potionEffects);
	}

	public String getRealName() {
		return this.realName;
	}

	public int getUnlockScore() {
		return this.requiredScoreToUnlock;
	}

	public boolean hasAlias() {
		return this.commandAlias;
	}

	public boolean hasCooldown() {
		return this.kitCooldown > 0;
	}

	public boolean hasDescription() {
		return !this.kitDescription.isEmpty();
	}

	public boolean isUserKit() {
		return this.userKit;
	}

	public OldKit setAlias(boolean commandAlias) {
		this.commandAlias = commandAlias;
		return this;
	}

	public OldKit setArmour(List<ItemStack> armour) {
		if (armour != null) this.kitArmour = armour;
		return this;
	}

	public OldKit setBreakableItems(boolean breakableItems) {
		this.itemBreaking = breakableItems;
		return this;
	}

	public OldKit setCommands(List<String> commands) {
		Validate.notNull(commands);
		this.kitCommands = commands;
		return this;
	}

	public OldKit setCooldown(long cooldown) {
		if (this.kitCooldown >= 0L) this.kitCooldown = cooldown;
		return this;
	}

	public OldKit setCost(double cost) {
		this.kitCost = cost;
		return this;
	}

	public OldKit setDescription(List<String> description) {
		if (description != null) this.kitDescription = description;
		return this;
	}

	public OldKit setGuiItem(ItemStack guiItem) {
		this.guiItem = guiItem != null ? guiItem : new ItemStack(Material.AIR);
		return this;
	}

	public OldKit setGuiPosition(int guiPosition) {
		this.guiPosition = guiPosition > 0 ? guiPosition : -1;
		return this;
	}

	public OldKit setItems(List<ItemStack> items) {
		if (items != null) {
			this.kitItems = new HashMap<>();
			for (int i = 0; i < items.size(); i++) this.kitItems.put(i, items.get(i));
		}
		return this;
	}

	public OldKit setItems(Map<Integer, ItemStack> items) {
		if (items != null) this.kitItems = items;
		return this;
	}

	public OldKit setKillstreaks(Map<Long, List<String>> killstreaks) {
		if (killstreaks != null) this.killstreakCommands = killstreaks;
		return this;
	}

	public OldKit setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
		return this;
	}

	public OldKit setName(String name) {
		Validate.notNull(name);
		Validate.notEmpty(name);
		this.kitName = name;
		return this;
	}

	public OldKit setPotionEffects(List<PotionEffect> potionEffects) {
		Validate.notNull(potionEffects);
		this.potionEffects = potionEffects;
		return this;
	}

	public OldKit setRealName(String realName) {
		Validate.notNull(realName);
		this.realName = realName;
		return this;
	}

	public void setUnlockScore(int unlockThreshold) {
		this.requiredScoreToUnlock = unlockThreshold;
	}

	public OldKit setUserKit(boolean userKit) {
		this.userKit = userKit;
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serializedKit = new HashMap<>();
		serializedKit.put("Name", this.kitName != null ? this.kitName : "Kit" + new Random().nextInt());
		serializedKit.put("Cost", this.kitCost);
		serializedKit.put("Cooldown", this.kitCooldown);
		if (!this.userKit) serializedKit.put("Command alias", false);
		serializedKit.put("Commands", this.kitCommands);
		serializedKit.put("Description", this.kitDescription);
		serializedKit.put("Item breaking", this.itemBreaking);
		serializedKit.put("Max health", this.maxHealth);
		serializedKit.put("GUI Position", this.guiPosition);
		if (this.requiredScoreToUnlock != -1) serializedKit.put("Score for auto-unlock", this.requiredScoreToUnlock);

		// GUI Item
		if (this.guiItem != null) {
			Map<String, Object> guiItemMap = new HashMap<>();
			guiItemMap.put("Type", this.guiItem.getType().toString());
			guiItemMap.put("Amount", this.guiItem.getAmount());
			guiItemMap.put("Data", this.guiItem.getDurability());
			// Dye colour
			if (this.guiItem.hasItemMeta() && this.guiItem.getItemMeta() instanceof LeatherArmorMeta) {
				guiItemMap.put("Dye", ((LeatherArmorMeta) this.guiItem.getItemMeta()).getColor().asRGB());
			}
			// Skull skin
			if (this.guiItem.getItemMeta() instanceof SkullMeta) {
				SkullMeta skullMeta = (SkullMeta) this.guiItem.getItemMeta();
				if (skullMeta.getOwner() != null) guiItemMap.put("Skin", skullMeta.getOwner());
			}
			// Enchantments
			Map<String, Integer> enchantmentMap = new HashMap<>();
			for (Map.Entry<Enchantment, Integer> entrySet : this.guiItem.getEnchantments().entrySet())
				enchantmentMap.put(entrySet.getKey().getName(), entrySet.getValue());
			if (!enchantmentMap.isEmpty()) guiItemMap.put("Enchantments", enchantmentMap);
			// Lores
			if (this.guiItem.hasItemMeta() && this.guiItem.getItemMeta().hasLore())
				guiItemMap.put("Lore", this.guiItem.getItemMeta().getLore());
			serializedKit.put("GUI Item", guiItemMap);
		}

		// Items
		if (this.kitItems != null && !this.kitItems.isEmpty()) {
			Map<String, Object> itemsMap = new HashMap<>();
			for (int i = 0; i < 36; i++) {
				ItemStack kitItem = this.kitItems.size() > i ? this.kitItems.get(i) : null;
				if (kitItem != null && kitItem.getType() != Material.AIR) {
					Map<String, Object> kitItemMap = new LinkedHashMap<>();
					kitItemMap.put("Type", kitItem.getType().toString());
					String itemName = kitItem.hasItemMeta() && kitItem.getItemMeta().hasDisplayName() ? kitItem.getItemMeta().getDisplayName() : null;
					if (itemName != null) kitItemMap.put("Name", ChatUtilities.replaceChatColours(itemName));
					kitItemMap.put("Amount", kitItem.getAmount());
					kitItemMap.put("Data", kitItem.getDurability());
					if (kitItem.getItemMeta() instanceof LeatherArmorMeta) {
						kitItemMap.put("Dye", ((LeatherArmorMeta) kitItem.getItemMeta()).getColor().asRGB());
					}
					if (kitItem.getItemMeta() instanceof SkullMeta) {
						SkullMeta skullMeta = (SkullMeta) kitItem.getItemMeta();
						if (skullMeta.getOwner() != null) kitItemMap.put("Skin", skullMeta.getOwner());
					}
					Map<String, Integer> enchantmentMap = new HashMap<>();
					for (Map.Entry<Enchantment, Integer> entrySet : kitItem.getEnchantments().entrySet())
						enchantmentMap.put(entrySet.getKey().getName(), entrySet.getValue());
					if (!enchantmentMap.isEmpty()) kitItemMap.put("Enchantments", enchantmentMap);
					if (kitItem.hasItemMeta() && kitItem.getItemMeta().hasLore())
						kitItemMap.put("Lore", ChatUtilities.replaceChatColours(kitItem.getItemMeta().getLore()));
					itemsMap.put("Slot " + i, kitItemMap);
				}
			}
			serializedKit.put("Items", itemsMap);
		}

		// Armour
		if (this.kitArmour != null && !this.kitArmour.isEmpty()) {
			Map<String, Object> armourMap = new HashMap<>();
			for (ItemStack kitArmour : this.kitArmour)
				if (kitArmour != null) {
					Map<String, Object> kitArmourMap = new HashMap<>();
					String armourName = kitArmour.hasItemMeta() && kitArmour.getItemMeta().hasDisplayName() ? kitArmour.getItemMeta().getDisplayName() : null;
					if (armourName != null) kitArmourMap.put("Name", ChatUtilities.replaceChatColours(armourName));
					kitArmourMap.put("Type", kitArmour.getType().toString());
					kitArmourMap.put("Data", kitArmour.getDurability());
					if (kitArmour.getItemMeta() instanceof LeatherArmorMeta) {
						kitArmourMap.put("Dye", ((LeatherArmorMeta) kitArmour.getItemMeta()).getColor().asRGB());
					}
					if (kitArmour.getItemMeta() instanceof SkullMeta) {
						SkullMeta skullMeta = (SkullMeta) kitArmour.getItemMeta();
						if (skullMeta.getOwner() != null) kitArmourMap.put("Skin", skullMeta.getOwner());
					}
					Map<String, Integer> enchantmentMap = new HashMap<>();
					for (Map.Entry<Enchantment, Integer> entrySet : kitArmour.getEnchantments().entrySet())
						enchantmentMap.put(entrySet.getKey().getName(), entrySet.getValue());
					if (!enchantmentMap.isEmpty()) kitArmourMap.put("Enchantments", enchantmentMap);
					if (kitArmour.hasItemMeta() && kitArmour.getItemMeta().hasLore())
						kitArmourMap.put("Lore", ChatUtilities.replaceChatColours(kitArmour.getItemMeta().getLore()));
					String[] armourNameSplit = kitArmour.getType().toString().contains("_") ? kitArmour.getType().toString().split("_") : null;
					String armourNameKey = armourNameSplit != null && armourNameSplit.length > 1 ? StringUtilities.capitalizeFully(armourNameSplit[1].toLowerCase()) : StringUtilities.capitalizeFully(kitArmour.getType().toString().toLowerCase()).replace('_', ' ');
					armourMap.put(armourNameKey, kitArmourMap);
				}
			serializedKit.put("Armour", armourMap);
		}

		// Potion effects
		if (this.potionEffects != null && !this.potionEffects.isEmpty()) {
			Map<String, Object> potionEffectsMap = new HashMap<>();
			for (PotionEffect potionEffect : this.potionEffects) {
				Map<String, Integer> potionEffectMap = new HashMap<>();
				potionEffectMap.put("Level", potionEffect.getAmplifier() + 1);
				potionEffectMap.put("Duration", potionEffect.getDuration() / 20);
				potionEffectsMap.put(StringUtilities.capitalizeFully(potionEffect.getType().getName().toLowerCase().replace('_', ' ')), potionEffectMap);
			}
			if (!potionEffectsMap.isEmpty()) serializedKit.put("Potion Effects", potionEffectsMap);
		}

		if (this.killstreakCommands != null && !this.killstreakCommands.isEmpty()) {
			Map<String, Object> killstreakCmds = new HashMap<>();
			for (Map.Entry<Long, List<String>> killstreakEntry : this.killstreakCommands.entrySet()) {
				if (killstreakEntry.getValue() != null && !killstreakEntry.getValue().isEmpty()) {
					killstreakCmds.put("Killstreak " + killstreakEntry.getKey(), killstreakEntry.getValue());
				}
			}
			if (!killstreakCmds.isEmpty()) serializedKit.put("Killstreaks", killstreakCmds);
		}

		return serializedKit;
	}

	@Override
	public OldKit clone() {
		return new OldKit(this.kitName).setRealName(this.realName).setCooldown(this.kitCooldown).setCommands(this.kitCommands).setGuiItem(this.guiItem).setPotionEffects(this.potionEffects).setCost(this.kitCost).setItems(this.kitItems).setArmour(this.kitArmour);
	}

	@Override
	public ListIterator<ItemStack> iterator() {
		return this.getMergedItems().listIterator();
	}

	@Override
	public String toString() {
		return this.serialize().toString();
	}

	public static OldKit deserialize(Map<String, Object> kitSection) throws NullPointerException, ClassCastException {
		OldKit kit = null;
		if (kitSection.containsKey("Name")) {
			try {
				String kitName = getObject(kitSection, "Name", String.class);
				kit = new OldKit(kitName);
				if (kitSection.containsKey("Cost")) kit.setCost(getObject(kitSection, "Cost", Double.class));
				if (kitSection.containsKey("Cooldown")) kit.setCooldown(getObject(kitSection, "Cooldown", Long.class));
				if (kitSection.containsKey("Command alias"))
					kit.setAlias(Boolean.valueOf(getObject(kitSection, "Command alias", String.class)));
				if (kitSection.containsKey("Commands"))
					kit.setCommands(getObject(kitSection, "Commands", List.class));
				if (kitSection.containsKey("Description"))
					kit.setDescription(getObject(kitSection, "Description", List.class));
				if (kitSection.containsKey("Item breaking"))
					kit.setBreakableItems(Boolean.valueOf(getObject(kitSection, "Item breaking", String.class)));
				if (kitSection.containsKey("Max health"))
					kit.setMaxHealth(getObject(kitSection, "Max health", Integer.class));
				if (kitSection.containsKey("GUI Position"))
					kit.setGuiPosition(getObject(kitSection, "GUI Position", Integer.class));
				if (kitSection.containsKey("Score for auto-unlock"))
					kit.setUnlockScore(getObject(kitSection, "Score for auto-unlock", Integer.class));
				if (kitSection.containsKey("GUI Item")) {
					Map<String, Object> guiItemMap = getValues(kitSection, "GUI Item");
					ItemStack guiItem = null;
					if (guiItemMap.containsKey("Type")) {
						String strType = (guiItemMap.get("Type") != null ? guiItemMap.get("Type") : "").toString();
						Material itemType = Utilities.isNumber(Integer.class, strType) ? BackwardsCompatibility.getMaterial(Integer.parseInt(strType)) : Material.getMaterial(strType.toUpperCase());
						if (itemType == null || itemType == Material.AIR) itemType = Material.DIAMOND_SWORD;
						int itemAmount = getObject(guiItemMap, "Amount", Integer.class, 1);
						short itemData = getObject(guiItemMap, "Data", Short.class, (short) 0);
						guiItem = new ItemStack(itemType, itemAmount, itemData);
						if (guiItemMap.containsKey("Dye")) {
							int itemDye = getObject(guiItemMap, "Dye", Integer.class);
							ItemUtilities.setDye(guiItem, itemDye);
						}
						if (guiItemMap.containsKey("Skin") && guiItem.getItemMeta() instanceof SkullMeta) {
							SkullMeta skullMeta = (SkullMeta) guiItem.getItemMeta();
							skullMeta.setOwner(getObject(guiItemMap, "Skin", String.class));
							guiItem.setItemMeta(skullMeta);
						}
						if (guiItemMap.containsKey("Enchantments")) {
							Map<String, Object> guiItemEnchantments = getValues(guiItemMap, "Enchantments");
							for (Map.Entry<String, Object> entrySet : guiItemEnchantments.entrySet()) {
								Enchantment enchantmentType = Utilities.isNumber(Integer.class, entrySet.getKey()) ? BackwardsCompatibility.getEnchantment(Integer.parseInt(entrySet.getKey())) : Enchantment.getByName(Utilities.getEnchantmentName(entrySet.getKey()));
								if (enchantmentType != null) {
									String enchantmentValue = entrySet.getValue().toString();
									int enchantmentLevel = Utilities.isNumber(Integer.class, enchantmentValue) ? Integer.parseInt(enchantmentValue) : 1;
									guiItem.addUnsafeEnchantment(enchantmentType, enchantmentLevel);
								}
							}
						}
						if (guiItemMap.containsKey("Lore")) {
							List<String> guiItemLore = getObject(guiItemMap, "Lore", List.class);
							ItemMeta guiItemMeta = guiItem.getItemMeta();
							if (guiItemMeta != null) {
								guiItemMeta.setLore(ChatUtilities.replaceChatCodes(guiItemLore));
								guiItem.setItemMeta(guiItemMeta);
							}
						}
					}
					if (guiItem != null) kit.setGuiItem(guiItem);
				}
				if (kitSection.containsKey("Items")) {
					Map<String, Object> itemsMap = getValues(kitSection, "Items");
					Map<Integer, ItemStack> kitItems = new HashMap<Integer, ItemStack>() {
						{
							for (int i = 0; i < 36; i++) {
								this.put(i, new ItemStack(Material.AIR));
							}
						}
					};

					for (Map.Entry<String, Object> entrySet : itemsMap.entrySet()) {
						String strSlot = entrySet.getKey();
						if (strSlot.contains(" ")) {
							String[] slotSplit = strSlot.split(" ");
							if (slotSplit[0].equals("Slot") && Utilities.isNumber(Integer.class, slotSplit[1])) {
								Map<String, Object> kitMap = getValues(entrySet);

								String strType = kitMap.containsKey("Type") ? getObject(kitMap, "Type", String.class) : Material.AIR.toString();
								Material itemType = Utilities.isNumber(Integer.class, strType) ? BackwardsCompatibility.getMaterial(Integer.parseInt(strType)) : Material.getMaterial(strType);
								if (itemType == null) continue;
								String itemName = kitMap.containsKey("Name") ? getObject(kitMap, "Name", String.class) : "";
								int itemAmount = kitMap.containsKey("Amount") ? getObject(kitMap, "Amount", Integer.class) : 1;
								short itemData = kitMap.containsKey("Data") ? getObject(kitMap, "Data", Short.class) : (short) 0;

								ItemStack kitItem = new ItemStack(itemType, itemAmount, itemData);
								if (itemName != null && !itemName.isEmpty()) {
									ItemMeta itemMeta = kitItem.getItemMeta();
									if (itemMeta != null) {
										itemMeta.setDisplayName(ChatUtilities.replaceChatCodes(itemName));
										kitItem.setItemMeta(itemMeta);
									}
								}
								if (kitMap.containsKey("Dye")) {
									int itemDye = getObject(kitMap, "Dye", Integer.class);
									ItemUtilities.setDye(kitItem, itemDye);
								}
								if (kitMap.containsKey("Skin") && kitItem.getItemMeta() instanceof SkullMeta) {
									SkullMeta skullMeta = (SkullMeta) kitItem.getItemMeta();
									skullMeta.setOwner(getObject(kitMap, "Skin", String.class));
									kitItem.setItemMeta(skullMeta);
								}
								if (kitMap.containsKey("Enchantments")) {
									Map<String, Object> guiItemEnchantments = getValues(kitMap, "Enchantments");
									for (Map.Entry<String, Object> enchantmentEntrySet : guiItemEnchantments.entrySet()) {
										Enchantment enchantmentType = Utilities.isNumber(Integer.class, entrySet.getKey()) ? BackwardsCompatibility.getEnchantment(Integer.parseInt(enchantmentEntrySet.getKey())) : Enchantment.getByName(Utilities.getEnchantmentName(enchantmentEntrySet.getKey()));
										if (enchantmentType != null) {
											String enchantmentValue = enchantmentEntrySet.getValue().toString();
											int enchantmentLevel = Utilities.isNumber(Integer.class, enchantmentValue) ? Integer.parseInt(enchantmentValue) : 1;
											kitItem.addUnsafeEnchantment(enchantmentType, enchantmentLevel);
										}
									}
								}
								if (kitMap.containsKey("Lore")) {
									List<String> guiItemLore = getObject(kitMap, "Lore", List.class);
									ItemMeta guiItemMeta = kitItem.getItemMeta();
									if (guiItemMeta != null) {
										guiItemMeta.setLore(ChatUtilities.replaceChatCodes(guiItemLore));
										kitItem.setItemMeta(guiItemMeta);
									}
								}
								try {
									kitItems.put(Integer.parseInt(slotSplit[1]), kitItem);
								} catch (Exception ex) {
									System.out.println("Could not register the item at slot " + slotSplit[1] + " in the kit '" + kitName + "' due to a(n) " + ex.getClass().getSimpleName() + " error.");
								}
							}
						}
					}
					kit.setItems(kitItems);
				}
				if (kitSection.containsKey("Armour")) {
					Map<String, Object> armourItemsMap = getValues(kitSection, "Armour");
					List<ItemStack> kitArmour = new ArrayList<>();
					for (Map.Entry<String, Object> entrySet : armourItemsMap.entrySet()) {
						Map<String, Object> kitMap = getValues(entrySet);
						String strType = getObject(kitMap, "Type", String.class);
						Material itemType = Utilities.isNumber(Integer.class, strType) ? BackwardsCompatibility.getMaterial(Integer.parseInt(strType)) : Material.getMaterial(strType);
						if (itemType == null) continue;
						String itemName = kitMap.containsKey("Name") ? getObject(kitMap, "Name", String.class) : "";
						String strItemDye = kitMap.containsKey("Dye") ? kitMap.get("Dye").toString() : "-1";
						int itemDye = Utilities.isNumber(Integer.class, strItemDye) ? Integer.parseInt(strItemDye) : Utilities.getDye(strItemDye);
						int itemAmount = kitMap.containsKey("Amount") ? getObject(kitMap, "Amount", Integer.class) : 1;
						short itemData = kitMap.containsKey("Data") ? getObject(kitMap, "Data", Short.class) : (short) 0;
						ItemStack kitArmourItem = new ItemStack(itemType, itemAmount);
						kitArmourItem.setDurability(itemData);
						if (itemName != null && !itemName.isEmpty()) {
							ItemMeta itemMeta = kitArmourItem.getItemMeta();
							if (itemMeta != null) {
								itemMeta.setDisplayName(ChatUtilities.replaceChatCodes(itemName));
								kitArmourItem.setItemMeta(itemMeta);
							}
						}
						if (itemDye != -1) {
							ItemMeta itemMeta = kitArmourItem.getItemMeta();
							if (itemMeta != null && itemMeta instanceof LeatherArmorMeta) {
								LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
								leatherArmorMeta.setColor(Color.fromRGB(itemDye));
								kitArmourItem.setItemMeta(leatherArmorMeta);
							}
						}
						if (kitMap.containsKey("Skin") && kitArmourItem.getItemMeta() instanceof SkullMeta) {
							SkullMeta skullMeta = (SkullMeta) kitArmourItem.getItemMeta();
							skullMeta.setOwner(getObject(kitMap, "Skin", String.class));
							kitArmourItem.setItemMeta(skullMeta);
						}
						if (kitMap.containsKey("Enchantments")) {
							Map<String, Object> kitArmourEnchantments = getValues(kitMap, "Enchantments");
							for (Map.Entry<String, Object> enchantmentEntrySet : kitArmourEnchantments.entrySet()) {
								Enchantment enchantmentType = Utilities.isNumber(Integer.class, entrySet.getKey()) ? BackwardsCompatibility.getEnchantment(Integer.parseInt(enchantmentEntrySet.getKey())) : Enchantment.getByName(Utilities.getEnchantmentName(enchantmentEntrySet.getKey()));
								if (enchantmentType != null) {
									String enchantmentValue = enchantmentEntrySet.getValue().toString();
									int enchantmentLevel = Utilities.isNumber(Integer.class, enchantmentValue) ? Integer.parseInt(enchantmentValue) : 1;
									kitArmourItem.addUnsafeEnchantment(enchantmentType, enchantmentLevel);
								}
							}
						}
						if (kitMap.containsKey("Lore")) {
							List<String> armourItemLore = getObject(kitMap, "Lore", List.class);
							ItemMeta armourItemMeta = kitArmourItem.getItemMeta();
							if (armourItemMeta != null) {
								armourItemMeta.setLore(ChatUtilities.replaceChatCodes(armourItemLore));
								kitArmourItem.setItemMeta(armourItemMeta);
							}
						}
						kitArmour.add(kitArmourItem);
					}
					kit.setArmour(kitArmour);
				}
				if (kitSection.containsKey("Potion Effects")) {
					List<PotionEffect> potionEffectList = new ArrayList<>();
					Map<String, Object> potionEffectsMap = getValues(kitSection, "Potion Effects");
					if (potionEffectsMap != null) {
						for (Map.Entry<String, Object> potionEntrySet : potionEffectsMap.entrySet()) {
							PotionEffectType effectType = Utilities.isNumber(Integer.class, potionEntrySet.getKey()) ? PotionEffectType.getById(Integer.parseInt(potionEntrySet.getKey())) : PotionEffectType.getByName(Utilities.getPotionName(potionEntrySet.getKey()));
							if (effectType != null && (potionEntrySet.getValue() instanceof ConfigurationSection || potionEntrySet.getValue() instanceof Map)) {
								Map<String, Object> potionEntrySetMap = getValues(potionEntrySet);
								int potionLevel = potionEntrySetMap.containsKey("Level") ? getObject(potionEntrySetMap, "Level", Integer.class) : 1;
								if (potionLevel > 0) potionLevel--;
								int potionDuration = potionEntrySetMap.containsKey("Duration") ? getObject(potionEntrySetMap, "Duration", Integer.class) : Integer.MAX_VALUE;
								try {
									potionEffectList.add(new PotionEffect(effectType, potionDuration == -1 ? Integer.MAX_VALUE : potionDuration * 20, potionLevel));
								} catch (Exception ex) {
									potionEffectList.add(new PotionEffect(effectType, Integer.MAX_VALUE, potionLevel));
								}
							}
						}
						kit.setPotionEffects(potionEffectList);
					}
				}
				if (kitSection.containsKey("Killstreaks")) {
					Map<Long, List<String>> killstreakCmds = new HashMap<>();
					Map<String, Object> killstreakMap = getValues(kitSection, "Killstreaks");
					if (killstreakMap != null) {
						for (Map.Entry<String, Object> killstreakEntry : killstreakMap.entrySet()) {
							if (killstreakEntry.getValue() instanceof List) {
								String strKillstreak = killstreakEntry.getKey().startsWith("Killstreak ") ? killstreakEntry.getKey().substring("Killstreak ".length(), killstreakEntry.getKey().length()) : killstreakEntry.getKey();
								if (Utilities.isNumber(Long.class, strKillstreak)) {
									try {
										long killstreak = Long.parseLong(strKillstreak);
										List<String> cmds = (List<String>) killstreakEntry.getValue();
										if (cmds != null && !cmds.isEmpty()) killstreakCmds.put(killstreak, cmds);
									} catch (Exception ignored) {
									}
								}
							}
						}
						kit.setKillstreaks(killstreakCmds);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return kit;
	}

	public static <T> T getObject(Map<String, Object> map, String key, Class<T> unused) throws ClassCastException {
		return getObject(map, key, unused, null);
	}

	public static <T> T getObject(Map<String, Object> map, String key, Class<T> unused, T defaultValue) throws ClassCastException {
		Object value = map.getOrDefault(key, null);
		return value != null ? (unused == Long.class ? (T) ((Long) Long.parseLong(value.toString())) : (unused == Integer.class ? (T) ((Integer) Integer.parseInt(value.toString())) : (unused == Short.class ? (T) ((Short) Short.parseShort(value.toString())) : (unused == Double.class ? (T) ((Double) Double.parseDouble(value.toString())) : (unused.isInstance(value) ? (T) value : defaultValue))))) : defaultValue;
	}

	public static Map<String, Object> getValues(Map<String, Object> mainMap, String key) {
		Object object = mainMap != null ? mainMap.get(key) : null;
		return object instanceof ConfigurationSection ? ((ConfigurationSection) object).getValues(false) : (object instanceof Map ? (Map<String, Object>) object : new HashMap<>());
	}

	public static Map<String, Object> getValues(Object object) {
		return object instanceof ConfigurationSection ? ((ConfigurationSection) object).getValues(false) : (object instanceof Map ? (Map<String, Object>) object : new HashMap<>());
	}

	public static Map<String, Object> getValues(Map.Entry<String, Object> entrySet) {
		return entrySet != null ? getValues(entrySet.getValue()) : null;
	}

}
