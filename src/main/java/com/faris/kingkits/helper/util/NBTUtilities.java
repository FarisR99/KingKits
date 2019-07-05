package com.faris.kingkits.helper.util;

import com.faris.BackwardsCompatibility;
import org.bukkit.inventory.ItemStack;

/**
 * @author KingFaris10
 */
public class NBTUtilities {

	private static final Class<?> classCraftItemStack;
	private static final Class<?> classNMSItemStack;
	private static final Class<?> classNBTBase;
	private static final Class<?> classNBTTagCompound;

	private static final ReflectionUtilities.FieldAccess fieldTag;
	private static final ReflectionUtilities.MethodInvoker methodAsNMSCopy;
	private static final ReflectionUtilities.MethodInvoker methodAsBukkitCopy;
	private static final ReflectionUtilities.MethodInvoker methodGetCompound;
	private static final ReflectionUtilities.MethodInvoker methodGetString;
	private static final ReflectionUtilities.MethodInvoker methodSet;
	private static final ReflectionUtilities.MethodInvoker methodSetString;

	static {
		classCraftItemStack = ReflectionUtilities.getBukkitClass("inventory.CraftItemStack");
		classNMSItemStack = ReflectionUtilities.getMinecraftClass("ItemStack");
		classNBTBase = ReflectionUtilities.getMinecraftClass("NBTBase");
		classNBTTagCompound = ReflectionUtilities.getMinecraftClass("NBTTagCompound");

		fieldTag = ReflectionUtilities.getField(classNMSItemStack, "tag");
		methodAsNMSCopy = ReflectionUtilities.getMethod(classCraftItemStack, "asNMSCopy", ItemStack.class);
		methodAsBukkitCopy = ReflectionUtilities.getMethod(classCraftItemStack, "asBukkitCopy", classNMSItemStack);
		methodGetCompound = ReflectionUtilities.getMethod(classNBTTagCompound, "getCompound", String.class);
		methodGetString = ReflectionUtilities.getMethod(classNBTTagCompound, "getString", String.class);
		methodSet = ReflectionUtilities.getMethod(classNBTTagCompound, "set", String.class, classNBTBase);
		methodSetString = ReflectionUtilities.getMethod(classNBTTagCompound, "setString", String.class, String.class);
	}

	private NBTUtilities() {
	}

	private static Object getNMSStack(ItemStack itemStack) throws Exception {
		if (itemStack != null) return methodAsNMSCopy.invoke(null, itemStack);
		else return null;
	}

	public static String getEgg(ItemStack itemStack) throws Exception {
		if (itemStack != null) {
			if (itemStack.getType() == BackwardsCompatibility.getMonsterEgg()) {
				Object nmsStack = getNMSStack(itemStack);
				if (nmsStack == null) return null;
				Object nbtTagCompound = fieldTag.getObject(nmsStack);
				if (nbtTagCompound == null) return null;
				Object entityTagCompound = methodGetCompound.invoke(nbtTagCompound, "EntityTag");
				if (entityTagCompound == null) return null;
				return (String) methodGetString.invoke(entityTagCompound, "id");
			}
		}
		return null;
	}

	public static ItemStack setEgg(ItemStack itemStack, String entityType) throws Exception {
		if (itemStack != null && entityType != null) {
			if (itemStack.getType() == BackwardsCompatibility.getMonsterEgg()) {
				Object nmsStack = methodAsNMSCopy.invoke(null, itemStack);
				Object nbtTagCompound = fieldTag.getObject(nmsStack);
				if (nbtTagCompound == null) nbtTagCompound = classNBTTagCompound.newInstance();
				Object entityTagCompound = methodGetCompound.invoke(nbtTagCompound, "EntityTag");
				if (entityTagCompound == null) entityTagCompound = classNBTTagCompound.newInstance();
				methodSetString.invoke(entityTagCompound, "id", entityType);
				methodSet.invoke(nbtTagCompound, "EntityTag", entityTagCompound);
				return (ItemStack) methodAsBukkitCopy.invoke(null, nmsStack);
			}
		}
		return itemStack;
	}

}
