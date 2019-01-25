package com.therandomlabs.vanilladeathchest.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import com.google.common.collect.Queues;
import com.therandomlabs.vanilladeathchest.api.event.PlayerEvent;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class DeathChestPlaceHandler implements PlayerEvent.DropAllItems, Consumer<MinecraftServer> {
	private static final Map<DimensionType, Queue<DeathChestPlacer>> PLACERS = new HashMap<>();

	@Override
	public boolean onPlayerDropAllItems(ServerWorld world, PlayerEntity player,
			List<ItemEntity> drops) {
		if(drops.isEmpty()) {
			return true;
		}

		final GameRules gameRules = world.getGameRules();

		if(gameRules.getBoolean("keepInventory")) {
			return true;
		}

		if(!(VDCConfig.misc.gameRuleName.isEmpty() ||
				gameRules.getBoolean(VDCConfig.misc.gameRuleName))) {
			return true;
		}

		final Queue<DeathChestPlacer> placers = getPlacers(world);
		placers.add(new DeathChestPlacer(world, player, drops));

		return false;
	}

	@Override
	public void accept(MinecraftServer server) {
		for(World world : server.getWorlds()) {
			worldTick(world);
		}
	}

	public static Queue<DeathChestPlacer> getPlacers(World world) {
		synchronized(PLACERS) {
			return PLACERS.computeIfAbsent(
					world.dimension.getType(),
					key -> Queues.newConcurrentLinkedQueue()
			);
		}
	}

	private static void worldTick(World world) {
		final Queue<DeathChestPlacer> placers = getPlacers(world);
		final List<DeathChestPlacer> toReadd = new ArrayList<>();
		DeathChestPlacer placer;

		while((placer = placers.poll()) != null) {
			if(!placer.run()) {
				toReadd.add(placer);
			}
		}

		placers.addAll(toReadd);
	}
}
