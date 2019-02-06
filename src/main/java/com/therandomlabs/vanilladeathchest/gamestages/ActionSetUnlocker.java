package com.therandomlabs.vanilladeathchest.gamestages;

import crafttweaker.api.item.IItemStack;

public class ActionSetUnlocker extends VDCAction {
	public ActionSetUnlocker(String stage, IItemStack unlockerItem) {
		super(
				stage,
				info -> {
					info.setUnlockerRegistryName(unlockerItem.getDefinition().getId());
					info.setUnlockerMeta(unlockerItem.getMetadata());
					info.setUnlockerConsumeAmount(unlockerItem.getAmount());
				},
				"Setting unlocker item to " + unlockerItem
		);
	}
}
