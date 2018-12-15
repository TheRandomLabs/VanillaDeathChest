package com.therandomlabs.vanilladeathchest.api.event;

import java.util.Map;
import net.fabricmc.fabric.util.HandlerList;
import net.minecraft.world.GameRules;

public final class GameRuleEvent {
	@FunctionalInterface
	public interface Modify {
		void modify(Map<String, GameRules.Key> keys);
	}

	public static final HandlerList<Modify> MODIFY = new HandlerList<>(Modify.class);
}
