package com.therandomlabs.vanilladeathchest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.StringTextComponent;

public class CommandVDCReload {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("vdcreload").
				requires(source -> source.hasPermissionLevel(4)).
				executes(context -> execute(context.getSource())));
	}

	public static int execute(ServerCommandSource source) {
		VDCConfig.reload();

		source.sendFeedback(
				new StringTextComponent("VanillaDeathChest configuration reloaded!"),
				true
		);

		return Command.SINGLE_SUCCESS;
	}
}
