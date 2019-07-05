package com.faris.kingkits.helper.util;

import com.faris.BackwardsCompatibility;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class EggUtilities {

	private EggUtilities() {
	}

	public static String getEgg(ItemStack itemStack) {
		if (itemStack != null) {
			if (itemStack.getType() == BackwardsCompatibility.getMonsterEgg()) {
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta instanceof SpawnEggMeta) {
					SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemMeta;
					return spawnEggMeta.getSpawnedType() != null ? StringUtilities.capitalizeFully(spawnEggMeta.getSpawnedType().name()) : null;
				}
			}
		}
		return null;
	}

	public static ItemStack setEgg(ItemStack itemStack, String entityType) {
		if (itemStack != null && entityType != null) {
			if (itemStack.getType() == BackwardsCompatibility.getMonsterEgg()) {
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (itemMeta instanceof SpawnEggMeta) {
					SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemMeta;
					if (entityType.startsWith("minecraft:")) {
						entityType = entityType.substring(10);
						EntityType actualType = EntityType.fromName(entityType);
						if (actualType != null) {
							spawnEggMeta.setSpawnedType(actualType);
							itemStack.setItemMeta(spawnEggMeta);
						}
					} else {
						entityType = entityType.replace(" ", "_").toUpperCase();
						boolean found = false;
						for (EntityType anEntity : EntityType.values()) {
							if (anEntity.name().equals(entityType)) {
								found = true;
								spawnEggMeta.setSpawnedType(anEntity);
								itemStack.setItemMeta(spawnEggMeta);
								break;
							}
						}
						if (!found) {
							EntityType actualType = EntityType.fromName(entityType);
							if (actualType != null) {
								spawnEggMeta.setSpawnedType(actualType);
								itemStack.setItemMeta(spawnEggMeta);
							}
						}
					}
				}
			}
		}
		return itemStack;
	}

}
