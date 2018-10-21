package com.therandomlabs.vanilladeathchest.config;

import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.item.EnumDyeColor;

public class VDCConfig {
	public static class Config {
		public @interface Comment {
			String value();
		}

		public @interface RangeInt {
			int min() default Integer.MIN_VALUE;

			int max() default Integer.MAX_VALUE;
		}

		public @interface RequiresMcRestart {}

		public @interface RequiresWorldRestart {}
	}

	public static class Misc {
		@Config.Comment("Whether death chests should be dropped when broken. " +
				"Enable for infinite chests.")
		public boolean dropDeathChests;

		@Config.Comment("The default value of the gamerule.")
		public boolean gameRuleDefaultValue = true;

		@Config.Comment("The name of the gamerule that specifies whether death chests should " +
				"spawn. Set this to an empty string to disable the gamerule.")
		public String gameRuleName = "spawnDeathChests";

		@Config.RequiresWorldRestart
		@Config.Comment("Whether to enable the /vdcreload command.")
		public boolean vdcreload = true;

		@Config.RequiresMcRestart
		@Config.Comment("Whether to enable the /vdcreloadclient command.")
		public boolean vdcreloadclient = true;
	}

	public static class Protection {
		@Config.Comment("Whether players in creative mode can bypass death chest protection.")
		public boolean bypassIfCreative = true;

		@Config.RangeInt(min = 0)
		@Config.Comment("The required permission level to bypass death chest proteciton.")
		public int bypassPermissionLevel = 4;

		@Config.Comment("Whether death chests should be protected. When this is enabled, " +
				"death chests can only be broken by their owners.")
		public boolean enabled = true;

		@Config.RangeInt(min = 0)
		@Config.Comment("The amount of time in ticks death chest protection should last. " +
				"The default is 5 in-game days. Set this to 0 to protect death chests " +
				"indefinitely.")
		public int period = 120000;
	}

	public static class Spawning {
		@Config.Comment("The message sent to a player when they die and a death chest is placed. " +
				"%d refers to the X, Y and Z coordinates. Set this to an empty string to disable " +
				"this message.")
		public String chatMessage = "Death chest spawned at [%d, %d, %d]";

		@Config.Comment("The type of death chest that should be placed.")
		public DeathChestPlacer.DeathChestType chestType =
				DeathChestPlacer.DeathChestType.SINGLE_OR_DOUBLE;

		@Config.Comment("The death chest location search radius.")
		public int locationSearchRadius = 8;

		@Config.Comment("The color of the shulker box if chestType is set to SHULKER_BOX.")
		public EnumDyeColor shulkerBoxColor = EnumDyeColor.WHITE;
	}

	@Config.Comment("Options that don't fit into any other categories.")
	public static Misc misc = new Misc();

	@Config.Comment("Options related to death chest protection.")
	public static Protection protection = new Protection();

	@Config.Comment("Options related to death chest spawning.")
	public static Spawning spawning = new Spawning();

	public static void reload() {

	}
}
