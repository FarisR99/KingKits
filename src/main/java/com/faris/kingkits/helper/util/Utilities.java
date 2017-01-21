package com.faris.kingkits.helper.util;

import com.faris.kingkits.Kit;
import com.faris.kingkits.controller.ConfigController;
import com.faris.kingkits.helper.json.JsonKitSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.math.BigDecimal;
import java.util.*;

public class Utilities {

	private static Gson gson = null;

	private static Comparator<String> ALPHANUMERICAL_ORDER = new Comparator<String>() {
		public int compare(String str1, String str2) {
			int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
			if (res == 0) res = str1.compareTo(str2);
			return res;
		}
	};

	private Utilities() {
	}

	public static Color deserializeColor(Map<String, Object> serializedColor) {
		Color deserializedColor = Color.BLACK;
		try {
			deserializedColor = Color.fromRGB(ObjectUtilities.getObject(serializedColor, Number.class, "Red", 0).intValue(), ObjectUtilities.getObject(serializedColor, Number.class, "Green", 0).intValue(), ObjectUtilities.getObject(serializedColor, Number.class, "Blue", 0).intValue());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return deserializedColor;
	}

	public static Comparator<String> getAlphanumericalComparator() {
		return ALPHANUMERICAL_ORDER;
	}

	public static int getDye(String friendlyName) {
		if (friendlyName != null) {
			if (friendlyName.equalsIgnoreCase("Aqua")) return Color.AQUA.asRGB();
			else if (friendlyName.equalsIgnoreCase("Black")) return Color.BLACK.asRGB();
			else if (friendlyName.equalsIgnoreCase("Blue")) return Color.BLUE.asRGB();
			else if (friendlyName.equalsIgnoreCase("Fuchsia")) return Color.FUCHSIA.asRGB();
			else if (friendlyName.equalsIgnoreCase("Gray") || friendlyName.equalsIgnoreCase("Grey"))
				return Color.GRAY.asRGB();
			else if (friendlyName.equalsIgnoreCase("Green")) return Color.GREEN.asRGB();
			else if (friendlyName.equalsIgnoreCase("Lime")) return Color.LIME.asRGB();
			else if (friendlyName.equalsIgnoreCase("Maroon")) return Color.MAROON.asRGB();
			else if (friendlyName.equalsIgnoreCase("Navy")) return Color.NAVY.asRGB();
			else if (friendlyName.equalsIgnoreCase("Olive")) return Color.OLIVE.asRGB();
			else if (friendlyName.equalsIgnoreCase("Orange")) return Color.ORANGE.asRGB();
			else if (friendlyName.equalsIgnoreCase("Purple")) return Color.PURPLE.asRGB();
			else if (friendlyName.equalsIgnoreCase("Red")) return Color.RED.asRGB();
			else if (friendlyName.equalsIgnoreCase("Silver")) return Color.SILVER.asRGB();
			else if (friendlyName.equalsIgnoreCase("Teal")) return Color.TEAL.asRGB();
			else if (friendlyName.equalsIgnoreCase("White")) return Color.WHITE.asRGB();
			else if (friendlyName.equalsIgnoreCase("Yellow")) return Color.YELLOW.asRGB();
		}
		return -1;
	}

	public static String getEnchantmentName(String friendlyName) {
		if (friendlyName != null) {
			if (friendlyName.equalsIgnoreCase("Sharpness") || friendlyName.equalsIgnoreCase("Sharp"))
				return Enchantment.DAMAGE_ALL.getName();
			else if (friendlyName.equalsIgnoreCase("Bane of Arthropods") || friendlyName.equalsIgnoreCase("Arthropods") || friendlyName.equalsIgnoreCase("Bane") || friendlyName.equalsIgnoreCase("Arthro"))
				return Enchantment.DAMAGE_ARTHROPODS.getName();
			else if (friendlyName.equalsIgnoreCase("Smite") || friendlyName.equalsIgnoreCase("Undead"))
				return Enchantment.DAMAGE_UNDEAD.getName();
			else if (friendlyName.equalsIgnoreCase("Power"))
				return Enchantment.ARROW_DAMAGE.getName();
			else if (friendlyName.equalsIgnoreCase("Flame") || friendlyName.equalsIgnoreCase("Flames"))
				return Enchantment.ARROW_FIRE.getName();
			else if (friendlyName.equalsIgnoreCase("Infinite") || friendlyName.equalsIgnoreCase("Infinity"))
				return Enchantment.ARROW_INFINITE.getName();
			else if (friendlyName.equalsIgnoreCase("Punch") || friendlyName.equalsIgnoreCase("Push"))
				return Enchantment.ARROW_KNOCKBACK.getName();
			else if (friendlyName.equalsIgnoreCase("Efficiency") || friendlyName.equalsIgnoreCase("Eff"))
				return Enchantment.DIG_SPEED.getName();
			else if (friendlyName.equalsIgnoreCase("Unbreaking") || friendlyName.equalsIgnoreCase("Durability") || friendlyName.equalsIgnoreCase("Dura"))
				return Enchantment.DURABILITY.getName();
			else if (friendlyName.equalsIgnoreCase("Fire Aspect") || friendlyName.equalsIgnoreCase("Fire"))
				return Enchantment.FIRE_ASPECT.getName();
			else if (friendlyName.equalsIgnoreCase("Knockback") || friendlyName.equalsIgnoreCase("Knock"))
				return Enchantment.KNOCKBACK.getName();
			else if (friendlyName.equalsIgnoreCase("Fortune") || friendlyName.equalsIgnoreCase("Fort"))
				return Enchantment.LOOT_BONUS_BLOCKS.getName();
			else if (friendlyName.equalsIgnoreCase("Looting") || friendlyName.equalsIgnoreCase("Loot"))
				return Enchantment.LOOT_BONUS_MOBS.getName();
			else if (friendlyName.equalsIgnoreCase("Luck") || friendlyName.equalsIgnoreCase("Luck of the Sea"))
				return Enchantment.LUCK.getName();
			else if (friendlyName.equalsIgnoreCase("Lure")) return Enchantment.LURE.getName();
			else if (friendlyName.equalsIgnoreCase("Oxygen") || friendlyName.equalsIgnoreCase("Breathing") || friendlyName.equalsIgnoreCase("Respiration"))
				return Enchantment.OXYGEN.getName();
			else if (friendlyName.equalsIgnoreCase("Protection") || friendlyName.equalsIgnoreCase("Prot"))
				return Enchantment.PROTECTION_ENVIRONMENTAL.getName();
			else if (friendlyName.equalsIgnoreCase("Blast Protection") || friendlyName.equalsIgnoreCase("BlastProt"))
				return Enchantment.PROTECTION_EXPLOSIONS.getName();
			else if (friendlyName.equalsIgnoreCase("Fall Protection") || friendlyName.equalsIgnoreCase("FallProt") || friendlyName.equalsIgnoreCase("Feather") || friendlyName.equalsIgnoreCase("Feather Falling"))
				return Enchantment.PROTECTION_FALL.getName();
			else if (friendlyName.equalsIgnoreCase("Fire Protection") || friendlyName.equalsIgnoreCase("FireProt"))
				return Enchantment.PROTECTION_FIRE.getName();
			else if (friendlyName.equalsIgnoreCase("Projectile Protection") || friendlyName.equalsIgnoreCase("ProjProt"))
				return Enchantment.PROTECTION_PROJECTILE.getName();
			else if (friendlyName.equalsIgnoreCase("Silk Touch") || friendlyName.equalsIgnoreCase("SilkTouch") || friendlyName.equalsIgnoreCase("Silk"))
				return Enchantment.SILK_TOUCH.getName();
			else if (friendlyName.equalsIgnoreCase("Thorns"))
				return Enchantment.THORNS.getName();
			else if (friendlyName.equalsIgnoreCase("Water Worker") || friendlyName.equalsIgnoreCase("Aqua Affinity"))
				return Enchantment.WATER_WORKER.getName();
			else if (friendlyName.equalsIgnoreCase("Depth Strider"))
				return Enchantment.DEPTH_STRIDER.getName();
			else if (friendlyName.equalsIgnoreCase("Frost Walker"))
				return Enchantment.FROST_WALKER.getName();
			else if (friendlyName.equalsIgnoreCase("Repair"))
				return Enchantment.MENDING.getName();
			else if (friendlyName.equalsIgnoreCase("Binding"))
				return "BINDING_CURSE";
			else if (friendlyName.equalsIgnoreCase("Vanishing"))
				return "VANISHING_CURSE";
		}
		return friendlyName != null ? friendlyName.replace(' ', '_').toUpperCase() : "";
	}

	public static Gson getGsonParser() {
		if (gson == null) gson = new GsonBuilder().registerTypeAdapter(Kit.class, new JsonKitSerializer()).create();
		return gson;
	}

	public static String getPotionName(String friendlyName) {
		if (friendlyName != null) {
			if (friendlyName.equalsIgnoreCase("Slowness"))
				return PotionEffectType.SLOW.getName();
			else if (friendlyName.equalsIgnoreCase("Resistance")) return PotionEffectType.DAMAGE_RESISTANCE.getName();
			else if (friendlyName.equalsIgnoreCase("Fast") || friendlyName.equalsIgnoreCase("Haste"))
				return PotionEffectType.FAST_DIGGING.getName();
			else if (friendlyName.equalsIgnoreCase("Fire") || friendlyName.equalsIgnoreCase("FireResistance"))
				return PotionEffectType.FIRE_RESISTANCE.getName();
			else if (friendlyName.equalsIgnoreCase("Harming"))
				return PotionEffectType.HARM.getName();
			else if (friendlyName.equalsIgnoreCase("Healing"))
				return PotionEffectType.HEAL.getName();
			else if (friendlyName.equalsIgnoreCase("Boost")) return PotionEffectType.HEALTH_BOOST.getName();
			else if (friendlyName.equalsIgnoreCase("Strength")) return PotionEffectType.INCREASE_DAMAGE.getName();
			else if (friendlyName.equalsIgnoreCase("Night")) return PotionEffectType.NIGHT_VISION.getName();
			else if (friendlyName.equalsIgnoreCase("Regen")) return PotionEffectType.REGENERATION.getName();
			else if (friendlyName.equalsIgnoreCase("Fatigue")) return PotionEffectType.SLOW_DIGGING.getName();
			else if (friendlyName.equalsIgnoreCase("Water")) return PotionEffectType.WATER_BREATHING.getName();
		}
		return friendlyName != null ? friendlyName.replace(' ', '_').toUpperCase() : "";
	}

	public static PotionType getPotionType(String name) {
		if (name != null) {
			for (PotionType potionType : PotionType.values()) {
				if (potionType != null && potionType.name().equals(name)) return potionType;
			}
			switch (name) {
				case "FATIGUE":
					return PotionType.WEAKNESS;
				case "HARM":
				case "HARMING":
				case "DAMAGE":
					return PotionType.INSTANT_DAMAGE;
				case "HEAL":
				case "HEALTH":
				case "HEALING":
					return PotionType.INSTANT_HEAL;
				case "LEAP":
				case "LEAPING":
					return PotionType.JUMP;
				case "REGENERATION":
					return PotionType.REGEN;
				case "SLOW":
					return PotionType.SLOWNESS;
			}
		}
		return null;
	}

	public static boolean isNumber(Class<? extends Number> numberClass, Object objNumber) {
		if (objNumber == null) return false;
		try {
			if (numberClass.isInstance(objNumber)) return true;

			String strNumber = objNumber.toString();
			String className = numberClass.getSimpleName();
			if (className.equalsIgnoreCase("BigDecimal")) {
				new BigDecimal(strNumber);
			} else if (className.equalsIgnoreCase("Byte")) {
				Byte.parseByte(strNumber);
			} else if (className.equalsIgnoreCase("Double")) {
				Double.parseDouble(strNumber);
			} else if (className.equalsIgnoreCase("Float")) {
				Float.parseFloat(strNumber);
			} else if (className.equalsIgnoreCase("Integer")) {
				Integer.parseInt(strNumber);
			} else if (className.equalsIgnoreCase("Long")) {
				Long.parseLong(strNumber);
			} else if (className.equalsIgnoreCase("Short")) {
				Short.parseShort(strNumber);
			} else {
				return false;
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isPvPWorld(World world) {
		return world != null && (ConfigController.getInstance().getPvPWorlds().contains("All") || ConfigController.getInstance().getPvPWorlds().contains(world.getName()));
	}

	public static boolean isUUID(Object objUUID) {
		if (objUUID == null) return false;
		try {
			if (!(objUUID instanceof UUID)) UUID.fromString(objUUID.toString());
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static Map<String, Object> serializeColor(Color aColor) {
		Map<String, Object> serializedColor = new LinkedHashMap<>();
		if (aColor != null) {
			serializedColor.put("Red", aColor.getRed());
			serializedColor.put("Green", aColor.getGreen());
			serializedColor.put("Blue", aColor.getBlue());
		}
		return serializedColor;
	}

	public static boolean silentlyClose(AutoCloseable closeable) {
		try {
			if (closeable != null) closeable.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static List<String> toStringList(List list) {
		List<String> stringList = new ArrayList<>();
		if (list != null) {
			for (Object object : list) stringList.add(object != null ? object.toString() : null);
		}
		return stringList;
	}

	public static Map<String, Object> toStringObjectMap(Map<?, ?> unknownMap) {
		if (unknownMap != null) {
			Map<String, Object> stringObjectMap = new LinkedHashMap<>();
			for (Map.Entry unknownEntry : unknownMap.entrySet()) {
				Object unknownValue = unknownEntry.getValue();
				if (unknownValue instanceof Map)
					stringObjectMap.put(ObjectUtilities.toString(unknownEntry.getKey()), toStringObjectMap((Map) unknownValue));
				else stringObjectMap.put(ObjectUtilities.toString(unknownEntry.getKey()), unknownValue);
			}
			return stringObjectMap;
		} else {
			return new HashMap<>();
		}
	}

}
