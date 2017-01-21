package com.faris.kingkits.helper.util;

import com.comphenix.attributes.Attributes;
import com.comphenix.attributes.Attributes.Attribute;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

public class ItemUtilities {

	private static Class classCraftItemFactory = null;

	static {
		classCraftItemFactory = ReflectionUtilities.getBukkitClass("CraftItemStack");
	}

	private static List<Material> damageableList = new ArrayList<Material>() {{
		for (Material material : Material.values()) {
			String materialName = material.name();
			if (materialName.endsWith("_PICKAXE") || materialName.endsWith("_AXE") || materialName.endsWith("_SWORD") || materialName.endsWith("_SPADE") || materialName.endsWith("_HOE")) {
				this.add(material);
			} else if (materialName.endsWith("_HELMET") || materialName.endsWith("_CHESTPLATE") || materialName.endsWith("_LEGGINGS") || materialName.endsWith("_BOOTS")) {
				this.add(material);
			}
		}
		this.add(Material.BOW);
		this.add(Material.SHIELD);
		this.add(Material.FLINT_AND_STEEL);
		this.add(Material.FISHING_ROD);
		this.add(Material.SHEARS);
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

				int amount = ObjectUtilities.getObject(serializedItem, Number.class, "Amount", 1).intValue();
				if (amount > 0) deserializedItem.setAmount(amount);

				if (serializedItem.containsKey("Durability"))
					deserializedItem.setDurability(ObjectUtilities.getObject(serializedItem, Number.class, "Durability", (short) 0).shortValue());

				if (!deserializedItem.hasItemMeta())
					deserializedItem.setItemMeta(ItemUtilities.createMeta(material));

				if (serializedItem.get("Attributes") != null) {
					Map<String, Object> attributesMap = ObjectUtilities.getMap(serializedItem.get("Attributes"));
					List<Attribute> attributesList = new ArrayList<>();
					for (Object serializedAttribute : attributesMap.values()) {
						Attribute deserializedAttribute = Attribute.deserialize(ObjectUtilities.getMap(serializedAttribute));
						if (deserializedAttribute != null) attributesList.add(deserializedAttribute);
					}
					if (!attributesList.isEmpty()) {
						Attributes itemAttributes = new Attributes(deserializedItem);
						for (Attribute attribute : attributesList) {
							itemAttributes.add(attribute);
						}
						deserializedItem = itemAttributes.getStack();
					}
				}
				if (serializedItem.get("Enchantments") != null) {
					Map<String, Object> enchantmentsMap = ObjectUtilities.getMap(serializedItem.get("Enchantments"));
					for (Map.Entry<String, Object> enchantmentEntry : enchantmentsMap.entrySet()) {
						Enchantment enchantment = Enchantment.getByName(Utilities.getEnchantmentName(enchantmentEntry.getKey()));
						if (enchantment != null && Utilities.isNumber(Integer.class, enchantmentEntry.getValue()))
							deserializedItem.addUnsafeEnchantment(enchantment, Integer.parseInt(enchantmentEntry.getValue().toString()));
					}
				}

				// NBT Tags
				if (material == Material.EGG && serializedItem.get("Egg") != null) {
					try {
						deserializedItem = NBTUtilities.setEgg(deserializedItem, ObjectUtilities.getObject(serializedItem, String.class, "Egg"));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				ItemMeta itemMeta = deserializedItem.getItemMeta();
				if (itemMeta != null) {
					if (serializedItem.get("Item flags") instanceof List) {
						List<String> strItemFlags = Utilities.toStringList(ObjectUtilities.getObject(serializedItem, List.class, "Item flags"));
						List<ItemFlag> itemFlags = new LinkedList<>();
						for (String strItemFlag : strItemFlags) {
							try {
								if (strItemFlag.equals("*")) {
									itemFlags.clear();
									for (ItemFlag itemFlag : ItemFlag.values()) {
										if (itemFlag != null) itemFlags.add(itemFlag);
									}
									break;
								} else {
									strItemFlag = strItemFlag.toUpperCase().replace(' ', '_');
									for (ItemFlag itemFlag : ItemFlag.values()) {
										if (itemFlag != null && itemFlag.name().equals(strItemFlag)) {
											itemFlags.add(itemFlag);
											break;
										}
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (!itemFlags.isEmpty())
							itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[itemFlags.size()]));
					}
					if (itemMeta instanceof PotionMeta) {
						PotionMeta potionMeta = (PotionMeta) itemMeta;
						if (serializedItem.containsKey("Potion")) {
							Map<String, Object> serializedPotion = ObjectUtilities.getMap(serializedItem.get("Potion"));
							if (!serializedPotion.isEmpty()) {
								PotionData deserializedPotion = deserializePotionData(serializedPotion);
								if (deserializedPotion != null) potionMeta.setBasePotionData(deserializedPotion);
							}
						}
						if (serializedItem.containsKey("Custom potion effects")) {
							Map<String, Object> serializedCustomEffects = ObjectUtilities.getMap(serializedItem.get("Custom potion effects"));
							if (!serializedCustomEffects.isEmpty()) {
								for (Object serializedCustomEffect : serializedCustomEffects.values()) {
									PotionEffect deserializedPotionEffect = deserializePotionEffect(ObjectUtilities.getMap(serializedCustomEffect));
									if (deserializedPotionEffect != null)
										potionMeta.addCustomEffect(deserializedPotionEffect, false);
								}
							}
						}
					}
					if (itemMeta instanceof LeatherArmorMeta && serializedItem.containsKey("Dye")) {
						LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
						if (serializedItem.get("Dye") instanceof String) {
							int dyeRGB = Utilities.getDye(ObjectUtilities.getObject(serializedItem, String.class, "Dye"));
							leatherArmorMeta.setColor(Color.fromRGB(Math.min(Math.max(dyeRGB, 0), 255)));
						} else if (serializedItem.get("Dye") instanceof Number) {
							leatherArmorMeta.setColor(Color.fromRGB(Math.min(Math.max(ObjectUtilities.getObject(serializedItem, Number.class, "Dye", 0).intValue(), 0), 255)));
						} else {
							Map<String, Object> deserializedDyeColor = ObjectUtilities.getMap(serializedItem.get("Dye"));
							leatherArmorMeta.setColor(Color.fromRGB(ObjectUtilities.getObject(deserializedDyeColor, Number.class, "Red", 0).intValue(), ObjectUtilities.getObject(deserializedDyeColor, Number.class, "Green", 0).intValue(), ObjectUtilities.getObject(deserializedDyeColor, Number.class, "Blue", 0).intValue()));
						}
					}
					if (itemMeta instanceof BannerMeta && serializedItem.containsKey("Banner")) {
						BannerMeta bannerMeta = (BannerMeta) itemMeta;
						Map<String, Object> serializedBanner = ObjectUtilities.getMap(serializedItem.get("Banner"));
						if (serializedBanner.containsKey("Base")) {
							Color deserializedBaseColor = Utilities.deserializeColor(ObjectUtilities.getMap(serializedBanner.get("Base")));
							if (deserializedBaseColor != null) {
								bannerMeta.setBaseColor(DyeColor.getByColor(deserializedBaseColor));
							}
						}
						if (serializedBanner.containsKey("Patterns")) {
							List<Pattern> patterns = new ArrayList<>();
							Map<String, Object> serializedPatterns = ObjectUtilities.getMap(serializedBanner.get("Patterns"));
							for (Object objSerializedPattern : serializedPatterns.values()) {
								Map<String, Object> serializedPattern = ObjectUtilities.getMap(objSerializedPattern);
								Pattern pattern = null;
								PatternType patternType = null;
								if (serializedPattern.get("Pattern") != null) {
									String strPatternType = ObjectUtilities.toString(serializedPattern.get("Pattern"));
									patternType = PatternType.getByIdentifier(strPatternType);
									if (patternType == null) {
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
									if (deserializedPatternColor != null) {
										pattern = new Pattern(DyeColor.getByColor(deserializedPatternColor), patternType);
									}
								}
								if (pattern != null) patterns.add(pattern);
							}
							bannerMeta.setPatterns(patterns);
						}
					}
					if (itemMeta instanceof BlockStateMeta && serializedItem.containsKey("Block state")) {
						BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
						if (blockStateMeta.getBlockState() instanceof Banner) {
							Banner banner = (Banner) blockStateMeta.getBlockState();
							Map<String, Object> serializedBanner = ObjectUtilities.getMap(serializedItem.get("Block state"));
							if (serializedBanner.containsKey("Base")) {
								Color deserializedBaseColor = Utilities.deserializeColor(ObjectUtilities.getMap(serializedBanner.get("Base")));
								if (deserializedBaseColor != null) {
									banner.setBaseColor(DyeColor.getByColor(deserializedBaseColor));
								}
							}
							if (serializedBanner.containsKey("Patterns")) {
								List<Pattern> patterns = new ArrayList<>();
								Map<String, Object> serializedPatterns = ObjectUtilities.getMap(serializedBanner.get("Patterns"));
								for (Object objSerializedPattern : serializedPatterns.values()) {
									Map<String, Object> serializedPattern = ObjectUtilities.getMap(objSerializedPattern);
									Pattern pattern = null;
									PatternType patternType = null;
									if (serializedPattern.get("Pattern") != null) {
										String strPatternType = ObjectUtilities.toString(serializedPattern.get("Pattern"));
										patternType = PatternType.getByIdentifier(strPatternType);
										if (patternType == null) {
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
										if (deserializedPatternColor != null) {
											pattern = new Pattern(DyeColor.getByColor(deserializedPatternColor), patternType);
										}
									}
									if (pattern != null) patterns.add(pattern);
								}
								banner.setPatterns(patterns);
							}
							banner.update();
							blockStateMeta.setBlockState(banner);
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
					if (itemMeta instanceof Repairable && serializedItem.containsKey("Repair cost")) {
						Repairable repairable = (Repairable) itemMeta;
						repairable.setRepairCost(ObjectUtilities.getObject(serializedItem, Number.class, "Repair cost", 0).intValue());
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

	public static PotionData deserializePotionData(Map<String, Object> serializedPotionData) {
		if (serializedPotionData != null && serializedPotionData.containsKey("Type")) {
			PotionType potionType = Utilities.getPotionType(ObjectUtilities.getObject(serializedPotionData, String.class, "Type").toUpperCase().replace(' ', '_'));
			if (potionType != null) {
				boolean extended = ObjectUtilities.getObject(serializedPotionData, Boolean.class, "Extended", false);
				boolean upgraded = ObjectUtilities.getObject(serializedPotionData, Boolean.class, "Upgraded", false);
				return new PotionData(potionType, extended, upgraded);
			}
		}
		return null;
	}

	public static PotionEffect deserializePotionEffect(Map<String, Object> serializedPotion) {
		if (serializedPotion != null && serializedPotion.containsKey("Type")) {
			PotionEffectType potionEffectType = PotionEffectType.getByName(Utilities.getPotionName(ObjectUtilities.getObject(serializedPotion, String.class, "Type")));
			if (potionEffectType != null) {
				int level = ObjectUtilities.getObject(serializedPotion, Number.class, "Level", 0).intValue();
				double duration = ObjectUtilities.getObject(serializedPotion, Number.class, "Duration", -1).doubleValue();
				boolean ambient = ObjectUtilities.getObject(serializedPotion, Boolean.class, "Ambient", false);
				boolean particles = ObjectUtilities.getObject(serializedPotion, Boolean.class, "Particles", true);
				Color color = serializedPotion.containsKey("Color") ? Color.fromRGB(ObjectUtilities.getObject(serializedPotion, Number.class, "Color", Color.GRAY.asRGB()).intValue()) : null;
				return new PotionEffect(potionEffectType, duration != -1D ? (int) (duration * 20D) : Integer.MAX_VALUE, Math.max(level, 0), ambient, particles, color);
			}
		}
		return null;
	}

	/**
	 * Get the core contents of an inventory.
	 *
	 * @param inventory The inventory
	 * @return The contents of the inventory.
	 * @deprecated Use {@link org.bukkit.inventory.Inventory#getStorageContents()}
	 */
	public static ItemStack[] getContents(Inventory inventory) {
		if (inventory != null) {
			if (inventory instanceof PlayerInventory) {
				PlayerInventory playerInventory = (PlayerInventory) inventory;
				ItemStack[] itemContents = playerInventory.getContents();
				if (itemContents == null) itemContents = new ItemStack[36];
				if (itemContents.length > 36) {
					ItemStack[] actualContents = new ItemStack[itemContents.length];
					System.arraycopy(itemContents, 0, actualContents, 0, 36);
					return actualContents;
				} else {
					return itemContents;
				}
			} else {
				return inventory.getContents();
			}
		} else {
			return new ItemStack[36];
		}
	}

	public static List<Material> getDamageableMaterials() {
		return new ArrayList<>(damageableList);
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

	private static Map<String, Object> serializeBanner(Banner banner) {
		Map<String, Object> serializedBanner = new LinkedHashMap<>();
		if (banner != null) {
			DyeColor baseDyeColor = banner.getBaseColor();
			if (baseDyeColor != null) {
				serializedBanner.put("Base", Utilities.serializeColor(baseDyeColor.getColor()));
			}
			List<Pattern> bannerPatterns = banner.getPatterns();
			if (bannerPatterns != null && !bannerPatterns.isEmpty()) {
				Map<String, Map<String, Object>> serializedPatterns = new LinkedHashMap<>();
				for (int patternIndex = 0; patternIndex < bannerPatterns.size(); patternIndex++) {
					Pattern bannerPattern = bannerPatterns.get(patternIndex);
					Map<String, Object> serializedPattern = new LinkedHashMap<>();
					if (bannerPattern.getColor() != null) {
						serializedPattern.put("Color", Utilities.serializeColor(bannerPattern.getColor().getColor()));
					}
					serializedPattern.put("Pattern", StringUtilities.capitalizeFully(bannerPattern.getPattern().name().replace('_', ' ')));
					serializedPatterns.put("Pattern " + (patternIndex + 1), serializedPattern);
				}
				serializedBanner.put("Patterns", serializedPatterns);
			}
		}
		return serializedBanner;
	}

	private static Map<String, Object> serializeBanner(BannerMeta bannerMeta) {
		Map<String, Object> serializedBanner = new LinkedHashMap<>();
		if (bannerMeta != null) {
			DyeColor baseDyeColor = bannerMeta.getBaseColor();
			if (baseDyeColor != null) {
				serializedBanner.put("Base", Utilities.serializeColor(baseDyeColor.getColor()));
			}
			List<Pattern> bannerPatterns = bannerMeta.getPatterns();
			if (bannerPatterns != null && !bannerPatterns.isEmpty()) {
				Map<String, Map<String, Object>> serializedPatterns = new LinkedHashMap<>();
				int patternIndex = 1;
				for (Pattern bannerPattern : bannerPatterns) {
					Map<String, Object> serializedPattern = new LinkedHashMap<>();
					if (bannerPattern.getColor() != null) {
						serializedPattern.put("Color", Utilities.serializeColor(bannerPattern.getColor().getColor()));
					}
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
			if (itemStack.getDurability() != 0) serializedItem.put("Durability", itemStack.getDurability());
			if (itemStack.getItemMeta() != null) {
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta.hasDisplayName())
					serializedItem.put("Name", ChatUtilities.replaceChatColours(itemMeta.getDisplayName()));
				if (itemMeta.hasLore())
					serializedItem.put("Lore", ChatUtilities.replaceChatColours(itemMeta.getLore()));
				if (itemMeta instanceof PotionMeta) {
					PotionMeta potionMeta = (PotionMeta) itemMeta;
					if (potionMeta.getBasePotionData() != null)
						serializedItem.put("Potion", serializePotionData(potionMeta.getBasePotionData()));
					if (potionMeta.hasCustomEffects()) {
						Map<String, Map<String, Object>> serializedCustomEffects = new LinkedHashMap<>();
						List<PotionEffect> potionEffects = potionMeta.getCustomEffects();
						for (int i = 0; i < potionEffects.size(); i++) {
							serializedCustomEffects.put("Potion " + (i + 1), serializePotionEffect(potionEffects.get(i)));
						}
						if (!serializedCustomEffects.isEmpty()) {
							serializedItem.put("Custom potion effects", serializedCustomEffects);
						}
					}
				}
				if (itemMeta instanceof LeatherArmorMeta) {
					Color dyeColour = ((LeatherArmorMeta) itemMeta).getColor();
					serializedItem.put("Dye", Utilities.serializeColor(dyeColour));
				}
				if (itemMeta instanceof BannerMeta) {
					BannerMeta bannerMeta = (BannerMeta) itemMeta;
					serializedItem.put("Banner", serializeBanner(bannerMeta));
				}
				if (itemMeta instanceof BlockStateMeta) {
					BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
					BlockState state = blockStateMeta.getBlockState();
					if (state instanceof Banner) {
						Banner bannerState = (Banner) state;
						serializedItem.put("Block state", serializeBanner(bannerState));
					}
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
				if (itemMeta instanceof Repairable) {
					Repairable repairable = (Repairable) itemMeta;
					if (repairable.hasRepairCost()) serializedItem.put("Repair cost", repairable.getRepairCost());
				}
			}

			// NBT Tags
			if (itemStack.getType() == Material.MONSTER_EGG) {
				try {
					String strEggType = NBTUtilities.getEgg(itemStack);
					if (strEggType != null) serializedItem.put("Egg", strEggType);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			final List<Attribute> itemAttributes = new Attributes(itemStack).getAll();
			if (!itemAttributes.isEmpty()) {
				Map<Integer, Map<String, Object>> serializedAttributes = new LinkedHashMap<>();
				for (int i = 0; i < itemAttributes.size(); i++)
					serializedAttributes.put(i, itemAttributes.get(i).serialize());
				serializedItem.put("Attributes", serializedAttributes);
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

	public static Map<String, Object> serializePotionData(PotionData potionData) {
		Map<String, Object> serializedPotionData = new LinkedHashMap<>();
		if (potionData != null) {
			serializedPotionData.put("Type", StringUtilities.capitalizeFully(potionData.getType().toString().replace('_', ' ')));
			serializedPotionData.put("Extended", potionData.isExtended());
			serializedPotionData.put("Upgraded", potionData.isUpgraded());
		}
		return serializedPotionData;
	}

	public static Map<String, Object> serializePotionEffect(PotionEffect potionEffect) {
		Map<String, Object> serializedPotionEffect = new LinkedHashMap<>();
		if (potionEffect != null) {
			serializedPotionEffect.put("Type", StringUtilities.capitalizeFully(potionEffect.getType().getName().replace('_', ' ')));
			serializedPotionEffect.put("Level", potionEffect.getAmplifier());
			serializedPotionEffect.put("Duration", potionEffect.getDuration() == Integer.MAX_VALUE ? -1D : (double) potionEffect.getDuration() / 20D);
			serializedPotionEffect.put("Ambient", potionEffect.isAmbient());
			serializedPotionEffect.put("Particles", potionEffect.hasParticles());
			if (potionEffect.getColor() != null) serializedPotionEffect.put("Color", potionEffect.getColor().asRGB());
		}
		return serializedPotionEffect;
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
