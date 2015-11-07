package com.faris.kingkits.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompassController implements Controller {

	private static CompassController instance = null;

	private Map<UUID, UUID> compassTargets = null;

	private CompassController() {
		this.compassTargets = new HashMap<>();
	}

	@Override
	public void shutdownController() {
		this.compassTargets.clear();
		this.compassTargets = null;
	}

	public UUID getTarget(UUID targeterUUID) {
		return this.compassTargets.get(targeterUUID);
	}

	public boolean hasTarget(UUID targeterUUID) {
		return this.compassTargets.containsKey(targeterUUID);
	}

	public void removeTarget(UUID targetUUID) {
		if (this.compassTargets.containsValue(targetUUID)) {
			for (Map.Entry<UUID, UUID> targeterEntry : new ArrayList<>(this.compassTargets.entrySet())) {
				if (targetUUID.equals(targeterEntry.getValue())) this.compassTargets.remove(targeterEntry.getKey());
			}
		}
	}

	public void removeTargeter(UUID targeterUUID) {
		this.compassTargets.remove(targeterUUID);
	}

	public void setTarget(UUID targeterUUID, UUID targetUUID) {
		if (targeterUUID != null && targetUUID != null) this.compassTargets.put(targeterUUID, targetUUID);
	}

	public static CompassController getInstance() {
		if (instance == null) instance = new CompassController();
		return instance;
	}

}
