package nl.arfie.bukkit.attributes;

/**
 * An AttributeType is the type of effect an {@link Attribute} will have on its
 * user.
 *
 * @author Ruud Verbeek
 * @see Attribute
 */
public enum AttributeType {

	/**
	 * Will set the user's maximum health to the given number of life points.
	 * One life point equals half a heart.
	 */
	MAX_HEALTH("generic.maxHealth"),
	/**
	 * Will set the user's number of blocks in which he can find and track other
	 * entities.
	 */
	FOLLOW_RANGE("generic.followRange"),
	/**
	 * Will set the movement speed of the user. Default is 0.7.
	 */
	MOVEMENT_SPEED("generic.movementSpeed"),
	/**
	 * Will set how resistive the user is to knockbacks. 0.0 = no knockback
	 * resistance, 1.0 = full knockback resistance.
	 */
	KNOCKBACK_RESISTANCE("generic.knockbackResistance"),
	/**
	 * Will set the user's armour defense points. 0.0 = no defense
	 * 0.0 = no defense, 30.0 = max defense.
	 */
	ARMOUR_DEFENSE("generic.armor"),
	/**
	 * Will set the number of life points the user's attacks will normally take
	 * off when hitting another Damageable.
	 */
	ATTACK_DAMAGE("generic.attackDamage"),
	/**
	 * Determines speed at which attack strength recharges. Value is the number of full-strength attacks per second.
	 */
	ATTACK_SPEED("generic.attackSpeed"),
	/**
	 * Affects the results of loot tables (e.g. when opening chests or chest minecarts, fishing, and killing mobs).
	 */
	LUCK("generic.luck"),
	/**
	 * Will set a horse's jumping strength. Default is 0.7.
	 */
	JUMP_STRENGTH("horse.jumpStrength"),
	/**
	 * Will set if a zombie should spawn other "reinforcing" zombies when hit.
	 * 0.0 = don't spawn reinforcements, 1.0 = spawn reinforcements.
	 */
	SPAWN_REINFORCEMENTS("zombie.spawnReinforcements");

	public String minecraftID;

	AttributeType(String minecraftID) {
		this.minecraftID = minecraftID;
	}

	public static AttributeType fromMinecraftID(String id) {
		for (AttributeType t : values()) {
			if (t.minecraftID.equalsIgnoreCase(id)) {
				return t;
			}
		}
		return null;
	}

}
