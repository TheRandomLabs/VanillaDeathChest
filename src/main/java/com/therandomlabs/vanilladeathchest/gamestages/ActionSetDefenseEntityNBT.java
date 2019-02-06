package com.therandomlabs.vanilladeathchest.gamestages;

import crafttweaker.api.data.DataMap;

public class ActionSetDefenseEntityNBT extends VDCAction {
	public ActionSetDefenseEntityNBT(String stage, DataMap nbt) {
		super(
				stage,
				info -> info.setDefenseEntityNBT(nbt.toString()),
				"Setting defense entity NBT to " + nbt
		);
	}
}
