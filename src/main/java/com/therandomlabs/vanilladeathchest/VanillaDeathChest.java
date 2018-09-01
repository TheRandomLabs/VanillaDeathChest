package com.therandomlabs.vanilladeathchest;

import java.lang.reflect.Field;
import java.util.Map;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.rift.listener.CommandAdder;
import org.dimdev.rift.listener.MinecraftStartListener;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public final class VanillaDeathChest implements
		InitializationListener, MinecraftStartListener, CommandAdder {
	public static final String MODID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@Override
	public void onInitialization() {
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins." + MODID + ".json");

		VDCConfig.reload();
	}

	@Override
	public void onMinecraftStart() {
		try {
			final Field definitionsField = findField(GameRules.class, "DEFINITIONS", "a");

			@SuppressWarnings("unchecked")
			final Map<String, GameRules.ValueDefinition> definitions =
					(Map<String, GameRules.ValueDefinition>) definitionsField.get(null);

			definitions.put(VDCConfig.misc.gameruleName, new GameRules.ValueDefinition(
					Boolean.toString(VDCConfig.misc.gameruleDefaultValue),
					GameRules.ValueType.BOOLEAN_VALUE
			));
		} catch(Exception ex) {
			throw new ReportedException(new CrashReport("Failed to register gamerule", ex));
		}
	}

	@Override
	public void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
		if(VDCConfig.misc.vdcreload) {
			CommandVDCReload.register(dispatcher);
		}
	}

	public static Field findField(Class<?> clazz, String name, String obfName) {
		for(Field field : clazz.getDeclaredFields()) {
			if(name.equals(field.getName()) || obfName.equals(field.getName())) {
				field.setAccessible(true);
				return field;
			}
		}

		return null;
	}
}
