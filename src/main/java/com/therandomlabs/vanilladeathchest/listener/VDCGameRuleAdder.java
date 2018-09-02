package com.therandomlabs.vanilladeathchest.listener;

import java.util.Map;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import com.therandomlabs.vanilladeathchest.api.listener.GameRuleDefinitionModifier;
import net.minecraft.world.GameRules;

public class VDCGameRuleAdder implements GameRuleDefinitionModifier {
	@Override
	public void modify(Map<String, GameRules.ValueDefinition> definitions) {
		definitions.put(VDCConfig.misc.gameruleName, new GameRules.ValueDefinition(
				Boolean.toString(VDCConfig.misc.gameruleDefaultValue),
				GameRules.ValueType.BOOLEAN_VALUE
		));
	}
}
