package com.therandomlabs.vanilladeathchest.gamestages;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.IData;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ModOnly("gamestages")
@ZenRegister
@ZenClass("mods." + VanillaDeathChest.MOD_ID + ".DeathChestDefense")
public final class DeathChestDefense {
	@ZenMethod
	public static void setUnlocker(String stage, IItemStack unlockerItem) {
		CraftTweakerAPI.apply(new ActionSetUnlocker(stage, unlockerItem));
	}

	@ZenMethod
	public static void setDamageUnlockerInsteadOfConsume(String stage, boolean flag) {
		CraftTweakerAPI.apply(new ActionSetDamageUnlocker(stage, flag));
	}

	@ZenMethod
	public static void setUnlockFailedMessage(String stage, String message) {
		CraftTweakerAPI.apply(new ActionSetUnlockFailedMessage(stage, message));
	}

	@ZenMethod
	public static void setDefenseEntity(String stage, IEntityDefinition defenseEntity) {
		CraftTweakerAPI.apply(new ActionSetDefenseEntity(stage, defenseEntity));
	}

	@ZenMethod
	public static void setDefenseEntityNBT(String stage, IData nbt) {
		CraftTweakerAPI.apply(new ActionSetDefenseEntityNBT(stage, (DataMap) nbt));
	}

	@ZenMethod
	public static void setDefenseEntitySpawnCount(String stage, int count) {
		CraftTweakerAPI.apply(new ActionSetDefenseEntitySpawnCount(stage, count));
	}
}
