package com.therandomlabs.vanilladeathchest.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandVDCReload {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literalArgument("vdcreload").
				requires(source -> source.func_197034_c(4)).
				executes(context -> execute(context.getSource())));
	}

	public static int execute(CommandSource source) {
		VDCConfig.reload();

		if(source.getServer().isDedicatedServer()) {
			source.sendFeedback(
					new TextComponentString("VanillaDeathChest configuration reloaded!"),
					true
			);
		} else {
			source.sendFeedback(
					new TextComponentTranslation("commands.vdcreloadclient.success"),
					true
			);
		}

		return Command.SINGLE_SUCCESS;
	}
}
