package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntitySpawnCount extends DeathChestAction {
	public ActionSetDefenseEntitySpawnCount(String stage, int count) {
		super(
				stage,
				info -> info.setDefenseEntitySpawnCount(count),
				"Attempting to set defense entity spawn count to " + count
		);
	}
}
