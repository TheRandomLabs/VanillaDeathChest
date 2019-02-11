package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.vanilladeathchest.api.event.block.BreakBlockCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.deathchest.DeathChestRemoveCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import com.therandomlabs.vanilladeathchest.command.VDCReloadCommand;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
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

public final class VanillaDeathChest implements ModInitializer {
	public static final String MOD_ID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final boolean CUBIC_CHUNKS_LOADED = false;

	@Override
	public void onInitialize() {
		VDCConfig.reload();

		if(!VDCConfig.misc.gameRuleName.isEmpty()) {
			GameRules.getKeys().put(
					VDCConfig.misc.gameRuleName,
					new GameRules.Key(
							Boolean.toString(VDCConfig.misc.gameRuleDefaultValue),
							GameRules.Type.BOOLEAN
					)
			);
		}

		if(VDCConfig.misc.vdcreload) {
			CommandRegistry.INSTANCE.register(false, VDCReloadCommand::register);
		}

		final DeathChestPlaceHandler placeHandler = new DeathChestPlaceHandler();

		PlayerDropAllItemsCallback.EVENT.register(placeHandler);
		ServerTickCallback.EVENT.register(placeHandler);

		final DeathChestInteractionHandler interactionHandler = new DeathChestInteractionHandler();

		BreakBlockCallback.EVENT.register(interactionHandler);
		UseBlockCallback.EVENT.register(interactionHandler);

		final DeathChestDropHandler dropHandler = new DeathChestDropHandler();

		GetBlockDropCallback.EVENT.register(dropHandler);
		ServerTickCallback.EVENT.register(dropHandler);
		DeathChestRemoveCallback.EVENT.register(dropHandler);

		final DefenseEntityHandler defenseEntityHandler = new DefenseEntityHandler();

		LivingEntityDropCallback.EVENT.register(defenseEntityHandler);
		LivingEntityDropExperienceCallback.EVENT.register(defenseEntityHandler);
		LivingEntityTickCallback.EVENT.register(defenseEntityHandler);
	}

	public static void crashReport(String message, Throwable throwable) {
		throw new CrashException(new CrashReport(message, throwable));
	}
}
