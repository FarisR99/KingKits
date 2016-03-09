package nl.arfie.bukkit.attributes;

import com.faris.kingkits.KingKits;
import nl.arfie.bukkit.attributes.wrapper.CraftItemStack;
import nl.arfie.bukkit.attributes.wrapper.MinecraftItemStack;
import nl.arfie.bukkit.attributes.wrapper.NBTTagCompound;
import nl.arfie.bukkit.attributes.wrapper.NBTTagList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Class to apply {@link Attribute}s or {@link Collection}s containing
 * Attributes to ItemStacks.
 * <p/>
 * <b>Example code:</b>
 * <pre>
 * // Instantiate some Attributes
 * Attribute a1 = new Attribute(AttributeType.ATTACK_DAMAGE,Operation.ADD_NUMBER,20.0),
 * 		a2 = new Attribute().setType(AttributeType.KNOCKBACK_RESISTANCE).setOperation(Operation.ADD_PERCENTAGE).setAmount(0.75);
 *
 * // Create an ItemStack with these Attributes
 * ItemStack is = new ItemStack(Material.NETHER_STAR);
 *
 * // Apply the attributes to said ItemStack
 * List<Attribute> list = new ArrayList<>();
 * list.add(a1);
 * list.add(a2);
 * Attributes.apply(is, list, true);
 * </pre>
 *
 * @author Ruud Verbeek
 * @see Attribute
 */
public class Attributes {

	/**
	 * Applies the given attribute to the given ItemStack
	 *
	 * @param original The original ItemStack to apply the attribute to
	 * @param attribute The Attribute to apply.
	 * @param replace Whether or not to remove the attributes that were already
	 * on the ItemStack
	 * @return A new ItemStack containing the attribute
	 */
	public static ItemStack apply(ItemStack original, Attribute attribute, boolean replace) {
		try {
			MinecraftItemStack stack = CraftItemStack.asNMSCopy(original);
			NBTTagCompound tag = stack.getTag();
			NBTTagList list = replace ? new NBTTagList() : tag.getList("AttributeModifiers", 10);
			list.add(attribute.write());
			tag.set("AttributeModifiers", list);
			stack.setTag(tag);
			return CraftItemStack.asCraftMirror(stack).getStack();
		} catch (InstantiationException | IllegalAccessException ex) {
			KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to apply attribute " + attribute.toString() + " to item stack!", ex);
			return original;
		}
	}

	/**
	 * Applies the given attributes to the given ItemStack
	 *
	 * @param original The original ItemStack to apply the attribute to
	 * @param attributes The Attributes to apply.
	 * @param replace Whether or not to remove the attributes that were already
	 * on the ItemStack
	 * @return A new ItemStack containing the attributes
	 */
	public static ItemStack apply(ItemStack original, Collection<? extends Attribute> attributes, boolean replace) {
		try {
			MinecraftItemStack stack = CraftItemStack.asNMSCopy(original);
			NBTTagCompound tag = stack.getTag();
			NBTTagList list = replace ? new NBTTagList() : tag.getList("AttributeModifiers", 10);
			for (Attribute attribute : attributes) {
				list.add(attribute.write());
			}
			tag.set("AttributeModifiers", list);
			stack.setTag(tag);
			return CraftItemStack.asCraftMirror(stack).getStack();
		} catch (InstantiationException | IllegalAccessException ex) {
			KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to aplly attributes " + attributes.toString() + " to item stack!", ex);
			return original;
		}
	}

	/**
	 * Returns a {@link List} containing the {@link Attribute}s on the given
	 * {@link ItemStack}.
	 *
	 * @param is the ItemStack to take the Attributes from
	 * @return a List containing the Attributes, or an empty list if there
	 * weren't any Attributes on the ItemStack or an error occurred.
	 */
	public static List<Attribute> fromStack(ItemStack is) {
		try {
			MinecraftItemStack mcis = CraftItemStack.asNMSCopy(is);
			NBTTagCompound tag = mcis.getTag();
			NBTTagList attributes;
			if ((attributes = tag.getList("AttributeModifiers", 10)) == null) {
				return new ArrayList<>();
			}
			List<Attribute> list = new ArrayList<>();
			for (int i = 0; i < attributes.size(); ++i) {
				NBTTagCompound attribute = attributes.get(i);
				list.add(Attribute.fromTag(attribute));
			}
			return list;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
			KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to load attributes  from " + is.toString(), ex);
			return new ArrayList<>();
		}
	}

}
