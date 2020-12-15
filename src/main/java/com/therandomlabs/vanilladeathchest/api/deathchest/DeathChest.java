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

package com.therandomlabs.vanilladeathchest.api.deathchest;

import java.util.UUID;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.world.storage.VDCSavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class DeathChest {
	private final ServerWorld world;
	private final UUID playerID;
	private final long creationTime;
	private final BlockPos pos;
	private final boolean isDoubleChest;
	private boolean unlocked;

	public DeathChest(
			ServerWorld world, UUID playerID, long creationTime, BlockPos pos,
			boolean isDoubleChest, boolean unlocked
	) {
		this.world = world;
		this.playerID = playerID;
		this.creationTime = creationTime;
		this.pos = pos;
		this.isDoubleChest = isDoubleChest;
		this.unlocked = unlocked;
	}

	public ServerWorld getWorld() {
		return world;
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public BlockPos getPos() {
		return pos;
	}

	public boolean isDoubleChest() {
		return isDoubleChest;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean flag) {
		unlocked = flag;
		VDCSavedData.get(world).markDirty();
	}

	public boolean canInteract(PlayerEntity player) {
		if(player == null) {
			return false;
		}

		if(!VDCConfig.Protection.enabled ||
				playerID.equals(player.getUuid()) ||
				(VDCConfig.Protection.bypassIfCreative && player.abilities.creativeMode)) {
			return true;
		}

		final OperatorEntry entry = player.getServer().getPlayerManager().getOpList().
				get(player.getGameProfile());

		if(entry == null ||
				entry.getPermissionLevel() < VDCConfig.Protection.bypassPermissionLevel) {
			if(VDCConfig.Protection.period == 0) {
				return false;
			}

			final long timeElapsed = player.getEntityWorld().getTime() - creationTime;
			return timeElapsed > VDCConfig.Protection.period;
		}

		return true;
	}
}
