package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntityNBT extends DeathChestAction {
	public ActionSetDefenseEntityNBT(String stage, String nbt) {
		super(
				stage,
				info -> info.setDefenseEntityNBT(nbt),
				"Attempting to set defense entity NBT to " + nbt
		);
	}
}
