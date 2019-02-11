package com.therandomlabs.vanilladeathchest;

public class CommonProxy {
	public void construct() {
		VDCConfig.reload();
	}

	public void preInit() {}

	public void init() {
		//Validate registry names
		VDCConfig.reload();
	}
}
