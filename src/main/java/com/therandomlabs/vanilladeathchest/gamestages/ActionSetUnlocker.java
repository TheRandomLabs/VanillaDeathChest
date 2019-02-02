package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUnlocker extends DeathChestAction {
	private final String registryName;

	public ActionSetUnlocker(String stage, String registryName) {
		super(stage);
		this.registryName = registryName;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setUnlockerRegistryName(registryName);
	}

	@Override
	public String description() {
		return "Attempting to set unlocker registry name to " + registryName;
	}
}
