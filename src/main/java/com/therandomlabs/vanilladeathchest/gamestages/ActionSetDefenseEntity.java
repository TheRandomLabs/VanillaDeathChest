package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntity extends DeathChestAction {
	private final String registryName;

	public ActionSetDefenseEntity(String stage, String registryName) {
		super(stage);
		this.registryName = registryName;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setDefenseEntityRegistryName(registryName);
	}

	@Override
	public String description() {
		return "Attempting to set defense entity registry name to " + registryName;
	}
}
