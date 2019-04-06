package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetContainerDisplayName extends VDCAction {
	public ActionSetContainerDisplayName(String stage, String name) {
		super(
				stage,
				info -> info.setContainerDisplayName(name),
				"Setting death chest container display name to " + name
		);
	}
}
