package com.therandomlabs.vanilladeathchest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = VanillaDeathChest.MODID, version = VanillaDeathChest.VERSION,
		acceptedMinecraftVersions = "[1.12,1.13)",
		acceptableRemoteVersions = VanillaDeathChest.ACCEPTABLE_REMOTE_VEERSIONS,
		updateJSON = VanillaDeathChest.UPDATE_JSON,
		certificateFingerprint = VanillaDeathChest.CERTIFICATE_FINGERPRINT)
public final class VanillaDeathChest {
	public static final String MODID = "vanilladeathchest";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTABLE_REMOTE_VEERSIONS = "*";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/VanillaDeathChest/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final Logger LOGGER = LogManager.getLogger(MODID);
}
