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

package com.therandomlabs.vanilladeathchest.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;

@Config(name = "spawning")
public final class Spawning implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	public VDCConfig.ContainerType containerType = VDCConfig.ContainerType.SINGLE_OR_DOUBLE_CHEST;

	@ConfigEntry.Gui.Tooltip
	public VDCConfig.ShulkerBoxColor shulkerBoxColor = VDCConfig.ShulkerBoxColor.WHITE;

	@ConfigEntry.Gui.Tooltip
	public List<String> dimensions = new ArrayList<>();

	@ConfigEntry.Gui.Tooltip
	public VDCConfig.DimensionListBehavior dimensionsBehavior =
			VDCConfig.DimensionListBehavior.BLACKLIST;

	@ConfigEntry.BoundedDiscrete(min = 1, max = Integer.MAX_VALUE)
	@ConfigEntry.Gui.Tooltip
	public int locationSearchRadius = 8;

	@ConfigEntry.Gui.Tooltip
	public boolean forcePlacementIfNoSuitableLocation = true;

	@ConfigEntry.Gui.Tooltip
	public boolean requirePlacementOnSolidBlocks;

	@ConfigEntry.Gui.Tooltip
	public String registryNameRegex = ".+";

	@ConfigEntry.Gui.Tooltip
	public boolean useContainerInInventory;

	@ConfigEntry.Gui.Tooltip
	public String containerDisplayName = "Death Chest";

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
		System.out.println("validating postload \n\n\n");
		dimensionIdentifiers = dimensions.stream().
				map(Identifier::new).
				collect(Collectors.toSet());
		dimensions = dimensionIdentifiers.stream().
				map(Identifier::toString).
				collect(Collectors.toList());
	}

	/**
	 * Returns whether death chest spawning is enabled in the specified world's dimension.
	 *
	 * @param world a {@link World}.
	 * @return {@code true} if death chest spawning is enabled in the specified dimension,
	 * or otherwise {@code false}.
	 */
	@SuppressWarnings({"ConstantConditions", "NullAway"})
	public boolean isDimensionEnabled(World world) {
		final Identifier identifier = world.getRegistryManager().
				get(Registry.DIMENSION_TYPE_KEY).
				getId(world.getDimension());

		if (identifier == null) {
			VanillaDeathChest.logger.error(
					"Failed to determine dimension", new RuntimeException()
			);
			return dimensionsBehavior == VDCConfig.DimensionListBehavior.BLACKLIST;
		}

		final boolean anyMatch = dimensionIdentifiers.stream().anyMatch(identifier::equals);

		if (dimensionsBehavior == VDCConfig.DimensionListBehavior.BLACKLIST) {
			return !anyMatch;
		}

		return anyMatch;
	}
}
