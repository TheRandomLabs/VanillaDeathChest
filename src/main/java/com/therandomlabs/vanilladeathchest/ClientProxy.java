package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.randomlib.config.CommandConfigReload;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();

		if(VDCConfig.Misc.vdcreloadclient) {
			ClientCommandHandler.instance.registerCommand(new CommandConfigReload(
					"vdcreloadclient", VDCConfig.class, Side.CLIENT
			));
		}
	}
}
