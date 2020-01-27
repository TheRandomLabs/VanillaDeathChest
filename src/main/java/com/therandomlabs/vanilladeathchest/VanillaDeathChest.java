/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2019 TheRandomLabs
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.utils.config.ConfigManager;
import com.therandomlabs.utils.fabric.FabricUtils;
import com.therandomlabs.utils.fabric.config.ConfigReloadCommand;
import com.therandomlabs.utils.fabric.config.FabricConfig;
import com.therandomlabs.vanilladeathchest.api.event.block.BreakBlockCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.ExplosionDetonationCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.deathchest.DeathChestRemoveCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import com.therandomlabs.vanilladeathchest.handler.DeathChestContentsChecker;
import com.therandomlabs.vanilladeathchest.handler.DeathChestDropHandler;
import com.therandomlabs.vanilladeathchest.handler.DeathChestInteractionHandler;
import com.therandomlabs.vanilladeathchest.handler.DeathChestPlaceHandler;
import com.therandomlabs.vanilladeathchest.handler.DefenseEntityHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
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

	private static final ConfigReloadCommand configReloadCommand =
			new ConfigReloadCommand("vdcreload", "vdcreloadclient", VDCConfig.class).
					serverSuccessMessage("VanillaDeathChest configuration reloaded!");

	private static final Method REGISTER = FabricUtils.findMethod(
			GameRules.class, "register", "method_8359", String.class, GameRules.RuleType.class
	);

	private static final Method OF = FabricUtils.findMethod(
			GameRules.BooleanRule.class, "create", "method_20759", boolean.class
	);

	private static GameRules.RuleKey<GameRules.BooleanRule> disableDeathChests;

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		FabricConfig.initialize();
		ConfigManager.register(VDCConfig.class);

		if (!VDCConfig.Misc.gameRuleName.isEmpty()) {
			try {
				disableDeathChests = (GameRules.RuleKey<GameRules.BooleanRule>) REGISTER.invoke(
						null, VDCConfig.Misc.gameRuleName, OF.invoke(null, false)
				);
			} catch (IllegalAccessException | InvocationTargetException ex) {
				crashReport("Failed to register disableDeathChests gamerule", ex);
			}
		}

		if (VDCConfig.Misc.vdcreload) {
			CommandRegistry.INSTANCE.register(
					false,
					dispatcher ->
							configReloadCommand.registerServer((CommandDispatcher) dispatcher)
			);
		}

		final DeathChestPlaceHandler placeHandler = new DeathChestPlaceHandler();

		PlayerDropAllItemsCallback.EVENT.register(placeHandler);
		ServerTickCallback.EVENT.register(placeHandler);

		final DeathChestInteractionHandler interactionHandler = new DeathChestInteractionHandler();

		BreakBlockCallback.EVENT.register(interactionHandler);
		UseBlockCallback.EVENT.register(interactionHandler);
		ExplosionDetonationCallback.EVENT.register(interactionHandler);

		final DeathChestDropHandler dropHandler = new DeathChestDropHandler();

		GetBlockDropCallback.EVENT.register(dropHandler);
		ServerTickCallback.EVENT.register(dropHandler);
		DeathChestRemoveCallback.EVENT.register(dropHandler);

		final DefenseEntityHandler defenseEntityHandler = new DefenseEntityHandler();

		LivingEntityDropCallback.EVENT.register(defenseEntityHandler);
		LivingEntityDropExperienceCallback.EVENT.register(defenseEntityHandler);
		LivingEntityTickCallback.EVENT.register(defenseEntityHandler);

		ServerTickCallback.EVENT.register(new DeathChestContentsChecker());
	}

	public static GameRules.RuleKey<GameRules.BooleanRule> getDisableDeathChestsKey() {
		return disableDeathChests;
	}

	public static void crashReport(String message, Throwable throwable) {
		throw new CrashException(new CrashReport(message, throwable));
	}
}
