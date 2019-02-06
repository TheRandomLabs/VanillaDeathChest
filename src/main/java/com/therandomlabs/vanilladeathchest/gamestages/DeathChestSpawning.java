package com.therandomlabs.vanilladeathchest.gamestages;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ModOnly("gamestages")
@ZenRegister
@ZenClass("mods." + VanillaDeathChest.MOD_ID + ".DeathChestSpawning")
public final class DeathChestSpawning {
	@ZenMethod
	public static void setChatMessage(String stage, String message) {
		CraftTweakerAPI.apply(new ActionSetChatMessage(stage, message));
	}
}
