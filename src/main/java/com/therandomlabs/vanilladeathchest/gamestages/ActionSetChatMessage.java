package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetChatMessage extends VDCAction {
	public ActionSetChatMessage(String stage, String message) {
		super(
				stage,
				info -> info.setChatMessage(message),
				"Setting death chest spawn chat message to " + message
		);
	}
}
