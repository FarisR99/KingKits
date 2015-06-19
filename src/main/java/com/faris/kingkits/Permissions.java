package com.faris.kingkits;

import org.bukkit.permissions.*;

import java.util.ArrayList;
import java.util.List;

public class Permissions {

	private static List<Permission> registeredPermissions = new ArrayList<>();

	public static Permission COMMAND_KK_CONFIG = null;
	public static Permission COMMAND_KK_RELOAD = null;
	public static Permission COMMAND_KK_SET_COOLDOWN = null;

	public static Permission COMMAND_KIT_CREATE = null;
	public static Permission COMMAND_KIT_DELETE = null;
	public static Permission COMMAND_KIT_RENAME = null;
	public static Permission COMMAND_UKIT_CREATE = null;
	public static Permission COMMAND_UKIT_DELETE = null;
	public static Permission COMMAND_UKIT_RENAME = null;

	public static Permission COMMAND_PREVIEW_KIT = null;
	public static Permission COMMAND_PREVIEW_KIT_OTHER = null;

	public static Permission KIT_USE = null;
	public static Permission KIT_USE_OTHER = null;
	public static Permission KIT_LIST = null;
	public static Permission KIT_LIST_TOOLTIP = null;

	public static Permission SIGN_CREATE = null;
	public static Permission SIGN_KIT = null;
	public static Permission SIGN_LIST = null;
	public static Permission SIGN_REFILL = null;

	public static Permission SOUP_QUICKSOUP = null;
	public static Permission SOUP_REFILL_SINGLE = null;
	public static Permission SOUP_REFILL_ALL = null;

	public static Permission KIT_COOLDOWN_BYPASS = null;
	public static Permission COMPASS = null;
	public static Permission COMMAND_KILLSTREAK = null;

	private Permissions() {
	}

	public static void clearPermissions() {
		registeredPermissions.clear();
	}

	public static List<Permission> getPermissions() {
		return registeredPermissions;
	}

	public static void initialisePermissions() {
		COMMAND_KK_CONFIG = registerPermission("kingkits.command.config");
		COMMAND_KK_RELOAD = registerPermission("kingkits.command.reload");
		COMMAND_KK_SET_COOLDOWN = registerPermission("kingkits.command.setcooldown");
		
		COMMAND_KIT_CREATE = registerPermission("kingkits.kit.create");
		COMMAND_KIT_DELETE = registerPermission("kingkits.kit.delete");
		COMMAND_KIT_RENAME = registerPermission("kingkits.kit.rename");
		COMMAND_UKIT_CREATE = registerPermission("kingkits.kit.ucreate");
		COMMAND_UKIT_DELETE = registerPermission("kingkits.kit.udelete");
		COMMAND_UKIT_RENAME = registerPermission("kingkits.kit.urename");
		
		COMMAND_PREVIEW_KIT = registerPermission("kingkits.kit.preview");
		COMMAND_PREVIEW_KIT_OTHER = registerPermission("kingkits.kit.preview.other");
		
		KIT_USE = registerPermission("kingkits.kit.use");
		KIT_USE_OTHER = registerPermission("kingkits.kit.use.other");
		KIT_LIST = registerPermission("kingkits.kit.list");
		KIT_LIST_TOOLTIP = registerPermission("kingkits.kit.list.tooltip");
		
		SIGN_CREATE = registerPermission("kingkits.kit.sign.create");
		SIGN_KIT = registerPermission("kingkits.kit.sign.use");
		SIGN_LIST = registerPermission("kingkits.kit.sign.list");
		SIGN_REFILL = registerPermission("kingkits.kit.sign.refill");
		
		SOUP_QUICKSOUP = registerPermission("kingkits.quicksoup");
		SOUP_REFILL_SINGLE = registerPermission("kingkits.refill.single");
		SOUP_REFILL_ALL = registerPermission("kingkits.refill.all");
		
		KIT_COOLDOWN_BYPASS = registerPermission("kingkits.kit.cooldown.bypass");
		COMPASS = registerPermission("kingkits.compass");
		COMMAND_KILLSTREAK = registerPermission("kingkits.command.killstreak");
	}

	private static Permission registerPermission(String permissionNode) {
		return permissionNode != null ? registerPermission(new Permission(permissionNode)) : null;
	}

	private static Permission registerPermission(Permission permission) {
		if (!registeredPermissions.contains(permission)) registeredPermissions.add(permission);
		return permission;
	}

}
