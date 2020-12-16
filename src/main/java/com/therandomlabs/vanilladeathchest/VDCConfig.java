/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 TheRandomLabs
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

import com.electronwill.nightconfig.core.conversion.SpecDoubleInRange;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.autoconfigtoml.TOMLConfigSerializer;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("CanBeFinal")
@TOMLConfigSerializer.Comment("VanillaDeathChest configuration.")
@Config(name = VanillaDeathChest.MOD_ID)
public final class VDCConfig implements ConfigData {
	public static final class Defense implements ConfigData {
		@TOMLConfigSerializer.Comment(
				"Whether the unlocker should be damaged rather than consumed."
		)
		public boolean damageUnlockerInsteadOfConsume;

		@TOMLConfigSerializer.Comment("Whether defense entities drop experience.")
		public boolean defenseEntityDropsExperience;

		@TOMLConfigSerializer.Comment("Whether defense entities drop items.")
		public boolean defenseEntityDropsItems;

		@SpecDoubleInRange(min = 0.0, max = Double.MAX_VALUE)
		@TOMLConfigSerializer.Comment(
				"The maximum distance squared that a defense entity can be from its chest."
		)
		public double defenseEntityMaxDistanceSquared = 64.0;

		@SpecDoubleInRange(min = 0.0, max = Double.MAX_VALUE)
		@TOMLConfigSerializer.Comment(
				"The maximum distance squared that a defense entity can be from its player if it " +
						"is too far away from its chest."
		)
		public double defenseEntityMaxDistanceSquaredFromPlayer = 64.0;

		@TOMLConfigSerializer.Comment("The defense entity NBT data.")
		public String defenseEntityNBT = "{}";

		@TOMLConfigSerializer.Comment({
				"The registry name of the defense entity.",
				"If the defense entity is a living entity, it will not automatically despawn.",
				"If the defense entity can have a revenge target, then the revenge target will " +
						"be set to the player that died."
		})
		public String defenseEntityRegistryName = "";

		@SpecIntInRange(min = 1, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment("The number of defense entities to spawn.")
		public int defenseEntitySpawnCount = 3;

		//not air
		@TOMLConfigSerializer.Comment("The registry name of the unlocker.")
		public String unlocker = "";

		@SpecIntInRange(min = 0, max = Short.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"How many times the unlocker should be consumed or damaged.",
				"If the unlocker cannot be consumed or damage this many times, the death chest " +
						"will not be unlocked.",
				"Note that only the stack that the player is holding will be consumed, and that " +
						"players in creative mode will not have their unlocker item consumed."
		})
		public int unlockerConsumeAmount = 1;

		@SpecIntInRange(min = 0, max = Short.MAX_VALUE)
		@TOMLConfigSerializer.Comment("The meta value of the unlocker.")
		public int unlockerMeta = Short.MAX_VALUE;

		@TOMLConfigSerializer.Comment({
				"The message that is sent to the player when they fail to unlock a death chest.",
				"This string takes the required amount and display name of the item as arguments."
		})
		public String unlockFailedMessage =
				"You need %s of the following item to retrieve your items: %s";

		@TOMLConfigSerializer.Comment(
				"Whether the unlock failed message should be a status message rather than a " +
						"chat message."
		)
		public boolean unlockFailedStatusMessage = true;

		@ConfigEntry.Gui.Excluded
		public EntityType<? extends Entity> defenseEntity;

		@ConfigEntry.Gui.Excluded
		public Item unlockerItem;

		@Override
		public void validatePostLoad() {
			if (!defenseEntityRegistryName.isEmpty()) {
				defenseEntity = Registry.ENTITY_TYPE.get(new Identifier(defenseEntityRegistryName));
			}

			if (!unlocker.isEmpty()) {
				unlockerItem = Registry.ITEM.get(new Identifier(unlocker));
			}

			try {
				StringNbtReader.parse(defenseEntityNBT);
			} catch (CommandSyntaxException ignored) {
				defenseEntityNBT = "{}";
			}
		}
	}

	public static final class Misc {
		@TOMLConfigSerializer.Comment(
				"Whether death chests should disappear when they are emptied.")
		public boolean deathChestsDisappearWhenEmptied = true;

		@TOMLConfigSerializer.Comment({
				"Whether death chests should be dropped when broken.",
				"Enable this for infinite chests."
		})
		public boolean dropDeathChests;

		@TOMLConfigSerializer.Comment({
				"The name of the disableDeathChests gamerule.",
				"Set this to an empty string to disable the gamerule."
		})
		public String gameRuleName = "disableDeathChests";

		@TOMLConfigSerializer.Comment("Whether to enable the /vdcconfigreload command.")
		public String configReloadCommand = "vdcconfigreload";
	}

	public static final class Protection {
		@TOMLConfigSerializer.Comment(
				"Whether players in creative mode should be able to bypass death chest " +
						"protection."
		)
		public boolean bypassIfCreative = true;

		@SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment(
				"The required permission level to bypass death chest protection.")
		public int bypassPermissionLevel = 3;

		@TOMLConfigSerializer.Comment({
				"Whether death chests should be protected.",
				"When this is enabled, death chests can only be broken by their owners."
		})
		public boolean enabled = true;

		@SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment({
				"The amount of time in ticks death chest protection should last.",
				"120000 ticks is 5 in-game days.",
				"Set this to 0 to protect death chests indefinitely."
		})
		public int period = 120000;
	}

	public static final class Spawning {
		@TOMLConfigSerializer.Comment({
				"The message sent to a player a death chest is placed after they die.",
				"%s refers to the X, Y and Z coordinates.",
				"Set this to an empty string to disable the message."
		})
		public String chatMessage = "Death chest spawned at [%s, %s, %s]";

		@TOMLConfigSerializer.Comment({
				"The display name of the death chest container.",
				"Leave this empty to not specify a custom name."
		})
		public String containerDisplayName = "Death Chest";

		@TOMLConfigSerializer.Comment("The type of death chest that should be placed.")
		public DeathChestPlacer.DeathChestType chestType =
				DeathChestPlacer.DeathChestType.SINGLE_OR_DOUBLE;

		@TOMLConfigSerializer.Comment(
				"Whether to force place a death chest at the location of a player's death if no " +
						"viable locations are found."
		)
		public boolean forcePlaceIfLocationNotFound = true;

		@SpecIntInRange(min = 1, max = Integer.MAX_VALUE)
		@TOMLConfigSerializer.Comment("The death chest location search radius.")
		public int locationSearchRadius = 8;

		@TOMLConfigSerializer.Comment("Whether death chests can only spawn on solid blocks.")
		public boolean mustBeOnSolidBlocks;

		@TOMLConfigSerializer.Comment(
				"A regular expression that matches the registry names of items that can be " +
						"placed in death chests."
		)
		public String registryNameRegex = ".+";

		@TOMLConfigSerializer.Comment({
				"Whether death chests should only be spawned if the container can be found in " +
						"the player's inventory.",
				"If this is enabled, the container is consumed if it is found."
		})
		public boolean useContainerInInventory;

		@TOMLConfigSerializer.Comment(
				"The color of the shulker box if chestType is set to SHULKER_BOX or " +
						"DOUBLE_SHULKER_BOX.")
		public ShulkerBoxColor shulkerBoxColor = ShulkerBoxColor.WHITE;
	}

	@TOMLConfigSerializer.Comment("Options related to death chest defense.")
	@ConfigEntry.Category("defense")
	@ConfigEntry.Gui.TransitiveObject
	public Defense defense = new Defense();

	@TOMLConfigSerializer.Comment("Options that don't fit into any other categories.")
	@ConfigEntry.Category("misc")
	@ConfigEntry.Gui.TransitiveObject
	public Misc misc = new Misc();

	@TOMLConfigSerializer.Comment("Options related to death chest protection.")
	@ConfigEntry.Category("protection")
	@ConfigEntry.Gui.TransitiveObject
	public Protection protection = new Protection();

	@TOMLConfigSerializer.Comment("Options related to death chest spawning.")
	@ConfigEntry.Category("spawning")
	@ConfigEntry.Gui.TransitiveObject
	public Spawning spawning = new Spawning();
}
