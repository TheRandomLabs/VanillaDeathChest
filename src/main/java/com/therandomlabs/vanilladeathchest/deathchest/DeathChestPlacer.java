/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TheRandomLabs
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

package com.therandomlabs.vanilladeathchest.deathchest;

import java.util.Queue;

import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.server.world.ServerWorld;

/**
 * Handles death chest placement.
 */
public final class DeathChestPlacer {
	/**
	 * Places all queued death chests that are ready to be placed in the specified world.
	 * This is called at the end of every world tick.
	 *
	 * @param world a {@link ServerWorld}.
	 */
	public static void placeQueued(ServerWorld world) {
		final DeathChestsState state = DeathChestsState.get(world);
		final Queue<DeathChest> queue = state.getQueuedDeathChests();

		//We wait two ticks to prevent conflicts with other mods that place things after death.
		if (queue.isEmpty() || queue.peek().getCreationTime() - world.getTime() < 2L) {
			return;
		}

		while (!queue.isEmpty() && queue.peek().getCreationTime() - world.getTime() >= 2L) {
			place(queue.poll());
		}

		state.markDirty();
	}

	/**
	 * Places a queued death chest.
	 *
	 * @param deathChest a queued {@link DeathChest}.
	 */
	public static void place(DeathChest deathChest) {
		//TODO
	}
}
