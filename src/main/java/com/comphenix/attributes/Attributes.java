// The MIT License (MIT)
//
// Copyright (c) 2015 Kristian Stangeland
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
// files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
// modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the 
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
// WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.comphenix.attributes;

import com.comphenix.attributes.NbtFactory.NbtCompound;
import com.comphenix.attributes.NbtFactory.NbtList;
import com.faris.kingkits.helper.util.ObjectUtilities;
import com.faris.kingkits.helper.util.Utilities;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

public class Attributes {
	public enum Operation {
		ADD_NUMBER(0),
		MULTIPLY_PERCENTAGE(1),
		ADD_PERCENTAGE(2);
		private int id;

		Operation(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static Operation fromId(int id) {
			// Linear scan is very fast for small N
			for (Operation op : values()) {
				if (op.getId() == id) {
					return op;
				}
			}
			throw new IllegalArgumentException("Corrupt operation ID " + id + " detected.");
		}
	}

	public static class AttributeType {
		private static ConcurrentMap<String, AttributeType> LOOKUP = Maps.newConcurrentMap();
		public static final AttributeType GENERIC_MAX_HEALTH = new AttributeType("generic.maxHealth").register();
		public static final AttributeType GENERIC_FOLLOW_RANGE = new AttributeType("generic.followRange").register();
		public static final AttributeType GENERIC_ATTACK_DAMAGE = new AttributeType("generic.attackDamage").register();
		public static final AttributeType GENERIC_MOVEMENT_SPEED = new AttributeType("generic.movementSpeed").register();
		public static final AttributeType GENERIC_KNOCKBACK_RESISTANCE = new AttributeType("generic.knockbackResistance").register();
		public static final AttributeType GENERIC_ARMOUR_DEFENSE = new AttributeType("generic.armor").register();
		public static final AttributeType GENERIC_ARMOUR_TOUGHNESS = new AttributeType("generic.armorToughness").register();
		public static final AttributeType GENERIC_ATTACK_SPEED = new AttributeType("generic.attackSpeed").register();
		public static final AttributeType GENERIC_LUCK = new AttributeType("generic.luck").register();
		public static final AttributeType HORSE_JUMP_STRENGTH = new AttributeType("horse.jumpStrength").register();
		public static final AttributeType ZOMBIE_SPAWN_REINFORCEMENTS = new AttributeType("zombie.spawnReinforcements").register();

		private static Map<String, AttributeType> TYPE_LOOKUP = new HashMap<>();

		private final String minecraftId;

		/**
		 * Construct a new attribute type.
		 * <p>
		 * Remember to {@link #register()} the type.
		 *
		 * @param minecraftId - the ID of the type.
		 */
		public AttributeType(String minecraftId) {
			this.minecraftId = minecraftId;
		}

		/**
		 * Retrieve the associated minecraft ID.
		 *
		 * @return The associated ID.
		 */
		public String getMinecraftId() {
			return minecraftId;
		}

		/**
		 * Register the type in the central registry.
		 *
		 * @return The registered type.
		 */
		// Constructors should have no side-effects!
		public AttributeType register() {
			AttributeType old = LOOKUP.putIfAbsent(minecraftId, this);
			return old != null ? old : this;
		}

		/**
		 * Retrieve the attribute type associated with a given ID.
		 *
		 * @param minecraftId The ID to search for.
		 * @return The attribute type, or NULL if not found.
		 */
		public static AttributeType fromId(String minecraftId) {
			return LOOKUP.get(minecraftId);
		}

		/**
		 * Retrieve every registered attribute type.
		 *
		 * @return Every type.
		 */
		public static Iterable<AttributeType> values() {
			return LOOKUP.values();
		}

		public static AttributeType valueOf(String name) {
			if (name == null) return null;
			String modName = name.replace(' ', '_').toUpperCase();
			if (TYPE_LOOKUP.containsKey(modName)) {
				return TYPE_LOOKUP.get("GENERIC_" + modName);
			} else if (TYPE_LOOKUP.containsKey("GENERIC_" + modName)) {
				return TYPE_LOOKUP.get("GENERIC_" + modName);
			} else if (TYPE_LOOKUP.containsKey("ZOMBIE_" + modName)) {
				return TYPE_LOOKUP.get("ZOMBIE_" + modName);
			} else if (TYPE_LOOKUP.containsKey("HORSE_" + modName)) {
				return TYPE_LOOKUP.get("HORSE_" + modName);
			} else {
				return fromId(name);
			}
		}

		public static void initialiseNameMap() {
			TYPE_LOOKUP.clear();
			try {
				for (Field field : AttributeType.class.getFields()) {
					if (field.getType() == AttributeType.class) {
						TYPE_LOOKUP.put(field.getName(), (AttributeType) field.get(null));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static class Attribute implements ConfigurationSerializable {
		private NbtCompound data;

		private Attribute(Builder builder) {
			data = NbtFactory.createCompound();
			setAmount(builder.amount);
			setOperation(builder.operation);
			setAttributeType(builder.type);
			setName(builder.name);
			setUUID(builder.uuid);
			if (builder.slot != null) setSlot(builder.slot);
		}

		private Attribute(NbtCompound data) {
			this.data = data;
		}

		public double getAmount() {
			try {
				return data.getDouble("Amount", 0D);
			} catch (Exception ex) {
				ex.printStackTrace();
				return 0D;
			}
		}

		public void setAmount(double amount) {
			data.put("Amount", amount);
		}

		public Operation getOperation() {
			return Operation.fromId(data.getInteger("Operation", 0));
		}

		public void setOperation(Operation operation) {
			Preconditions.checkNotNull(operation, "operation cannot be NULL.");
			data.put("Operation", operation.getId());
		}

		public AttributeType getAttributeType() {
			return AttributeType.fromId(data.getString("AttributeName", null));
		}

		public void setAttributeType(AttributeType type) {
			Preconditions.checkNotNull(type, "type cannot be NULL.");
			data.put("AttributeName", type.getMinecraftId());
		}

		public String getName() {
			return data.getString("Name", null);
		}

		public void setName(String name) {
			Preconditions.checkNotNull(name, "name cannot be NULL.");
			data.put("Name", name);
		}

		public UUID getUUID() {
			return new UUID(data.getLong("UUIDMost", null), data.getLong("UUIDLeast", null));
		}

		public void setUUID(UUID id) {
			Preconditions.checkNotNull(id, "id cannot be NULL.");
			data.put("UUIDLeast", id.getLeastSignificantBits());
			data.put("UUIDMost", id.getMostSignificantBits());
		}

		public String getSlot() {
			return data.getString("Slot", null);
		}

		private boolean hasSlot() {
			return data.containsKey("Slot") && data.get("Slot") != null;
		}

		public void setSlot(String slot) {
			Preconditions.checkNotNull(slot, "slot cannot be NULL.");
			data.put("Slot", slot);
		}

		/**
		 * Construct a new attribute builder with a random UUID and default operation of adding numbers.
		 *
		 * @return The attribute builder.
		 */
		public static Builder newBuilder() {
			return new Builder().uuid(UUID.randomUUID()).operation(Operation.ADD_NUMBER);
		}

		@Override
		public Map<String, Object> serialize() {
			if (this.getAttributeType() == null) {
				return new HashMap<String, Object>() {
					{
						this.put("AttributeType", data.getString("AttributeName", null));
					}
				};
			}
			Map<String, Object> serializedAttribute = new LinkedHashMap<>();
			serializedAttribute.put("UUID", this.getUUID().toString());
			serializedAttribute.put("Name", this.getName());
			serializedAttribute.put("Type", this.getAttributeType().getMinecraftId());
			serializedAttribute.put("Amount", this.getAmount());
			if (this.hasSlot()) serializedAttribute.put("Slot", this.getSlot());
			Operation operation = this.getOperation();
			if (operation != null) serializedAttribute.put("Operation", operation.name());
			return serializedAttribute;
		}

		public static Attribute deserialize(Map<String, Object> serializedAttribute) {
			Attribute deserializedAttribute = null;
			if (serializedAttribute != null && serializedAttribute.containsKey("Name")) {
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
					Builder attributeBuilder = new Builder().name(ObjectUtilities.getObject(serializedAttribute, String.class, "Name")).type(attributeType).operation(operation).amount(attributeAmount);
					if (attributeUUID != null) attributeBuilder.uuid(attributeUUID);
					if (serializedAttribute.containsKey("Slot"))
						attributeBuilder.slot(ObjectUtilities.getObject(serializedAttribute, String.class, "Slot"));
					deserializedAttribute = attributeBuilder.build();
				}
			}
			return deserializedAttribute;
		}

		// Makes it easier to construct an attribute
		public static class Builder {
			private double amount;
			private Operation operation = Operation.ADD_NUMBER;
			private AttributeType type;
			private String name;
			private UUID uuid;
			private String slot;

			private Builder() {
				// Don't make this accessible
			}

			public Builder amount(double amount) {
				this.amount = amount;
				return this;
			}

			public Builder operation(Operation operation) {
				this.operation = operation;
				return this;
			}

			public Builder type(AttributeType type) {
				this.type = type;
				return this;
			}

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder uuid(UUID uuid) {
				this.uuid = uuid;
				return this;
			}

			public Builder slot(String slot) {
				this.slot = slot;
				return this;
			}

			public Attribute build() {
				return new Attribute(this);
			}
		}
	}

	// This may be modified
	public ItemStack stack;
	private NbtList attributes;

	public Attributes(ItemStack stack) {
		// Create a CraftItemStack (under the hood)
		this.stack = NbtFactory.getCraftItemStack(stack);
		loadAttributes(false);
	}

	/**
	 * Load the NBT list from the TAG compound.
	 *
	 * @param createIfMissing - create the list if its missing.
	 */
	private void loadAttributes(boolean createIfMissing) {
		if (this.attributes == null) {
			NbtCompound nbt = NbtFactory.fromItemTag(this.stack);
			this.attributes = nbt.getList("AttributeModifiers", createIfMissing);
		}
	}

	/**
	 * Remove the NBT list from the TAG compound.
	 */
	private void removeAttributes() {
		NbtCompound nbt = NbtFactory.fromItemTag(this.stack);
		nbt.remove("AttributeModifiers");
		this.attributes = null;
	}

	/**
	 * Retrieve the modified item stack.
	 *
	 * @return The modified item stack.
	 */
	public ItemStack getStack() {
		return stack;
	}

	/**
	 * Retrieve the number of attributes.
	 *
	 * @return Number of attributes.
	 */
	public int size() {
		return attributes != null ? attributes.size() : 0;
	}

	/**
	 * Add a new attribute to the list.
	 *
	 * @param attribute - the new attribute.
	 */
	public void add(Attribute attribute) {
		Preconditions.checkNotNull(attribute.getName(), "must specify an attribute name.");
		loadAttributes(true);
		int removeIndex = -1;
		for (int i = 0; i < this.attributes.size(); i++) {
			NbtCompound attributeData = (NbtCompound) this.attributes.get(i);
			if (attribute.getName().equals(attributeData.getString("AttributeName", null))) {
				removeIndex = i;
				break;
			}
		}
		if (removeIndex != -1) this.attributes.remove(removeIndex);
		this.attributes.add(attribute.data);
	}

	/**
	 * Remove the first instance of the given attribute.
	 * <p>
	 * The attribute will be removed using its UUID.
	 *
	 * @param attribute - the attribute to remove.
	 * @return TRUE if the attribute was removed, FALSE otherwise.
	 */
	public boolean remove(Attribute attribute) {
		if (attributes == null)
			return false;
		UUID uuid = attribute.getUUID();

		for (Iterator<Attribute> it = values().iterator(); it.hasNext(); ) {
			if (Objects.equal(it.next().getUUID(), uuid)) {
				it.remove();

				// Last removed attribute?
				if (size() == 0) {
					removeAttributes();
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove every attribute.
	 */
	public void clear() {
		removeAttributes();
	}

	/**
	 * Retrieve the attribute at a given index.
	 *
	 * @param index - the index to look up.
	 * @return The attribute at that index.
	 */
	public Attribute get(int index) {
		if (size() == 0)
			throw new IllegalStateException("Attribute list is empty.");
		return new Attribute((NbtCompound) attributes.get(index));
	}

	/**
	 * Retrieve all the attributes.
	 *
	 * @return The attributes.
	 */
	public List<Attribute> getAll() {
		if (size() == 0) return new ArrayList<>();
		List<Attribute> attributesList = new ArrayList<>();
		for (Object attribute : this.attributes) {
			attributesList.add(new Attribute((NbtCompound) attribute));
		}
		return attributesList;
	}

	// We can't make Attributes itself iterable without splitting it up into separate classes
	public Iterable<Attribute> values() {
		return () -> {
			// Handle the empty case
			if (size() == 0)
				return Collections.<Attribute>emptyList().iterator();

			return Iterators.transform(attributes.iterator(), element -> new Attribute((NbtCompound) element));
		};
	}
}