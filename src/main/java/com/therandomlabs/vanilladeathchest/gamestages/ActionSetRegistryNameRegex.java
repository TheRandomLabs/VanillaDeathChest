package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetRegistryNameRegex extends VDCAction {
	public ActionSetRegistryNameRegex(String stage, String regex) {
		super(
				stage,
				info -> info.setRegistryNameRegex(regex),
				"Setting registry name regex to " + regex
		);
	}
}
