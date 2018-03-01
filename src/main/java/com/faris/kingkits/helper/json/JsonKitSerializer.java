package com.faris.kingkits.helper.json;

import com.faris.kingkits.Kit;
import com.faris.kingkits.helper.util.JsonUtilities;
import com.faris.kingkits.helper.util.Utilities;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class JsonKitSerializer implements JsonSerializer<Kit>, JsonDeserializer<Kit> {

	@Override
	public JsonElement serialize(final Kit kit, Type type, JsonSerializationContext jsonSerializationContext) {
		return kit != null ? JsonUtilities.fromMap(kit.serialize()) : new JsonObject();
	}

	@Override
	public Kit deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		if (jsonElement instanceof JsonObject) {
			Map<String, Object> serializedKit = JsonUtilities.toMap((JsonObject) jsonElement);
			return Kit.deserialize(serializedKit);
		} else if (jsonElement instanceof JsonPrimitive) {
			JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonElement;
			if (jsonPrimitive.isString()) {
				JsonObject jsonObject = Utilities.getGsonParser().fromJson(jsonPrimitive.getAsString(), JsonObject.class);
				return this.deserialize(jsonObject, type, jsonDeserializationContext);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
