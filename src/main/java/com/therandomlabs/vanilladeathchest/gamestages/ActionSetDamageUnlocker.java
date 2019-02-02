package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDamageUnlocker extends DeathChestAction {
	private final boolean flag;

	public ActionSetDamageUnlocker(String stage, boolean flag) {
		super(stage);
		this.flag = flag;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setDamageUnlockerInsteadOfConsume(flag);
	}

	@Override
	public String description() {
		return "Unlocker items will be " + (flag ? "damaged" : "consumed");
	}
}
