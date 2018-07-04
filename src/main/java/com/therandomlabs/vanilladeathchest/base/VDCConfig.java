package com.therandomlabs.vanilladeathchest.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MODID)
@Config(modid = VanillaDeathChest.MODID, name = VanillaDeathChest.MODID, category = "")
public class VDCConfig {
	public static class General {
		@Config.LangKey("vanilladeathchest.config.general.chatMessage")
		@Config.Comment("The message that should be sent to a player when they die and a " +
				"death chest is placed. %d refers to the X, Y and Z coordinates. Set this to an " +
				"empty string to disable this message.")
		public String chatMessage = "Death chest spawned at [%d, %d, %d]";

		@Config.LangKey("vanilladeathchest.config.general.dropDeathChests")
		@Config.Comment("Whether death chests should be dropped when broken. " +
				"Enable for infinite chests.")
		public boolean dropDeathChests;

		@Config.LangKey("vanilladeathchest.config.general.locationSearchRadius")
		@Config.Comment("The death chest location search radius.")
		public int locationSearchRadius = 8;

		@Config.LangKey("vanilladeathchest.config.general.spawnDeathChestsGamerule")
		@Config.Comment("Whether to enable the spawnDeathChests gamerule.")
		public boolean spawnDeathChestsGamerule = true;

		@Config.LangKey("vanilladeathchest.config.general.useDoubleChests")
		@Config.Comment("Whether to use double chests.")
		public boolean useDoubleChests = true;
	}

	@Config.LangKey("vanilladeathchest.config.general")
	@Config.Comment("General options.")
	public static General general = new General();

	private static final Method GET_CONFIGURATION = ReflectionHelper.findMethod(ConfigManager.class,
			"getConfiguration", "getConfiguration", String.class, String.class);

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

	private static void modifyConfig() throws Exception {
		final Configuration config = (Configuration) GET_CONFIGURATION.invoke(null,
				VanillaDeathChest.MODID, VanillaDeathChest.MODID);

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
			config.getCategory(name).getValues().forEach((key, property) ->
					property.setComment(comments.get(property)));
		}
	}
}
