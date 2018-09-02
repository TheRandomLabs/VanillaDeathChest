package com.therandomlabs.vanilladeathchest;

import com.therandomlabs.vanilladeathchest.command.CommandVDCReload;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);

		if(VDCConfig.misc.vdcreloadclient) {
			ClientCommandHandler.instance.registerCommand(new CommandVDCReload(Side.CLIENT));
		}
	}
}
