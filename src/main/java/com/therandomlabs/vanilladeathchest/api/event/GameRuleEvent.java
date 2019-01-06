package com.therandomlabs.vanilladeathchest.api.event;

import java.util.Map;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.world.GameRules;

public final class GameRuleEvent {
	@FunctionalInterface
	public interface Modify {
		void modify(Map<String, GameRules.Key> keys);
	}

	public static final HandlerArray<Modify> MODIFY = new HandlerArray<>(Modify.class);
}
