package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.randomlib.config.CommandConfigReload;
import com.therandomlabs.randomlib.config.ConfigManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(VanillaDeathChest.MOD_ID)
public final class VanillaDeathChest {
	public static final String MOD_ID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final boolean CUBIC_CHUNKS_LOADED = false;

	public VanillaDeathChest() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		ConfigManager.register(VDCConfig.class);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		ConfigManager.reloadFromDisk(VDCConfig.class);
	}

	private void serverStarting(FMLServerStartingEvent event) {
		if(VDCConfig.Misc.vdcreload) {
			CommandConfigReload.server(
					event.getCommandDispatcher(), "vdcreload", "vdcreloadclient", VDCConfig.class,
					"VanillaDeathChest configuration reloaded!"
			);
		}
	}
}
