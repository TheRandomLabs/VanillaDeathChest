package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntity extends DeathChestAction {
	public ActionSetDefenseEntity(String stage, String registryName) {
		super(
				stage,
				info -> info.setDefenseEntityRegistryName(registryName),
				"Attempting to set defense entity registry name to " + registryName
		);
	}
}
