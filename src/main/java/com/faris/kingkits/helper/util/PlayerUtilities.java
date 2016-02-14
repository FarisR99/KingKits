package com.faris.kingkits.helper.util;

import com.faris.kingkits.Kit;
import com.faris.kingkits.Messages;
import com.faris.kingkits.helper.Time;
import com.faris.kingkits.hook.VaultAPI;
import com.faris.kingkits.player.KitPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class PlayerUtilities {

	private static Class classCraftPlayer = null;

	private static Class classPacketPlayInClientCommand = null;
	private static Class classEnumClientCommand = null;

	private PlayerUtilities() {
	}

	public static boolean checkPlayer(CommandSender sender, KitPlayer kitPlayer) {
		if (sender == null) return false;
		if (kitPlayer == null) {
			if (!(sender instanceof Player) || ((Player) sender).isOnline())
				Messages.sendMessage(sender, Messages.GENERAL_COMMAND_ERROR, NullPointerException.class.getName());
			return false;
		}
		return true;
	}

	public static double getBalance(Player player) {
		if (BukkitUtilities.hasPlugin("Vault")) return VaultAPI.getBalance(player);
		return 0D;
	}

	public static double getDefaultMaxHealth() {
		return 20D;
	}

	public static float getDefaultWalkSpeed() {
		return 0.2F;
	}

	public static boolean incrementMoney(Player player, double amount) {
		if (player != null && amount != 0D) {
			if (BukkitUtilities.hasPlugin("Vault")) {
				VaultAPI.giveMoney(player, amount);
				return true;
			}
		}
		return false;
	}

	public static void respawnPlayer(Player player) throws Exception {
		if (player != null) {
			boolean spigotMethod = false;
			if (Bukkit.getServer().getVersion().contains("Spigot")) {
				if (classCraftPlayer == null)
					classCraftPlayer = ReflectionUtilities.getBukkitClass("entity.CraftPlayer");

				ReflectionUtilities.MethodInvoker methodGetSpigot = ReflectionUtilities.getMethod(classCraftPlayer, "spigot");
				if (methodGetSpigot != null) {
					Object spigotInstance = methodGetSpigot.invoke(classCraftPlayer.cast(player));
					ReflectionUtilities.MethodInvoker methodRespawn = ReflectionUtilities.getMethod(spigotInstance.getClass(), "respawn");
					if (methodRespawn != null) {
						methodRespawn.invoke(spigotInstance);
						spigotMethod = true;
					}
				}
			}
			if (!spigotMethod) {
				if (classCraftPlayer == null)
					classCraftPlayer = ReflectionUtilities.getBukkitClass("entity.CraftPlayer");
				if (classPacketPlayInClientCommand == null)
					classPacketPlayInClientCommand = ReflectionUtilities.getMinecraftClass("PacketPlayInClientCommand");
				if (classEnumClientCommand == null)
					classEnumClientCommand = ReflectionUtilities.getClass(classPacketPlayInClientCommand, "EnumClientCommand");

				ReflectionUtilities.MethodInvoker methodGetHandle = ReflectionUtilities.getMethod(classCraftPlayer, "getHandle");
				Validate.notNull(methodGetHandle);
				Object entityPlayer = methodGetHandle.invoke(player);

				ReflectionUtilities.ConstructorInvoker constructorPacket = ReflectionUtilities.getConstructor(classPacketPlayInClientCommand, classEnumClientCommand);
				Object enumPerformRespawn = ReflectionUtilities.getEnum(classEnumClientCommand, "PERFORM_RESPAWN");
				Validate.notNull(enumPerformRespawn);

				ReflectionUtilities.FieldAccess fieldPlayerConnection = ReflectionUtilities.getField(entityPlayer.getClass(), "playerConnection");
				Object playerConnection = fieldPlayerConnection.getObject(entityPlayer);

				ReflectionUtilities.MethodInvoker methodA = ReflectionUtilities.getMethod(playerConnection.getClass(), "a", classPacketPlayInClientCommand);
				Object objPacket = constructorPacket.newInstance(enumPerformRespawn);
				methodA.invoke(playerConnection, objPacket);
			}
		}
	}

	public static void sendKitDelayMessage(CommandSender sender, Kit kit, long timestamp) {
		if (sender != null && kit != null) {
			long cooldownSeconds = (long) (kit.getCooldown() - (((double) System.currentTimeMillis() - (double) timestamp) / 1_000D));
			String rawMessage = Messages.KIT_DELAY.getRawMessage();
			int numberOfReplacements = 0;
			while (rawMessage.contains("%s")) {
				numberOfReplacements++;
				rawMessage = rawMessage.replaceFirst("%s", "");
			}
			if (numberOfReplacements >= 2) {
				Time delay = new Time(cooldownSeconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) cooldownSeconds);
				int intDelay = 0;
				String strUnit = "";
				if (delay.getDays() > 0) {
					intDelay = delay.getDays();
					strUnit = intDelay != 1 ? Messages.TIME_DAY_PLURAL.getMessage() : Messages.TIME_DAY_SINGULAR.getMessage();
				} else if (delay.getHours() > 0) {
					intDelay = delay.getHours();
					strUnit = intDelay != 1 ? Messages.TIME_HOUR_PLURAL.getMessage() : Messages.TIME_HOUR_SINGULAR.getMessage();
				} else if (delay.getMinutes() > 0) {
					intDelay = delay.getMinutes();
					strUnit = intDelay != 1 ? Messages.TIME_MINUTE_PLURAL.getMessage() : Messages.TIME_MINUTE_SINGULAR.getMessage();
				} else {
					intDelay = delay.getSeconds();
					strUnit = intDelay != 1 ? Messages.TIME_SECOND_PLURAL.getMessage() : Messages.TIME_SECOND_SINGULAR.getMessage();
				}
				Messages.sendMessage(sender, Messages.KIT_DELAY, String.valueOf(intDelay), strUnit);
			} else {
				Messages.sendMessage(sender, Messages.KIT_DELAY, String.valueOf(cooldownSeconds));
			}
		}
	}

	public static boolean setBalance(Player player, double balance) {
		if (player != null) {
			if (BukkitUtilities.hasPlugin("Vault")) {
				VaultAPI.setBalance(player, balance);
				return true;
			}
		}
		return false;
	}

}
