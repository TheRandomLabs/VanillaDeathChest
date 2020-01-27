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

	public static DeathChest getDeathChest(ServerWorld world, BlockPos pos) {
		final Block block = world.getBlockState(pos).getBlock();

		if(block != Blocks.CHEST && !(block instanceof ShulkerBoxBlock)) {
			return null;
		}

		final Map<BlockPos, DeathChest> deathChests = VDCSavedData.get(world).getDeathChests();
		return deathChests.get(pos);
	}

	public static DeathChest removeDeathChest(ServerWorld world, BlockPos pos) {
		final Map<BlockPos, DeathChest> deathChests = VDCSavedData.get(world).getDeathChests();
		final DeathChest chest = deathChests.remove(pos);

		if (chest == null) {
			return null;
		}

		final BlockPos west;
		final BlockPos east;

		if(chest.isDoubleChest()) {
			if(chest.getPos().equals(pos)) {
				west = pos;
				east = pos.east();

				deathChests.remove(east);
			} else {
				west = pos.west();
				east = pos;

				deathChests.remove(west);
			}
		} else {
			west = pos;
			east = null;
		}

		DeathChestRemoveCallback.EVENT.invoker().onRemove(chest, west, east);
		return chest;
	}
}
