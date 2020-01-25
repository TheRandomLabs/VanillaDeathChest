package com.therandomlabs.vanilladeathchest;

import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.utils.config.ConfigManager;
import com.therandomlabs.utils.fabric.config.CommandConfigReload;
import com.therandomlabs.utils.fabric.config.FabricConfig;
import com.therandomlabs.vanilladeathchest.api.event.block.BreakBlockCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.ExplosionDetonationCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.deathchest.DeathChestRemoveCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import com.therandomlabs.vanilladeathchest.handler.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class VanillaDeathChest implements ModInitializer {
	public static final String MOD_ID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final boolean IS_DEOBFUSCATED =
			FabricLoader.getInstance().isDevelopmentEnvironment();

	public static final boolean CUBIC_CHUNKS_LOADED = false;

	private static final Method REGISTER = findMethod(
			GameRules.class, "register", "method_8359", String.class, GameRules.RuleType.class
	);

	private static final Method OF = findMethod(
			GameRules.BooleanRule.class, "of", "method_20759", boolean.class
	);

	private static GameRules.RuleKey<GameRules.BooleanRule> disableDeathChests;

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		FabricConfig.initialize();
		ConfigManager.register(VDCConfig.class);

		if(!VDCConfig.Misc.gameRuleName.isEmpty()) {
			try {
				disableDeathChests = (GameRules.RuleKey<GameRules.BooleanRule>) REGISTER.invoke(
						null, VDCConfig.Misc.gameRuleName, OF.invoke(null, false)
				);
			} catch(IllegalAccessException | InvocationTargetException ex) {
				crashReport("Failed to register disableDeathChests gamerule", ex);
			}
		}

		if(VDCConfig.Misc.vdcreload) {
			CommandRegistry.INSTANCE.register(
					false,
					dispatcher -> CommandConfigReload.server(
							//TODO remove cast
							(CommandDispatcher) dispatcher, "vdcreload", "vdcreloadclient",
							VDCConfig.class, "VanillaDeathChest configuration reloaded!"
					)
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

	public static Method findMethod(Class<?> clazz, String name, String obfName,
			Class<?>... parameterTypes) {
		for(Method method : clazz.getDeclaredMethods()) {
			final String methodName = method.getName();

			if((name.equals(methodName) || obfName.equals(methodName)) &&
					Arrays.equals(method.getParameterTypes(), parameterTypes)) {
				method.setAccessible(true);
				return method;
			}
		}

		return null;
	}

	public static void crashReport(String message, Throwable throwable) {
		throw new CrashException(new CrashReport(message, throwable));
	}
}
