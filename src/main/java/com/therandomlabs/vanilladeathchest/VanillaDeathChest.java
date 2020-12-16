/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 TheRandomLabs
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
import com.therandomlabs.vanilladeathchest.api.event.block.BreakBlockCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.ExplosionDetonationCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.deathchest.DeathChestRemoveCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import com.therandomlabs.vanilladeathchest.command.VDCConfigReloadCommand;
import com.therandomlabs.vanilladeathchest.handler.DeathChestContentsChecker;
import com.therandomlabs.vanilladeathchest.handler.DeathChestDropHandler;
import com.therandomlabs.vanilladeathchest.handler.DeathChestInteractionHandler;
import com.therandomlabs.vanilladeathchest.handler.DeathChestPlaceHandler;
import com.therandomlabs.vanilladeathchest.handler.DefenseEntityHandler;
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

/**
 * The main VanillaDeathChest class.
 */
@SuppressWarnings("NullAway")
public final class VanillaDeathChest implements ModInitializer {
	/**
	 * The VanillaDeathChest mod ID.
	 */
	public static final String MOD_ID = "vanilladeathchest";

	/**
	 * The VanillaDeathChest logger.
	 */
	public static final Logger logger = LogManager.getLogger(MOD_ID);

	private static TOMLConfigSerializer<VDCConfig> serializer;
	public static final GameRules.Key<GameRules.BooleanRule> DISABLE_DEATH_CHESTS =
			GameRuleRegistry.register(
					VanillaDeathChest.config().misc.gameRuleName, GameRules.Category.DROPS,
					GameRuleFactory.createBooleanRule(false)
			);

	@Override
	public void onInitialize() {
		reloadConfig();
		CommandRegistrationCallback.EVENT.register(VDCConfigReloadCommand::register);

		final DeathChestPlaceHandler placeHandler = new DeathChestPlaceHandler();

		PlayerDropAllItemsCallback.EVENT.register(placeHandler);
		ServerTickEvents.END_WORLD_TICK.register(placeHandler);

		final DeathChestInteractionHandler interactionHandler = new DeathChestInteractionHandler();

		BreakBlockCallback.EVENT.register(interactionHandler);
		UseBlockCallback.EVENT.register(interactionHandler);
		ExplosionDetonationCallback.EVENT.register(interactionHandler);

		final DeathChestDropHandler dropHandler = new DeathChestDropHandler();

		GetBlockDropCallback.EVENT.register(dropHandler);
		ServerTickEvents.END_SERVER_TICK.register(dropHandler);
		DeathChestRemoveCallback.EVENT.register(dropHandler);

		final DefenseEntityHandler defenseEntityHandler = new DefenseEntityHandler();

		LivingEntityDropCallback.EVENT.register(defenseEntityHandler);
		LivingEntityDropExperienceCallback.EVENT.register(defenseEntityHandler);
		LivingEntityTickCallback.EVENT.register(defenseEntityHandler);

		ServerTickEvents.END_WORLD_TICK.register(new DeathChestContentsChecker());
	}

	/**
	 * Returns the VanillaDeathChest configuration.
	 *
	 * @return a {@link VDCConfig} object.
	 */
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
