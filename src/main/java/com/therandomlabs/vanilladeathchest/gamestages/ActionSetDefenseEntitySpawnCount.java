package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntitySpawnCount extends VDCAction {
	public ActionSetDefenseEntitySpawnCount(String stage, int count) {
		super(
				stage,
				info -> info.setDefenseEntitySpawnCount(count),
				"Setting defense entity spawn count to " + count
		);
	}
}
