package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public final class VDCGameRuleAdder {
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if(VDCConfig.Misc.gameRuleName.isEmpty()) {
			return;
		}

		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final GameRules gameRules = world.getGameRules();

		if(!gameRules.hasRule(VDCConfig.Misc.gameRuleName)) {
			gameRules.setOrCreateGameRule(
					VDCConfig.Misc.gameRuleName,
					Boolean.toString(VDCConfig.Misc.gameRuleDefaultValue)
			);
		}
	}
}
