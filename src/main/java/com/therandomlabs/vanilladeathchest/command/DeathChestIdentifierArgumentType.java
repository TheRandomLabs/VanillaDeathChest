package com.therandomlabs.vanilladeathchest.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

/**
 * A death chest identifier {@link ArgumentType}.
 */
public final class DeathChestIdentifierArgumentType implements ArgumentType<UUID> {
	private static final SimpleCommandExceptionType INVALID_IDENTIFIER_EXCEPTION =
			new SimpleCommandExceptionType(
					new TranslatableText("argument.death_chest_identifier.invalid")
			);

	private static final Collection<String> EXAMPLES =
			Collections.singletonList("dd12be42-52a9-4a91-a8a1-11c01849e498");

	private DeathChestIdentifierArgumentType() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID parse(StringReader reader) throws CommandSyntaxException {
		try {
			return UUID.fromString(reader.readUnquotedString());
		} catch (IllegalArgumentException ex) {
			throw INVALID_IDENTIFIER_EXCEPTION.create();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context, SuggestionsBuilder builder
	) {
		if (!(context.getSource() instanceof CommandSource)) {
			return Suggestions.empty();
		}

		if (context.getSource() instanceof ServerCommandSource) {
			return CommandSource.suggestMatching(
					getIdentifiers(context).stream().map(UUID::toString), builder
			);
		}

		//Request suggestions from the server.
		return ((CommandSource) context.getSource()).getCompletions(
				(CommandContext<CommandSource>) context, builder
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	/**
	 * Returns a new {@link DeathChestIdentifierArgumentType} instance.
	 *
	 * @return a new {@link DeathChestIdentifierArgumentType} instance.
	 */
	public static DeathChestIdentifierArgumentType identifier() {
		return new DeathChestIdentifierArgumentType();
	}

	/**
	 * Returns the death chest identifier for the specified command context and argument name.
	 *
	 * @param context a {@link CommandContext} with a {@link ServerCommandSource}.
	 * @param name an argument name.
	 * @return a {@link UUID}.
	 * @throws CommandSyntaxException if an invalid identifier is provided.
	 */
	public static UUID getIdentifier(CommandContext<ServerCommandSource> context, String name)
			throws CommandSyntaxException {
		final UUID identifier = context.getArgument(name, UUID.class);

		if (getIdentifiers(context).contains(identifier)) {
			return identifier;
		}

		throw INVALID_IDENTIFIER_EXCEPTION.create();
	}

	/**
	 * Returns the death chest for the specified command context and argument name.
	 *
	 * @param context a {@link CommandContext} with a {@link ServerCommandSource}.
	 * @param name an argument name.
	 * @return a {@link DeathChest}.
	 * @throws CommandSyntaxException if an invalid identifier is provided.
	 */
	@SuppressWarnings("NullAway")
	public static DeathChest getDeathChest(
			CommandContext<ServerCommandSource> context, String name
	) throws CommandSyntaxException {
		return DeathChestsState.get(context.getSource().getWorld()).
				getDeathChest(getIdentifier(context, name));
	}

	@SuppressWarnings("unchecked")
	private static Set<UUID> getIdentifiers(CommandContext<?> context) {
		return DeathChestsState.get(
				((CommandContext<ServerCommandSource>) context).getSource().getWorld()
		).getDeathChestIdentifiers();
	}
}
