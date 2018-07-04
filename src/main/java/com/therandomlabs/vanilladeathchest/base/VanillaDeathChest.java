package com.therandomlabs.vanilladeathchest.base;

import com.therandomlabs.vanilladeathchest.CommandVDCReload;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = VanillaDeathChest.MODID, version = VanillaDeathChest.VERSION,
		acceptedMinecraftVersions = VanillaDeathChest.ACCEPTED_MINECRAFT_VERSIONS,
		acceptableRemoteVersions = VanillaDeathChest.ACCEPTABLE_REMOTE_VERSIONS,
		updateJSON = VanillaDeathChest.UPDATE_JSON,
		certificateFingerprint = VanillaDeathChest.CERTIFICATE_FINGERPRINT)
public final class VanillaDeathChest {
	public static final String MODID = "vanilladeathchest";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12,1.13)";
	public static final String ACCEPTABLE_REMOTE_VERSIONS = "*";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/VanillaDeathChest/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final String GAMERULE_NAME = "spawnDeathChests";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@SidedProxy(clientSide = "com.therandomlabs.vanilladeathchest.base.ClientProxy",
			serverSide = "com.therandomlabs.vanilladeathchest.base.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		VDCConfig.reload();
	}

	@Mod.EventHandler
	public static void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandVDCReload(Side.SERVER));
	}
}
