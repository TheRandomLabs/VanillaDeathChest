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

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.ViewerCount;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles the automatic removal of empty death chests.
 */
public final class DeathChestAutoRemover {
	private DeathChestAutoRemover() {}

	/**
	 * Removes all empty death chests in loaded chunks.
	 * This is called at the end of every world tick.
	 *
	 * @param world a {@link ServerWorld}.
	 */
	public static void removeEmpty(ServerWorld world) {
		if (!VanillaDeathChest.config().misc.removeEmptyDeathChests) {
			return;
		}

		for (DeathChest deathChest : DeathChestsState.get(world).getDeathChests()) {
			removeIfEmpty(world, deathChest.getPos());

			if (deathChest.isDoubleChest()) {
				removeIfEmpty(world, deathChest.getPos().east());
			}
		}
	}

	private static void removeIfEmpty(World world, BlockPos pos) {
		//Don't unnecessarily load any chunks.
		if (!world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
			return;
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);

		if (!(blockEntity instanceof LockableContainerBlockEntity) ||
				!((LockableContainerBlockEntity) blockEntity).isEmpty()) {
			return;
		}

		if (!VanillaDeathChest.config().misc.onlyRemoveClosedEmptyDeathChests ||
				!(blockEntity instanceof ViewerCount) ||
				((ViewerCount) blockEntity).getViewerCount() == 0) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}
}
