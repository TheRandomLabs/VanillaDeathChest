package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public final class VanillaDeathChest implements InitializationListener {
	public static final String MOD_ID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialization() {
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins." + MOD_ID + ".json");

		VDCConfig.reload();
	}

	public static void crashReport(String message, Throwable throwable) {
		throw new ReportedException(new CrashReport(message, throwable));
	}
}
