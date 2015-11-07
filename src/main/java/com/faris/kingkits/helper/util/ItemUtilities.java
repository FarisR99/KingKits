package com.faris.kingkits.helper.util;

import org.bukkit.*;
import org.bukkit.block.banner.*;
import org.bukkit.enchantments.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.*;
import org.bukkit.potion.*;

import java.util.*;

public class ItemUtilities {

	private static Class classCraftItemFactory = null;

	private static List<Material> damageableList = new ArrayList<Material>() {{
		for (Material material : Material.values()) {
			String materialName = material.name();
			if (materialName.endsWith("_PICKAXE") || materialName.endsWith("_AXE") || materialName.endsWith("_SWORD") || materialName.endsWith("_SPADE") || materialName.endsWith("_HOE")) {
				this.add(material);
			} else if (materialName.endsWith("_HELMET") || materialName.endsWith("_CHESTPLATE") || materialName.endsWith("_LEGGINGS") || materialName.endsWith("_BOOTS")) {
				this.add(material);
			}
			this.add(Material.FLINT_AND_STEEL);
			this.add(Material.FISHING_ROD);
		}
	}};

	private ItemUtilities() {
	}

	public static ItemStack addLores(ItemStack itemStack, List<String> lore) {
		if (itemStack != null) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) itemMeta = createMeta(itemStack.getType());
			if (itemMeta != null) {
				itemMeta.setLore(ChatUtilities.replaceChatCodes(lore));
				itemStack.setItemMeta(itemMeta);
			}
		}
		return itemStack;
	}

	public static ItemMeta createMeta(Material itemType) {
		try {
			if (classCraftItemFactory == null)
				classCraftItemFactory = ReflectionUtilities.getBukkitClass("CraftItemStack");
			ReflectionUtilities.MethodInvoker methodGetInstance = ReflectionUtilities.getMethod(classCraftItemFactory, "instance");
			if (methodGetInstance != null) {
				Object objCraftItemFactory = methodGetInstance.invoke(null);
				if (objCraftItemFactory != null) {
					ReflectionUtilities.MethodInvoker methodGetItemMeta = ReflectionUtilities.getMethod(classCraftItemFactory, "getItemMeta", Material.class);
					return (ItemMeta) methodGetItemMeta.invoke(objCraftItemFactory, itemType);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static ItemStack createPotion(PotionEffectType potionEffectType, int level, boolean splash) {
		Potion potion = new Potion(PotionType.getByEffect(potionEffectType));
		potion.setLevel(level);
		potion.setSplash(splash);
		return potion.toItemStack(1);
	}

	public static List<Material> getDamageableMaterials() {
		return new ArrayList<>(damageableList);
	}

	public static ItemStack deserializeItem(Map<String, Object> serializedItem) {
		return deserializeItem(serializedItem, null);
	}

	public static ItemStack deserializeItem(Map<String, Object> serializedItem, ItemStack defaultItem) {
		ItemStack deserializedItem = defaultItem != null ? defaultItem.clone() : null;
		if (serializedItem != null) {
			String strMaterial = ObjectUtilities.getObject(serializedItem, String.class, "Type", "");
			Material material = Material.matchMaterial(strMaterial);
			if (material != null) {
				deserializedItem = new ItemStack(material);

				int amount = ObjectUtilities.getObject(serializedItem, Integer.class, "Amount", 1);
				if (amount > 0) deserializedItem.setAmount(amount);

				if (serializedItem.containsKey("Data")) {
					byte data = ObjectUtilities.getObject(serializedItem, Byte.class, "Data", (byte) 0);

					MaterialData itemMaterialData = deserializedItem.getData();
					if (itemMaterialData == null) itemMaterialData = new MaterialData(material);
					itemMaterialData.setData(data);
					deserializedItem.setData(itemMaterialData);
				}
				if (serializedItem.containsKey("Durability"))
					deserializedItem.setDurability(ObjectUtilities.getObject(serializedItem, Short.class, "Durability", (short) 0));
				if (serializedItem.get("Potion") != null) {
					Map<String, Object> serializedPotion = ObjectUtilities.getMap(serializedItem.get("Potion"));
					if (serializedPotion != null && serializedPotion.containsKey("Type")) {
						PotionType potionType = PotionType.getByEffect(PotionEffectType.getByName(ObjectUtilities.getObject(serializedPotion, String.class, "Type").toUpperCase().replace(' ', '_')));
						if (potionType != null) {
							Potion potion = new Potion(potionType);
							if (serializedPotion.containsKey("Extended") && !potionType.isInstant()) {
								try {
									potion.setHasExtendedDuration(ObjectUtilities.getObject(serializedPotion, Boolean.class, "Extended"));
								} catch (Exception ignored) {
								}
							}
							if (serializedPotion.containsKey("Level")) {
								try {
									int maxLevel = potion.getType().getMaxLevel();
									if (maxLevel > 0) {
										int level = ObjectUtilities.getObject(serializedPotion, Integer.class, "Level");
										if (level > 0) potion.setLevel(Math.min(level, maxLevel));
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							if (serializedPotion.containsKey("Splash")) {
								try {
									potion.setSplash(ObjectUtilities.getObject(serializedPotion, Boolean.class, "Splash"));
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							potion.apply(deserializedItem);
						}
					}
				}

				if (serializedItem.get("Enchantments") != null) {
					Map<String, Object> enchantmentsMap = ObjectUtilities.getMap(serializedItem.get("Enchantments"));
					if (enchantmentsMap != null) {
						for (Map.Entry<String, Object> enchantmentEntry : enchantmentsMap.entrySet()) {
							Enchantment enchantment = Enchantment.getByName(Utilities.getEnchantmentName(enchantmentEntry.getKey()));
							if (enchantment != null && Utilities.isNumber(Integer.class, enchantmentEntry.getValue()))
								deserializedItem.addUnsafeEnchantment(enchantment, Integer.parseInt(enchantmentEntry.getValue().toString()));
						}
					}
				}

				if (!deserializedItem.hasItemMeta())
					deserializedItem.setItemMeta(ItemUtilities.createMeta(material));
				ItemMeta itemMeta = deserializedItem.getItemMeta();
				if (itemMeta != null) {
					if (serializedItem.get("Item flags") instanceof List) {
						List<String> strItemFlags = Utilities.toStringList(ObjectUtilities.getObject(serializedItem, List.class, "Item flags"));
						List<ItemFlag> itemFlags = new LinkedList<>();
						for (String strItemFlag : strItemFlags) {
							try {
								for (ItemFlag itemFlag : ItemFlag.values()) {
									if (itemFlag.name().replace('_', ' ').equalsIgnoreCase(strItemFlag.replace('_', ' '))) {
										itemFlags.add(itemFlag);
										break;
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (!itemFlags.isEmpty())
							itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[itemFlags.size()]));
					}
					if (itemMeta instanceof LeatherArmorMeta && serializedItem.containsKey("Dye")) {
						LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
						if (serializedItem.get("Dye") instanceof String) {
							int dyeRGB = Utilities.getDye(ObjectUtilities.getObject(serializedItem, String.class, "Dye"));
							leatherArmorMeta.setColor(Color.fromRGB(Math.min(Math.max(dyeRGB, 0), 255)));
						} else if (serializedItem.get("Dye") instanceof Integer) {
							leatherArmorMeta.setColor(Color.fromRGB(Math.min(Math.max(ObjectUtilities.getObject(serializedItem, Integer.class, "Dye", 0), 0), 255)));
						} else {
							Map<String, Object> deserializedDyeColor = ObjectUtilities.getMap(serializedItem.get("Dye"));
							leatherArmorMeta.setColor(Color.fromRGB(ObjectUtilities.getObject(deserializedDyeColor, Integer.class, "Red", 0), ObjectUtilities.getObject(deserializedDyeColor, Integer.class, "Green", 0), ObjectUtilities.getObject(deserializedDyeColor, Integer.class, "Blue", 0)));
						}
					}
					if (itemMeta instanceof BannerMeta && serializedItem.containsKey("Banner")) {
						BannerMeta bannerMeta = (BannerMeta) itemMeta;
						Map<String, Object> serializedBanner = ObjectUtilities.getMap(serializedItem.get("Banner"));
						if (serializedBanner.containsKey("Base")) {
							Color deserializedBaseColor = Utilities.deserializeColor(ObjectUtilities.getMap(serializedBanner.get("Base")));
							if (deserializedBaseColor != null)
								bannerMeta.setBaseColor(DyeColor.getByColor(deserializedBaseColor));
						}
						if (serializedBanner.containsKey("Patterns")) {
							List<Pattern> patterns = new ArrayList<>();
							Map<String, Object> serializedPatterns = ObjectUtilities.getMap(serializedItem.get("Patterns"));
							for (Object objSerializedPattern : serializedPatterns.values()) {
								Map<String, Object> serializedPattern = ObjectUtilities.getMap(objSerializedPattern);
								Pattern pattern = null;
								PatternType patternType = null;
								if (serializedPattern.get("Pattern") != null) {
									String strPatternType = ObjectUtilities.toString(serializedPattern.get("Pattern"));
									patternType = PatternType.getByIdentifier(strPatternType);
									if (patternType != null) {
										for (PatternType aPatternType : PatternType.values()) {
											if (aPatternType.name().replace('_', ' ').equalsIgnoreCase(strPatternType)) {
												patternType = aPatternType;
												break;
											}
										}
									}
								}
								if (patternType != null && serializedPattern.get("Color") != null) {
									Color deserializedPatternColor = Utilities.deserializeColor(ObjectUtilities.getMap(serializedPattern.get("Color")));
									if (deserializedPatternColor != null)
										pattern = new Pattern(DyeColor.getByColor(deserializedPatternColor), patternType);
								}
								if (pattern != null) patterns.add(pattern);
							}
							bannerMeta.setPatterns(patterns);
						}
					}
					if (itemMeta instanceof BookMeta && serializedItem.containsKey("Book")) {
						BookMeta bookMeta = (BookMeta) itemMeta;
						Map<String, Object> serializedBook = ObjectUtilities.getMap(serializedItem.get("Book"));
						if (serializedBook.containsKey("Title"))
							bookMeta.setTitle(ChatUtilities.replaceChatCodes(ObjectUtilities.getObject(serializedBook, String.class, "Title")));
						if (serializedBook.containsKey("Author"))
							bookMeta.setAuthor(ObjectUtilities.getObject(serializedBook, String.class, "Author"));
						if (serializedBook.containsKey("Pages"))
							bookMeta.setPages(ChatUtilities.replaceChatCodes(Utilities.toStringList(ObjectUtilities.getObject(serializedBook, List.class, "Pages"))));
					}
					if (itemMeta instanceof MapMeta && serializedItem.containsKey("Map scaling")) {
						MapMeta mapMeta = (MapMeta) itemMeta;
						boolean mapScaling = ObjectUtilities.getObject(serializedItem, Boolean.class, "Map scaling");
						mapMeta.setScaling(mapScaling);
					}
					if (itemMeta instanceof SkullMeta && serializedItem.containsKey("Skull")) {
						SkullMeta skullMeta = (SkullMeta) itemMeta;
						String ownerUsername = ObjectUtilities.toString(serializedItem.get("Skull"));
						skullMeta.setOwner(ownerUsername);
					}
					if (serializedItem.get("Name") != null)
						itemMeta.setDisplayName(ChatUtilities.replaceChatCodes(serializedItem.get("Name").toString()));
					if (serializedItem.get("Lore") instanceof List)
						itemMeta.setLore(ChatUtilities.replaceChatCodes(Utilities.toStringList(ObjectUtilities.getObject(serializedItem, List.class, "Lore"))));
					deserializedItem.setItemMeta(itemMeta);
				}
			}
		}
		return deserializedItem;
	}

	public static String getFriendlyName(Material material) {
		return material == null ? getFriendlyName(Material.AIR) : StringUtilities.capitalizeFully(material.name().replace('_', ' '));
	}

	public static String getName(ItemStack itemStack) {
		if (itemStack != null) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			return itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : StringUtilities.capitalizeFully(itemStack.getType().name().replace('_', ' '));
		}
		return "";
	}

	public static boolean hasName(ItemStack itemStack) {
		if (itemStack != null) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			return itemMeta != null && itemMeta.hasDisplayName();
		}
		return false;
	}

	public static boolean isNull(ItemStack itemStack) {
		return itemStack == null || itemStack.getType() == Material.AIR;
	}

	public static ItemStack renameItem(ItemStack itemStack, String name) {
		if (itemStack != null) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) itemMeta = createMeta(itemStack.getType());
			if (itemMeta != null) {
				itemMeta.setDisplayName(ChatUtilities.replaceChatCodes("&f" + name));
				itemStack.setItemMeta(itemMeta);
			}
		}
		return itemStack;
	}

	private static Map<String, Object> serializeBanner(BannerMeta bannerMeta) {
		Map<String, Object> serializedBanner = new LinkedHashMap<>();
		if (bannerMeta != null) {
			DyeColor baseDyeColor = bannerMeta.getBaseColor();
			if (baseDyeColor != null) serializedBanner.put("Base", Utilities.serializeColor(baseDyeColor.getColor()));
			List<Pattern> bannerPatterns = bannerMeta.getPatterns();
			if (bannerPatterns != null && !bannerPatterns.isEmpty()) {
				Map<String, Map<String, Object>> serializedPatterns = new LinkedHashMap<>();
				int patternIndex = 1;
				for (Pattern bannerPattern : bannerPatterns) {
					Map<String, Object> serializedPattern = new LinkedHashMap<>();
					if (bannerPattern.getColor() != null)
						serializedPattern.put("Color", Utilities.serializeColor(bannerPattern.getColor().getColor()));
					serializedPattern.put("Pattern", StringUtilities.capitalizeFully(bannerPattern.getPattern().name().replace('_', ' ')));
					serializedPatterns.put("Pattern " + patternIndex++, serializedPattern);
				}
				serializedBanner.put("Patterns", serializedPatterns);
			}
		}
		return serializedBanner;
	}

	private static Map<String, Object> serializeBook(BookMeta bookMeta) {
		Map<String, Object> serializedBook = new LinkedHashMap<>();
		if (bookMeta != null) {
			if (bookMeta.hasTitle()) serializedBook.put("Title", ChatUtilities.replaceChatColours(bookMeta.getTitle()));
			if (bookMeta.hasAuthor()) serializedBook.put("Author", bookMeta.getAuthor());
			if (bookMeta.hasPages()) serializedBook.put("Pages", ChatUtilities.replaceChatColours(bookMeta.getPages()));
		}
		return serializedBook;
	}

	public static Map<String, Object> serializeItem(final ItemStack itemStack) {
		Map<String, Object> serializedItem = new LinkedHashMap<>();
		if (itemStack != null) {
			serializedItem.put("Type", getFriendlyName(itemStack.getType()));
			if (itemStack.getAmount() != 1) serializedItem.put("Amount", itemStack.getAmount());
			if (itemStack.getData().getData() != 0) serializedItem.put("Data", itemStack.getData().getData());
			if (itemStack.getDurability() != 0) serializedItem.put("Durability", itemStack.getDurability());
			if (itemStack.getItemMeta() != null) {
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta.hasDisplayName())
					serializedItem.put("Name", ChatUtilities.replaceChatColours(itemMeta.getDisplayName()));
				if (itemMeta.hasLore())
					serializedItem.put("Lore", ChatUtilities.replaceChatColours(itemMeta.getLore()));
				if (itemMeta instanceof LeatherArmorMeta) {
					Color dyeColour = ((LeatherArmorMeta) itemMeta).getColor();
					serializedItem.put("Dye", Utilities.serializeColor(dyeColour));
				}
				if (itemMeta instanceof BannerMeta) {
					BannerMeta bannerMeta = (BannerMeta) itemMeta;
					serializedItem.put("Banner", serializeBanner(bannerMeta));
				}
				if (itemMeta instanceof BookMeta) {
					BookMeta bookMeta = (BookMeta) itemMeta;
					serializedItem.put("Book", serializeBook(bookMeta));
				}
				if (itemMeta instanceof MapMeta) {
					MapMeta mapMeta = (MapMeta) itemMeta;
					serializedItem.put("Map scaling", mapMeta.isScaling());
				}
				if (itemMeta instanceof SkullMeta) {
					String ownerUsername = ((SkullMeta) itemMeta).getOwner();
					if (ownerUsername != null) serializedItem.put("Skull", ownerUsername);
				}
			}
			if (itemStack.getType() == Material.POTION) {
				try {
					Potion potion;
					short damage = itemStack.getDurability();
					PotionType type = PotionType.getByDamageValue(damage & 15);
					if (type != null && type != PotionType.WATER) {
						int level = (damage & 32) >> 5;
						++level;
						potion = new Potion(type, level);
					} else {
						potion = new Potion(damage & 63);
					}
					if ((damage & 16384) > 0) potion = potion.splash();

					if ((type != null && (type != PotionType.INSTANT_DAMAGE || type == PotionType.FIRE_RESISTANCE)) && !type.isInstant() && (damage & 64) > 0) {
						try {
							potion = potion.extend();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					Map<String, Object> serializedPotion = new LinkedHashMap<>();
					if (potion != null) {
						PotionEffectType potionEffectType = null;
						if (potion.getType() != null) potionEffectType = potion.getType().getEffectType();
						else if (!potion.getEffects().isEmpty())
							potionEffectType = new ArrayList<>(potion.getEffects()).get(0).getType();
						if (potionEffectType != null)
							serializedPotion.put("Type", StringUtilities.capitalizeFully(potionEffectType.getName().replace('_', ' ')));
						serializedPotion.put("Level", potion.getLevel());
						serializedPotion.put("Splash", potion.isSplash());
						serializedPotion.put("Extended", potion.hasExtendedDuration());
						serializedItem.put("Potion", serializedPotion);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (!itemStack.getEnchantments().isEmpty()) {
				serializedItem.put("Enchantments", new LinkedHashMap<String, Integer>() {{
					for (Map.Entry<Enchantment, Integer> enchantmentEntry : itemStack.getEnchantments().entrySet())
						this.put(StringUtilities.capitalizeFully(enchantmentEntry.getKey().getName().replace('_', ' ')), enchantmentEntry.getValue());
				}});
			}
			if (itemStack.getItemMeta() != null) {
				final Set<ItemFlag> itemFlags = itemStack.getItemMeta().getItemFlags();
				if (itemFlags != null && !itemFlags.isEmpty()) {
					serializedItem.put("Item flags", new LinkedList<String>() {{
						for (ItemFlag itemFlag : itemFlags)
							this.add(StringUtilities.capitalizeFully(itemFlag.name().replace('_', ' ')));
					}});
				}
			}
		} else {
			serializedItem.put("Type", getFriendlyName(null));
		}
		return serializedItem;
	}

	public static ItemStack setDye(ItemStack itemStack, int dyeRGB) {
		if (itemStack != null) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) itemMeta = createMeta(itemStack.getType());
			if (itemMeta != null) {
				if (itemMeta instanceof LeatherArmorMeta) {
					LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
					leatherArmorMeta.setColor(Color.fromRGB(dyeRGB));
					itemStack.setItemMeta(leatherArmorMeta);
				} else {
					itemStack.setItemMeta(itemMeta);
				}
			}
		}
		return itemStack;
	}

}
