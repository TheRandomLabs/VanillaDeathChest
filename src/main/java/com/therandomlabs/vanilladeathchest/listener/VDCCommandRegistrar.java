package com.therandomlabs.vanilladeathchest.listener;

import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.vanilladeathchest.command.CommandVDCReload;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.command.CommandSource;
import org.dimdev.rift.listener.CommandAdder;

public class VDCCommandRegistrar implements CommandAdder {
	@Override
	public void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
		if(VDCConfig.misc.vdcreload) {
			CommandVDCReload.register(dispatcher);
		}
	}
}
