package com.therandomlabs.vanilladeathchest.gamestages;

import java.util.function.Consumer;
import crafttweaker.IAction;

public abstract class DeathChestAction implements IAction {
	private final String stage;
	private final Consumer<DeathChestStageInfo> action;
	private final String description;

	protected DeathChestAction(String stage, Consumer<DeathChestStageInfo> action,
			String description) {
		this.stage = stage;
		this.action = action;
		this.description = description;
	}

	@Override
	public final void apply() {
		action.accept(
				DeathChestStageInfo.STAGES.computeIfAbsent(stage, key -> new DeathChestStageInfo())
		);
	}

	@Override
	public final String describe() {
		return description + " for stage " + stage;
	}
}
