package com.faris.kingkits.helper.api;

import com.faris.kingkits.helper.util.ChatUtilities;
import com.faris.kingkits.helper.util.ReflectionUtilities;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TitleAPI {

	private static boolean initialised = false;

	private static Class classCraftPlayer = null;
	private static Class classEntityPlayer = null;

	private static Class classEnumTitleAction = null;

	private static Class classPacket = null;
	private static Class classPacketPlayOutTitle = null;

	private static Class classIChatBaseComponent = null;
	private static Class classChatSerializer = null;
	private static Class classPlayerConnection = null;

	private TitleAPI() {
	}

	public static void deinitialiseReflection() {
		classCraftPlayer = null;
		classEntityPlayer = null;
		classEnumTitleAction = null;
		classPacket = null;
		classPacketPlayOutTitle = null;
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
			classPacketPlayOutTitle = ReflectionUtilities.getMinecraftClass("PacketPlayOutTitle");
			Validate.notNull(classPacketPlayOutTitle);

			classEnumTitleAction = ReflectionUtilities.getClass(classPacketPlayOutTitle, "EnumTitleAction");
			Validate.notNull(classEnumTitleAction);

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

	public static void sendTitle(Player player, double fadeInTime, double durationTime, double fadeOutTime, String title) throws Exception {
		sendTitle(player, fadeInTime, durationTime, fadeOutTime, title, null);
	}

	public static void sendTitle(Player player, double fadeInTime, double durationTime, double fadeOutTime, String title, String subtitle) throws Exception {
		if (player == null) return;
		if (initialised) {
			try {
				ReflectionUtilities.MethodInvoker methodGetHandle = ReflectionUtilities.getMethod(classCraftPlayer, "getHandle");
				Object objCraftPlayer = classCraftPlayer.cast(player);
				Object entityPlayer = methodGetHandle.invoke(objCraftPlayer);

				ReflectionUtilities.FieldAccess fieldPlayerConnection = ReflectionUtilities.getField(classEntityPlayer, "playerConnection");
				Object playerConnection = fieldPlayerConnection.getObject(entityPlayer);
				ReflectionUtilities.MethodInvoker methodSendPacket = ReflectionUtilities.getMethod(classPlayerConnection, "sendPacket", classPacket);

				ReflectionUtilities.ConstructorInvoker constructorClassPacketPlayOutTitle1 = ReflectionUtilities.getConstructor(classPacketPlayOutTitle, classEnumTitleAction, classIChatBaseComponent, int.class, int.class, int.class);
				ReflectionUtilities.ConstructorInvoker constructorClassPacketPlayOutTitle2 = ReflectionUtilities.getConstructor(classPacketPlayOutTitle, classEnumTitleAction, classIChatBaseComponent);

				Object objPacketPlayOutTimes = constructorClassPacketPlayOutTitle1.newInstance(Enum.valueOf(classEnumTitleAction, "TIMES"), null, (int) (fadeInTime * 10), (int) (durationTime * 10), (int) (fadeOutTime * 10));
				methodSendPacket.invoke(playerConnection, objPacketPlayOutTimes);

				ReflectionUtilities.FieldAccess fieldGson = ReflectionUtilities.getFieldByClass(classChatSerializer, "Gson", 0);
				if (fieldGson != null) {
					ReflectionUtilities.MethodInvoker methodFromJson = ReflectionUtilities.getMethod(fieldGson.getField().getType(), "fromJson", String.class, Class.class);

					if (subtitle != null) {
						subtitle = subtitle.replaceAll("%name%", player.getName());
						subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
						subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

						Object objJsonTitleSub = methodFromJson.invoke(fieldGson.getObject(null), "{\"text\": \"" + subtitle + "\"}", classIChatBaseComponent);
						Object objTitleSub = classIChatBaseComponent.cast(objJsonTitleSub);

						objPacketPlayOutTimes = constructorClassPacketPlayOutTitle2.newInstance(Enum.valueOf(classEnumTitleAction, "SUBTITLE"), objTitleSub);
						methodSendPacket.invoke(playerConnection, objPacketPlayOutTimes);
					}

					if (title != null) {
						title = title.replaceAll("%name%", player.getName());
						title = title.replaceAll("%player%", player.getDisplayName());
						title = ChatColor.translateAlternateColorCodes('&', title);

						Object objJsonTitleMain = methodFromJson.invoke(fieldGson.getObject(null), "{\"text\": \"" + title + "\"}", classIChatBaseComponent);
						Object objTitleMain = classIChatBaseComponent.cast(objJsonTitleMain);

						objPacketPlayOutTimes = constructorClassPacketPlayOutTitle2.newInstance(Enum.valueOf(classEnumTitleAction, "TITLE"), objTitleMain);
						methodSendPacket.invoke(playerConnection, objPacketPlayOutTimes);
					}
				} else {
					initialised = false;

					if (title != null) player.sendMessage(ChatUtilities.replaceChatCodes(title));
					if (subtitle != null) player.sendMessage(ChatUtilities.replaceChatCodes(subtitle));
				}
			} catch (Exception ex) {
				initialised = false;
				throw ex;
			}
		}
	}

}
