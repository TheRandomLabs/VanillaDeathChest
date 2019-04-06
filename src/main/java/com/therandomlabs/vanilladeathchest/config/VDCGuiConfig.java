package com.therandomlabs.vanilladeathchest.config;

import com.therandomlabs.randomlib.config.ConfigManager;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class VDCGuiConfig extends GuiConfig {
	public VDCGuiConfig(GuiScreen parentScreen) {
		super(
				parentScreen,
				ConfigManager.getConfigElements(VDCConfig.class),
				VanillaDeathChest.MOD_ID,
				VanillaDeathChest.MOD_ID,
				false,
				false,
				ConfigManager.getPathString(VDCConfig.class)
		);
	}
}
