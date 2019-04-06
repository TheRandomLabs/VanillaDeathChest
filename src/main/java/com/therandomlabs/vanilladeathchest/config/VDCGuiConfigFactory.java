package com.therandomlabs.vanilladeathchest.config;

import com.therandomlabs.randomlib.config.TRLGuiConfigFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public class VDCGuiConfigFactory extends TRLGuiConfigFactory {
	@Override
	public Class<? extends GuiConfig> mainConfigGuiClass() {
		return VDCGuiConfig.class;
	}

	@SuppressWarnings("deprecation")
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
}
