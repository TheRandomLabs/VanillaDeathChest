package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.vanilladeathchest.command.CommandVDCReload;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();

		if(VDCConfig.misc.vdcreloadclient) {
			ClientCommandHandler.instance.registerCommand(new CommandVDCReload(Side.CLIENT));
		}
	}
}
