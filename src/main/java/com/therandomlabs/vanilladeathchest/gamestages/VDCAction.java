package com.therandomlabs.vanilladeathchest.gamestages;

import java.util.function.Consumer;

import crafttweaker.IAction;

public class VDCAction implements IAction {
	private final String stage;
	private final Consumer<VDCStageInfo> action;
	private final String description;

	public VDCAction(String stage, Consumer<VDCStageInfo> action, String description) {
		this.stage = stage;
		this.action = action;
		this.description = description;
	}

	@Override
	public final void apply() {
		action.accept(
				VDCStageInfo.STAGES.computeIfAbsent(stage, key -> new VDCStageInfo())
		);
	}

	@Override
	public final String describe() {
		return description + " for stage " + stage;
	}
}
