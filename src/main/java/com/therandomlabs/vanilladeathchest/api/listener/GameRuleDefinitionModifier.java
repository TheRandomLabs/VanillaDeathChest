package com.therandomlabs.vanilladeathchest.api.listener;

import java.util.Map;
import net.minecraft.world.GameRules;

public interface GameRuleDefinitionModifier {
	void modify(Map<String, GameRules.ValueDefinition> definitions);
}
