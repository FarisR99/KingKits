package nl.arfie.bukkit.attributes.wrapper;

import com.faris.kingkits.KingKits;

import java.util.logging.Level;

public class NBTTagCompound extends NBTBase {

	private final static Class<?> clazz = loadClass("net.minecraft.server", "NBTTagCompound");

	static {
		declareMethod(clazz, "setDouble", String.class, double.class);
		declareMethod(clazz, "getDouble", String.class);
		declareMethod(clazz, "setInt", String.class, int.class);
		declareMethod(clazz, "getInt", String.class);
		declareMethod(clazz, "setString", String.class, String.class);
		declareMethod(clazz, "getString", String.class);
		declareMethod(clazz, "setLong", String.class, long.class);
		declareMethod(clazz, "getLong", String.class);
		declareMethod(clazz, "set", String.class, loadClass("net.minecraft.server", "NBTBase"));
		declareMethod(clazz, "getList", String.class, int.class);
		declareMethod(clazz, "hasKey", String.class);
	}

	public NBTTagCompound() throws InstantiationException, IllegalAccessException {
		super(clazz.newInstance());
	}

	public NBTTagCompound(Object instance) throws InstantiationException, IllegalAccessException {
		super(instance == null ? clazz.newInstance() : instance);
	}

	public void setDouble(String key, double value) {
		invokeMethod("setDouble", key, value);
	}

	public double getDouble(String key) {
		return (double) invokeMethod("getDouble", key);
	}

	public void setInt(String key, int value) {
		invokeMethod("setInt", key, value);
	}

	public int getInt(String key) {
		return (int) invokeMethod("getInt", key);
	}

	public void setString(String key, String value) {
		invokeMethod("setString", key, value);
	}

	public String getString(String key) {
		return (String) invokeMethod("getString", key);
	}

	public void setLong(String key, long value) {
		invokeMethod("setLong", key, value);
	}

	public long getLong(String key) {
		return (long) invokeMethod("getLong", key);
	}

	public void set(String key, NBTBase value) {
		invokeMethod("set", key, value.instance);
	}

	public void set(String key, NBTTagList value) {
		invokeMethod("set", key, value.instance);
	}

	public NBTTagList getList(String key, int paramInt) {
		try {
			return new NBTTagList(invokeMethod("getList", key, paramInt));
		} catch (InstantiationException | IllegalAccessException ex) {
			KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to get List for " + instance.getClass().getName() + "!", ex);
			return null;
		}
	}

	public boolean hasKey(String key) {
		return (boolean) invokeMethod("hasKey", key);
	}

}
