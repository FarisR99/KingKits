package nl.arfie.bukkit.attributes.wrapper;

public class NBTBase extends SourceWrapper {

	private final static Class<?> clazz = loadClass("net.minecraft.server", "NBTBase");

	public NBTBase() throws InstantiationException, IllegalAccessException {
		super(clazz.newInstance());
	}

	public NBTBase(Object instance) {
		super(instance);
	}

}
