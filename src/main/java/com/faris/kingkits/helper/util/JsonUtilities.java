package com.faris.kingkits.helper.util;

import com.faris.kingkits.helper.json.JsonSerializable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonUtilities {

	private JsonUtilities() {
	}

	public static JsonArray fromArray(List list) {
		JsonArray jsonArray = new JsonArray();
		if (list != null) {
			for (Object listItem : list) {
				JsonElement jsonListItem;
				if (listItem instanceof List) jsonListItem = fromArray((List) listItem);
				else if (listItem instanceof Map) jsonListItem = fromMap((Map<?, ?>) listItem);
				else if (listItem instanceof JsonSerializable)
					jsonListItem = fromPrimitive(((JsonSerializable) listItem).serializeToJson());
				else jsonListItem = fromPrimitive(listItem);
				jsonArray.add(jsonListItem == null ? new JsonPrimitive(ObjectUtilities.toString(listItem)) : jsonListItem);
			}
		}
		return jsonArray;
	}

	public static JsonObject fromMap(Map<?, ?> map) {
		JsonObject jsonObject = new JsonObject();
		if (map != null) {
			for (Map.Entry<?, ?> mapEntry : map.entrySet()) {
				try {
					String mapKey = ObjectUtilities.toString(mapEntry.getKey());
					JsonElement mapValue;
					if (mapEntry.getValue() instanceof JsonSerializable) {
						mapValue = fromPrimitive(((JsonSerializable) mapEntry.getValue()).serializeToJson());
					} else if (mapEntry.getValue() instanceof Map) {
						mapValue = fromMap((Map<?, ?>) mapEntry.getValue());
					} else if (mapEntry.getValue() instanceof List) {
						mapValue = fromArray((List) mapEntry.getValue());
					} else {
						mapValue = fromPrimitive(mapEntry.getValue());
						if (mapValue == null) mapValue = fromPrimitive(ObjectUtilities.toString(mapEntry.getValue()));
					}
					if (mapValue != null) jsonObject.add(mapKey, mapValue);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return jsonObject;
	}

	public static JsonPrimitive fromPrimitive(Object object) {
		if (object != null) {
			if (object instanceof Number) {
				JsonPrimitive jsonPrimitive;

				Number objectNumber = (Number) object;
				if (objectNumber instanceof Byte) jsonPrimitive = new JsonPrimitive(objectNumber.byteValue());
				else if (objectNumber instanceof Double) jsonPrimitive = new JsonPrimitive(objectNumber.doubleValue());
				else if (objectNumber instanceof Float) jsonPrimitive = new JsonPrimitive(objectNumber.floatValue());
				else if (objectNumber instanceof Integer) jsonPrimitive = new JsonPrimitive(objectNumber.intValue());
				else if (objectNumber instanceof Long) jsonPrimitive = new JsonPrimitive(objectNumber.longValue());
				else if (objectNumber instanceof Short) jsonPrimitive = new JsonPrimitive(objectNumber.shortValue());
				else jsonPrimitive = new JsonPrimitive(objectNumber);

				return jsonPrimitive;
			} else if (object instanceof Boolean) {
				return new JsonPrimitive((Boolean) object);
			} else if (object instanceof String) {
				return new JsonPrimitive((String) object);
			} else if (object instanceof Character) {
				return new JsonPrimitive((Character) object);
			}
		}
		return null;
	}

	public static List<Object> toArray(JsonArray jsonArray) {
		List<Object> list = null;
		if (jsonArray != null) {
			list = new ArrayList<>();
			for (JsonElement jsonElement : jsonArray) {
				if (jsonElement instanceof JsonArray) {
					List<Object> rawArray = toArray((JsonArray) jsonElement);
					if (rawArray != null) list.add(rawArray);
				} else if (jsonElement instanceof JsonObject) {
					Map<String, Object> rawMap = toMap((JsonObject) jsonElement);
					if (rawMap != null) list.add(rawMap);
				} else if (jsonElement instanceof JsonPrimitive) {
					Object rawObject = toPrimitive((JsonPrimitive) jsonElement);
					if (rawObject != null) list.add(rawObject);
				}
			}
		}
		return list;
	}

	public static Map<String, Object> toMap(JsonObject jsonObject) {
		Map<String, Object> map = null;
		if (jsonObject != null) {
			map = new LinkedHashMap<>();
			for (Map.Entry<String, JsonElement> jsonElementEntry : jsonObject.entrySet()) {
				if (jsonElementEntry.getValue() instanceof JsonArray) {
					List<Object> rawArray = toArray((JsonArray) jsonElementEntry.getValue());
					if (rawArray != null) map.put(jsonElementEntry.getKey(), rawArray);
				} else if (jsonElementEntry.getValue() instanceof JsonObject) {
					Map<String, Object> rawMap = toMap((JsonObject) jsonElementEntry.getValue());
					if (rawMap != null) map.put(jsonElementEntry.getKey(), rawMap);
				} else if (jsonElementEntry.getValue() instanceof JsonPrimitive) {
					Object rawObject = toPrimitive((JsonPrimitive) jsonElementEntry.getValue());
					if (rawObject != null) map.put(jsonElementEntry.getKey(), rawObject);
				}
			}
		}
		return map;
	}

	public static Object toPrimitive(JsonPrimitive jsonPrimitive) {
		Object primitive = null;
		if (jsonPrimitive != null) {
			if (jsonPrimitive.isBoolean()) {
				primitive = jsonPrimitive.getAsBoolean();
			} else if (jsonPrimitive.isNumber()) {
				Number primitiveNumber = jsonPrimitive.getAsNumber();
				if (primitiveNumber instanceof Byte) primitive = primitiveNumber.byteValue();
				else if (primitiveNumber instanceof Double) primitive = primitiveNumber.doubleValue();
				else if (primitiveNumber instanceof Float) primitive = primitiveNumber.floatValue();
				else if (primitiveNumber instanceof Integer) primitive = primitiveNumber.intValue();
				else if (primitiveNumber instanceof Long) primitive = primitiveNumber.longValue();
				else if (primitiveNumber instanceof Short) primitive = primitiveNumber.shortValue();
				else primitive = primitiveNumber;
			} else if (jsonPrimitive.isString()) {
				primitive = jsonPrimitive.getAsString();
			}
		}
		return primitive;
	}

}
