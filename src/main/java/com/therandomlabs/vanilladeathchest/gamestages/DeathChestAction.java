package com.therandomlabs.vanilladeathchest.gamestages;

import crafttweaker.IAction;

public abstract class DeathChestAction implements IAction {
	private final String stage;

	protected DeathChestAction(String stage) {
		this.stage = stage;
	}

	@Override
	public final void apply() {
		apply(DeathChestStageInfo.STAGES.computeIfAbsent(stage, key -> new DeathChestStageInfo()));
	}

	@Override
	public final String describe() {
		return description() + " for stage " + stage;
	}

	public abstract void apply(DeathChestStageInfo info);

	public abstract String description();
}
