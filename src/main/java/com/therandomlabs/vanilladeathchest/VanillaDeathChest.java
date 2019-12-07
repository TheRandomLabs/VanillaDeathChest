package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.utils.config.ConfigManager;
import com.therandomlabs.utils.forge.config.ConfigReloadCommand;
import com.therandomlabs.utils.forge.config.ForgeConfig;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(VanillaDeathChest.MOD_ID)
public final class VanillaDeathChest {
	public static final String MOD_ID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static GameRules.RuleKey<GameRules.BooleanValue> disableDeathChests;

	public VanillaDeathChest() {
		ForgeConfig.initialize();
		ConfigManager.register(VDCConfig.class);

		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);

		if (!VDCConfig.Misc.gameRuleName.isEmpty()) {
			disableDeathChests = GameRules.register(
					VDCConfig.Misc.gameRuleName, GameRules.BooleanValue.create(false)
			);
		}
	}

	private void serverStarting(FMLServerStartingEvent event) {
		if (VDCConfig.Misc.vdcreload) {
			ConfigReloadCommand.server(
					event.getCommandDispatcher(), "vdcreload", "vdcreloadclient", VDCConfig.class,
					"VanillaDeathChest configuration reloaded!"
			);
		}
	}

	public static GameRules.RuleKey<GameRules.BooleanValue> getDisableDeathChestsKey() {
		return disableDeathChests;
	}
}
