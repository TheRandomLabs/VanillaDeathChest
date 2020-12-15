/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.vanilladeathchest.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.Queues;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class DeathChestPlaceHandler
		implements PlayerDropAllItemsCallback, ServerTickEvents.EndWorldTick {
	private static final Map<DimensionType, Queue<DeathChestPlacer>> PLACERS = new HashMap<>();

	@Override
	public boolean onPlayerDropAllItems(
			ServerWorld world, PlayerEntity player, List<ItemEntity> drops
	) {
		if (drops.isEmpty()) {
			return true;
		}

		final GameRules gameRules = world.getGameRules();

		if (gameRules.getBoolean(GameRules.KEEP_INVENTORY)) {
			return true;
		}

		final GameRules.Key<GameRules.BooleanRule> key =
				VanillaDeathChest.getDisableDeathChestsKey();

		if (key != null && gameRules.getBoolean(key)) {
			return true;
		}

		final Queue<DeathChestPlacer> placers = getPlacers(world);
		placers.add(new DeathChestPlacer(world, player, drops));
		return false;
	}

	@Override
	public void onEndTick(ServerWorld world) {
		final Queue<DeathChestPlacer> placers = getPlacers(world);
		final List<DeathChestPlacer> toReadd = new ArrayList<>();
		DeathChestPlacer placer;

		while ((placer = placers.poll()) != null) {
			if (!placer.run()) {
				toReadd.add(placer);
			}
		}

		placers.addAll(toReadd);
	}

	public static Queue<DeathChestPlacer> getPlacers(World world) {
		synchronized (PLACERS) {
			return PLACERS.computeIfAbsent(
					world.getDimension(),
					key -> Queues.newConcurrentLinkedQueue()
			);
		}
	}
}
