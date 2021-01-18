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
