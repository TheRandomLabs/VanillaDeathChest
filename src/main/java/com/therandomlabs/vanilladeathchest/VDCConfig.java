package com.therandomlabs.vanilladeathchest;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MODID)
@Config(modid = VanillaDeathChest.MODID, name = VanillaDeathChest.MODID, category = "")
public class VDCConfig {
	public static class Misc {
		@Config.LangKey("vanilladeathchest.config.misc.dropDeathChests")
		@Config.Comment("Whether death chests should be dropped when broken. " +
				"Enable for infinite chests.")
		public boolean dropDeathChests;

		@Config.LangKey("vanilladeathchest.config.misc.gameRuleDefaultValue")
		@Config.Comment("The default value of the gamerule.")
		public boolean gameRuleDefaultValue = true;

		@Config.LangKey("vanilladeathchest.config.misc.gameruleName")
		@Config.Comment("The name of the gamerule that specifies whether death chests should " +
				"spawn. Set this to an empty string to disable the gamerule.")
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

	public static class Protection {
		@Config.LangKey("vanilladeathchest.config.protection.bypassIfCreative")
		@Config.Comment("Whether players in creative mode can bypass death chest protection.")
		public boolean bypassIfCreative = true;

		@Config.RangeInt(min = 0)
		@Config.LangKey("vanilladeathchest.config.protection.bypassPermissionLevel")
		@Config.Comment("The required permission level to bypass death chest proteciton.")
		public int bypassPermissionLevel = 4;

		@Config.LangKey("vanilladeathchest.config.protection.enabled")
		@Config.Comment("Whether death chests should be protected. When this is enabled, " +
				"death chests can only be broken by their owners.")
		public boolean enabled = true;

		@Config.RangeInt(min = 0)
		@Config.LangKey("vanilladeathchest.config.protection.period")
		@Config.Comment("The amount of time in ticks death chest protection should last. " +
				"The default is 5 in-game days. Set this to 0 to protect death chests " +
				"indefinitely.")
		public int period = 120000;
	}

	public static class Spawning {
		@Config.LangKey("vanilladeathchest.config.spawning.chatMessage")
		@Config.Comment("The message sent to a player when they die and a death chest is placed. " +
				"%d refers to the X, Y and Z coordinates. Set this to an empty string to disable " +
				"this message.")
		public String chatMessage = "Death chest spawned at [%d, %d, %d]";

		@Config.LangKey("vanilladeathchest.config.spawning.chestType")
		@Config.Comment("The type of death chest that should be placed.")
		public DeathChestPlacer.DeathChestType chestType =
				DeathChestPlacer.DeathChestType.SINGLE_OR_DOUBLE;

		@Config.LangKey("vanilladeathchest.config.spawning.locationSearchRadius")
		@Config.Comment("The death chest location search radius.")
		public int locationSearchRadius = 8;

		@Config.LangKey("vanilladeathchest.config.spawning.shulkerBoxColor")
		@Config.Comment("The color of the shulker box if chestType is set to SHULKER_BOX.")
		public EnumDyeColor shulkerBoxColor = EnumDyeColor.WHITE;
	}

	@Config.LangKey("vanilladeathchest.config.misc")
	@Config.Comment("Options that don't fit into any other categories.")
	public static Misc misc = new Misc();

	@Config.LangKey("vanilladeathchest.config.protection")
	@Config.Comment("Options related to death chest protection.")
	public static Protection protection = new Protection();

	@Config.LangKey("vanilladeathchest.config.spawning")
	@Config.Comment("Options related to death chest spawning.")
	public static Spawning spawning = new Spawning();

	private static final Method GET_CONFIGURATION = ReflectionHelper.findMethod(
			ConfigManager.class, "getConfiguration", "getConfiguration", String.class, String.class
	);

	private static final Field CONFIGS = ReflectionHelper.findField(ConfigManager.class, "CONFIGS");

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(VanillaDeathChest.MODID)) {
			reload();
		}
	}

	public static void reload() {
		ConfigManager.sync(VanillaDeathChest.MODID, Config.Type.INSTANCE);

		try {
			modifyConfig();
		} catch(Exception ex) {
			throw new ReportedException(new CrashReport("Error while modifying config", ex));
		}
	}

	public static void reloadFromDisk() {
		try {
			final File file =
					new File(Loader.instance().getConfigDir(), VanillaDeathChest.MODID + ".cfg");
			((Map) CONFIGS.get(null)).remove(file.getAbsolutePath());
			reload();
		} catch(Exception ex) {
			throw new ReportedException(new CrashReport("Error while modifying config", ex));
		}
	}

	private static void modifyConfig() throws IllegalAccessException, InvocationTargetException {
		final Configuration config = (Configuration) GET_CONFIGURATION.invoke(
				null, VanillaDeathChest.MODID, VanillaDeathChest.MODID
		);

		final Map<Property, String> comments = new HashMap<>();

		//Remove old elements
		for(String name : config.getCategoryNames()) {
			final ConfigCategory category = config.getCategory(name);

			category.getValues().forEach((key, property) -> {
				final String comment = property.getComment();

				if(comment == null || comment.isEmpty()) {
					category.remove(key);
					return;
				}

				//Add default value to comment
				comments.put(property, comment);
				property.setComment(comment + "\n" + "Default: " + property.getDefault());
			});

			if(category.getValues().isEmpty() || category.getComment() == null) {
				config.removeCategory(category);
			}
		}

		config.save();

		//Remove default values from comments so they don't show up in the configuration GUI
		for(String name : config.getCategoryNames()) {
			config.getCategory(name).getValues().forEach(
					(key, property) -> property.setComment(comments.get(property))
			);
		}
	}
}
