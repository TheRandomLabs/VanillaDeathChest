package com.therandomlabs.vanilladeathchest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = VanillaDeathChest.MODID)
public class MiscEventHandler {
	private static final Map<Integer, Queue<Runnable>> callbacks = Maps.newHashMap();
	private static final List<BlockPos> chests = new ArrayList<>();

	private static Queue<Runnable> getWorldQueue(World world) {
		return getWorldQueue(world.provider.getDimension());
	}

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
		getWorldQueue(world).add(callback);
	}

	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if(event.side == Side.SERVER && event.phase == Phase.END) {
			final Queue<Runnable> callbacks = getWorldQueue(event.world);

			Runnable callback;
			while((callback = callbacks.poll()) != null) {
				callback.run();
			}
		}
	}

	@SubscribeEvent
	public static void onCreateSpawn(WorldEvent.CreateSpawnPosition event) {
		final World world = event.getWorld();
		if(!world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD) {
			world.getGameRules().setOrCreateGameRule("dontSpawnDeathChests", "false");
		}
	}

	@SubscribeEvent
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		if(chests.contains(event.getPos())) {
			for(ItemStack stack : event.getDrops()) {
				if(stack.getItem() == Item.getItemFromBlock(Blocks.CHEST) &&
						stack.getCount() == 1 && stack.getMetadata() == 0) {
					event.getDrops().remove(stack);
					break;
				}
			}

			chests.remove(event.getPos());
		}
	}

	static void addChest(BlockPos pos) {
		if(!VanillaDeathChest.dropDeathChest) {
			chests.add(pos);
		}
	}
}
