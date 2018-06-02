package com.therandomlabs.vanilladeathchest;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = VanillaDeathChest.MODID, version = VanillaDeathChest.VERSION,
		acceptedMinecraftVersions = "[1.12,1.13)",
		acceptableRemoteVersions = VanillaDeathChest.ACCEPTABLE_REMOTE_VERSIONS,
		updateJSON = VanillaDeathChest.UPDATE_JSON,
		certificateFingerprint = VanillaDeathChest.CERTIFICATE_FINGERPRINT)
public final class VanillaDeathChest {
	public static final String MODID = "vanilladeathchest";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTABLE_REMOTE_VERSIONS = "*";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/VanillaDeathChest/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static boolean dropDeathChest;
	public static boolean sendChatMessage;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		final Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		dropDeathChest = config.getBoolean("dropDeathChest", "general", false, "Whether the " +
				"death chest should be dropped when it is broken. Enable this if you like chest " +
				"dupe bugs.");
		sendChatMessage = config.getBoolean("sendChatMessage", "general", true, "Whether the " +
				"position of the death chest should be sent in chat to the player who " +
				"died.");
		config.save();
	}
}
