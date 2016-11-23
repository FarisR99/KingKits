package com.faris.kingkits.helper.api;

import com.faris.kingkits.helper.util.ReflectionUtilities;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionBarAPI {

	private static boolean initialised = false;

	private static Class classCraftPlayer = null;
	private static Class classEntityPlayer = null;

	private static Class classPacket = null;
	private static Class classPacketPlayOutChat = null;

	private static Class classIChatBaseComponent = null;
	private static Class classChatSerializer = null;
	private static Class classPlayerConnection = null;

	private ActionBarAPI() {
	}

	public static void deinitialiseReflection() {
		classCraftPlayer = null;
		classEntityPlayer = null;
		classPacket = null;
		classPacketPlayOutChat = null;
		classIChatBaseComponent = null;
		classChatSerializer = null;
		classPlayerConnection = null;

		initialised = false;
	}

	public static void initialiseReflection() {
		try {
			classCraftPlayer = ReflectionUtilities.getBukkitClass("entity.CraftPlayer");
			Validate.notNull(classCraftPlayer);
			classEntityPlayer = ReflectionUtilities.getMinecraftClass("EntityPlayer");
			Validate.notNull(classEntityPlayer);

			classPacket = ReflectionUtilities.getMinecraftClass("Packet");
			Validate.notNull(classPacket);
			classPacketPlayOutChat = ReflectionUtilities.getMinecraftClass("PacketPlayOutChat");
			Validate.notNull(classPacketPlayOutChat);

			classIChatBaseComponent = ReflectionUtilities.getMinecraftClass("IChatBaseComponent");
			Validate.notNull(classIChatBaseComponent);
			classChatSerializer = ReflectionUtilities.getClass(classIChatBaseComponent, "ChatSerializer");
			Validate.notNull(classChatSerializer);
			classPlayerConnection = ReflectionUtilities.getMinecraftClass("PlayerConnection");
			Validate.notNull(classPlayerConnection);

			initialised = true;
		} catch (Exception ex) {
			ex.printStackTrace();

			initialised = false;
		}
	}

	public static boolean isInitialised() {
		return initialised;
	}

	public static void sendActionBar(Player player, String message) throws Exception {
		if (player == null || message == null) return;
		message = message.replaceAll("%name%", player.getName());
		message = message.replaceAll("%player%", player.getDisplayName());
		message = ChatColor.translateAlternateColorCodes('&', message);
		if (initialised) {
			try {
				ReflectionUtilities.MethodInvoker methodGetHandle = ReflectionUtilities.getMethod(classCraftPlayer, "getHandle");
				Object objCraftPlayer = classCraftPlayer.cast(player);
				Object entityPlayer = methodGetHandle.invoke(objCraftPlayer);

				ReflectionUtilities.FieldAccess fieldPlayerConnection = ReflectionUtilities.getField(classEntityPlayer, "playerConnection");
				Object playerConnection = fieldPlayerConnection.getObject(entityPlayer);
				ReflectionUtilities.MethodInvoker methodSendPacket = ReflectionUtilities.getMethod(classPlayerConnection, "sendPacket", classPacket);

				ReflectionUtilities.ConstructorInvoker constructorClassPacketPlayOutChat = ReflectionUtilities.getConstructor(classPacketPlayOutChat, classIChatBaseComponent, byte.class);
				ReflectionUtilities.FieldAccess fieldGson = ReflectionUtilities.getFieldByClass(classChatSerializer, "Gson", 0);
				if (fieldGson != null) {
					ReflectionUtilities.MethodInvoker methodFromJson = ReflectionUtilities.getMethod(fieldGson.getField().getType(), "fromJson", String.class, Class.class);

					Object objJsonTitleMain = methodFromJson.invoke(fieldGson.getObject(null), "{\"text\": \"" + message + "\"}", classIChatBaseComponent);
					Object objTitleMain = classIChatBaseComponent.cast(objJsonTitleMain);

					Object objPacketPlayOutTimes = constructorClassPacketPlayOutChat.newInstance(objTitleMain, (byte) 2);
					methodSendPacket.invoke(playerConnection, objPacketPlayOutTimes);
				} else {
					initialised = false;
				}
			} catch (Exception ex) {
				initialised = false;
				throw ex;
			}
		}
	}

}
