package com.therandomlabs.vanilladeathchest.gamestages;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ModOnly("gamestages")
@ZenRegister
@ZenClass("mods." + VanillaDeathChest.MOD_ID + ".DeathChestDefense")
public final class DeathChestDefense {
	@ZenMethod
	public static void setDamageUnlockerInsteadOfConsume(String stage, boolean flag) {
		CraftTweakerAPI.apply(new ActionSetDamageUnlocker(stage, flag));
	}

	@ZenMethod
	public static void setDefenseEntityNBT(String stage, String nbt) {
		CraftTweakerAPI.apply(new ActionSetDefenseEntityNBT(stage, nbt));
	}

	@ZenMethod
	public static void setDefenseEntityRegistryName(String stage, String registryName) {
		CraftTweakerAPI.apply(new ActionSetDefenseEntity(stage, registryName));
	}

	@ZenMethod
	public static void setDefenseEntitySpawnCount(String stage, int count) {
		CraftTweakerAPI.apply(new ActionSetDefenseEntitySpawnCount(stage, count));
	}

	@ZenMethod
	public static void setUnlockerConsumeAmount(String stage, short amount) {
		CraftTweakerAPI.apply(new ActionSetUnlockerConsumeAmount(stage, amount));
	}

	@ZenMethod
	public static void setUnlockerMeta(String stage, short meta) {
		CraftTweakerAPI.apply(new ActionSetUnlockerMeta(stage, meta));
	}

	@ZenMethod
	public static void setUnlockerRegistryName(String stage, String registryName) {
		CraftTweakerAPI.apply(new ActionSetUnlocker(stage, registryName));
	}
}
