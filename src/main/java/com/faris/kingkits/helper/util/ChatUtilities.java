package com.faris.kingkits.helper.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ChatUtilities {

	private ChatUtilities() {
	}

	public static String replaceChatCodes(String aString) {
		return aString != null ? ChatColor.translateAlternateColorCodes('&', aString) : "";
	}

	public static String[] replaceChatCodes(String... manyStrings) {
		if (manyStrings != null) {
			for (int i = 0; i < manyStrings.length; i++)
				manyStrings[i] = replaceChatCodes(manyStrings[i]);
		}
		return manyStrings;
	}

	public static List<String> replaceChatCodes(List<String> someStrings) {
		if (someStrings != null) {
			for (int i = 0; i < someStrings.size(); i++)
				someStrings.set(i, replaceChatCodes(someStrings.get(i)));
		}
		return someStrings != null ? someStrings : new ArrayList<String>();
	}

	public static List<String> replaceChatColours(List<String> someStrings) {
		List<String> newStrings = new ArrayList<>();
		if (someStrings != null) {
			for (String aString : someStrings) newStrings.add(replaceChatColours(aString));
		}
		return newStrings;
	}

	public static String replaceChatColours(String aString) {
		if (aString != null) {
			for (ChatColor chatColor : ChatColor.values())
				aString = aString.replace(chatColor.toString(), "&" + chatColor.getChar());
			for (int i = 0; i < 10; i++)
				aString = aString.replace("\\u0026" + i, "&" + i);
			aString = aString.replace("\\u0026" + "a", "&" + "a");
			aString = aString.replace("\\u0026" + "b", "&" + "b");
			aString = aString.replace("\\u0026" + "c", "&" + "c");
			aString = aString.replace("\\u0026" + "d", "&" + "d");
			aString = aString.replace("\\u0026" + "e", "&" + "e");
			aString = aString.replace("\\u0026" + "f", "&" + "f");
			aString = aString.replace("\\u0026" + "l", "&" + "l");
			aString = aString.replace("\\u0026" + "n", "&" + "n");
			aString = aString.replace("\\u0026" + "o", "&" + "o");
			aString = aString.replace("\\u0026" + "r", "&" + "r");
		}
		return aString != null ? (aString.startsWith("&f") ? aString.substring(2) : aString) : "";
	}

	public static void sendConsoleMessage(String... messages) {
		if (messages != null) {
			for (String message : messages)
				Bukkit.getServer().getConsoleSender().sendMessage(replaceChatCodes(message));
		}
	}

	public static String stripColour(String aString) {
		return aString != null ? ChatColor.stripColor(aString) : "";
	}

}
