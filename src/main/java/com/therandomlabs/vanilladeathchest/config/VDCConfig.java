package com.therandomlabs.vanilladeathchest.config;

import com.therandomlabs.randomlib.config.Config;
import com.therandomlabs.randomlib.config.ConfigColor;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

@Config(VanillaDeathChest.MOD_ID)
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

		@Config.Previous("unlockerRegistryName")
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
		public static int unlockerMeta = OreDictionary.WILDCARD_VALUE;

		@Config.Property({
				"The message that is sent to the player when they fail to unlock a death chest.",
				"This string takes the required amount and display name of the item as arguments."
		})
		public static String unlockFailedMessage =
				"You need %s of the following item to retrieve your items: %s";

		@Config.Property(
				"Whether the unlock failed message should be a status message rather than a " +
						"chat message."
		)
		public static boolean unlockFailedStatusMessage = true;

		public static ResourceLocation defenseEntity;

		@SuppressWarnings("Duplicates")
		public static void onReload() {
			try {
				JsonToNBT.getTagFromJson(defenseEntityNBT);
			} catch(NBTException ex) {
				defenseEntityNBT = "{}";
			}

			final ResourceLocation[] entityNames =
					EntityList.getEntityNameList().toArray(new ResourceLocation[0]);
			final int index = ArrayUtils.indexOf(
					entityNames, new ResourceLocation(defenseEntityRegistryName)
			);

			if(index == ArrayUtils.INDEX_NOT_FOUND) {
				defenseEntity = null;
				defenseEntityRegistryName = "";
			} else {
				defenseEntity = entityNames[index];
				defenseEntityRegistryName = defenseEntity.toString();
			}
		}
	}

	public static final class Misc {
		@Config.Property({
				"Whether death chests should be dropped when broken.",
				"Enable this for infinite chests."
		})
		public static boolean dropDeathChests;

		@Config.Property("The default value of the spawnDeathChests gamerule.")
		public static boolean gameRuleDefaultValue = true;

		@Config.Property({
				"The name of the spawnDeathChests gamerule.",
				"Set this to an empty string to disable the gamerule."
		})
		public static String gameRuleName = "spawnDeathChests";

		@Config.RequiresWorldReload
		@Config.Property("Whether to enable the /vdcreload command.")
		public static boolean vdcreload = true;

		@Config.RequiresMCRestart
		@Config.Property("Whether to enable the /vdcreloadclient command.")
		public static boolean vdcreloadclient = true;
	}

	public static final class Protection {
		@Config.Property(
				"Whether players in creative mode should be able to bypass death chest " +
						"protection."
		)
		public static boolean bypassIfCreative = true;

		@Config.RangeInt(min = 0)
		@Config.Property("The required permission level to bypass death chest proteciton.")
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
				"The message sent to a player when they die and a death chest is placed.",
				"%d refers to the X, Y and Z coordinates.",
				"Set this to an empty string to disable the message."
		})
		public static String chatMessage = "Death chest spawned at [%s, %s, %s]";

		@Config.Property("The type of death chest that should be placed.")
		public static DeathChestPlacer.DeathChestType chestType =
				DeathChestPlacer.DeathChestType.SINGLE_OR_DOUBLE;

		@Config.Property(
				"Whether to force place a death chest at the location of a player's death if no " +
						"viable locations are found."
		)
		public static boolean forcePlaceIfLocationNotFound;

		@Config.RangeInt(min = 1)
		@Config.Property("The death chest location search radius.")
		public static int locationSearchRadius = 8;

		@Config.Property("Whether death chests can only spwan on solid blocks.")
		public static boolean mustBeOnSolidBlocks;

		@Config.Property("The color of the shulker box if chestType is set to SHULKER_BOX.")
		public static ConfigColor shulkerBoxColor = ConfigColor.WHITE;
	}

	@Config.Category("Options related to death chest defense.")
	public static final Defense defense = null;

	@Config.Category("Options that don't fit into any other categories.")
	public static final Misc misc = null;

	@Config.Category("Options related to death chest protection.")
	public static final Protection protection = null;

	@Config.Category("Options related to death chest spawning.")
	public static final Spawning spawning = null;

	static {
		ConfigColor.setTranslationKeyPrefix(
				"vanilladeathchest.config.spawning.chestType.randomShulkerBoxColor."
		);
	}
}
