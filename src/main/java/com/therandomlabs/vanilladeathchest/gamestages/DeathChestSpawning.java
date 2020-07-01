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

	@ZenMethod
	public static void setContainerDisplayName(String stage, String name) {
		CraftTweakerAPI.apply(new ActionSetContainerDisplayName(stage, name));
	}

	@ZenMethod
	public static void setRegistryNameRegex(String stage, String regex) {
		CraftTweakerAPI.apply(new ActionSetRegistryNameRegex(stage, regex));
	}
}
