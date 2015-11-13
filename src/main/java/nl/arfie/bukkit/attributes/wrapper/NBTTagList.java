package nl.arfie.bukkit.attributes.wrapper;

public class NBTTagList extends NBTBase {

	private final static Class<?> clazz = loadClass("net.minecraft.server", "NBTTagList");

	static {
		declareMethod(clazz, "size");
		declareMethod(clazz, "get", int.class);
		declareMethod(clazz, "add", loadClass("net.minecraft.server", "NBTBase"));
	}

	public NBTTagList() throws InstantiationException, IllegalAccessException {
		super(clazz.newInstance());
	}

	public NBTTagList(Object instance) throws InstantiationException, IllegalAccessException {
		super(instance == null ? clazz.newInstance() : instance);
	}

	public int size() {
		return (int) invokeMethod("size");
	}

	public NBTTagCompound get(int index) throws InstantiationException, IllegalAccessException {
		return new NBTTagCompound(invokeMethod("get", index));
	}

	public void add(NBTBase base) {
		invokeMethod("add", base.instance);
	}

}
