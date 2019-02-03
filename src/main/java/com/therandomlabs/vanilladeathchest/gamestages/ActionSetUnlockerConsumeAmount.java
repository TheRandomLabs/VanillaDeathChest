package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUnlockerConsumeAmount extends DeathChestAction {
	public ActionSetUnlockerConsumeAmount(String stage, short amount) {
		super(
				stage,
				info -> info.setUnlockerConsumeAmount(amount),
				"Attempting to set unlocker consume amount to " + amount
		);
	}
}
