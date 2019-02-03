package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUnlockerMeta extends DeathChestAction {
	public ActionSetUnlockerMeta(String stage, short meta) {
		super(
				stage,
				info -> info.setUnlockerMeta(meta),
				"Attempting to set unlocker meta to " + meta
		);
	}
}
