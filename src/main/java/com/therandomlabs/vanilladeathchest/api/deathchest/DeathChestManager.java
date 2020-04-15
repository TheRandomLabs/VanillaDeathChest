/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2019 TheRandomLabs
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

package com.therandomlabs.vanilladeathchest.api.deathchest;

import java.util.Map;

import com.therandomlabs.vanilladeathchest.api.event.deathchest.DeathChestRemoveCallback;
import com.therandomlabs.vanilladeathchest.world.storage.VDCSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DeathChestManager {
	private DeathChestManager() {}

	public static void addDeathChest(
			ServerWorld world, PlayerEntity player, BlockPos pos, boolean isDoubleChest
	) {
		final VDCSavedData data = VDCSavedData.get(world);
		final Map<BlockPos, DeathChest> deathChests = data.getDeathChests();
		final DeathChest deathChest = new DeathChest(
				world, player.getUuid(), world.getTime(), pos, isDoubleChest, false
		);

		deathChests.put(pos, deathChest);

		if(isDoubleChest) {
			deathChests.put(pos.east(), deathChest);
		}

		data.markDirty();
	}

	public static boolean isDeathChest(ServerWorld world, BlockPos pos) {
		return getDeathChest(world, pos) != null;
	}

	public static boolean isLocked(ServerWorld world, BlockPos pos) {
		final DeathChest deathChest = getDeathChest(world, pos);
		return deathChest != null && !deathChest.isUnlocked();
	}

	@Nullable
	public static DeathChest getDeathChest(ServerWorld world, BlockPos pos) {
		final Block block = world.getBlockState(pos).getBlock();

		if (block != Blocks.CHEST && !(block instanceof ShulkerBoxBlock)) {
			return null;
		}

		final Map<BlockPos, DeathChest> deathChests = VDCSavedData.get(world).getDeathChests();
		return deathChests.get(pos);
	}

	@Nullable
	public static DeathChest removeDeathChest(ServerWorld world, BlockPos pos) {
		final Map<BlockPos, DeathChest> deathChests = VDCSavedData.get(world).getDeathChests();
		final DeathChest chest = deathChests.remove(pos);

		if (chest == null) {
			return null;
		}

		final BlockPos west;
		final BlockPos east;

		if (chest.isDoubleChest()) {
			if (chest.getPos().equals(pos)) {
				west = pos;
				east = pos.east();
			} else {
				west = pos.west();
				east = pos;
			}
		} else {
			west = pos;
			east = null;
		}

		DeathChestRemoveCallback.EVENT.invoker().onRemove(chest, west, east);
		return chest;
	}
}
