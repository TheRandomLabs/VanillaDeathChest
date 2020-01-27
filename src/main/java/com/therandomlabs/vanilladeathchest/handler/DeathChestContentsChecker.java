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

package com.therandomlabs.vanilladeathchest.handler;

import java.util.Map;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.world.storage.VDCSavedData;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkManager;

public class DeathChestContentsChecker implements ServerTickCallback {
	@Override
	public void tick(MinecraftServer server) {
		for(ServerWorld world : server.getWorlds()) {
			worldTick(world);
		}
	}

	private static void worldTick(ServerWorld world) {
		if(!VDCConfig.Misc.deathChestsDisappearWhenEmptied) {
			return;
		}

		final VDCSavedData savedData = VDCSavedData.get(world);
		final ChunkManager provider = world.getChunkManager();

		for(Map.Entry<BlockPos, DeathChest> entry : savedData.getDeathChests().entrySet()) {
			final BlockPos pos = entry.getKey();

			//Make sure we don't unnecessarily load any chunks
			if (!provider.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
				continue;
			}

			final BlockEntity blockEntity = world.getBlockEntity(pos);

			if (!(blockEntity instanceof LockableContainerBlockEntity)) {
				continue;
			}

			if (((LockableContainerBlockEntity) blockEntity).isInvEmpty() &&
					DeathChestManager.removeDeathChest(world, pos) != null) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());

				if (entry.getValue().isDoubleChest()) {
					world.setBlockState(pos.east(), Blocks.AIR.getDefaultState());
				}
			}
		}
	}
}
