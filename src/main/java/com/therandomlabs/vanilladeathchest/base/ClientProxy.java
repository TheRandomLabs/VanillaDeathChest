package com.therandomlabs.vanilladeathchest.base;

import com.therandomlabs.vanilladeathchest.CommandVDCReload;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ClientCommandHandler.instance.registerCommand(new CommandVDCReload(Side.CLIENT));
	}
}
