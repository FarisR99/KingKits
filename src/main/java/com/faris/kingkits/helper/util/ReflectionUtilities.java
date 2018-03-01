package com.faris.kingkits.helper.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author KingFaris10
 * @version 1.2.0
 */
@SuppressWarnings("rawtypes")
public class ReflectionUtilities {

	private static String BUKKIT_VERSION = "";
	private static String OBC_PREFIX = "";
	private static String NMS_PREFIX = "";

	static {
		try {
			OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
			NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
			BUKKIT_VERSION = OBC_PREFIX.replace(".", ",").split(",")[3];
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private ReflectionUtilities() {
	}

	/**
	 * Get a class from OBC.
	 *
	 * @param className - The name of the class.
	 * @return The class from OBC package.
	 */
	public static Class getBukkitClass(String className) {
		try {
			return Class.forName(OBC_PREFIX + "." + className);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Get the Bukkit NMS version ID.
	 *
	 * @return The bukkit NMS version ID.
	 */
	public static String getBukkitVersion() {
		return BUKKIT_VERSION;
	}

	/**
	 * Get a class within a class.
	 *
	 * @param clazz - The main class.
	 * @param className - The class' name.
	 * @return The class within a class.
	 */
	public static Class getClass(Class clazz, String className) {
		if (clazz != null && className != null) {
			try {
				do {
					for (Class insideClass : clazz.getDeclaredClasses()) {
						if (insideClass != null && insideClass.getSimpleName().equals(className)) return insideClass;
					}
					for (Class insideClass : clazz.getClasses()) {
						if (insideClass != null && insideClass.getSimpleName().equals(className)) return insideClass;
					}
				} while (clazz.getSuperclass() != Object.class && ((clazz = clazz.getSuperclass()) != null));
			} catch (Exception ignore) {
			}
		}
		return null;
	}

	/**
	 * Get a constructor from a class by the constructor index.
	 *
	 * @param clazz - The class.
	 * @param constructorIndex - The constructor's index in the array of constructors. E.g. 0 for the first constructor.
	 * @return The constructor from a class.
	 */
	public static ConstructorInvoker getConstructor(Class clazz, int constructorIndex) {
		if (clazz != null) {
			try {
				Constructor constructor = clazz.getConstructors()[constructorIndex];
				if (constructor == null) constructor = clazz.getDeclaredConstructors()[constructorIndex];
				if (constructor != null) return new ConstructorInvoker(constructor);
				else if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
					return getConstructor(clazz.getSuperclass(), constructorIndex);
			} catch (ArrayIndexOutOfBoundsException ignore) {
				try {
					Constructor method = clazz.getDeclaredConstructors()[constructorIndex];
					if (method != null) return new ConstructorInvoker(method);
					else if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
						return getConstructor(clazz.getSuperclass(), constructorIndex);
				} catch (Exception ignored) {
				}
			} catch (Exception ignore) {
			}
		}
		return null;
	}

	/**
	 * Get a constructor from a class by the parameter types.
	 *
	 * @param clazz - The class.
	 * @param parameterTypes - The parameter types (classes).
	 * @return The constructor from a class.
	 */
	public static ConstructorInvoker getConstructor(Class clazz, Class... parameterTypes) {
		if (clazz != null) {
			try {
				Constructor constructor = clazz.getConstructor(parameterTypes);
				if (constructor == null) constructor = clazz.getDeclaredConstructor(parameterTypes);
				if (constructor != null) return new ConstructorInvoker(constructor);
				else if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
					return getConstructor(clazz.getSuperclass(), parameterTypes);
			} catch (NoSuchMethodException ignore) {
				try {
					Constructor constructor = clazz.getDeclaredConstructor(parameterTypes);
					if (constructor != null) return new ConstructorInvoker(constructor);
					else if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
						return getConstructor(clazz.getSuperclass(), parameterTypes);
				} catch (Exception ignored) {
				}
			} catch (Exception ignore) {
			}
		}
		return null;
	}

	/**
	 * Get all the declared fields in a class.
	 *
	 * @param clazz - The class.
	 * @return The list of the declared fields.
	 */
	public static List<FieldAccess> getDeclaredFields(Class clazz, boolean superClass) {
		List<FieldAccess> fieldList = new ArrayList<>();
		if (clazz != null) {
			try {
				if (superClass) {
					do {
						for (Field field : clazz.getDeclaredFields()) {
							if (field != null) fieldList.add(new FieldAccess(field));
						}
					} while (clazz.getSuperclass() != Object.class && ((clazz = clazz.getSuperclass()) != null));
				} else {
					for (Field field : clazz.getDeclaredFields()) {
						if (field != null) fieldList.add(new FieldAccess(field));
					}
				}
			} catch (Exception ignore) {
			}
		}
		return fieldList;
	}

	/**
	 * Get an enum constant from an enum.
	 *
	 * @param enumClass - The class (that's an enum)
	 * @param enumName - The constant's name
	 * @return The enum constant.
	 */
	public static Object getEnum(Class enumClass, String enumName) {
		if (enumClass != null) {
			for (Object ob : enumClass.getEnumConstants()) {
				if (ob != null && ob.toString().equals(enumName)) return ob;
			}
		}
		return null;
	}

	/**
	 * Get a field from a class by the field name.
	 *
	 * @param clazz - The class.
	 * @param fieldName - The name of the method.
	 * @return The field from a class.
	 */
	public static FieldAccess getField(Class clazz, String fieldName) {
		if (clazz != null && fieldName != null) {
			do {
				try {
					Field field = clazz.getField(fieldName);
					if (field == null) field = clazz.getDeclaredField(fieldName);
					if (field != null) return new FieldAccess(field);
				} catch (NoSuchFieldException ex) {
					try {
						Field field = clazz.getDeclaredField(fieldName);
						if (field != null) return new FieldAccess(field);
					} catch (Exception ignored) {
					}
				} catch (Exception ignored) {
				}
			} while (clazz.getSuperclass() != Object.class && ((clazz = clazz.getSuperclass()) != null));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Get a field as a type.
	 * @param instance - The instance as an Object.
	 * @param fieldName - The field name.
	 * @return The field's value for that instance.
	 */
	public static <T> T getField(Object instance, String fieldName) {
		Class<?> checkClass = instance.getClass();
		do {
			try {
				Field field = checkClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return (T) field.get(instance);
			} catch (Exception ignored) {
			}
		} while (checkClass.getSuperclass() != Object.class && ((checkClass = checkClass.getSuperclass()) != null));
		return null;
	}

	/**
	 * Get a field from a class by the field type's class name.
	 *
	 * @param clazz - The class.
	 * @param className - The field type's name.
	 * @param fieldIndex - The field index.
	 * @return The field from a class.
	 */
	public static FieldAccess getFieldByClass(Class clazz, String className, int fieldIndex) {
		List<FieldAccess> fieldAccessList = new LinkedList<>();
		for (FieldAccess field : getFields(clazz)) {
			if (field.getField().getType().getSimpleName().equals(className)) fieldAccessList.add(field);
		}
		return fieldIndex < fieldAccessList.size() ? fieldAccessList.get(fieldIndex) : null;
	}

	/**
	 * Get all the fields in a class.
	 *
	 * @param clazz - The class.
	 * @return The Map of the fields and the old accessibility from a class.
	 */
	public static List<FieldAccess> getFields(Class clazz) {
		List<FieldAccess> fieldList = new ArrayList<>();
		if (clazz != null) {
			try {
				for (Field field : clazz.getFields()) {
					if (field != null) fieldList.add(new FieldAccess(field));
				}
				for (Field field : clazz.getDeclaredFields()) {
					if (field != null) fieldList.add(new FieldAccess(field));
				}
			} catch (Exception ignored) {
			}
		}
		return fieldList;
	}

	/**
	 * Get a method from a class by the method name.
	 *
	 * @param clazz - The class.
	 * @param methodName - The name of the method.
	 * @return The method from a class.
	 */
	@SuppressWarnings("unchecked")
	public static MethodInvoker getMethod(Class clazz, String methodName, Class... parameterTypes) {
		return getMethod(clazz, methodName, true, parameterTypes);
	}

	/**
	 * Get a method from a class by the method name.
	 *
	 * @param clazz - The class.
	 * @param methodName - The name of the method.
	 * @return The method from a class.
	 */
	@SuppressWarnings("unchecked")
	public static MethodInvoker getMethod(Class clazz, String methodName, boolean superClasses, Class... parameterTypes) {
		if (clazz != null && methodName != null) {
			try {
				Method method = clazz.getMethod(methodName, parameterTypes);
				if (method == null) method = clazz.getDeclaredMethod(methodName, parameterTypes);
				if (method != null) return new MethodInvoker(method);
				else if (superClasses && clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
					return getMethod(clazz.getSuperclass(), methodName, parameterTypes);
			} catch (NoSuchMethodException ignore) {
				try {
					Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
					if (method != null) return new MethodInvoker(method);
					else if (superClasses && clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
						return getMethod(clazz.getSuperclass(), methodName, parameterTypes);
				} catch (Exception ignored) {
				}
			} catch (Exception ignore) {
			}
		}
		return null;
	}

	/**
	 * Get all the methods in a class and its super class(es).
	 *
	 * @param clazz - The class.
	 * @return The List of methods in a class and its super class(es).
	 */
	public static List<MethodInvoker> getMethods(Class clazz) {
		return getMethods(clazz, true);
	}

	/**
	 * Get all the methods in a class and optionally its super class.
	 *
	 * @param clazz - The class.
	 * @param showSuperclassMethods - Return methods from the super class.
	 * @return The List of methods in a class.
	 */
	public static List<MethodInvoker> getMethods(Class clazz, boolean showSuperclassMethods) {
		List<MethodInvoker> methodList = new ArrayList<>();
		if (clazz != null) {
			try {
				for (Method method : clazz.getMethods()) {
					if (method != null) {
						boolean isSuperclassMethod = false;
						if (!showSuperclassMethods && clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
							for (Method superMethod : clazz.getSuperclass().getMethods()) {
								if (superMethod != null && superMethod.getName().equals(method.getName())) {
									isSuperclassMethod = true;
									break;
								}
							}
						}
						if (showSuperclassMethods || !isSuperclassMethod) methodList.add(new MethodInvoker(method));
					}
				}
				for (Method method : clazz.getDeclaredMethods()) {
					if (method != null) {
						boolean isSuperclassMethod = false;
						if (!showSuperclassMethods && clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
							for (Method superMethod : clazz.getSuperclass().getDeclaredMethods()) {
								if (superMethod != null && superMethod.getName().equals(method.getName())) {
									isSuperclassMethod = true;
									break;
								}
							}
						}
						if (showSuperclassMethods || !isSuperclassMethod) methodList.add(new MethodInvoker(method));
					}
				}
			} catch (Exception ignored) {
			}
		}
		return methodList;
	}

	/**
	 * Get a class from NMS.
	 *
	 * @param className - The name of the class.
	 * @return The class from NMS package.
	 */
	public static Class getMinecraftClass(String className) {
		try {
			return Class.forName(NMS_PREFIX + "." + className);
		} catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	/**
	 * Check if a class is a subclass of a super class.
	 * @param clazz - The class.
	 * @param superClass - The super class.
	 * @return Whether the class is a subclass of a super class.
	 */
	public static boolean instanceOf(Class clazz, Class superClass) {
		return clazz != null && superClass != null && clazz.isAssignableFrom(superClass);
	}

	private interface ReflectionAccess {
		boolean isAccessible();

		ReflectionAccess setAccessible();

		ReflectionAccess setAccessible(boolean flag);

		boolean wasAccessible();
	}

	public static class ConstructorInvoker implements ReflectionAccess {
		private Constructor constructor = null;
		private boolean wasAccessible = false;

		public ConstructorInvoker(Constructor method) {
			this(method, method.isAccessible());
		}

		public ConstructorInvoker(Constructor method, boolean wasAccessible) {
			this.constructor = method;
			this.wasAccessible = wasAccessible;
		}

		public Constructor getConstructor() {
			return this.constructor;
		}

		@Override
		public boolean isAccessible() {
			return this.constructor.isAccessible();
		}

		public Object newInstance(Object... parameters) throws Exception {
			this.constructor.setAccessible(true);
			Object newInstance = parameters != null && parameters.length > 0 ? this.constructor.newInstance(parameters) : this.constructor.newInstance();
			if (!this.wasAccessible()) this.constructor.setAccessible(false);
			return newInstance;
		}

		@Override
		public ConstructorInvoker setAccessible() {
			return this.setAccessible(this.wasAccessible);
		}

		@Override
		public ConstructorInvoker setAccessible(boolean flag) {
			this.constructor.setAccessible(flag);
			return this;
		}

		@Override
		public boolean wasAccessible() {
			return this.wasAccessible;
		}
	}

	@SuppressWarnings("unchecked")
	public static class FieldAccess implements ReflectionAccess {
		private Field field = null;
		private boolean wasAccessible = false;

		public FieldAccess(Field method) {
			this(method, method.isAccessible());
		}

		public FieldAccess(Field method, boolean wasAccessible) {
			this.field = method;
			this.wasAccessible = wasAccessible;
		}

		public <T> T get(Class<T> unused) throws Exception {
			return unused == String.class ? (T) this.getObject(null).toString() : (unused == Integer.class ? (T) Integer.valueOf(this.getObject(null).toString()) : unused == Boolean.class ? (T) Boolean.valueOf(this.getObject(null).toString()) : (T) this.getObject(null));
		}

		public <T> T get(Class<T> unused, Object instance) throws Exception {
			return unused == String.class ? (T) this.getObject(instance).toString() : (unused == Integer.class ? (T) Integer.valueOf(this.getObject(instance).toString()) : unused == Boolean.class ? (T) Boolean.valueOf(this.getObject(instance).toString()) : (T) this.getObject(instance));
		}

		public Object getObject(Object instance) throws Exception {
			this.field.setAccessible(true);
			try {
				Object value = this.field.get(instance);
				this.field.setAccessible(this.wasAccessible());
				return value;
			} catch (Exception ex) {
				this.field.setAccessible(this.wasAccessible());
				throw ex;
			}
		}

		public Field getField() {
			return this.field;
		}

		@Override
		public boolean isAccessible() {
			return this.field.isAccessible();
		}

		public boolean isStatic() {
			return Modifier.isStatic(this.field.getModifiers());
		}

		public void set(Object value) throws Exception {
			this.set(null, value);
		}

		public void set(Object instance, Object value) throws Exception {
			if (Modifier.isFinal(this.field.getModifiers())) {
				this.field.setAccessible(true);

				FieldAccess modifiersFieldAccess = ReflectionUtilities.getField(Field.class, "modifiers");
				Field modifiersField = modifiersFieldAccess.getField();
				modifiersField.setAccessible(true);

				int previousModifier = modifiersField.getInt(this.field);
				modifiersField.setInt(this.field, this.field.getModifiers() & ~Modifier.FINAL);

				this.field.set(instance, value);

				modifiersField.setInt(this.field, previousModifier);
				modifiersField.setAccessible(modifiersFieldAccess.wasAccessible());

				this.field.setAccessible(this.wasAccessible());
			} else {
				this.field.setAccessible(true);
				this.field.set(instance, value);
				if (!this.wasAccessible()) this.field.setAccessible(false);
			}
		}

		@Override
		public FieldAccess setAccessible() {
			return this.setAccessible(this.wasAccessible);
		}

		@Override
		public FieldAccess setAccessible(boolean flag) {
			this.field.setAccessible(flag);
			return this;
		}

		@Override
		public boolean wasAccessible() {
			return this.wasAccessible;
		}
	}

	public static class MethodInvoker implements ReflectionAccess {
		private Method method = null;
		private boolean wasAccessible = false;

		public MethodInvoker(Method method) {
			this(method, method.isAccessible());
		}

		public MethodInvoker(Method method, boolean wasAccessible) {
			this.method = method;
			this.wasAccessible = wasAccessible;
		}

		public Method getMethod() {
			return this.method;
		}

		public Object invoke(Object instance, Object... paramValues) throws Exception {
			this.method.setAccessible(true);
			Object invoked = this.method.invoke(instance, paramValues);
			this.method.setAccessible(this.wasAccessible());
			return invoked;
		}

		@Override
		public boolean isAccessible() {
			return this.method.isAccessible();
		}

		@Override
		public MethodInvoker setAccessible() {
			return this.setAccessible(this.wasAccessible);
		}

		@Override
		public MethodInvoker setAccessible(boolean flag) {
			this.method.setAccessible(flag);
			return this;
		}

		@Override
		public boolean wasAccessible() {
			return this.wasAccessible;
		}
	}

}