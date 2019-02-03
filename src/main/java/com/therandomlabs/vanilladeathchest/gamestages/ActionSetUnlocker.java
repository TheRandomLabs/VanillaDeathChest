package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUnlocker extends DeathChestAction {
	public ActionSetUnlocker(String stage, String registryName) {
		super(
				stage,
				info -> info.setUnlockerRegistryName(registryName),
				"Attempting to set unlocker registry name to " + registryName
		);
	}
}
