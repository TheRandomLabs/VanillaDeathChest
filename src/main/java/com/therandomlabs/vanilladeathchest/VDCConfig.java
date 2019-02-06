package com.therandomlabs.vanilladeathchest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.vanilladeathchest.util.ColorConfig;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import com.therandomlabs.vanilladeathchest.util.VDCUtils;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
@Config(modid = VanillaDeathChest.MOD_ID, name = VanillaDeathChest.MOD_ID, category = "")
public final class VDCConfig {
	public static final class Defense {
		@Config.LangKey("vanilladeathchest.config.defense.damageUnlockerInsteadOfConsume")
		@Config.Comment("Whether the unlocker should be damaged rather than consumed.")
		public boolean damageUnlockerInsteadOfConsume;

		@Config.LangKey("vanilladeathchest.config.defense.defenseEntityDropsExperience")
		@Config.Comment("Whether defense entities drop experience.")
		public boolean defenseEntityDropsExperience;

		@Config.LangKey("vanilladeathchest.config.defense.defenseEntityDropsItems")
		@Config.Comment("Whether defense entities drop items.")
		public boolean defenseEntityDropsItems;

		@Config.RangeDouble(min = 0.0)
		@Config.LangKey("vanilladeathchest.config.defense.defenseEntityMaxDistanceSquared")
		@Config.Comment(
				"The maximum distance squared that a defense entity can be from its chest."
		)
		public double defenseEntityMaxDistanceSquared = 64.0;

		@Config.RangeDouble(min = 0.0)
		@Config.LangKey(
				"vanilladeathchest.config.defense.defenseEntityMaxDistanceSquaredFromPlayer"
		)
		@Config.Comment(
				"The maximum distance squared that a defense entity can be from its player if it " +
						"is too far away from its chest."
		)
		public double defenseEntityMaxDistanceSquaredFromPlayer = 64.0;

		@Config.LangKey("vanilladeathchest.config.defense.defenseEntityNBT")
		@Config.Comment("The defense entity NBT data.")
		public String defenseEntityNBT = "{}";

		@Config.LangKey("vanilladeathchest.config.defense.defenseEntityRegistryName")
		@Config.Comment({
				"The registry name of the defense entity.",
				"If the defense entity is a living entity, it will not automatically despawn.",
				"If the defense entity can have a revenge target, then the revenge target will " +
						"be set to the player that died."
		})
		public String defenseEntityRegistryName = "";

		@Config.RangeInt(min = 1)
		@Config.LangKey("vanilladeathchest.config.defense.defenseEntitySpawnCount")
		@Config.Comment("The number of defense entities to spawn.")
		public int defenseEntitySpawnCount = 3;

		@Config.RangeInt(min = -126, max = 127)
		@Config.LangKey("vanilladeathchest.config.defense.unlockerConsumeAmount")
		@Config.Comment({
				"How many times the unlocker should be consumed or damaged.",
				"If the unlocker cannot be consumed or damage this many times, the death chest " +
						"will not be unlocked.",
				"Note that only the stack that the player is holding will be consumed, and that " +
						"players in creative mode will not have their unlocker item consumed."
		})
		public int unlockerConsumeAmount = 1;

		@Config.RangeInt(min = 0, max = Short.MAX_VALUE)
		@Config.LangKey("vanilladeathchest.config.defense.unlockerMeta")
		@Config.Comment("The meta value of the unlocker.")
		public int unlockerMeta = OreDictionary.WILDCARD_VALUE;

		@Config.LangKey("vanilladeathchest.config.defense.unlockerRegistryName")
		@Config.Comment("The registry name of the unlocker.")
		public String unlockerRegistryName = "";

		@Config.LangKey("vanilladeathchest.config.defense.unlockFailedMessage")
		@Config.Comment({
				"The message that is sent to the player when they fail to unlock a death chest.",
				"This string takes the required amount and display name of the item as arguments."
		})
		public String unlockFailedMessage =
				"You need %s of the following item to retrieve your items: %s";

		@Config.LangKey("vanilladeathchest.config.defense.unlockFailedStatusMessage")
		@Config.Comment(
				"Whether the unlock failed message should be a status message rather than a " +
						"chat message."
		)
		public boolean unlockFailedStatusMessage = true;

		@Config.Ignore
		public ResourceLocation defenseEntity;

		@Config.Ignore
		public Item unlocker;

		@SuppressWarnings("Duplicates")
		private void reload() {
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

			unlocker = VanillaDeathChest.ITEM_REGISTRY.getValue(
					new ResourceLocation(unlockerRegistryName)
			);

			if(unlocker != null) {
				if(unlocker == Items.AIR) {
					unlocker = null;
					unlockerRegistryName = "";
				} else {
					unlockerRegistryName = unlocker.getRegistryName().toString();
				}
			}
		}
	}

	public static final class Misc {
		@Config.LangKey("vanilladeathchest.config.misc.dropDeathChests")
		@Config.Comment({
				"Whether death chests should be dropped when broken.",
				"Enable this for infinite chests."
		})
		public boolean dropDeathChests;

		@Config.LangKey("vanilladeathchest.config.misc.gameRuleDefaultValue")
		@Config.Comment("The default value of the spawnDeathChests gamerule.")
		public boolean gameRuleDefaultValue = true;

		@Config.LangKey("vanilladeathchest.config.misc.gameRuleName")
		@Config.Comment({
				"The name of the spawnDeathChests gamerule.",
				"Set this to an empty string to disable the gamerule."
		})
		public String gameRuleName = "spawnDeathChests";

		@Config.RequiresWorldRestart
		@Config.LangKey("vanilladeathchest.config.misc.vdcreload")
		@Config.Comment("Whether to enable the /vdcreload command.")
		public boolean vdcreload = true;

		@Config.RequiresMcRestart
		@Config.LangKey("vanilladeathchest.config.misc.vdcreloadclient")
		@Config.Comment("Whether to enable the /vdcreloadclient command.")
		public boolean vdcreloadclient = true;
	}

	public static final class Protection {
		@Config.LangKey("vanilladeathchest.config.protection.bypassIfCreative")
		@Config.Comment(
				"Whether players in creative mode should be able to bypass death chest " +
						"protection."
		)
		public boolean bypassIfCreative = true;

		@Config.RangeInt(min = 0)
		@Config.LangKey("vanilladeathchest.config.protection.bypassPermissionLevel")
		@Config.Comment("The required permission level to bypass death chest proteciton.")
		public int bypassPermissionLevel = 3;

		@Config.LangKey("vanilladeathchest.config.protection.enabled")
		@Config.Comment({
				"Whether death chests should be protected.",
				"When this is enabled, death chests can only be broken by their owners."
		})
		public boolean enabled = true;

		@Config.RangeInt(min = 0)
		@Config.LangKey("vanilladeathchest.config.protection.period")
		@Config.Comment({
				"The amount of time in ticks death chest protection should last.",
				"120000 ticks is 5 in-game days.",
				"Set this to 0 to protect death chests indefinitely."
		})
		public int period = 120000;
	}

	public static final class Spawning {
		@Config.LangKey("vanilladeathchest.config.spawning.chatMessage")
		@Config.Comment({
				"The message sent to a player when they die and a death chest is placed.",
				"%d refers to the X, Y and Z coordinates.",
				"Set this to an empty string to disable the message."
		})
		public String chatMessage = "Death chest spawned at [%s, %s, %s]";

		@Config.LangKey("vanilladeathchest.config.spawning.chestType")
		@Config.Comment("The type of death chest that should be placed.")
		public DeathChestPlacer.DeathChestType chestType =
				DeathChestPlacer.DeathChestType.SINGLE_OR_DOUBLE;

		@Config.LangKey("vanilladeathchest.config.spawning.forcePlaceIfLocationNotFound")
		@Config.Comment(
				"Whether to force place a death chest at the location of a player's death if no " +
						"viable locations are found."
		)
		public boolean forcePlaceIfLocationNotFound;

		@Config.RangeInt(min = 1)
		@Config.LangKey("vanilladeathchest.config.spawning.locationSearchRadius")
		@Config.Comment("The death chest location search radius.")
		public int locationSearchRadius = 8;

		@Config.LangKey("vanilladeathchest.config.spawning.mustBeOnSolidBlocks")
		@Config.Comment("Whether death chests can only spwan on solid blocks.")
		public boolean mustBeOnSolidBlocks;

		@Config.LangKey("vanilladeathchest.config.spawning.shulkerBoxColor")
		@Config.Comment("The color of the shulker box if chestType is set to SHULKER_BOX.")
		public ColorConfig shulkerBoxColor = ColorConfig.WHITE;
	}

	@Config.LangKey("vanilladeathchest.config.defense")
	@Config.Comment("Options related to death chest defense.")
	public static final Defense defense = new Defense();

	@Config.LangKey("vanilladeathchest.config.misc")
	@Config.Comment("Options that don't fit into any other categories.")
	public static final Misc misc = new Misc();

	@Config.LangKey("vanilladeathchest.config.protection")
	@Config.Comment("Options related to death chest protection.")
	public static final Protection protection = new Protection();

	@Config.LangKey("vanilladeathchest.config.spawning")
	@Config.Comment("Options related to death chest spawning.")
	public static final Spawning spawning = new Spawning();

	private static final Method GET_CONFIGURATION = VDCUtils.findMethod(
			ConfigManager.class, "getConfiguration", "getConfiguration", String.class, String.class
	);

	private static final Method SYNC = VDCUtils.findMethod(
			ConfigManager.class, "sync", "sync", Configuration.class, Class.class, String.class,
			String.class, boolean.class, Object.class
	);

	private static final Map<Property, String> comments = new HashMap<>();

	private static boolean firstReload = true;

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(VanillaDeathChest.MOD_ID)) {
			reload();
		}
	}

	public static void reload() {
		try {
			final Configuration config = (Configuration) GET_CONFIGURATION.invoke(
					null, VanillaDeathChest.MOD_ID, VanillaDeathChest.MOD_ID
			);

			//reload() is only called by CommonProxy and VDCConfig
			//Forge syncs the config during mod construction, so this first sync is not necessary
			if(!firstReload) {
				SYNC.invoke(
						null, config, VDCConfig.class, VanillaDeathChest.MOD_ID, "", false, null
				);
			}

			firstReload = false;

			onReload();

			//Remove old elements
			for(String name : config.getCategoryNames()) {
				final ConfigCategory category = config.getCategory(name);

				category.getValues().forEach((key, property) -> {
					final String comment = property.getComment();

					if(comment == null || comment.isEmpty()) {
						category.remove(key);
						return;
					}

					String newComment = comments.get(property);

					if(newComment == null) {
						newComment = comment + "\nDefault: " + property.getDefault();
						comments.put(property, newComment);
					}

					property.setComment(newComment);
				});

				if(category.getValues().isEmpty() || category.getComment() == null) {
					config.removeCategory(category);
				}
			}

			config.save();

			SYNC.invoke(null, config, VDCConfig.class, VanillaDeathChest.MOD_ID, "", false, null);

			//Remove default values, min/max values and valid values from the comments so
			//they don't show up twice in the configuration GUI
			for(String name : config.getCategoryNames()) {
				final ConfigCategory category = config.getCategory(name);

				category.getValues().forEach((key, property) -> {
					final String[] comment = property.getComment().split("\n");
					final StringBuilder prunedComment = new StringBuilder();

					for(String line : comment) {
						if(line.startsWith("Default:") || line.startsWith("Min:")) {
							break;
						}

						prunedComment.append(line).append("\n");
					}

					final String commentString = prunedComment.toString();
					property.setComment(commentString.substring(0, commentString.length() - 1));
				});
			}
		} catch(Exception ex) {
			VanillaDeathChest.LOGGER.error("Error while modifying config", ex);
		}
	}

	public static void reloadFromDisk() {
		try {
			final Configuration config = (Configuration) GET_CONFIGURATION.invoke(
					null, VanillaDeathChest.MOD_ID, VanillaDeathChest.MOD_ID
			);
			final Configuration tempConfig = new Configuration(config.getConfigFile());

			tempConfig.load();

			for(String name : tempConfig.getCategoryNames()) {
				final Map<String, Property> properties = tempConfig.getCategory(name).getValues();

				for(Map.Entry<String, Property> entry : properties.entrySet()) {
					config.getCategory(name).get(entry.getKey()).set(entry.getValue().getString());
				}
			}

			reload();

			MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(
					VanillaDeathChest.MOD_ID, null, true, false
			));
		} catch(Exception ex) {
			VanillaDeathChest.LOGGER.error("Error while modifying config", ex);
		}
	}

	private static void onReload() {
		defense.reload();
	}
}
