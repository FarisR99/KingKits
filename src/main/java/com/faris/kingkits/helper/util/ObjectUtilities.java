package com.faris.kingkits.helper.util;

import com.google.gson.JsonObject;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ObjectUtilities {

	private ObjectUtilities() {
	}

	public static <T> T getObject(JsonObject jsonObject, Class<T> unused, String key) {
		return getObject(jsonObject, unused, key, null);
	}

	public static <T> T getObject(JsonObject jsonObject, Class<T> unused, String key, T defaultValue) {
		try {
			Object value = jsonObject.get(key);
			if (value != null) {
				if (unused == Boolean.class) return unused.cast(Boolean.valueOf(Objects.toString(value)));
				else if (unused == Byte.class) return unused.cast(Byte.valueOf(Objects.toString(value)));
				else if (unused == Float.class) return unused.cast(Float.valueOf(Objects.toString(value)));
				else if (unused == Integer.class) return unused.cast(Integer.valueOf(Objects.toString(value)));
				else if (unused == Long.class) return unused.cast(Long.valueOf(Objects.toString(value)));
				else if (unused == Short.class) return unused.cast(Short.valueOf(Objects.toString(value)));
				else if (unused == String.class) return unused.cast(toString(value));
				else return unused.cast(value);
			}
		} catch (Exception ignored) {
		}
		return defaultValue;
	}

	public static <K, V, T> T getObject(Map<K, V> map, Class<T> unused, K key) {
		return getObject(map, unused, key, null);
	}

	public static <K, V, T> T getObject(Map<K, V> map, Class<T> unused, K key, T defaultValue) {
		try {
			V value = map.get(key);
			if (value != null) {
				if (unused == Boolean.class) return unused.cast(Boolean.valueOf(Objects.toString(value)));
				else if (unused == Byte.class) return unused.cast(Byte.valueOf(Objects.toString(value)));
				else if (unused == Float.class) return unused.cast(Float.valueOf(Objects.toString(value)));
				else if (unused == Integer.class) return unused.cast(Integer.valueOf(Objects.toString(value)));
				else if (unused == Long.class) return unused.cast(Long.valueOf(Objects.toString(value)));
				else if (unused == Short.class) return unused.cast(Short.valueOf(Objects.toString(value)));
				else if (unused == String.class) return unused.cast(toString(value));
				else return unused.cast(value);
			}
		} catch (Exception ignored) {
		}
		return defaultValue;
	}

	public static <K, V, T> T getObject(Map<K, V> map, Class<T> unused, K key, T defaultValue, Runnable error) {
		try {
			V value = map.get(key);
			if (value != null) {
				if (unused == Boolean.class) return unused.cast(Boolean.valueOf(Objects.toString(value)));
				else if (unused == Byte.class) return unused.cast(Byte.valueOf(Objects.toString(value)));
				else if (unused == Float.class) return unused.cast(Float.valueOf(Objects.toString(value)));
				else if (unused == Integer.class) return unused.cast(Integer.valueOf(Objects.toString(value)));
				else if (unused == Long.class) return unused.cast(Long.valueOf(Objects.toString(value)));
				else if (unused == Short.class) return unused.cast(Short.valueOf(Objects.toString(value)));
				else if (unused == String.class) return unused.cast(toString(value));
				else return unused.cast(value);
			}
		} catch (Exception ignored) {
			error.run();
		}
		return defaultValue;
	}

	public static Map<String, Object> getMap(Object objMap) {
		if (objMap instanceof ConfigurationSection) {
			return ((ConfigurationSection) objMap).getValues(false);
		} else if (objMap instanceof Map) {
			return (Map<String, Object>) objMap;
		} else {
			return new HashMap<>();
		}
	}

	public static String toString(Object object) {
		return Objects.toString(object);
	}

}
