package nl.arfie.bukkit.attributes.wrapper;

import com.faris.kingkits.KingKits;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class MinecraftItemStack extends SourceWrapper {

	private final static Class<?> clazz = loadClass("net.minecraft.server", "ItemStack");
	private static Field tagField;

	static {
		try {
			tagField = clazz.getDeclaredField("tag");
			tagField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException ex) {
			KingKits.getInstance().getLogger().info("Failed to set \"Tag\" accessible!");
		}
	}

	public MinecraftItemStack() throws InstantiationException, IllegalAccessException {
		super(clazz.newInstance());
	}

	public MinecraftItemStack(Object instance) {
		super(instance);
	}

	public void setTag(NBTTagCompound tag) {
		try {
			tagField.set(instance, tag.instance);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
			KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to set tag for " + instance.getClass().getName() + "!", ex);
		}
	}

	public NBTTagCompound getTag() {
		try {
			return new NBTTagCompound(tagField.get(instance));
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException | InstantiationException ex) {
			KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to get tag for " + instance.getClass().getName() + "!", ex);
			return null;
		}
	}

}
