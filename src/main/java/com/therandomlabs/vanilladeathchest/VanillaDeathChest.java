package com.therandomlabs.vanilladeathchest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.therandomlabs.vanilladeathchest.common.CommandVDCReload;
import com.therandomlabs.vanilladeathchest.common.DeathChestHandler;
import com.therandomlabs.vanilladeathchest.util.Callback;
import com.therandomlabs.vanilladeathchest.util.DeathChest;
import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.rift.listener.CommandAdder;
import org.dimdev.rift.listener.ServerTickable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class VanillaDeathChest implements CommandAdder, ServerTickable {
	public static final String MODID = "vanilladeathchest";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	private static final Map<DimensionType, Queue<Callback>> CALLBACKS = new HashMap<>();

	static {
		VDCConfig.reload();

		try {
			final Field definitionsField = findField(GameRules.class, "field_196232_a", "a");

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

	@Override
	public void serverTick(MinecraftServer server) {
		for(World world : server.worlds) {
			final Queue<Callback> callbacks = getCallbacks(world);
			final List<Callback> toReadd = new ArrayList<>();
			Callback callback;

			while((callback = callbacks.poll()) != null) {
				if(!callback.run()) {
					toReadd.add(callback);
				}
			}

			callbacks.addAll(toReadd);
		}
	}

	private static Queue<Callback> getCallbacks(World world) {
		synchronized(CALLBACKS) {
			final DimensionType id = world.provider.getDimensionType();
			Queue<Callback> callbacks = CALLBACKS.get(id);

			if(callbacks == null) {
				callbacks = Queues.newConcurrentLinkedQueue();
				CALLBACKS.put(id, callbacks);
			}

			return callbacks;
		}
	}

	public static boolean onBlockInteract(CallbackInfo info, EntityPlayer player, World world,
			BlockPos pos) {
		if(world.isRemote) {
			return false;
		}

		final DeathChest deathChest = DeathChestHandler.getDeathChest(world, pos);

		if(deathChest == null) {
			return false;
		}

		if(deathChest.canInteract(player)) {
			return true;
		}

		info.cancel();
		return false;
	}

	public static void onDeath(EntityPlayer player, List<EntityItem> drops) {
		final World world = player.getEntityWorld();

		if(world.isRemote) {
			return;
		}

		final GameRules gameRules = world.getGameRules();

		if(gameRules.getBoolean("keepInventory")) {
			return;
		}

		if(!(VDCConfig.misc.gameruleName.isEmpty() ||
				gameRules.getBoolean(VDCConfig.misc.gameruleName))) {
			return;
		}

		getCallbacks(world).add(new DeathChestHandler.DCCallback(world, player, drops));
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
