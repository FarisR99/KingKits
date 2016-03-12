package com.faris.kingkits.helper.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

/**
 * @author KingFaris10
 */
public class PotionUtilities {

	private static final Class<?> classCraftItemStack;
	private static final Class<?> classNMSItemStack;
	private static final Class<?> classNBTTagCompound;

	private static final ReflectionUtilities.FieldAccess fieldTag;
	private static final ReflectionUtilities.MethodInvoker methodAsNMSCopy;
	private static final ReflectionUtilities.MethodInvoker methodAsBukkitCopy;
	private static final ReflectionUtilities.MethodInvoker methodHasKey;
	private static final ReflectionUtilities.MethodInvoker methodGetString;
	private static final ReflectionUtilities.MethodInvoker methodSetString;

	static {
		classCraftItemStack = ReflectionUtilities.getBukkitClass("inventory.CraftItemStack");
		classNMSItemStack = ReflectionUtilities.getMinecraftClass("ItemStack");
		classNBTTagCompound = ReflectionUtilities.getMinecraftClass("NBTTagCompound");

		fieldTag = ReflectionUtilities.getField(classNMSItemStack, "tag");
		methodAsNMSCopy = ReflectionUtilities.getMethod(classCraftItemStack, "asNMSCopy", ItemStack.class);
		methodAsBukkitCopy = ReflectionUtilities.getMethod(classCraftItemStack, "asBukkitCopy", classNMSItemStack);
		methodHasKey = ReflectionUtilities.getMethod(classNBTTagCompound, "hasKey", String.class);
		methodGetString = ReflectionUtilities.getMethod(classNBTTagCompound, "getString", String.class);
		methodSetString = ReflectionUtilities.getMethod(classNBTTagCompound, "setString", String.class, String.class);
	}

	private PotionUtilities() {
	}

	private static Object getNMSStack(ItemStack itemStack) throws Exception {
		if (itemStack != null) return methodAsNMSCopy.invoke(null, itemStack);
		else return null;
	}

	public static String getPotion(ItemStack itemStack) throws Exception {
		if (itemStack != null) {
			if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.TIPPED_ARROW) {
				Object nmsStack = getNMSStack(itemStack);
				if (nmsStack != null) {
					Object nbtTagCompound = fieldTag.getObject(nmsStack);
					if (nbtTagCompound != null) {
						String strPotionType = (String) methodGetString.invoke(nbtTagCompound, "Potion");
						return strPotionType != null && strPotionType.isEmpty() ? null : strPotionType;
					}
				}
			}
		}
		return null;
	}

	public static PotionType getPotionType(String name) {
		if (name != null) {
			for (PotionType potionType : PotionType.values()) {
				if (potionType != null && potionType.name().equals(name)) return potionType;
			}
		}
		return null;
	}

	public static boolean isPotion(ItemStack itemStack) throws Exception {
		if (itemStack != null) {
			if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.TIPPED_ARROW) {
				Object nmsStack = getNMSStack(itemStack);
				if (nmsStack != null) {
					Object nbtTagCompound = fieldTag.getObject(nmsStack);
					return nbtTagCompound != null && (boolean) methodHasKey.invoke(nbtTagCompound, "Potion");
				}
			}
		}
		return false;
	}

	public static ItemStack setPotion(ItemStack itemStack, String potionType) throws Exception {
		if (itemStack != null && potionType != null) {
			if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.TIPPED_ARROW) {
				Object nmsStack = getNMSStack(itemStack);
				if (nmsStack != null) {
					Object nbtTagCompound = fieldTag.getObject(nmsStack);
					boolean hadTag = nbtTagCompound != null;
					if (!hadTag) nbtTagCompound = classNBTTagCompound.getConstructors()[0].newInstance();
					methodSetString.invoke(nbtTagCompound, "Potion", potionType);
					if (!hadTag) fieldTag.set(nmsStack, nbtTagCompound);
					return (ItemStack) methodAsBukkitCopy.invoke(null, nmsStack);
				}
			}
		}
		return itemStack;
	}

}
