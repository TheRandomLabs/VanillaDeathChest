package com.therandomlabs.vanilladeathchest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.google.common.collect.Queues;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.dimdev.rift.listener.ServerTickable;

public final class CallbackHandler implements ServerTickable {
	private static final Map<DimensionType, Queue<Callback>> CALLBACKS = new HashMap<>();

	@Override
	public void serverTick(MinecraftServer server) {
		for(World world : server.worlds) {
			CallbackHandler.worldTick(world);
		}

		DeathChestHandler.JUST_REMOVED.clear();
	}

	public static void worldTick(World world) {
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

	public static Queue<Callback> getCallbacks(World world) {
		synchronized(CALLBACKS) {
			final DimensionType id = world.dimension.getType();
			Queue<Callback> callbacks = CALLBACKS.get(id);

			if(callbacks == null) {
				callbacks = Queues.newConcurrentLinkedQueue();
				CALLBACKS.put(id, callbacks);
			}

			return callbacks;
		}
	}
}
