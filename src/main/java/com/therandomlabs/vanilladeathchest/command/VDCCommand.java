/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.vanilladeathchest.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestPlacer;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

/**
 * The command that reloads the VanillaDeathChest configuration.
 */
public final class VDCCommand {
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER =
			(context, builder) -> CommandSource.suggestMatching(
					DeathChestsState.get(
							context.getSource().getWorld()
					).getDeathChestIdentifierStrings(), builder
			);

	private static final SimpleCommandExceptionType INVALID_IDENTIFIER_EXCEPTION =
			new SimpleCommandExceptionType(new LiteralText("Invalid death chest identifier"));

	private VDCCommand() {}

	/**
	 * Registers the command that reloads the VanillaDeathChest configuration.
	 *
	 * @param dispatcher the {@link CommandDispatcher}.
	 * @param dedicated whether the server is dedicated.
	 */
	public static void register(
			CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated
	) {
		final LiteralCommandNode<ServerCommandSource> commandNode = dispatcher.register(
				CommandManager.literal("vanilladeathchest").
						then(CommandManager.literal("reloadconfig").
								requires(source -> source.hasPermissionLevel(4)).
								executes(
										context -> executeReloadConfig(context.getSource())
								)
						).
						then(CommandManager.literal("restoreinventory").
								requires(source -> source.hasPermissionLevel(2)).
								then(
										CommandManager.argument(
												"identifier", UuidArgumentType.uuid()
										).suggests(SUGGESTION_PROVIDER).executes(
												context -> executeRestoreInventory(
														context.getSource(), getDeathChest(context)
												)
										).then(
												CommandManager.argument(
														"targets", EntityArgumentType.players()
												).executes(
														context -> executeRestoreInventory(
																context.getSource(),
																getDeathChest(context),
																EntityArgumentType.getPlayers(
																		context, "targets"
																)
														)
												)
										)
								)
						).
						then(CommandManager.literal("place").
								requires(source -> source.hasPermissionLevel(2)).
								then(
										CommandManager.argument(
												"identifier", UuidArgumentType.uuid()
										).suggests(SUGGESTION_PROVIDER).executes(
												context -> executePlace(
														context.getSource(), getDeathChest(context)
												)
										)
								)
						)
		);
		dispatcher.register(CommandManager.literal("vdc").redirect(commandNode));
	}

	private static int executeReloadConfig(ServerCommandSource source) {
		VanillaDeathChest.reloadConfig();
		source.sendFeedback(new LiteralText("VanillaDeathChest configuration reloaded!"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int executeRestoreInventory(ServerCommandSource source, DeathChest deathChest)
			throws CommandSyntaxException {
		return executeRestoreInventory(
				source, deathChest, Collections.singleton(source.getPlayer())
		);
	}

	private static int executeRestoreInventory(
			ServerCommandSource source, DeathChest deathChest,
			Collection<ServerPlayerEntity> players
	) throws CommandSyntaxException {
		for (ServerPlayerEntity player : players) {
			final PlayerInventory inventory = player.inventory;

			for (int i = 0; i < inventory.size(); i++) {
				inventory.setStack(i, deathChest.getInventory().getStack(i).copy());
			}
		}

		source.sendFeedback(new LiteralText("Inventory restored!"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int executePlace(ServerCommandSource source, DeathChest deathChest) {
		DeathChestPlacer.placeAndFillContainer(deathChest);
		DeathChestsState.get(source.getWorld()).addDeathChest(deathChest);
		final BlockPos pos = deathChest.getPos();
		source.sendFeedback(new LiteralText(String.format(
				"Death chest placed at [%s, %s, %s]", pos.getX(), pos.getY(), pos.getZ()
		)), true);
		return Command.SINGLE_SUCCESS;
	}

	private static DeathChest getDeathChest(CommandContext<ServerCommandSource> context)
			throws CommandSyntaxException {
		final DeathChestsState state = DeathChestsState.get(context.getSource().getWorld());
		final Set<UUID> identifiers = state.getDeathChestIdentifiers();
		final UUID identifier = context.getArgument("identifier", UUID.class);

		if (identifiers.contains(identifier)) {
			return state.getDeathChest(identifier);
		}

		throw INVALID_IDENTIFIER_EXCEPTION.create();
	}
}
