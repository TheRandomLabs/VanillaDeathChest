package com.therandomlabs.vanilladeathchest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.google.common.collect.Queues;
import com.therandomlabs.vanilladeathchest.base.VDCConfig;
import com.therandomlabs.vanilladeathchest.base.VanillaDeathChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber(modid = VanillaDeathChest.MODID)
public class VDCEventHandler {
	@GameRegistry.ObjectHolder("minecraft:chest")
	public static final Item CHEST = null;

	private static final Map<Integer, Queue<Callback>> CALLBACKS = new HashMap<>();

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if(!VDCConfig.general.spawnDeathChestsGamerule) {
			return;
		}

		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final GameRules gamerules = world.getGameRules();

		if(!gamerules.hasRule(VanillaDeathChest.GAMERULE_NAME)) {
			gamerules.setOrCreateGameRule(VanillaDeathChest.GAMERULE_NAME, "true");
		}
	}

	@SubscribeEvent
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		if(VDCConfig.general.dropDeathChests) {
			return;
		}

		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		if(!DeathChestHandler.isDeathChest(world, event.getPos())) {
			return;
		}

		final List<ItemStack> drops = event.getDrops();

		for(ItemStack stack : drops) {
			if(stack.getItem() == CHEST && stack.getCount() == 1 && stack.getMetadata() == 0) {
				drops.remove(stack);
				break;
			}
		}
	}

	private static Queue<Callback> getCallbacks(World world) {
		synchronized(CALLBACKS) {
			final int id = world.provider.getDimension();
			Queue<Callback> callbacks = CALLBACKS.get(id);

			if(callbacks == null) {
				callbacks = Queues.newConcurrentLinkedQueue();
				CALLBACKS.put(id, callbacks);
			}

			return callbacks;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.world.isRemote || event.phase != TickEvent.Phase.END) {
			return;
		}

		final Queue<Callback> callbacks = getCallbacks(event.world);
		final List<Callback> toReadd = new ArrayList<>();
		Callback callback;

		while((callback = callbacks.poll()) != null) {
			if(!callback.run()) {
				toReadd.add(callback);
			}
		}

		callbacks.addAll(toReadd);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerDrops(PlayerDropsEvent event) {
		final EntityPlayer player = event.getEntityPlayer();
		final World world = player.getEntityWorld();

		if(world.isRemote) {
			return;
		}

		final List<EntityItem> drops = event.getDrops();

		if(player instanceof FakePlayer || drops.isEmpty()) {
			return;
		}

		final GameRules gameRules = world.getGameRules();

		if(gameRules.getBoolean("keepInventory") || !gameRules.getBoolean("spawnDeathChests")) {
			return;
		}

		getCallbacks(world).add(new DeathChestHandler.DCCallback(world, player, drops));

		event.setCanceled(true);
	}
}
