package nl.arfie.bukkit.attributes;

import com.faris.kingkits.helper.util.ObjectUtilities;
import com.faris.kingkits.helper.util.Utilities;
import com.google.common.base.Preconditions;
import nl.arfie.bukkit.attributes.wrapper.NBTTagCompound;
import org.bukkit.configuration.serialization.*;
import org.bukkit.inventory.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single Attribute that can be applied to ItemStacks.
 *
 * @author Ruud Verbeek
 * @see AttributeType, Operation
 */
public class Attribute implements ConfigurationSerializable {

	UUID uuid;
	AttributeType type;
	Operation operation;
	double amount;

	/**
	 * Constructs an Attribute with the given
	 * {@link AttributeType}, {@link Operation}, amount and {@link UUID}.
	 *
	 * @param type The type of this Attribute.
	 * @param operation The operation this Attribute uses to apply its amount to
	 * the final value.
	 * @param amount The amount of this Attribute.
	 * @param uuid This Attribute's {@link UUID}. Must be unique.
	 */
	public Attribute(AttributeType type, Operation operation, double amount, UUID uuid) {
		this.type = type;
		this.operation = operation == null ? Operation.ADD_NUMBER : operation;
		this.amount = amount;
		this.uuid = uuid;
	}

	/**
	 * Constructs an Attribute using the given {@link AttributeType}, amount and
	 * {@link UUID}. {@link Operation#ADD_NUMBER} will be used as
	 * {@link Operation}.
	 *
	 * @param type The type of this Attribute.
	 * @param amount The amount of this Attribute.
	 * @param uuid This Attribute's {@link UUID}. Must be unique.
	 */
	public Attribute(AttributeType type, double amount, UUID uuid) {
		this(type, Operation.ADD_NUMBER, amount, uuid);
	}

	/**
	 * Constructs an Attribute using the given
	 * {@link AttributeType}, {@link Operation} and amount. A random
	 * {@link UUID} will be used.
	 *
	 * @param type The type of this Attribute.
	 * @param operation The operation this Attribute uses to apply its amount to
	 * the final value.
	 * @param amount The amount of this Attribute
	 */
	public Attribute(AttributeType type, Operation operation, double amount) {
		this(type, operation, amount, UUID.randomUUID());
	}

	/**
	 * Constructs an Attribute using the give {@link AttributeType} and amount.
	 * {@link Operation#ADD_NUMBER} will be used as {@link Operation} and a
	 * random {@link UUID} will be used.
	 *
	 * @param type The type of this Attribute.
	 * @param amount The amount of this Attribute.
	 */
	public Attribute(AttributeType type, double amount) {
		this(type, Operation.ADD_NUMBER, amount, UUID.randomUUID());
	}

	/**
	 * Constructs an Attribute with {@link Operation#ADD_NUMBER} as
	 * {@link Operation}, 0.0 as amount and a random {@link UUID}. The
	 * {@link AttributeType} must still be set using {@link #setType}.
	 */
	public Attribute() {
		this(null, Operation.ADD_NUMBER, 0.0, UUID.randomUUID());
	}

	/**
	 * Sets the {@link AttributeType} of this Attribute.
	 *
	 * @param type The {@link AttributeType}
	 * @return This Attribute
	 */
	public Attribute setType(AttributeType type) {
		this.type = type;
		return this;
	}

	/**
	 * Sets the {@link Operation} of this Attribute.
	 *
	 * @param operation The {@link Operation}
	 * @return This Attribute
	 */
	public Attribute setOperation(Operation operation) {
		this.operation = operation;
		return this;
	}

	/**
	 * Sets the amount of this Attribute.
	 *
	 * @param amount The amount
	 * @return This Attribute
	 */
	public Attribute setAmount(double amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Sets the {@link UUID} of this Attribute.
	 *
	 * @param uuid The {@link UUID}
	 * @return This Attribute
	 */
	public Attribute setUUID(UUID uuid) {
		this.uuid = uuid;
		return this;
	}

	/**
	 * Returns this Attribute's {@link AttributeType}
	 *
	 * @return The {@link AttributeType}
	 */
	public AttributeType getType() {
		return type;
	}

	/**
	 * Returns this Attribute's {@link Operation}
	 *
	 * @return The {@link Operation}
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Returns this Attribute's amount
	 *
	 * @return The amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * Returns this Attribute's {@link UUID}
	 *
	 * @return The {@link UUID}
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	NBTTagCompound write() throws InstantiationException, IllegalAccessException {
		Preconditions.checkNotNull(type, "Type cannot be null.");
		if (this.operation == null) operation = Operation.ADD_NUMBER;
		if (this.uuid == null) this.uuid = UUID.randomUUID();

		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("AttributeName", type.minecraftID);
		tag.setString("Name", type.minecraftID);
		tag.setInt("Operation", operation.id);
		tag.setDouble("Amount", amount);
		tag.setLong("UUIDMost", uuid.getMostSignificantBits());
		tag.setLong("UUIDLeast", uuid.getLeastSignificantBits());
		return tag;
	}

	/**
	 * Returns whether or not this attribute is equal to o.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Attribute)) {
			return false;
		}
		Attribute a = (Attribute) o;
		return (uuid.equals(a.uuid) || (type == a.type && operation == a.operation && amount == a.amount));
	}

	static Attribute fromTag(NBTTagCompound c) throws IllegalArgumentException {
		Attribute a = new Attribute();
		if (c.hasKey("AttributeName")) {
			a.setType(AttributeType.fromMinecraftID(c.getString("AttributeName")));
		} else {
			throw new IllegalArgumentException("No AttributeName specified.");
		}
		if (c.hasKey("Operation")) {
			a.setOperation(Operation.fromID(c.getInt("Operation")));
		} else {
			throw new IllegalArgumentException("No Operation specified.");
		}
		if (c.hasKey("Amount")) {
			a.setAmount(c.getDouble("Amount"));
		} else {
			throw new IllegalArgumentException("No Amount specified.");
		}
		if (c.hasKey("UUIDMost") && c.hasKey("UUIDLeast")) {
			a.setUUID(new UUID(c.getLong("UUIDLeast"), c.getLong("UUIDMost")));
		} else {
			a.setUUID(UUID.randomUUID());
		}
		return a;
	}

	/**
	 * Shortcut to {@link Attributes#apply}.
	 *
	 * @param is ItemStack to apply this attribute to
	 * @param replace Whether or not to replace existing attributes on the
	 * ItemStack
	 * @return the new ItemStack containing this Attribute
	 */
	public ItemStack apply(ItemStack is, boolean replace) {
		return Attributes.apply(is, this, replace);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serializedAttribute = new LinkedHashMap<>();
		serializedAttribute.put("UUID", this.getUUID().toString());
		serializedAttribute.put("Type", this.getType().name());
		serializedAttribute.put("Amount", this.getAmount());
		if (this.operation != null) serializedAttribute.put("Operation", this.getOperation().name());
		return serializedAttribute;
	}

	public static Attribute deserialize(Map<String, Object> serializedAttribute) {
		Attribute deserializedAttribute = null;
		if (serializedAttribute != null && serializedAttribute.containsKey("Type")) {
			AttributeType attributeType = null;
			try {
				attributeType = AttributeType.valueOf(ObjectUtilities.getObject(serializedAttribute, String.class, "Type"));
			} catch (Exception ignored) {
			}
			if (attributeType != null) {
				UUID attributeUUID = Utilities.isUUID(serializedAttribute.get("UUID")) ? UUID.fromString(serializedAttribute.get("UUID").toString()) : null;
				double attributeAmount = ObjectUtilities.getObject(serializedAttribute, Number.class, "Amount", 0D).doubleValue();
				Operation operation = null;
				try {
					operation = Operation.valueOf(ObjectUtilities.getObject(serializedAttribute, String.class, "Operation"));
				} catch (Exception ignored) {
				}
				if (operation == null) operation = Operation.ADD_NUMBER;
				if (attributeUUID == null)
					deserializedAttribute = new Attribute(attributeType, operation, attributeAmount);
				else deserializedAttribute = new Attribute(attributeType, operation, attributeAmount, attributeUUID);
			}
		}
		return deserializedAttribute;
	}

}
