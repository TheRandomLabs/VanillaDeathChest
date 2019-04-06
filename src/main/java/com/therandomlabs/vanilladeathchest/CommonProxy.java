package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.randomlib.config.ConfigManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;

public class CommonProxy {
	public void preInit() {}

	public void init() {
		ConfigManager.register(VDCConfig.class);
	}
}
