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

package com.therandomlabs.vanilladeathchest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.electronwill.nightconfig.core.conversion.SpecDoubleInRange;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.autoconfigtoml.TOMLConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("CanBeFinal")
@TOMLConfigSerializer.Comment("VanillaDeathChest configuration.")
@Config(name = VanillaDeathChest.MOD_ID)
public final class VDCConfig implements ConfigData {
	public static final class Spawning implements ConfigData {
		@TOMLConfigSerializer.Comment({
				"The death chest container type.",
				"SINGLE_CHEST: Only single chests.",
				"SINGLE_OR_DOUBLE_CHEST: Single or double chests.",
				"SINGLE_SHULKER_BOX: Single shulker boxes.",
				"SINGLE_OR_DOUBLE_SHULKER_BOX: Single or double shulker boxes."
		})
		@ConfigEntry.Gui.Tooltip
		public ContainerType containerType = ContainerType.SINGLE_OR_DOUBLE_CHEST;

		@TOMLConfigSerializer.Comment({
				"The color of the shulker box if the container type is a shulker box.",
				"WHITE: White.",
				"ORANGE: Orange.",
				"MAGENTA: Magenta.",
				"LIGHT_BLUE: Light blue.",
				"YELLOW: Yellow.",
				"LIME: Lime.",
				"PINK: Pink.",
				"GRAY: Gray.",
				"LIGHT_GRAY: Light gray.",
				"CYAN: Cyan.",
				"PURPLE: Purple.",
				"BLUE: Blue.",
				"BROWN: Brown.",
				"GREEN: Green.",
				"RED: Red.",
				"BLACK: Black.",
				"RANDOM: Random color."
		})
		@ConfigEntry.Gui.Tooltip
		public ShulkerBoxColor shulkerBoxColor = ShulkerBoxColor.WHITE;

		@TOMLConfigSerializer.Comment(
				"The dimensions that death chests should or should not spawn in."
		)
		@ConfigEntry.Gui.Tooltip
		public List<String> dimensions = new ArrayList<>();

		@TOMLConfigSerializer.Comment({
				"Whether the dimensions list should be a blacklist or a whitelist.",
				"BLACKLIST: blacklist.",
				"WHITELIST: whitelist."
		})
		@ConfigEntry.Gui.Tooltip
		public DimensionListBehavior dimensionsBehavior = DimensionListBehavior.BLACKLIST;

		@SpecIntInRange(min = 1, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment(
				"The radius around the location of a player's death in which a suitable death " +
						"chest placement location should be searched for."
		)
		@ConfigEntry.Gui.Tooltip
		public int locationSearchRadius = 8;

		@TOMLConfigSerializer.Comment(
				"Causes a death chest to be forcibly placed at the location of a player's death " +
						"if no suitable locations are found nearby."
		)
		@ConfigEntry.Gui.Tooltip
		public boolean forcePlacementIfNoSuitableLocation = true;

		@TOMLConfigSerializer.Comment("Requires death chest placement to be on solid blocks.")
		@ConfigEntry.Gui.Tooltip
		public boolean requirePlacementOnSolidBlocks;

		@TOMLConfigSerializer.Comment(
				"A regular expression that matches the registry names of items that can be " +
						"placed in death chests."
		)
		@ConfigEntry.Gui.Tooltip
		public String registryNameRegex = ".+";

		@TOMLConfigSerializer.Comment({
				"Causes death chests to only be spawned if the necessary container is in the " +
						"player's inventory.",
				"If this is enabled, the container is consumed if it is found."
		})
		@ConfigEntry.Gui.Tooltip
		public boolean useContainerInInventory;

		@TOMLConfigSerializer.Comment({
				"The display name of the death chest container.",
				"Set this to an empty string to cause a custom display name to not be used."
		})
		@ConfigEntry.Gui.Tooltip
		public String containerDisplayName = "Death Chest";

		@TOMLConfigSerializer.Comment({
				"The message sent to a player after a death chest is placed when they die.",
				"The X, Y and Z coordinates are provided as arguments.",
				"Set this to an empty string to disable this message."
		})
		@ConfigEntry.Gui.Tooltip
		public String spawnMessage = "Death chest spawned at [%s, %s, %s]";

		@Nullable
		@ConfigEntry.Gui.Excluded
		private Set<Identifier> dimensionIdentifiers;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validatePostLoad() {
			dimensionIdentifiers = dimensions.stream().
					map(Identifier::new).
					collect(Collectors.toSet());
			dimensions = dimensionIdentifiers.stream().
					map(Identifier::toString).
					collect(Collectors.toList());
		}

		/**
		 * Returns whether death chest spawning is enabled in the specified dimension.
		 *
		 * @param dimension a {@link DimensionType}.
		 * @return {@code true} if death chest spawning is enabled in the specified dimension,
		 * or otherwise {@code false}.
		 */
		@SuppressWarnings({"ConstantConditions", "NullAway"})
		public boolean isDimensionEnabled(DimensionType dimension) {
			final boolean anyMatch =
					dimensionIdentifiers.stream().anyMatch(dimension.getSkyProperties()::equals);

			if (dimensionsBehavior == DimensionListBehavior.BLACKLIST) {
				return !anyMatch;
			}

			return anyMatch;
		}
	}

	public static final class KeyItem implements ConfigData {
		@TOMLConfigSerializer.Comment({
				"The registry name of the key item.",
				"A player must be holding this item in their main hand to unlock a death chest.",
				"Set this to an empty string to allow death chests to be unlocked without an item."
		})
		@ConfigEntry.Gui.Tooltip
		public String registryName = "";

		@SpecIntInRange(min = 0, max = Short.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"The meta value of the key item.",
				"Set this to " + Short.MAX_VALUE + " to not require a specific meta value."
		})
		@ConfigEntry.Gui.Tooltip
		public int meta = Short.MAX_VALUE;

		@TOMLConfigSerializer.Comment({
				"The key consumption behavior.",
				"CONSUME: Consume the item.",
				"DAMAGE: Damage the item."
		})
		@ConfigEntry.Gui.Tooltip
		public KeyConsumptionBehavior consumptionBehavior = KeyConsumptionBehavior.CONSUME;

		@SpecIntInRange(min = 0, max = Short.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"The amount by which the key item should be consumed.",
				"If the key item cannot be consumed this many times, the death chest will not " +
						"be unlocked.",
				"Players in creative mode will not have their key item consumed."
		})
		@ConfigEntry.Gui.Tooltip
		public int amountToConsume = 1;

		@TOMLConfigSerializer.Comment({
				"The message that is sent to the player when they fail to unlock a death chest.",
				"This string takes the required amount (%1$s) and display name (%2$s) of the " +
						"item as arguments.",
				"Set this to an empty string to disable this message."
		})
		@ConfigEntry.Gui.Tooltip
		public String unlockFailureMessage = "You need %s of %s to retrieve your items";

		@TOMLConfigSerializer.Comment(
				"Whether the unlock failed message should be sent as a status message rather " +
						"than a chat message."
		)
		@ConfigEntry.Gui.Tooltip
		public boolean unlockFailureStatusMessage = true;

		@Nullable
		@ConfigEntry.Gui.Excluded
		public Item item;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validatePostLoad() {
			if (!registryName.isEmpty()) {
				item = Registry.ITEM.get(new Identifier(registryName));

				if (item == Items.AIR) {
					registryName = "";
					item = null;
				} else {
					registryName = new Identifier(registryName).toString();
				}
			}
		}
	}

	public static final class DefenseEntities implements ConfigData {
		@TOMLConfigSerializer.Comment({
				"The registry name of the defense entity.",
				"If the defense entity is a living entity, it will not automatically despawn.",
				"If the defense entity can have a revenge target, then the revenge target will " +
						"be set to its player.",
				"Set this to an empty string to disable defense entities."
		})
		@ConfigEntry.Gui.Tooltip
		public String registryName = "";

		@TOMLConfigSerializer.Comment("Custom NBT data for defense entities in JSON format.")
		@ConfigEntry.Gui.Tooltip
		public String nbtTag = "{}";

		@TOMLConfigSerializer.Comment("Causes defense entities to drop experience.")
		@ConfigEntry.Gui.Tooltip
		public boolean dropExperience;

		@TOMLConfigSerializer.Comment("Causes defense entities to drop items.")
		@ConfigEntry.Gui.Tooltip
		public boolean dropItems;

		@SpecIntInRange(min = 1, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment(
				"The number of defense entities that are spawned when a death chest is placed."
		)
		@ConfigEntry.Gui.Tooltip
		public int spawnCount = 3;

		@SpecDoubleInRange(min = 0.0, max = Double.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"The maximum squared distance that a defense entity can be from its chest when " +
						"a player is not nearby.",
				"Set this to 0.0 to disable the limit so that defense entities are not " +
						"teleported back to their death chest."
		})
		@ConfigEntry.Gui.Tooltip
		public double maxSquaredDistanceFromChest = 64.0;

		@SpecDoubleInRange(min = 0.0, max = Double.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"The maximum squared distance that a defense entity can be from its player when" +
						"its chest is not within the maximum squared distance.",
				"Set this to 0.0 to disable the limit so that defense entities are teleported " +
						"back to their death chest regardless of their distance from the player."
		})
		@ConfigEntry.Gui.Tooltip
		public double maxSquaredDistanceFromPlayer = 64.0;

		@Nullable
		@ConfigEntry.Gui.Excluded
		public EntityType<? extends Entity> entityType;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void validatePostLoad() {
			if (!registryName.isEmpty()) {
				final Optional<EntityType<?>> optional =
						Registry.ENTITY_TYPE.getOrEmpty(new Identifier(registryName));

				if (optional.isPresent()) {
					registryName = new Identifier(registryName).toString();
					entityType = optional.get();
				} else {
					registryName = "";
					entityType = null;
				}
			}

			try {
				StringNbtReader.parse(nbtTag);
			} catch (CommandSyntaxException ignored) {
				nbtTag = "{}";
			}
		}
	}

	public static final class Protection {
		@TOMLConfigSerializer.Comment({
				"Enables death chest protection.",
				"When a death chest is protected, it can only be unlocked by its owner."
		})
		@ConfigEntry.Gui.Tooltip
		public boolean enable = true;

		@SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment(
				"The required permission level to bypass death chest protection."
		)
		@ConfigEntry.Gui.Tooltip
		public int bypassPermissionLevel = 3;

		@TOMLConfigSerializer.Comment(
				"Causes players in creative mode to be able to bypass death chest protection."
		)
		@ConfigEntry.Gui.Tooltip
		public boolean bypassInCreativeMode = true;

		@SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"The length of death chest protection in ticks.",
				"120000 ticks is five in-game days.",
				"Set this to 0 to cause death chests to be protected indefinitely."
		})
		@ConfigEntry.Gui.Tooltip
		public int period = 120000;
	}

	public static final class Misc {
		@TOMLConfigSerializer.Comment("Causes death chests to be removed when they are emptied.")
		@ConfigEntry.Gui.Tooltip
		public boolean removeEmptyDeathChests = true;

		@TOMLConfigSerializer.Comment("Causes death chests to be dropped when they are broken.")
		@ConfigEntry.Gui.Tooltip
		public boolean dropDeathChests;

		@TOMLConfigSerializer.Comment({
				"The name of the game rule that controls whether death chests should be spawned.",
				"Set this to an empty string to disable the game rule.",
				"Changes to this option are applied after a game restart."
		})
		@ConfigEntry.Gui.Tooltip
		public String gameRuleName = "spawnDeathChests";

		@TOMLConfigSerializer.Comment({
				"The name of the command that reloads this configuration from disk.",
				"Set this to an empty string to disable the command.",
				"Changes to this option are applied when a server is loaded."
		})
		@ConfigEntry.Gui.Tooltip
		public String configReloadCommand = "vdcconfigreload";
	}

	/**
	 * The death chest container type.
	 */
	public enum ContainerType {
		/**
		 * Only single chests.
		 */
		SINGLE_CHEST,
		/**
		 * Single or double chests.
		 */
		SINGLE_OR_DOUBLE_CHEST,
		/**
		 * Only single shulker boxes.
		 */
		SINGLE_SHULKER_BOX,
		/**
		 * Single or double shulker boxes.
		 */
		SINGLE_OR_DOUBLE_SHULKER_BOX
	}

	/**
	 * The shulker box color.
	 */
	public enum ShulkerBoxColor {
		/**
		 * White.
		 */
		WHITE,
		/**
		 * Orange.
		 */
		ORANGE,
		/**
		 * Magenta.
		 */
		MAGENTA,
		/**
		 * Light blue.
		 */
		LIGHT_BLUE,
		/**
		 * Yellow.
		 */
		YELLOW,
		/**
		 * Lime.
		 */
		LIME,
		/**
		 * Pink.
		 */
		PINK,
		/**
		 * Gray.
		 */
		GRAY,
		/**
		 * Light gray.
		 */
		LIGHT_GRAY,
		/**
		 * Cyan.
		 */
		CYAN,
		/**
		 * Purple.
		 */
		PURPLE,
		/**
		 * Blue.
		 */
		BLUE,
		/**
		 * Brown.
		 */
		BROWN,
		/**
		 * Green.
		 */
		GREEN,
		/**
		 * Red.
		 */
		RED,
		/**
		 * Black.
		 */
		BLACK,
		/**
		 * Random color.
		 */
		RANDOM;

		private static final Random random = new Random();

		@Nullable
		private final DyeColor color;

		ShulkerBoxColor() {
			color = "RANDOM".equals(name()) ? null : DyeColor.valueOf(name());
		}

		/**
		 * Returns this shulker box color as a {@link DyeColor}.
		 *
		 * @return this shulker box color as a {@link DyeColor}.
		 */
		public DyeColor get() {
			return color == null ? DyeColor.byId(random.nextInt(16)) : color;
		}
	}

	/**
	 * Dimension list behaviors.
	 */
	public enum DimensionListBehavior {
		/**
		 * Blacklist.
		 */
		BLACKLIST,
		/**
		 * Whitelist.
		 */
		WHITELIST
	}

	/**
	 * Key item consumption behaviors.
	 */
	public enum KeyConsumptionBehavior {
		/**
		 * Consume the item.
		 */
		CONSUME,
		/**
		 * Damage the item.
		 */
		DAMAGE
	}

	@TOMLConfigSerializer.Comment("Options related to death chest spawning.")
	@ConfigEntry.Category("spawning")
	@ConfigEntry.Gui.TransitiveObject
	public Spawning spawning = new Spawning();

	@TOMLConfigSerializer.Comment("Options related to the death chest key item.")
	@ConfigEntry.Category("key_item")
	@ConfigEntry.Gui.TransitiveObject
	public KeyItem keyItem = new KeyItem();

	@TOMLConfigSerializer.Comment("Options related to death chest defense entities.")
	@ConfigEntry.Category("defense_entities")
	@ConfigEntry.Gui.TransitiveObject
	public DefenseEntities defenseEntities = new DefenseEntities();

	@TOMLConfigSerializer.Comment("Options related to death chest protection.")
	@ConfigEntry.Category("protection")
	@ConfigEntry.Gui.TransitiveObject
	public Protection protection = new Protection();

	@TOMLConfigSerializer.Comment("Miscellaneous options.")
	@ConfigEntry.Category("misc")
	@ConfigEntry.Gui.TransitiveObject
	public Misc misc = new Misc();
}
