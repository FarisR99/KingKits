package com.faris;

import com.faris.kingkits.helper.util.ReflectionUtilities;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BackwardsCompatibility {

	private static boolean beforeV1_13 = false;

	private static Material mushroomSoup = null;
	private static Material monsterEgg = null;
	private static Material bookAndQuill = null;
	private static Material emptyMap = null;
	private static Material map = null;

	private static List<Material> monsterEggs = new ArrayList<>();

	private static ReflectionUtilities.ConstructorInvoker constructorPotionEffect = null;

	private static ReflectionUtilities.MethodInvoker methodGetMaterial = null;
	private static ReflectionUtilities.MethodInvoker methodGetById = null;

	static {
		final String bukkitVersion = ReflectionUtilities.getBukkitVersion();
		final String[] versionSplit = bukkitVersion.split("_");
		final String prefix = versionSplit[0];
		final int version = Integer.parseInt(versionSplit[1]);

		beforeV1_13 = prefix.equals("v1") && version >= 7 && version <= 12;

		if (beforeV1_13) {
			mushroomSoup = (Material) ReflectionUtilities.getEnum(Material.class, "MUSHROOM_SOUP");
			monsterEgg = (Material) ReflectionUtilities.getEnum(Material.class, "MONSTER_EGG");
			bookAndQuill = (Material) ReflectionUtilities.getEnum(Material.class, "BOOK_AND_QUILL");
			emptyMap = (Material) ReflectionUtilities.getEnum(Material.class, "EMPTY_MAP");
			map = (Material) ReflectionUtilities.getEnum(Material.class, "MAP");

			if (monsterEgg == null) throw new IllegalStateException("Monster egg material is null");
			monsterEggs.add(monsterEgg);

			constructorPotionEffect = ReflectionUtilities.getConstructor(PotionEffect.class, PotionEffectType.class, int.class, int.class, boolean.class, boolean.class, Color.class);

			methodGetMaterial = ReflectionUtilities.getMethod(Material.class, "getMaterial", int.class);
			methodGetById = ReflectionUtilities.getMethod(Enchantment.class, "getById", int.class);
		} else {
			mushroomSoup = (Material) ReflectionUtilities.getEnum(Material.class, "MUSHROOM_STEW");
			bookAndQuill = (Material) ReflectionUtilities.getEnum(Material.class, "WRITABLE_BOOK");
			emptyMap = (Material) ReflectionUtilities.getEnum(Material.class, "MAP");
			map = (Material) ReflectionUtilities.getEnum(Material.class, "FILLED_MAP");

			constructorPotionEffect = ReflectionUtilities.getConstructor(PotionEffect.class, PotionEffectType.class, int.class, int.class, boolean.class, boolean.class, boolean.class);

			Class<?> creatureClass = ReflectionUtilities.getBukkitClass("entity.CraftCreature");
			if (creatureClass != null) {
				for (Material material : Material.values()) {
					if (material.name().endsWith("_SPAWN_EGG")) {
						String mobClassName = WordUtils.capitalizeFully(material.name().substring(0, material.name().length() - "_SPAWN_EGG".length()), new char[]{'_'}).replace("_", "");
						Class<?> mobClass = ReflectionUtilities.getBukkitClass("entity.Craft" + mobClassName);
						if (mobClass != null) {
							if (creatureClass.isAssignableFrom(mobClass)) monsterEggs.add(material);
						}
					}
				}
			}
		}
	}

	public static PotionEffect createPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color) {
		if (beforeV1_13) {
			try {
				return (PotionEffect) constructorPotionEffect.newInstance(type, duration, amplifier, ambient, particles, color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				return (PotionEffect) constructorPotionEffect.newInstance(type, duration, amplifier, ambient, particles, particles);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new PotionEffect(type, duration, amplifier, ambient);
	}

	public static Material getBookAndQuill() {
		return bookAndQuill;
	}

	public static Material getEmptyMap() {
		return emptyMap;
	}

	public static Enchantment getEnchantment(int enchantmentId) {
		if (methodGetById != null) {
			try {
				return (Enchantment) methodGetById.invoke(null, enchantmentId);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public static Material getMap() {
		return map;
	}

	public static Material getMaterial(int typeId) {
		if (methodGetMaterial != null) {
			try {
				return (Material) methodGetMaterial.invoke(null, typeId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			for (Material material : Material.values()) {
				if (material.getId() == typeId) return material;
			}
		}
		return null;
	}

	public static Material getMonsterEgg() {
		return monsterEgg;
	}

	public static List<Material> getMonsterEggs() {
		return monsterEggs;
	}

	public static Material getMushroomSoup() {
		return mushroomSoup;
	}

	public static boolean isBeforeV1_13() {
		return beforeV1_13;
	}

}
