package com.faris.kingkits;

import com.faris.kingkits.helper.util.ReflectionUtilities;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;

public class Permissions {

	private static Map<String, Permission> permissionList = new HashMap<>();

	public static Permission ADMIN = null;

	public static Permission COMMAND_RELOAD = null;
	public static Permission COMMAND_CONFIG = null;
	public static Permission COMMAND_KILLSTREAK = null;
	public static Permission COMMAND_KILLSTREAK_OTHER = null;

	public static Permission COMMAND_KIT = null;
	public static Permission COMMAND_KIT_OTHER = null;
	public static Permission COMMAND_KIT_LIST = null;
	public static Permission COMMAND_KIT_CREATE = null;
	public static Permission COMMAND_KIT_DELETE = null;
	public static Permission COMMAND_KIT_RENAME = null;
	public static Permission COMMAND_KIT_PREVIEW = null;
	public static Permission COMMAND_KIT_PREVIEW_OTHER = null;
	public static Permission COMMAND_UKIT_CREATE = null;
	public static Permission COMMAND_UKIT_DELETE = null;
	public static Permission COMMAND_UKIT_RENAME = null;
	public static Permission COMMAND_SOUP_REFILL_SINGLE = null;
	public static Permission COMMAND_SOUP_REFILL_ALL = null;

	public static Permission COMPASS = null;

	public static Permission SIGN_KIT_CREATE = null;
	public static Permission SIGN_KIT_LIST_CREATE = null;
	public static Permission SIGN_REFILL_CREATE = null;
	public static Permission SIGN_KIT_USE = null;
	public static Permission SIGN_KIT_LIST_USE = null;
	public static Permission SIGN_REFILL_USE = null;

	public static Permission SOUP_QUICKSOUP = null;

	public static void initialisePermissions() {
		permissionList.clear();

		registerPermission("ADMIN", "kingkits.admin");

		registerPermission("COMMAND_RELOAD", "kingkits.command.reload");
		registerPermission("COMMAND_CONFIG", "kingkits.command.config");
		registerPermission("COMMAND_KILLSTREAK", "kingkits.command.killstreak");
		registerPermission("COMMAND_KILLSTREAK_OTHER", "kingkits.command.killstreak.other");

		registerPermission("COMMAND_KIT", "kingkits.command.kit");
		registerPermission("COMMAND_KIT_OTHER", "kingkits.command.kit.other");
		registerPermission("COMMAND_KIT_LIST", "kingkits.command.kit.list");
		registerPermission("COMMAND_KIT_CREATE", "kingkits.command.createkit");
		registerPermission("COMMAND_KIT_DELETE", "kingkits.command.deletekit");
		registerPermission("COMMAND_KIT_RENAME", "kingkits.command.renamekit");
		registerPermission("COMMAND_KIT_PREVIEW", "kingkits.command.previewkit");
		registerPermission("COMMAND_KIT_PREVIEW_OTHER", "kingkits.command.previewkit.other");
		registerPermission("COMMAND_UKIT_CREATE", "kingkits.command.createukit");
		registerPermission("COMMAND_UKIT_DELETE", "kingkits.command.deleteukit");
		registerPermission("COMMAND_UKIT_RENAME", "kingkits.command.renameukit");
		registerPermission("COMMAND_SOUP_REFILL_SINGLE", "kingkits.command.refill");
		registerPermission("COMMAND_SOUP_REFILL_ALL", "kingkits.command.refill.all");

		registerPermission("COMPASS", "kingkits.compass");

		registerPermission("SIGN_KIT_CREATE", "kingkits.sign.kit.create");
		registerPermission("SIGN_KIT_USE", "kingkits.sign.kit.use");
		registerPermission("SIGN_KIT_LIST_CREATE", "kingkits.sign.list.create");
		registerPermission("SIGN_KIT_LIST_USE", "kingkits.sign.list.use");
		registerPermission("SIGN_REFILL_CREATE", "kingkits.sign.refill.create");
		registerPermission("SIGN_REFILL_USE", "kingkits.sign.refill.use");

		registerPermission("SOUP_QUICKSOUP", "kingkits.quicksoup");

		for (Permission permission : permissionList.values()) {
			try {
				Bukkit.getServer().getPluginManager().addPermission(permission);
			} catch (Exception ignored) {
			}
		}
	}

	public static void deinitialisePermissions() {
		for (Map.Entry<String, Permission> permissionEntry : permissionList.entrySet()) {
			try {
				Bukkit.getServer().getPluginManager().removePermission(permissionEntry.getValue());
			} catch (Exception ignored) {
			}
			try {
				ReflectionUtilities.FieldAccess permissionField = ReflectionUtilities.getField(Permissions.class, permissionEntry.getKey());
				if (permissionField == null) continue;
				permissionField.set(null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		permissionList.clear();
	}

	private static Permission registerPermission(String fieldName, String permissionNode) {
		if (fieldName == null || permissionNode == null) return null;
		ReflectionUtilities.FieldAccess permissionField = ReflectionUtilities.getField(Permissions.class, fieldName);
		if (permissionField == null) return null;
		Permission permission = new Permission(permissionNode);
		try {
			permissionField.set(permission);
			permissionList.put(permissionField.getField().getName(), permission);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return permission;
	}

}
