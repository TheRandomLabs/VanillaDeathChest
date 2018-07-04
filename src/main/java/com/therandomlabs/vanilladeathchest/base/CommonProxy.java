package com.therandomlabs.vanilladeathchest.base;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		VDCConfig.reload();
	}
}
