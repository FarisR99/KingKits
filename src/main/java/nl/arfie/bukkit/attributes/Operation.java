package nl.arfie.bukkit.attributes;

/**
 * An Operation will indicate the way that an Attribute's amount value changes
 * the user's base value of the given {@link AttributeType}. Default is
 * {@link #ADD_NUMBER}.
 *
 * When applying operations, there are three variables: amount, base and output.
 * <ul>
 * <li><tt>amount</tt> indicates the amount of this {@link Attribute}</li>
 * <li><tt>base</tt> indicates the base value of this {@link AttributeType}. For
 * example: 0.7 for a Player's MOVEMENT_SPEED.</li>
 * <li><tt>ouput</tt> is the final value of this attribute.</li>
 * </ul>
 *
 * @author Ruud Verbeek
 * @see AttributeType, Attribute
 */
public enum Operation {

	/**
	 * Will add <tt>amount</tt> to <tt>output</tt>. Will be applied before any
	 * other operation.
	 */
	ADD_NUMBER(0),
	/**
	 * Will add <tt>amount * base</tt> to <tt>output</tt> after applying all
	 * {@link #ADD_NUMBER} operations and before applying any
	 * {@link #ADD_PERCENTAGE} operations.
	 */
	MULTIPLY_PERCENTAGE(1),
	/**
	 * Will add <tt>output * (1+amount)</tt> to the output, after applying all
	 * other operations.
	 */
	ADD_PERCENTAGE(2);
	public int id;

	Operation(int id) {
		this.id = id;
	}

	public static Operation fromID(int id) {
		for (Operation o : values()) {
			if (o.id == id) {
				return o;
			}
		}
		return null;
	}

}
