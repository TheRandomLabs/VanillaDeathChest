package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUnlockerMeta extends DeathChestAction {
	private final short meta;

	public ActionSetUnlockerMeta(String stage, short meta) {
		super(stage);
		this.meta = meta;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setUnlockerMeta(meta);
	}

	@Override
	public String description() {
		return "Attempting to set unlocker meta to " + meta;
	}
}
