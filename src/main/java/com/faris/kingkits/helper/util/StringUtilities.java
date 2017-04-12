package com.faris.kingkits.helper.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtilities {

	private StringUtilities() {
	}

	public static String capitalize(String name) {
		if (name != null) {
			if (name.length() > 1) {
				return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			} else {
				return name.toUpperCase();
			}
		} else {
			return "";
		}
	}

	public static String capitalizeFully(String name) {
		if (name != null) {
			if (name.length() > 1) {
				if (name.contains("_")) {
					StringBuilder sbName = new StringBuilder();
					for (String subName : name.split("_")) {
						sbName.append(subName.substring(0, 1).toUpperCase()).append(subName.substring(1).toLowerCase()).append(" ");
					}
					return sbName.toString().substring(0, sbName.length() - 1);
				} else if (name.contains(" ")) {
					StringBuilder sbName = new StringBuilder();
					for (String subName : name.split(" ")) {
						sbName.append(subName.substring(0, 1).toUpperCase()).append(subName.substring(1).toLowerCase()).append(" ");
					}
					return sbName.toString().substring(0, sbName.length() - 1);
				} else {
					return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
				}
			} else {
				return name.toUpperCase();
			}
		} else {
			return "";
		}
	}

	public static boolean containsIllegalCharacters(String aString) {
		return !aString.matches("[a-zA-Z0-9_]*");
	}

	public static String joinString(String[] strings) {
		return joinString(strings, 0);
	}

	public static String joinString(String[] strings, int startIndex) {
		return joinString(strings, startIndex, " ");
	}

	public static String joinString(String[] strings, int startIndex, String separator) {
		if (strings != null && strings.length > 0) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = startIndex; i < strings.length; i++) {
				if (i == strings.length - 1) stringBuilder.append(strings[i]);
				else stringBuilder.append(strings[i]).append(separator);
			}
			return stringBuilder.toString();
		}
		return "";
	}

	public static List<String> toLowerCase(List<String> aList) {
		List<String> lowercasedList = new ArrayList<>();
		if (aList != null) {
			for (String aString : aList) lowercasedList.add(aString.toLowerCase());
		}
		return lowercasedList;
	}

	public static String trimString(String aString, int maxLength) {
		return aString != null ? (aString.length() > maxLength ? aString.substring(0, maxLength) : aString) : "";
	}

}
