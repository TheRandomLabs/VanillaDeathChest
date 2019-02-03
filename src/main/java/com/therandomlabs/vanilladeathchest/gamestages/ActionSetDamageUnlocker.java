package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDamageUnlocker extends DeathChestAction {
	public ActionSetDamageUnlocker(String stage, boolean flag) {
		super(
				stage,
				info -> info.setDamageUnlockerInsteadOfConsume(flag),
				"Unlocker items will be " + (flag ? "damaged" : "consumed")
		);
	}
}
