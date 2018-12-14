package com.therandomlabs.vanilladeathchest.handler;

import java.util.Map;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import com.therandomlabs.vanilladeathchest.api.event.GameRuleEvent;
import net.minecraft.world.GameRules;

public class VDCGameRuleAdder implements GameRuleEvent.Modify {
	@Override
	public void modify(Map<String, GameRules.Key> keys) {
		keys.put(
				VDCConfig.misc.gameRuleName,
				new GameRules.Key(
						Boolean.toString(VDCConfig.misc.gameRuleDefaultValue),
						GameRules.Type.BOOLEAN
				)
		);
	}
}
