package com.therandomlabs.vanilladeathchest.command;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

public class CommandVDCReload extends CommandBase {
	private final boolean client;

	public CommandVDCReload(Side side) {
		client = side.isClient();
	}

	@Override
	public String getName() {
		return client ? "vdcreloadclient" : "vdcreload";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return client ? 0 : 4;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return client ? "commands.vdcreloadclient.usage" : "/" + getName();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {
		VDCConfig.reload();

		if(server.isDedicatedServer()) {
			notifyCommandListener(sender, this, "VanillaDeathChest configuration reloaded!");
		} else {
			sender.sendMessage(new TextComponentTranslation("commands.vdcreloadclient.success"));
		}
	}
}
