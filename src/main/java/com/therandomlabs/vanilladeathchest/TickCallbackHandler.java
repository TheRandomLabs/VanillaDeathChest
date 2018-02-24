package com.therandomlabs.vanilladeathchest;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@EventBusSubscriber(modid = VanillaDeathChest.MODID)
public class TickCallbackHandler {
	private static final Map<Integer, Queue<Callable>> callbacks = Maps.newHashMap();

	public static abstract class Callable implements Runnable {
		final WeakReference<World> world;
		boolean ready;

		protected Callable(World world) {
			this.world = new WeakReference<>(world);
		}
	}

	private static Queue<Callable> getWorldQueue(World world) {
		return getWorldQueue(world.provider.getDimension());
	}

	private static Queue<Callable> getWorldQueue(int worldId) {
		synchronized(callbacks) {
			Queue<Callable> result = callbacks.get(worldId);

			if(result == null) {
				result = Queues.newConcurrentLinkedQueue();
				callbacks.put(worldId, result);
			}

			return result;
		}
	}

	public static void addCallback(Callable callback) {
		getWorldQueue(callback.world.get()).add(callback);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldTick(WorldTickEvent event) {
		if(!event.world.isRemote && event.phase == Phase.END) {
			final Queue<Callable> callbacks = getWorldQueue(event.world);
			final Iterator<Callable> it = callbacks.iterator();
			final Set<Callable> toRemove = new HashSet<>();

			Callable callback;
			while(it.hasNext()) {
				callback = it.next();
				if(!callback.ready) {
					callback.ready = true;
				} else {
					callback.run();
					toRemove.add(callback);
				}
			}

			callbacks.removeAll(toRemove);
		}
	}
}
