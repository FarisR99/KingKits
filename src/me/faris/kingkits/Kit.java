package me.faris.kingkits;

import me.faris.kingkits.helpers.Utils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Kit implements Iterable<ItemStack>, ConfigurationSerializable {
    private String kitName = "";
    private double kitCost = 0D;

    private ItemStack guiItem = null;

    private List<ItemStack> kitItems = new ArrayList<ItemStack>();
    private List<ItemStack> kitArmour = new ArrayList<ItemStack>();
    private List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();

    public Kit(String kitName) {
        Validate.notNull(kitName);
        Validate.notEmpty(kitName);
        this.kitName = kitName;
        this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
    }

    public Kit(String kitName, double kitCost) {
        Validate.notNull(kitName);
        Validate.notEmpty(kitName);
        this.kitName = kitName;
        this.kitCost = kitCost;
        this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
    }

    public Kit(String kitName, List<ItemStack> kitItems) {
        Validate.notNull(kitName);
        Validate.notNull(kitItems);
        Validate.notEmpty(kitName);
        this.kitItems = kitItems;
        this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
    }

    public Kit(String kitName, List<ItemStack> kitItems, List<PotionEffect> potionEffects) {
        Validate.notNull(kitName);
        Validate.notNull(kitItems);
        Validate.notNull(potionEffects);
        Validate.notEmpty(kitName);
        this.kitItems = kitItems;
        this.potionEffects = potionEffects;
        this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
    }

    public Kit(String kitName, double kitCost, List<ItemStack> kitItems) {
        Validate.notNull(kitName);
        Validate.notNull(kitItems);
        Validate.notEmpty(kitName);
        this.kitItems = kitItems;
        this.kitCost = kitCost;
        this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
    }

    public Kit(String kitName, double kitCost, List<ItemStack> kitItems, List<PotionEffect> potionEffects) {
        Validate.notNull(kitName);
        Validate.notNull(kitItems);
        Validate.notNull(potionEffects);
        Validate.notEmpty(kitName);
        this.kitItems = kitItems;
        this.kitCost = kitCost;
        this.potionEffects = potionEffects;
        this.guiItem = new ItemStack(Material.DIAMOND_SWORD, 1);
    }

    public Kit addItem(ItemStack itemStack) {
        Validate.notNull(itemStack);
        this.kitItems.add(itemStack);
        return this;
    }

    public List<ItemStack> getArmour() {
        return Collections.unmodifiableList(this.kitArmour);
    }

    public double getCost() {
        return this.kitCost;
    }

    public ItemStack getGuiItem() {
        return this.guiItem;
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(this.kitItems);
    }

    public List<ItemStack> getMergedItems() {
        List<ItemStack> kitItems = new ArrayList<ItemStack>(this.kitItems);
        kitItems.addAll(this.kitArmour);
        return Collections.unmodifiableList(kitItems);
    }

    public String getName() {
        return this.kitName;
    }

    public List<PotionEffect> getPotionEffects() {
        return Collections.unmodifiableList(this.potionEffects);
    }

    public Kit removeItem(ItemStack itemStack) {
        Validate.notNull(itemStack);
        this.kitItems.remove(itemStack);
        return this;
    }

    public Kit setArmour(List<ItemStack> armour) {
        Validate.notNull(armour);
        this.kitArmour = armour;
        return this;
    }

    public Kit setCost(double cost) {
        this.kitCost = cost;
        return this;
    }

    public Kit setGuiItem(ItemStack guiItem) {
        Validate.notNull(guiItem);
        this.guiItem = guiItem;
        return this;
    }

    public Kit setItems(List<ItemStack> items) {
        Validate.notNull(items);
        this.kitItems = items;
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

    @SuppressWarnings("deprecation")
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedKit = new HashMap<String, Object>();
        serializedKit.put("Name", this.kitName != null ? this.kitName : "Kit" + new Random().nextInt());
        serializedKit.put("Cost", this.kitCost);

        /** GUI Item **/
        if (this.guiItem != null) {
            Map<String, Object> guiItemMap = new HashMap<String, Object>();
            guiItemMap.put("Type", this.guiItem.getType().toString());
            guiItemMap.put("Amount", this.guiItem.getAmount());
            guiItemMap.put("Data", this.guiItem.getDurability());
            // Enchantments
            Map<String, Integer> enchantmentMap = new HashMap<String, Integer>();
            for (Map.Entry<Enchantment, Integer> entrySet : this.guiItem.getEnchantments().entrySet())
                enchantmentMap.put(entrySet.getKey().getName(), entrySet.getValue());
            if (!enchantmentMap.isEmpty()) guiItemMap.put("Enchantments", enchantmentMap);
            // Lores
            if (this.guiItem.hasItemMeta() && this.guiItem.getItemMeta().hasLore())
                guiItemMap.put("Lore", this.guiItem.getItemMeta().getLore());
            serializedKit.put("GUI Item", guiItemMap);
        }

        /** Items **/
        if (this.kitItems != null && !this.kitItems.isEmpty()) {
            Map<String, Object> itemsMap = new HashMap<String, Object>();
            for (ItemStack kitItem : this.kitItems) {
                if (kitItem != null) {
                    Map<String, Object> kitItemMap = new HashMap<String, Object>();
                    String itemName = kitItem.hasItemMeta() && kitItem.getItemMeta().hasDisplayName() ? kitItem.getItemMeta().getDisplayName() : null;
                    if (itemName != null) kitItemMap.put("Name", Utils.replaceBukkitColour(itemName));
                    kitItemMap.put("Amount", kitItem.getAmount());
                    kitItemMap.put("Data", kitItem.getDurability());
                    Map<String, Integer> enchantmentMap = new HashMap<String, Integer>();
                    for (Map.Entry<Enchantment, Integer> entrySet : kitItem.getEnchantments().entrySet())
                        enchantmentMap.put(entrySet.getKey().getName(), entrySet.getValue());
                    if (!enchantmentMap.isEmpty()) kitItemMap.put("Enchantments", enchantmentMap);
                    if (kitItem.hasItemMeta() && kitItem.getItemMeta().hasLore())
                        kitItemMap.put("Lore", Utils.replaceBukkitColours(kitItem.getItemMeta().getLore()));
                    itemsMap.put(kitItem.getType().toString(), kitItemMap);
                }
            }
            serializedKit.put("Items", itemsMap);
        }

        /** Armour **/
        if (this.kitArmour != null && !this.kitArmour.isEmpty()) {
            Map<String, Object> armourMap = new HashMap<String, Object>();
            for (ItemStack kitArmour : this.kitArmour) {
                if (kitArmour != null) {
                    Map<String, Object> kitArmourMap = new HashMap<String, Object>();
                    String armourName = kitArmour.hasItemMeta() && kitArmour.getItemMeta().hasDisplayName() ? kitArmour.getItemMeta().getDisplayName() : null;
                    if (armourName != null) kitArmourMap.put("Name", Utils.replaceBukkitColour(armourName));
                    kitArmourMap.put("Type", kitArmour.getType().toString());
                    int dyeColour = Utils.ItemUtils.getDye(kitArmour);
                    if (dyeColour > 0)
                        kitArmourMap.put("Dye", dyeColour);
                    kitArmourMap.put("Data", kitArmour.getDurability());
                    Map<String, Integer> enchantmentMap = new HashMap<String, Integer>();
                    for (Map.Entry<Enchantment, Integer> entrySet : kitArmour.getEnchantments().entrySet())
                        enchantmentMap.put(entrySet.getKey().getName(), entrySet.getValue());
                    if (!enchantmentMap.isEmpty()) kitArmourMap.put("Enchantments", enchantmentMap);
                    if (kitArmour.hasItemMeta() && kitArmour.getItemMeta().hasLore())
                        kitArmourMap.put("Lore", Utils.replaceBukkitColours(kitArmour.getItemMeta().getLore()));
                    String[] armourNameSplit = kitArmour.getType().toString().contains("_") ? kitArmour.getType().toString().split("_") : null;
                    String armourNameKey = armourNameSplit != null && armourNameSplit.length > 1 ? WordUtils.capitalize(armourNameSplit[1].toLowerCase()) : WordUtils.capitalizeFully(kitArmour.getType().toString().toLowerCase()).replace("_", " ");
                    armourMap.put(armourNameKey, kitArmourMap);
                }
            }
            serializedKit.put("Armour", armourMap);
        }

        /** Potion Effects **/
        if (this.potionEffects != null && !this.potionEffects.isEmpty()) {
            Map<String, Object> potionEffectsMap = new HashMap<String, Object>();
            for (PotionEffect potionEffect : this.potionEffects) {
                Map<String, Integer> potionEffectMap = new HashMap<String, Integer>();
                potionEffectMap.put("Level", potionEffect.getAmplifier() + 1);
                potionEffectMap.put("Duration", potionEffect.getDuration() / 20);
                potionEffectsMap.put(WordUtils.capitalizeFully(potionEffect.getType().getName().toLowerCase().replace("_", " ")), potionEffectMap);
            }
            if (!potionEffectsMap.isEmpty()) serializedKit.put("Potion Effects", potionEffectsMap);
        }

        return serializedKit;
    }

    @Override
    public Kit clone() {
        return new Kit(this.kitName).setGuiItem(this.guiItem).setPotionEffects(this.potionEffects).setCost(this.kitCost).setItems(this.kitItems).setArmour(this.kitArmour);
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        return this.getMergedItems().listIterator();
    }

    @Override
    public String toString() {
        return this.serialize().toString();
    }

    public static Kit deserialize(Map<String, Object> kitSection) throws NullPointerException, ClassCastException {
        Kit kit = null;
        if (kitSection.containsKey("Name")) {
            try {
                String kitName = getObject(kitSection, "Name", String.class);
                kit = new Kit(kitName);
                if (kitSection.containsKey("Cost")) kit.setCost(getObject(kitSection, "Cost", Double.class));
                if (kitSection.containsKey("GUI Item")) {
                    Map<String, Object> guiItemMap = getValues(kitSection, "GUI Item");
                    ItemStack guiItem = null;
                    if (guiItemMap.containsKey("Type")) {
                        String strType = getObject(guiItemMap, "Type", String.class);
                        Material itemType = null;
                        if (Utils.isInteger(strType)) {
                            itemType = Material.getMaterial(Integer.parseInt(strType));
                        } else {
                            itemType = Material.getMaterial(strType);
                        }
                        if (itemType == null) itemType = Material.DIAMOND_SWORD;
                        int itemAmount = guiItemMap.containsKey("Amount") ? getObject(guiItemMap, "Amount", Integer.class) : 1;
                        short itemData = guiItemMap.containsKey("Data") ? getObject(guiItemMap, "Data", Short.class) : (short) 0;
                        guiItem = new ItemStack(itemType, itemAmount, itemData);
                        if (guiItemMap.containsKey("Enchantments")) {
                            Map<String, Object> guiItemEnchantments = getValues(guiItemMap, "Enchantments");
                            for (Map.Entry<String, Object> entrySet : guiItemEnchantments.entrySet()) {
                                Enchantment enchantmentType = Utils.isInteger(entrySet.getKey()) ? Enchantment.getById(Integer.parseInt(entrySet.getKey())) : Enchantment.getByName(Utils.getEnchantmentName(entrySet.getKey()));
                                if (enchantmentType != null) {
                                    String enchantmentValue = entrySet.getValue().toString();
                                    int enchantmentLevel = Utils.isInteger(enchantmentValue) ? Integer.parseInt(enchantmentValue) : 1;
                                    guiItem.addUnsafeEnchantment(enchantmentType, enchantmentLevel);
                                }
                            }
                        }
                        if (guiItemMap.containsKey("Lore")) {
                            List<String> guiItemLore = getObject(guiItemMap, "Lore", List.class);
                            ItemMeta guiItemMeta = guiItem.getItemMeta();
                            if (guiItemMeta != null) {
                                guiItemMeta.setLore(Utils.replaceChatColours(guiItemLore));
                                guiItem.setItemMeta(guiItemMeta);
                            }
                        }
                    }
                    if (guiItem != null) kit.setGuiItem(guiItem);
                }
                if (kitSection.containsKey("Items")) {
                    Map<String, Object> itemsMap = getValues(kitSection, "Items");
                    List<ItemStack> kitItems = new ArrayList<ItemStack>();
                    for (Map.Entry<String, Object> entrySet : itemsMap.entrySet()) {
                        Map<String, Object> kitMap = getValues(entrySet);
                        String strType = entrySet.getKey();
                        ItemStack kitItem = null;
                        Material kitMaterial = null;
                        Material itemType = null;
                        if (Utils.isInteger(strType)) {
                            itemType = Material.getMaterial(Integer.parseInt(strType));
                        } else {
                            itemType = Material.getMaterial(strType);
                        }
                        if (itemType == null) continue;
                        String itemName = kitMap.containsKey("Name") ? getObject(kitMap, "Name", String.class) : "";
                        int itemAmount = kitMap.containsKey("Amount") ? getObject(kitMap, "Amount", Integer.class) : 1;
                        short itemData = kitMap.containsKey("Data") ? getObject(kitMap, "Data", Short.class) : (short) 0;
                        kitItem = new ItemStack(itemType, itemAmount, itemData);
                        if (itemName != null && !itemName.isEmpty()) {
                            ItemMeta itemMeta = kitItem.getItemMeta();
                            if (itemMeta != null) {
                                itemMeta.setDisplayName(Utils.replaceChatColour(itemName));
                                kitItem.setItemMeta(itemMeta);
                            }
                        }
                        if (kitMap.containsKey("Enchantments")) {
                            Map<String, Object> guiItemEnchantments = getValues(kitMap, "Enchantments");
                            for (Map.Entry<String, Object> enchantmentEntrySet : guiItemEnchantments.entrySet()) {
                                Enchantment enchantmentType = Utils.isInteger(entrySet.getKey()) ? Enchantment.getById(Integer.parseInt(enchantmentEntrySet.getKey())) : Enchantment.getByName(Utils.getEnchantmentName(enchantmentEntrySet.getKey()));
                                if (enchantmentType != null) {
                                    String enchantmentValue = enchantmentEntrySet.getValue().toString();
                                    int enchantmentLevel = Utils.isInteger(enchantmentValue) ? Integer.parseInt(enchantmentValue) : 1;
                                    kitItem.addUnsafeEnchantment(enchantmentType, enchantmentLevel);
                                }
                            }
                        }
                        if (kitMap.containsKey("Lore")) {
                            List<String> guiItemLore = getObject(kitMap, "Lore", List.class);
                            ItemMeta guiItemMeta = kitItem.getItemMeta();
                            if (guiItemMeta != null) {
                                guiItemMeta.setLore(Utils.replaceChatColours(guiItemLore));
                                kitItem.setItemMeta(guiItemMeta);
                            }
                        }
                        if (kitItem != null) kitItems.add(kitItem);
                    }
                    kit.setItems(kitItems);
                }
                if (kitSection.containsKey("Armour")) {
                    Map<String, Object> armourItemsMap = getValues(kitSection, "Armour");
                    List<ItemStack> kitArmour = new ArrayList<ItemStack>();
                    for (Map.Entry<String, Object> entrySet : armourItemsMap.entrySet()) {
                        Map<String, Object> kitMap = getValues(entrySet);
                        String strType = getObject(kitMap, "Type", String.class);
                        ItemStack kitArmourItem = null;
                        Material itemType = null;
                        if (Utils.isInteger(strType)) {
                            itemType = Material.getMaterial(Integer.parseInt(strType));
                        } else {
                            itemType = Material.getMaterial(strType);
                        }
                        if (itemType == null) continue;
                        String itemName = kitMap.containsKey("Name") ? getObject(kitMap, "Name", String.class) : "";
                        String strItemDye = kitMap.containsKey("Dye") ? kitMap.get("Dye").toString() : "-1";
                        int itemDye = Utils.isInteger(strItemDye) ? Integer.parseInt(strItemDye) : Utils.getDye(strItemDye);
                        int itemAmount = kitMap.containsKey("Amount") ? getObject(kitMap, "Amount", Integer.class) : 1;
                        short itemData = kitMap.containsKey("Data") ? getObject(kitMap, "Data", Short.class) : (short) 0;
                        kitArmourItem = new ItemStack(itemType, itemAmount, itemData);
                        if (itemName != null && !itemName.isEmpty()) {
                            ItemMeta itemMeta = kitArmourItem.getItemMeta();
                            if (itemMeta != null) {
                                itemMeta.setDisplayName(Utils.replaceChatColour(itemName));
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
                        if (kitMap.containsKey("Enchantments")) {
                            Map<String, Object> kitArmourEnchantments = getValues(kitMap, "Enchantments");
                            for (Map.Entry<String, Object> enchantmentEntrySet : kitArmourEnchantments.entrySet()) {
                                Enchantment enchantmentType = Utils.isInteger(entrySet.getKey()) ? Enchantment.getById(Integer.parseInt(enchantmentEntrySet.getKey())) : Enchantment.getByName(Utils.getEnchantmentName(enchantmentEntrySet.getKey()));
                                if (enchantmentType != null) {
                                    String enchantmentValue = enchantmentEntrySet.getValue().toString();
                                    int enchantmentLevel = Utils.isInteger(enchantmentValue) ? Integer.parseInt(enchantmentValue) : 1;
                                    kitArmourItem.addUnsafeEnchantment(enchantmentType, enchantmentLevel);
                                }
                            }
                        }
                        if (kitMap.containsKey("Lore")) {
                            List<String> armourItemLore = getObject(kitMap, "Lore", List.class);
                            ItemMeta armourItemMeta = kitArmourItem.getItemMeta();
                            if (armourItemMeta != null) {
                                armourItemMeta.setLore(Utils.replaceChatColours(armourItemLore));
                                kitArmourItem.setItemMeta(armourItemMeta);
                            }
                        }
                        if (kitArmourItem != null) kitArmour.add(kitArmourItem);
                    }
                    kit.setArmour(kitArmour);
                }
                if (kitSection.containsKey("Potion Effects")) {
                    List<PotionEffect> potionEffectList = new ArrayList<PotionEffect>();
                    Map<String, Object> potionEffectsMap = getValues(kitSection, "Potion Effects");
                    for (Map.Entry<String, Object> potionEntrySet : potionEffectsMap.entrySet()) {
                        PotionEffectType effectType = Utils.isInteger(potionEntrySet.getKey()) ? PotionEffectType.getById(Integer.parseInt(potionEntrySet.getKey())) : PotionEffectType.getByName(Utils.getPotionName(potionEntrySet.getKey()));
                        if (effectType != null && (potionEntrySet.getValue() instanceof ConfigurationSection || potionEntrySet.getValue() instanceof Map)) {
                            Map<String, Object> potionEntrySetMap = getValues(potionEntrySet);
                            int potionLevel = potionEntrySetMap.containsKey("Level") ? getObject(potionEntrySetMap, "Level", Integer.class) : 1;
                            if (potionLevel > 0) potionLevel--;
                            int potionDuration = potionEntrySetMap.containsKey("Duration") ? getObject(potionEntrySetMap, "Duration", Integer.class) : Integer.MAX_VALUE;
                            try {
                                potionEffectList.add(new PotionEffect(effectType, potionDuration * 20, potionLevel));
                            } catch (Exception ex) {
                                potionEffectList.add(new PotionEffect(effectType, Integer.MAX_VALUE, potionLevel));
                            }
                        }
                    }
                    kit.setPotionEffects(potionEffectList);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return kit;
    }

    @SuppressWarnings("unused")
    private static <T> T getObject(Map<String, Object> map, String key, Class<T> unused) throws ClassCastException {
        try {
            T value = map.containsKey(key) ? (T) map.get(key) : null;
            return value != null ? (unused == Integer.class ? (T) ((Integer) Integer.parseInt(value.toString())) : (unused == Short.class ? (T) ((Short) Short.parseShort(value.toString())) : value)) : null;
        } catch (ClassCastException ex) {
            throw ex;
        }
    }

    private static Map<String, Object> getValues(Map<String, Object> mainMap, String key) {
        Object object = mainMap != null ? mainMap.get(key) : null;
        return object instanceof ConfigurationSection ? ((ConfigurationSection) object).getValues(false) : (object instanceof Map ? (Map<String, Object>) object : new HashMap<String, Object>());
    }

    private static Map<String, Object> getValues(Map.Entry<String, Object> entrySet) {
        Object object = entrySet != null ? entrySet.getValue() : null;
        return object instanceof ConfigurationSection ? ((ConfigurationSection) object).getValues(false) : (object instanceof Map ? (Map<String, Object>) object : new HashMap<String, Object>());
    }

}
