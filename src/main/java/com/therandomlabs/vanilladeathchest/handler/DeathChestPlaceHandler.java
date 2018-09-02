package com.therandomlabs.vanilladeathchest.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.google.common.collect.Queues;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MODID)
public final class DeathChestPlaceHandler {
	private static final Map<DimensionType, Queue<DeathChestPlacer>> PLACERS = new HashMap<>();

	@SubscribeEvent
	public static void onPlayerDrops(PlayerDropsEvent event) {
		final List<EntityItem> drops = event.getDrops();

		if(drops.isEmpty()) {
			return;
		}

		final EntityPlayer player = event.getEntityPlayer();

		if(player instanceof FakePlayer) {
			return;
		}

		final World world = player.getEntityWorld();
		final GameRules gameRules = world.getGameRules();

		if(gameRules.getBoolean("keepInventory")) {
			return;
		}

		if(!(VDCConfig.misc.gameruleName.isEmpty() ||
				gameRules.getBoolean(VDCConfig.misc.gameruleName))) {
			return;
		}

		final Queue<DeathChestPlacer> placers = getPlacers(world);
		placers.add(new DeathChestPlacer(world, player, drops));

		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void worldTick(TickEvent.WorldTickEvent event) {
		final Queue<DeathChestPlacer> placers = getPlacers(event.world);
		final List<DeathChestPlacer> toReadd = new ArrayList<>();
		DeathChestPlacer placer;

		while((placer = placers.poll()) != null) {
			if(!placer.run()) {
				toReadd.add(placer);
			}
		}

		placers.addAll(toReadd);
	}

	public static Queue<DeathChestPlacer> getPlacers(World world) {
		synchronized(PLACERS) {
			final DimensionType id = world.provider.getDimensionType();
			Queue<DeathChestPlacer> placers = PLACERS.get(id);

			if(placers == null) {
				placers = Queues.newConcurrentLinkedQueue();
				PLACERS.put(id, placers);
			}

			return placers;
		}
	}
}
