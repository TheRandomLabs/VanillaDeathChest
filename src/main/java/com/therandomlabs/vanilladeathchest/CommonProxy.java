package com.therandomlabs.vanilladeathchest;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		VDCConfig.reload();
	}
}
