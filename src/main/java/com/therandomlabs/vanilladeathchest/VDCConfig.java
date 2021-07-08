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

import java.util.Random;

import com.therandomlabs.vanilladeathchest.config.DefenseEntities;
import com.therandomlabs.vanilladeathchest.config.KeyItem;
import com.therandomlabs.vanilladeathchest.config.Misc;
import com.therandomlabs.vanilladeathchest.config.Protection;
import com.therandomlabs.vanilladeathchest.config.Spawning;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.util.DyeColor;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("CanBeFinal")
@Config(name = VanillaDeathChest.MOD_ID)
public final class VDCConfig extends PartitioningSerializer.GlobalData {
	@ConfigEntry.Category("spawning")
	@ConfigEntry.Gui.TransitiveObject
	public Spawning spawning = new Spawning();

	@ConfigEntry.Category("key_item")
	@ConfigEntry.Gui.TransitiveObject
	public KeyItem keyItem = new KeyItem();

	@ConfigEntry.Category("defense_entities")
	@ConfigEntry.Gui.TransitiveObject
	public DefenseEntities defenseEntities = new DefenseEntities();

	@ConfigEntry.Category("protection")
	@ConfigEntry.Gui.TransitiveObject
	public Protection protection = new Protection();

	@ConfigEntry.Category("misc")
	@ConfigEntry.Gui.TransitiveObject
	public Misc misc = new Misc();

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
}
