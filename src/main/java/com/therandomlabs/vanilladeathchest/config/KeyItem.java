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

import com.therandomlabs.vanilladeathchest.VDCConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;

@Config(name = "key_item")
public final class KeyItem implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	public String registryName = "";

	@ConfigEntry.BoundedDiscrete(min = 0, max = Short.MAX_VALUE)
	@ConfigEntry.Gui.Tooltip
	public int meta = Short.MAX_VALUE;

	@ConfigEntry.Gui.Tooltip
	public VDCConfig.KeyConsumptionBehavior
			consumptionBehavior = VDCConfig.KeyConsumptionBehavior.CONSUME;

	@ConfigEntry.BoundedDiscrete(min = 0, max = Short.MAX_VALUE)
	@ConfigEntry.Gui.Tooltip
	public int amountToConsume = 1;

	@ConfigEntry.Gui.Tooltip
	public String unlockFailureMessage = "You need %s of %s to retrieve your items";

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
