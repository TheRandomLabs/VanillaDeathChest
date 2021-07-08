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

import java.util.Optional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;

@Config(name = "defense_entities")
public final class DefenseEntities implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	public String registryName = "";

	@ConfigEntry.Gui.Tooltip
	public String nbtTag = "{}";

	@ConfigEntry.Gui.Tooltip
	public boolean dropExperience;

	@ConfigEntry.Gui.Tooltip
	public boolean dropItems;

	@ConfigEntry.BoundedDiscrete(min = 1, max = Integer.MAX_VALUE)
	@ConfigEntry.Gui.Tooltip
	public int spawnCount = 3;

	@ConfigEntry.BoundedDiscrete(min = 0, max = (long) Double.MAX_VALUE)
	@ConfigEntry.Gui.Tooltip
	public double maxSquaredDistanceFromChest = 64.0;

	@ConfigEntry.BoundedDiscrete(min = 0, max = (long) Double.MAX_VALUE)
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
		} catch (CommandSyntaxException ex) {
			nbtTag = "{}";
		}
	}
}
