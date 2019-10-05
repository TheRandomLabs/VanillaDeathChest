package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.randomlib.config.CommandConfigReload;
import com.therandomlabs.randomlib.config.ConfigManager;
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
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		ConfigManager.register(VDCConfig.class);

		if(!VDCConfig.Misc.gameRuleName.isEmpty()) {
			disableDeathChests = GameRules.register(
					VDCConfig.Misc.gameRuleName, GameRules.BooleanValue.create(false)
			);
		}
	}

	private void serverStarting(FMLServerStartingEvent event) {
		if(VDCConfig.Misc.vdcreload) {
			CommandConfigReload.server(
					event.getCommandDispatcher(), "vdcreload", "vdcreloadclient", VDCConfig.class,
					"VanillaDeathChest configuration reloaded!"
			);
		}
	}

	public static GameRules.RuleKey<GameRules.BooleanValue> getDisableDeathChestsKey() {
		return disableDeathChests;
	}
}
