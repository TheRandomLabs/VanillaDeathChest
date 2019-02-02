package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUnlockerConsumeAmount extends DeathChestAction {
	private final short amount;

	public ActionSetUnlockerConsumeAmount(String stage, short amount) {
		super(stage);
		this.amount = amount;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setUnlockerConsumeAmount(amount);
	}

	@Override
	public String description() {
		return "Attempting to set unlocker consume amount to " + amount;
	}
}
