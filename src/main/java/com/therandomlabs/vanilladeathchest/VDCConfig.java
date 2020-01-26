/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2019 TheRandomLabs
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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.utils.config.Config;
import com.therandomlabs.utils.fabric.config.ColorProperty;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.item.Item;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("NullAway")
@Config(id = VanillaDeathChest.MOD_ID, comment = "VanillaDeathChest configuration")
public final class VDCConfig {
	public static final class Defense {
		@Config.Property("Whether the unlocker should be damaged rather than consumed.")
		public static boolean damageUnlockerInsteadOfConsume;

		@Config.Property("Whether defense entities drop experience.")
		public static boolean defenseEntityDropsExperience;

		@Config.Property("Whether defense entities drop items.")
		public static boolean defenseEntityDropsItems;

		@Config.RangeDouble(min = 0.0)
		@Config.Property(
				"The maximum distance squared that a defense entity can be from its chest."
		)
		public static double defenseEntityMaxDistanceSquared = 64.0;

		@Config.RangeDouble(min = 0.0)
		@Config.Property(
				"The maximum distance squared that a defense entity can be from its player if it " +
						"is too far away from its chest."
		)
		public static double defenseEntityMaxDistanceSquaredFromPlayer = 64.0;

		@Config.Property("The defense entity NBT data.")
		public static String defenseEntityNBT = "{}";

		@Config.Property({
				"The registry name of the defense entity.",
				"If the defense entity is a living entity, it will not automatically despawn.",
				"If the defense entity can have a revenge target, then the revenge target will " +
						"be set to the player that died."
		})
		public static String defenseEntityRegistryName = "";

		@Config.RangeInt(min = 1)
		@Config.Property("The number of defense entities to spawn.")
		public static int defenseEntitySpawnCount = 3;

		@Config.Blacklist("minecraft:air")
		@Config.Property("The registry name of the unlocker.")
		public static Item unlocker;

		@Config.RangeInt(min = 0, max = Short.MAX_VALUE)
		@Config.Property({
				"How many times the unlocker should be consumed or damaged.",
				"If the unlocker cannot be consumed or damage this many times, the death chest " +
						"will not be unlocked.",
				"Note that only the stack that the player is holding will be consumed, and that " +
						"players in creative mode will not have their unlocker item consumed."
		})
		public static int unlockerConsumeAmount = 1;

		@Config.RangeInt(min = 0, max = Short.MAX_VALUE)
		@Config.Property("The meta value of the unlocker.")
		public static int unlockerMeta = Short.MAX_VALUE;

		@Config.Property({
				"The message that is sent to the player when they fail to unlock a death chest.",
				"This string takes the required amount and display name of the item as arguments."
		})
		public static String unlockFailedMessage =
				"You need %s of the following item to retrieve your items: %s";

		@SuppressWarnings("GrazieInspection")
		@Config.Property(
				"Whether the unlock failed message should be a status message rather than a " +
						"chat message."
		)
		public static boolean unlockFailedStatusMessage = true;

		public static Identifier defenseEntity;

		@SuppressWarnings("Duplicates")
		public static void onReload() {
			try {
				StringNbtReader.parse(defenseEntityNBT);
			} catch(CommandSyntaxException ignored) {
				defenseEntityNBT = "{}";
			}

			final Identifier entityID = new Identifier(defenseEntityRegistryName);

			if(Registry.ENTITY_TYPE.containsId(entityID)) {
				defenseEntity = entityID;
				defenseEntityRegistryName = entityID.toString();
			} else {
				defenseEntity = null;
				defenseEntityRegistryName = "";
			}
		}
	}

	public static final class Misc {
		@Config.Property("Whether death chests should disappear when they are emptied.")
		public static boolean deathChestsDisappearWhenEmptied = true;

		@Config.Property({
				"Whether death chests should be dropped when broken.",
				"Enable this for infinite chests."
		})
		public static boolean dropDeathChests;

		@Config.Property({
				"The name of the disableDeathChests gamerule.",
				"Set this to an empty string to disable the gamerule."
		})
		public static String gameRuleName = "disableDeathChests";

		@Config.RequiresReload
		@Config.Property("Whether to enable the /vdcreload command.")
		public static boolean vdcreload = true;
	}

	public static final class Protection {
		@Config.Property(
				"Whether players in creative mode should be able to bypass death chest " +
						"protection."
		)
		public static boolean bypassIfCreative = true;

		@Config.RangeInt(min = 0)
		@Config.Property("The required permission level to bypass death chest protection.")
		public static int bypassPermissionLevel = 3;

		@Config.Property({
				"Whether death chests should be protected.",
				"When this is enabled, death chests can only be broken by their owners."
		})
		public static boolean enabled = true;

		@Config.RangeInt(min = 0)
		@Config.Property({
				"The amount of time in ticks death chest protection should last.",
				"120000 ticks is 5 in-game days.",
				"Set this to 0 to protect death chests indefinitely."
		})
		public static int period = 120000;
	}

	public static final class Spawning {
		@Config.Property({
				"The message sent to a player a death chest is placed after they die.",
				"%s refers to the X, Y and Z coordinates.",
				"Set this to an empty string to disable the message."
		})
		public static String chatMessage = "Death chest spawned at [%s, %s, %s]";

		@Config.Property({
				"The display name of the death chest container.",
				"Leave this empty to not specify a custom name."
		})
		public static String containerDisplayName = "Death Chest";

		@Config.Property("The type of death chest that should be placed.")
		public static DeathChestPlacer.DeathChestType chestType =
				DeathChestPlacer.DeathChestType.SINGLE_OR_DOUBLE;

		@Config.Property(
				"Whether to force place a death chest at the location of a player's death if no " +
						"viable locations are found."
		)
		public static boolean forcePlaceIfLocationNotFound = true;

		@Config.RangeInt(min = 1)
		@Config.Property("The death chest location search radius.")
		public static int locationSearchRadius = 8;

		@Config.Property("Whether death chests can only spawn on solid blocks.")
		public static boolean mustBeOnSolidBlocks;

		@Config.Property("The color of the shulker box if chestType is set to SHULKER_BOX.")
		public static ColorProperty shulkerBoxColor = ColorProperty.WHITE;
	}

	@Config.Category("Options related to death chest defense.")
	public static final Defense defense = null;

	@Config.Category("Options that don't fit into any other categories.")
	public static final Misc misc = null;

	@Config.Category("Options related to death chest protection.")
	public static final Protection protection = null;

	@Config.Category("Options related to death chest spawning.")
	public static final Spawning spawning = null;
}
