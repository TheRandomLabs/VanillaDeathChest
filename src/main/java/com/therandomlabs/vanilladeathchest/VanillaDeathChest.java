/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.autoconfigtoml.TOMLConfigSerializer;
import com.therandomlabs.vanilladeathchest.command.VDCCommand;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestAutoRemover;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestInteractions;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestPlacer;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The main VanillaDeathChest class.
 */
public final class VanillaDeathChest implements ModInitializer {
	/**
	 * The VanillaDeathChest mod ID.
	 */
	public static final String MOD_ID = "vanilladeathchest";

	/**
	 * The VanillaDeathChest logger. This should only be used by VanillaDeathChest.
	 */
	public static final Logger logger = LogManager.getLogger(MOD_ID);

	/**
	 * The game rule that controls whether death chests should be spawned.
	 */
	public static final GameRules.@Nullable Key<GameRules.BooleanRule> SPAWN_DEATH_CHESTS;

	@SuppressWarnings("PMD.NonThreadSafeSingleton")
	@Nullable
	private static TOMLConfigSerializer<VDCConfig> serializer;

	static {
		final String gameRuleName = VanillaDeathChest.config().misc.gameRuleName;

		if (gameRuleName.isEmpty()) {
			SPAWN_DEATH_CHESTS = null;
		} else {
			SPAWN_DEATH_CHESTS = GameRuleRegistry.register(
					gameRuleName, GameRules.Category.DROPS, GameRuleFactory.createBooleanRule(true)
			);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onInitialize() {
		reloadConfig();
		CommandRegistrationCallback.EVENT.register(VDCCommand::register);
		ServerTickEvents.END_WORLD_TICK.register(DeathChestAutoRemover::removeEmpty);
		ServerTickEvents.END_WORLD_TICK.register(DeathChestPlacer::placeQueued);
		UseBlockCallback.EVENT.register(DeathChestInteractions::interact);
	}

	/**
	 * Returns the VanillaDeathChest configuration.
	 *
	 * @return a {@link VDCConfig} object.
	 */
	@SuppressWarnings("NullAway")
	public static VDCConfig config() {
		if (serializer == null) {
			reloadConfig();
		}

		return serializer.getConfig();
	}

	/**
	 * Reloads the VanillaDeathChest configuration from disk.
	 */
	public static void reloadConfig() {
		if (serializer == null) {
			AutoConfig.register(VDCConfig.class, (definition, configClass) -> {
				serializer = new TOMLConfigSerializer<>(definition, configClass);
				return serializer;
			});
		} else {
			serializer.reloadFromDisk();
		}
	}
}
