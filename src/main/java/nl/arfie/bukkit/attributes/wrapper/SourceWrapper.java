package nl.arfie.bukkit.attributes.wrapper;

import com.faris.kingkits.KingKits;
import org.bukkit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

abstract class SourceWrapper {

	protected Object instance;

	private static final HashMap<String, Method> methods = new HashMap<>();

	protected static String v;

	static {
		String pkgName = Bukkit.getServer().getClass().getPackage().getName();
		v = "." + pkgName.substring(pkgName.lastIndexOf('.') + 1) + ".";
	}

	public SourceWrapper(Object instance) {
		this.instance = instance;
	}

	protected static Class<?> loadClass(String start, String end) {
		try {
			return Bukkit.class.getClassLoader().loadClass(start + v + end);
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}

	protected static void declareMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			methods.put(name, clazz.getMethod(name, parameterTypes));
		} catch (NoSuchMethodException | SecurityException ex) {
			if (parameterTypes != null && parameterTypes.length > 0) {
				KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to get declared method " + name + " with parameters " + Arrays.deepToString(parameterTypes) + " for " + clazz.getClass().getName() + "!", ex);
			} else {
				KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to get declared method " + name + " for " + clazz.getClass().getName() + "!", ex);
			}
		}
	}

	protected Object invokeMethod(String name, Object... args) {
		try {
			return methods.get(name).invoke(instance, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			if (args != null && args.length > 0) {
				KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to invoke method " + name + " with parameters " + Arrays.deepToString(args) + " for " + instance.getClass().getName() + "!", ex);
			} else {
				KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to invoke method " + name + " for " + instance.getClass().getName() + "!", ex);
			}
			return null;
		}
	}

	protected static Object invokeStaticMethod(String name, Object... args) {
		try {
			return methods.get(name).invoke(null, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			if (args != null && args.length > 0) {
				KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to invoke static metod " + name + " with parameters " + Arrays.deepToString(args) + "!", ex);
			} else {
				KingKits.getInstance().getLogger().log(Level.SEVERE, "Failed to invoke static metod " + name + "!", ex);
			}
			return null;
		}
	}

}
