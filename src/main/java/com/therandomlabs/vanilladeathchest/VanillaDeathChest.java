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

import com.therandomlabs.vanilladeathchest.command.VDCCommand;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestAutoRemover;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestInteractions;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestPlacer;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
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

	@Nullable
	private static VDCConfig config;

	/**
	 * The game rule that controls whether death chests should be spawned.
	 *
	 */
	public static GameRules.@Nullable Key<GameRules.BooleanRule> SPAWN_DEATH_CHESTS;

	/**
	 * Loads the spawn death chest options from the config.
	 */
	public static void loadSpawnDeathChestsOption() {
		final String gameRuleName = VanillaDeathChest.getConfig().misc.gameRuleName;

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
		registerConfig();
		reloadConfig(); // registers and attempts to load config
		loadSpawnDeathChestsOption();
		CommandRegistrationCallback.EVENT.register(VDCCommand::register);
		ServerTickEvents.START_WORLD_TICK.register(DeathChestPlacer::placeQueued);
		ServerTickEvents.END_WORLD_TICK.register(DeathChestAutoRemover::removeEmpty);
		UseBlockCallback.EVENT.register(DeathChestInteractions::interact);
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(DeathChestsState::onBlockEntityUnload);
	}

	/**
	 * Returns the VanillaDeathChest configuration.
	 *
	 * @return a {@link VDCConfig} object.
	 */
	@SuppressWarnings("NullAway")
	public static VDCConfig getConfig() {
		if (config == null) {
			reloadConfig();
		}

		return config;
	}

	/**
	 * Reloads the VanillaDeathChest configuration.
	 */
	public static void reloadConfig() {
		ConfigHolder<VDCConfig> configObj = AutoConfig.getConfigHolder(VDCConfig.class);
		configObj.load();
		config = configObj.getConfig();
	}

	/**
	 * Registers the VanillaDeathChest configuration.
	 */
	public static void registerConfig() {
		AutoConfig.register(VDCConfig.class, JanksonConfigSerializer::new);
	}
}
