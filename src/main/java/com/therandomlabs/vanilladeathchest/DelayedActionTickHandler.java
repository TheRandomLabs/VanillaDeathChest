package com.therandomlabs.vanilladeathchest;

import java.util.Map;
import java.util.Queue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = VanillaDeathChest.MODID)
public class DelayedActionTickHandler {
	private DelayedActionTickHandler() {}

	private static Map<Integer, Queue<Runnable>> callbacks = Maps.newHashMap();

	private static Queue<Runnable> getWorldQueue(int worldId) {
		synchronized(callbacks) {
			Queue<Runnable> result = callbacks.get(worldId);

			if(result == null) {
				result = Queues.newConcurrentLinkedQueue();
				callbacks.put(worldId, result);
			}

			return result;
		}
	}

	public static void addTickCallback(World world, Runnable callback) {
		int worldId = world.provider.getDimension();
		getWorldQueue(worldId).add(callback);
	}

	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if(event.side == Side.SERVER && event.phase == Phase.END) {
			final int worldId = event.world.provider.getDimension();
			final Queue<Runnable> callbacks = getWorldQueue(worldId);

			Runnable callback;
			while((callback = callbacks.poll()) != null) {
				callback.run();
			}
		}
	}
}
