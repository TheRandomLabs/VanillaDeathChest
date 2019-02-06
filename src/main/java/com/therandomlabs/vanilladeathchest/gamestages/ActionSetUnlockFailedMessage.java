package com.therandomlabs.vanilladeathchest.gamestages;

public final class ActionSetUnlockFailedMessage extends VDCAction {
	public ActionSetUnlockFailedMessage(String stage, String message) {
		super(
				stage,
				info -> info.setUnlockFailedMessage(message),
				"Setting unlock failed message to " + message
		);
	}
}
