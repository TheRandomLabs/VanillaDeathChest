package com.therandomlabs.vanilladeathchest.gamestages;

public class ActionSetDefenseEntityNBT extends DeathChestAction {
	private final String nbt;

	public ActionSetDefenseEntityNBT(String stage, String nbt) {
		super(stage);
		this.nbt = nbt;
	}

	@Override
	public void apply(DeathChestStageInfo info) {
		info.setDefenseEntityNBT(nbt);
	}

	@Override
	public String description() {
		return "Attempting to set defense entity NBT to " + nbt;
	}
}
