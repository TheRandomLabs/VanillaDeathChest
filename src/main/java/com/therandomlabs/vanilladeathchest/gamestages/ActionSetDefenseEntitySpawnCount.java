package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntitySpawnCount extends DeathChestAction {
	private final int count;

	public ActionSetDefenseEntitySpawnCount(String stage, int count) {
		super(stage);
		this.count = count;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setDefenseEntitySpawnCount(count);
	}

	@Override
	public String description() {
		return "Attempting to set defense entity spawn count to " + count;
	}
}
