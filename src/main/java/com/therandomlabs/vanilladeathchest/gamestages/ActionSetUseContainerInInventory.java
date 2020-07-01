package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetUseContainerInInventory extends VDCAction {
	public ActionSetUseContainerInInventory(String stage, boolean flag) {
		super(
				stage,
				info -> info.setUseContainerInInventory(flag),
				"Containers will " + (flag ? "" : "not ") + "be consumed from player inventory"
		);
	}
}
