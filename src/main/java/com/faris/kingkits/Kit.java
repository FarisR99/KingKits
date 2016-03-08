package com.faris.kingkits;

import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.helper.json.JsonSerializable;
import com.faris.kingkits.helper.util.*;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Kit implements Cloneable, ConfigurationSerializable, JsonSerializable {

	// Kit variables
	private String kitName;
	private List<String> kitDescription = new ArrayList<>();
	private double kitCost = ConfigController.getInstance().getKitDefaultCost();
	private double kitCooldown = ConfigController.getInstance().getKitDefaultCooldown();
	private List<String> kitCommands = ConfigController.getInstance().getKitDefaultCommands();
	private boolean userKit = false;

	// GUI variables
	private int guiPosition = -1;
	private ItemStack guiItem = null;

	// Kit contents
	private Map<Integer, ItemStack> kitItems = new HashMap<>();
	private ItemStack[] kitArmour = new ItemStack[4];
	private List<PotionEffect> potionEffects = new ArrayList<>();
	private Map<Integer, List<String>> killstreakCommands = ConfigController.getInstance().getKitDefaultKillstreakCommands();
	private ItemStack offHand = null;

	// Other
	private boolean commandAlias = ConfigController.getInstance().getKitDefaultCommandAlias();
	private boolean itemBreaking = ConfigController.getInstance().getKitDefaultBreakableItems();
	private double maxHealth = ConfigController.getInstance().getKitDefaultMaxHealth();
	private float walkSpeed = ConfigController.getInstance().getKitDefaultWalkSpeed();
	private int autoUnlockScore = -1;

	public Kit(String kitName) {
		Validate.notNull(kitName);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
	}

	public Kit(String kitName, double kitCost) {
		Validate.notNull(kitName);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitCost = kitCost;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
	}

	public Kit(String kitName, Map<Integer, ItemStack> kitItems) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
	}

	public Kit(String kitName, Map<Integer, ItemStack> kitItems, List<PotionEffect> potionEffects) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notNull(potionEffects);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.potionEffects = potionEffects;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
	}

	public Kit(String kitName, Map<Integer, ItemStack> kitItems, ItemStack[] kitArmour, List<PotionEffect> potionEffects, ItemStack offHand) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notNull(kitArmour);
		Validate.notNull(potionEffects);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.kitArmour = kitArmour;
		this.potionEffects = potionEffects;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
		if(offHand != null) {
			this.offHand = offHand;
		}
	}

	public Kit(String kitName, double kitCost, Map<Integer, ItemStack> kitItems) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.kitCost = kitCost;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
	}

	public Kit(String kitName, double kitCost, Map<Integer, ItemStack> kitItems, List<PotionEffect> potionEffects) {
		Validate.notNull(kitName);
		Validate.notNull(kitItems);
		Validate.notNull(potionEffects);
		Validate.notEmpty(kitName);
		this.kitName = kitName;
		this.kitItems = kitItems;
		this.kitCost = kitCost;
		this.potionEffects = potionEffects;
		this.guiItem = new ItemStack(Material.DIAMOND_SWORD);
	}

	public boolean canItemsBreak() {
		return this.itemBreaking;
	}

	@Override
	public Kit clone() {
		try {
			return (Kit) super.clone();
		} catch (Exception ex) {
			ex.printStackTrace();

			Kit kit = new Kit(this.kitName);
			kit.setUserKit(this.isUserKit());
			kit.setAlias(this.hasCommandAlias());
			kit.setAutoUnlockScore(this.getAutoUnlockScore());
			kit.setCost(this.getCost());
			kit.setCooldown(this.getCooldown());
			kit.setDescription(this.getDescription());
			kit.setGuiItem(this.getGuiItem());
			kit.setGuiPosition(this.getGuiPosition());
			kit.setItemsBreakable(this.canItemsBreak());
			kit.setCommands(this.getCommands());
			kit.setKillstreakCommands(this.getKillstreaks());
			kit.setMaxHealth(this.getMaxHealth());
			kit.setWalkSpeed(this.getWalkSpeed());
			kit.setItems(this.getItems());
			kit.setArmour(this.getArmour());
			kit.setPotionEffects(this.getPotionEffects());

			return kit;
		}
	}

	public ItemStack[] getArmour() {
		return Arrays.copyOf(this.kitArmour, this.kitArmour.length);
	}

	public int getAutoUnlockScore() {
		return this.autoUnlockScore;
	}

	public List<String> getCommands() {
		return this.kitCommands;
	}

	public double getCooldown() {
		return this.kitCooldown;
	}

	public double getCost() {
		return this.kitCost;
	}

	public List<String> getDescription() {
		return this.kitDescription;
	}

	public ItemStack getGuiItem() {
		return this.guiItem;
	}

	public int getGuiPosition() {
		return this.guiPosition;
	}
	
	public ItemStack getOffHand() {
		if(offHand != null) {
			return offHand;
		} else {
			return new ItemStack(Material.AIR);
		}
	}

	public Map<Integer, ItemStack> getItems() {
		return Collections.unmodifiableMap(this.kitItems);
	}

	public Map<Integer, List<String>> getKillstreaks() {
		return this.killstreakCommands;
	}

	public double getMaxHealth() {
		return this.maxHealth;
	}

	public String getName() {
		return this.kitName;
	}

	public List<PotionEffect> getPotionEffects() {
		return Collections.unmodifiableList(this.potionEffects);
	}

	public float getWalkSpeed() {
		return this.walkSpeed;
	}

	public boolean hasCommandAlias() {
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

	public Kit setAlias(boolean commandAlias) {
		this.commandAlias = commandAlias;
		return this;
	}

	public Kit setArmour(ItemStack[] armour) {
		if (armour != null) this.kitArmour = armour;
		return this;
	}

	public Kit setAutoUnlockScore(int autoUnlockScore) {
		this.autoUnlockScore = autoUnlockScore < 0 ? -1 : autoUnlockScore;
		return this;
	}

	public Kit setCommands(List<String> commands) {
		Validate.notNull(commands);
		this.kitCommands = commands;
		return this;
	}

	public Kit setCooldown(double cooldown) {
		if (this.kitCooldown >= 0D) this.kitCooldown = cooldown;
		return this;
	}
	
	public Kit setOffHand(ItemStack offHand) {
		this.offHand = offHand;
		return this;
	}

	public Kit setCost(double cost) {
		this.kitCost = Math.abs(cost);
		return this;
	}

	public Kit setDescription(List<String> description) {
		if (description != null) this.kitDescription = description;
		return this;
	}

	public Kit setGuiItem(ItemStack guiItem) {
		this.guiItem = guiItem != null ? guiItem : new ItemStack(Material.AIR);
		return this;
	}

	public Kit setGuiPosition(int guiPosition) {
		this.guiPosition = guiPosition > 0 ? guiPosition : -1;
		return this;
	}

	public Kit setItems(Map<Integer, ItemStack> items) {
		if (items != null) this.kitItems = items;
		return this;
	}

	public Kit setItemsBreakable(boolean breakableItems) {
		this.itemBreaking = breakableItems;
		return this;
	}

	public Kit setKillstreakCommands(Map<Integer, List<String>> killstreaks) {
		if (killstreaks != null) this.killstreakCommands = killstreaks;
		return this;
	}

	public Kit setMaxHealth(double maxHealth) {
		this.maxHealth = maxHealth;
		return this;
	}

	public Kit setName(String name) {
		Validate.notNull(name);
		Validate.notEmpty(name);
		this.kitName = name;
		return this;
	}

	public Kit setPotionEffects(List<PotionEffect> potionEffects) {
		Validate.notNull(potionEffects);
		this.potionEffects = potionEffects;
		return this;
	}

	public Kit setUserKit(boolean isUserKit) {
		this.userKit = isUserKit;
		return this;
	}

	public Kit setWalkSpeed(float speed) {
		this.walkSpeed = Math.min(Math.max(speed, 0.01F), 1F);
		return this;
	}

	@Override
	public String toString() {
		return this.serialize().toString();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serializedMap = new LinkedHashMap<>();
		serializedMap.put("Name", this.kitName);
		serializedMap.put("Command alias", this.commandAlias);
		serializedMap.put("Description", ChatUtilities.replaceChatColours(this.kitDescription));
		serializedMap.put("Cost", this.kitCost);
		serializedMap.put("Cooldown", this.kitCooldown);
		serializedMap.put("Max health", this.maxHealth);
		serializedMap.put("Walk speed", this.walkSpeed);
		serializedMap.put("Auto-unlock score", this.autoUnlockScore);
		serializedMap.put("Breakable items", this.itemBreaking);
		serializedMap.put("Commands", this.kitCommands);
		if(offHand != null) {
			serializedMap.put("offHand", ItemUtilities.serializeItem(this.offHand));
		}
		serializedMap.put("Killstreak commands", new LinkedHashMap<String, List<String>>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			for (Map.Entry<Integer, List<String>> killstreakCommandsEntry : killstreakCommands.entrySet())
				this.put("Killstreak " + killstreakCommandsEntry.getKey(), killstreakCommandsEntry.getValue());
		}});
		serializedMap.put("GUI", new LinkedHashMap<String, Object>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			this.put("Position", guiPosition);
			this.put("Item", ItemUtilities.serializeItem(guiItem));
		}});
		serializedMap.put("Items", new LinkedHashMap<String, Map<String, Object>>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			for (Map.Entry<Integer, ItemStack> itemEntry : kitItems.entrySet()) {
				if (!ItemUtilities.isNull(itemEntry.getValue()))
					this.put("Slot " + itemEntry.getKey(), ItemUtilities.serializeItem(itemEntry.getValue()));
			}
		}});
		serializedMap.put("Armour", new LinkedHashMap<String, Map<String, Object>>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			if (!ItemUtilities.isNull(kitArmour[3])) this.put("Helmet", ItemUtilities.serializeItem(kitArmour[3]));
			if (!ItemUtilities.isNull(kitArmour[2])) this.put("Chestplate", ItemUtilities.serializeItem(kitArmour[2]));
			if (!ItemUtilities.isNull(kitArmour[1])) this.put("Leggings", ItemUtilities.serializeItem(kitArmour[1]));
			if (!ItemUtilities.isNull(kitArmour[0])) this.put("Boots", ItemUtilities.serializeItem(kitArmour[0]));
		}});
		serializedMap.put("Potion effects", new LinkedHashMap<Integer, Map<String, Object>>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			for (int i = 0; i < potionEffects.size(); i++)
				this.put((i + 1), serializePotionEffect(potionEffects.get(i)));
		}});
		return serializedMap;
	}

	public static Kit deserialize(final Map<String, Object> serializedKit) {
		final Kit deserializedKit;
		if (serializedKit != null && serializedKit.get("Name") != null) {
			deserializedKit = new Kit(ObjectUtilities.getObject(serializedKit, String.class, "Name"));
			if (serializedKit.containsKey("Command alias"))
				deserializedKit.setAlias(ObjectUtilities.getObject(serializedKit, Boolean.class, "Command alias", false));
			if (serializedKit.containsKey("Description"))
				deserializedKit.setDescription(Utilities.toStringList(ObjectUtilities.getObject(serializedKit, List.class, "Description")));
			if (serializedKit.containsKey("Cost"))
				deserializedKit.setCost(ObjectUtilities.getObject(serializedKit, Number.class, "Cost", 0D).doubleValue());
			if (serializedKit.containsKey("Cooldown"))
				deserializedKit.setCooldown(ObjectUtilities.getObject(serializedKit, Number.class, "Cooldown", 0D).doubleValue());
			if (serializedKit.containsKey("Max health"))
				deserializedKit.setMaxHealth(ObjectUtilities.getObject(serializedKit, Number.class, "Max health", PlayerUtilities.getDefaultMaxHealth()).doubleValue());
			if (serializedKit.containsKey("Walk speed"))
				deserializedKit.setWalkSpeed(ObjectUtilities.getObject(serializedKit, Number.class, "Walk speed", PlayerUtilities.getDefaultWalkSpeed()).floatValue());
			if (serializedKit.containsKey("Auto-unlock score"))
				deserializedKit.setAutoUnlockScore(ObjectUtilities.getObject(serializedKit, Number.class, "Auto-unlock score", -1).intValue());
			if (serializedKit.containsKey("Breakable items"))
				deserializedKit.setItemsBreakable(ObjectUtilities.getObject(serializedKit, Boolean.class, "Breakable items", true));
			if (serializedKit.containsKey("offHand"))
				deserializedKit.setOffHand(ItemUtilities.deserializeItem(ObjectUtilities.getMap(serializedKit.get("offHand"))));
			if (serializedKit.containsKey("Commands"))
				deserializedKit.setCommands(Utilities.toStringList(ObjectUtilities.getObject(serializedKit, List.class, "Commands", new ArrayList<Object>())));
			if (serializedKit.containsKey("Killstreak commands")) {
				Map<Integer, List<String>> killstreaksCommands = new HashMap<>();
				Map<String, Object> killstreakMap = ObjectUtilities.getMap(serializedKit.get("Killstreak commands"));
				for (Map.Entry<String, Object> killstreakEntry : killstreakMap.entrySet()) {
					try {
						if (killstreakEntry.getValue() instanceof List) {
							String strKillstreak = killstreakEntry.getKey().startsWith("Killstreak ") ? killstreakEntry.getKey().substring(11) : killstreakEntry.getKey();
							if (Utilities.isNumber(Integer.class, strKillstreak)) {
								int killstreak = Integer.parseInt(strKillstreak);
								List<String> commands = Utilities.toStringList((List<?>) killstreakEntry.getValue());
								if (commands != null && !commands.isEmpty())
									killstreaksCommands.put(killstreak, commands);
							}
						} else if (killstreakEntry.getValue() instanceof String) {
							String strKillstreak = killstreakEntry.getKey().startsWith("Killstreak ") ? killstreakEntry.getKey().substring(11) : killstreakEntry.getKey();
							if (Utilities.isNumber(Integer.class, strKillstreak)) {
								int killstreak = Integer.parseInt(strKillstreak);
								String command = (String) killstreakEntry.getValue();
								if (command != null && !command.isEmpty())
									killstreaksCommands.put(killstreak, new ArrayList<>(Collections.singletonList(command)));
							}
						}
					} catch (Exception ignored) {
					}
				}
				deserializedKit.setKillstreakCommands(killstreaksCommands);
			}
			if (serializedKit.containsKey("GUI")) {
				Map<String, Object> guiSection = ObjectUtilities.getMap(serializedKit.get("GUI"));
				if (guiSection.containsKey("Position"))
					deserializedKit.setGuiPosition(ObjectUtilities.getObject(guiSection, Integer.class, "Position", -1));
				if (guiSection.containsKey("Item")) {
					ItemStack guiItem = ItemUtilities.deserializeItem(ObjectUtilities.getMap(guiSection.get("Item")));
					if (guiItem == null) guiItem = new ItemStack(Material.DIAMOND_SWORD);
					ItemMeta guiItemMeta = guiItem.getItemMeta();
					if (guiItemMeta != null) {
						List<String> guiItemLore = guiItemMeta.getLore();
						if (guiItemLore != null) {
							for (int i = 0; i < guiItemLore.size(); i++) {
								String guiItemLoreLine = guiItemLore.get(i);
								guiItemLoreLine = guiItemLoreLine.replace("<cooldown>", String.valueOf(deserializedKit.getCooldown()));
								guiItemLoreLine = guiItemLoreLine.replace("<cost>", String.valueOf(deserializedKit.getCost()));
								guiItemLoreLine = guiItemLoreLine.replace("<maxhealth>", String.valueOf(deserializedKit.getMaxHealth()));
								guiItemLoreLine = guiItemLoreLine.replace("<walkspeed>", String.valueOf(deserializedKit.getWalkSpeed()));
								guiItemLore.set(i, guiItemLoreLine);
							}
							guiItemMeta.setLore(guiItemLore);
							guiItem.setItemMeta(guiItemMeta);
						}
					}
					deserializedKit.setGuiItem(guiItem);
				}
			}
			if (serializedKit.containsKey("Items")) {
				Map<Integer, ItemStack> kitItems = new LinkedHashMap<>();
				Map<String, Object> itemsSection = ObjectUtilities.getMap(serializedKit.get("Items"));
				for (Map.Entry<String, Object> itemEntry : itemsSection.entrySet()) {
					ItemStack deserializedItem = ItemUtilities.deserializeItem(ObjectUtilities.getMap(itemEntry.getValue()));
					if (deserializedItem != null) {
						String strSlot = itemEntry.getKey().startsWith("Slot ") ? itemEntry.getKey().substring(5) : itemEntry.getKey();
						if (Utilities.isNumber(Integer.class, strSlot)) {
							int slotNumber = Integer.parseInt(strSlot);
							if (slotNumber >= 0) kitItems.put(slotNumber, deserializedItem);
						}
					}
				}
				deserializedKit.setItems(kitItems);
			}
			if (serializedKit.containsKey("Armour")) {
				ItemStack[] kitArmour = new ItemStack[4];
				Map<String, Object> armourSection = ObjectUtilities.getMap(serializedKit.get("Armour"));
				if (armourSection.containsKey("Helmet")) {
					ItemStack helmetItem = ItemUtilities.deserializeItem(ObjectUtilities.getMap(armourSection.get("Helmet")));
					if (helmetItem != null) kitArmour[3] = helmetItem;
				}
				if (armourSection.containsKey("Chestplate")) {
					ItemStack chestplateItem = ItemUtilities.deserializeItem(ObjectUtilities.getMap(armourSection.get("Chestplate")));
					if (chestplateItem != null) kitArmour[2] = chestplateItem;
				}
				if (armourSection.containsKey("Leggings")) {
					ItemStack leggingsItem = ItemUtilities.deserializeItem(ObjectUtilities.getMap(armourSection.get("Leggings")));
					if (leggingsItem != null) kitArmour[1] = leggingsItem;
				}
				if (armourSection.containsKey("Boots")) {
					ItemStack bootsItem = ItemUtilities.deserializeItem(ObjectUtilities.getMap(armourSection.get("Boots")));
					if (bootsItem != null) kitArmour[0] = bootsItem;
				}
				deserializedKit.setArmour(kitArmour);
			}
			if (serializedKit.containsKey("Potion effects")) {
				Map<String, Object> potionEffectsSection = ObjectUtilities.getMap(serializedKit.get("Potion effects"));
				List<PotionEffect> potionEffects = new ArrayList<>();
				for (Object potionEffectSection : potionEffectsSection.values()) {
					Map<String, Object> serializedPotionEffect = ObjectUtilities.getMap(potionEffectSection);
					PotionEffect deserializedPotionEffect = deserializePotionEffect(serializedPotionEffect);
					if (deserializedPotionEffect != null) potionEffects.add(deserializedPotionEffect);
				}
				deserializedKit.setPotionEffects(potionEffects);
			}
		} else {
			deserializedKit = null;
		}
		return deserializedKit;
	}

	@Override
	public String serializeToJson() {
		return Utilities.getGsonParser().toJson(this);
	}

	public static Kit deserializeFromJson(String serializedKitToJson) {
		return Utilities.getGsonParser().fromJson(new JsonPrimitive(serializedKitToJson), Kit.class); // Utilities.getGsonParser().fromJson(Utilities.getGsonParser().fromJson(serializedKitToJson, JsonObject.class), Kit.class);
	}

	public static Map<String, Object> serializePotionEffect(PotionEffect potionEffect) {
		Map<String, Object> serializedPotionEffect = new LinkedHashMap<>();
		if (potionEffect != null) {
			serializedPotionEffect.put("Type", StringUtilities.capitalizeFully(potionEffect.getType().getName().replace('_', ' ')));
			serializedPotionEffect.put("Level", potionEffect.getAmplifier());
			serializedPotionEffect.put("Duration", potionEffect.getDuration() == Integer.MAX_VALUE ? -1 : potionEffect.getDuration());
			if (!potionEffect.isAmbient()) serializedPotionEffect.put("Ambient", false);
			if (!potionEffect.hasParticles()) serializedPotionEffect.put("Particles", false);
		}
		return serializedPotionEffect;
	}

	public static PotionEffect deserializePotionEffect(Map<String, Object> serializedPotion) {
		if (serializedPotion != null && serializedPotion.containsKey("Type")) {
			PotionEffectType potionEffectType = PotionEffectType.getByName(ObjectUtilities.getObject(serializedPotion, String.class, "Type").toUpperCase().replace(' ', '_'));
			if (potionEffectType != null) {
				int level = ObjectUtilities.getObject(serializedPotion, Number.class, "Level", 0).intValue();
				int duration = ObjectUtilities.getObject(serializedPotion, Number.class, "Duration", -1).intValue();
				boolean ambient = ObjectUtilities.getObject(serializedPotion, Boolean.class, "Ambient", true);
				boolean particles = ObjectUtilities.getObject(serializedPotion, Boolean.class, "Particles", true);
				return new PotionEffect(potionEffectType, duration != -1 ? duration * 20 : Integer.MAX_VALUE, Math.max(level, 0), ambient, particles);
			}
		}
		return null;
	}

}
