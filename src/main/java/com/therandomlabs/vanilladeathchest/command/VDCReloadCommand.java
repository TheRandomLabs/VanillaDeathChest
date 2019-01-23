package com.therandomlabs.vanilladeathchest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;

public final class VDCReloadCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("vdcreload").
				requires(source -> source.hasPermissionLevel(4)).
				executes(context -> execute(context.getSource())));
	}

	public static int execute(ServerCommandSource source) {
		VDCConfig.reload();

		final MinecraftServer server = source.getMinecraftServer();

		if(server != null && server.isDedicated()) {
			source.sendFeedback(
					new StringTextComponent("VanillaDeathChest configuration reloaded!"),
					true
			);
		} else {
			source.sendFeedback(
					new TranslatableTextComponent("commands.vdcreloadclient.success"),
					true
			);
		}

		return Command.SINGLE_SUCCESS;
	}
}
