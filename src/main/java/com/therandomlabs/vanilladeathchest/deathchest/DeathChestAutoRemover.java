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

		for (DeathChest deathChest : DeathChestsState.get(world).getDeathChests().values()) {
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

		if (!(blockEntity instanceof LockableContainerBlockEntity)) {
			return;
		}

		final int viewerCount = blockEntity instanceof ViewerCount ?
				((ViewerCount) blockEntity).getViewerCount() : 0;

		if (((LockableContainerBlockEntity) blockEntity).isEmpty() && viewerCount == 0) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}
}
