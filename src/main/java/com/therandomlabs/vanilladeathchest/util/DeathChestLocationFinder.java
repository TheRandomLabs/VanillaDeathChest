package com.therandomlabs.vanilladeathchest.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.therandomlabs.randomlib.BooleanWrapper;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathChestLocationFinder {
	private static class SearchOrder implements Iterable<BlockPos> {
		public final int size;
		private List<BlockPos> translations;

		public SearchOrder(int size) {
			this.size = size;

			translations = new ArrayList<>();

			for (int x = 0; x <= size; x++) {
				add(x);
				add(-x);
			}

			translations = ImmutableList.copyOf(translations);
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return translations.iterator();
		}

		private void add(int x) {
			for (int y = 0; y <= size; y++) {
				add(x, y);
				add(x, -y);
			}
		}

		private void add(int x, int y) {
			for (int z = 0; z <= size; z++) {
				translations.add(new BlockPos(x, y, z));
				translations.add(new BlockPos(x, y, -z));
			}
		}
	}

	private static SearchOrder searchOrder;

	public static Iterable<BlockPos> getSearchOrder(int size) {
		if (searchOrder == null || searchOrder.size != size) {
			searchOrder = new SearchOrder(size);
		}

		return searchOrder;
	}

	public static BlockPos findLocation(
			World world, EntityPlayer player, BlockPos pos,
			BooleanWrapper doubleChest
	) {
		int y = pos.getY();

		if (!VanillaDeathChest.CUBIC_CHUNKS_LOADED) {
			if (y < 1) {
				y = 1;
			}

			final int actualHeight = world.getActualHeight();

			if (y > actualHeight) {
				y = actualHeight;
			}
		}

		final boolean isDoubleChest = doubleChest.get();
		final BlockPos searchPos = new BlockPos(pos.getX(), y, pos.getZ());

		BlockPos singleChestPos = null;

		for (BlockPos translation : getSearchOrder(VDCConfig.Spawning.locationSearchRadius)) {
			final BlockPos potentialPos = searchPos.add(translation);

			if (canPlace(world, player, potentialPos)) {
				if ((!isDoubleChest || canPlace(world, player, potentialPos.east())) &&
						isOnSolidBlocks(world, potentialPos, isDoubleChest)) {
					return potentialPos;
				}

				if (singleChestPos == null) {
					singleChestPos = potentialPos;
				}
			}
		}

		if (singleChestPos != null) {
			doubleChest.set(false);
			return singleChestPos;
		}

		return VDCConfig.Spawning.forcePlaceIfLocationNotFound ? pos : null;
	}

	public static boolean canPlace(
			World world, EntityPlayer player, BlockPos pos,
			boolean doubleChest
	) {
		if (doubleChest) {
			return canPlace(world, player, pos) && canPlace(world, player, pos.east());
		}

		return canPlace(world, player, pos);
	}

	public static boolean canPlace(World world, EntityPlayer player, BlockPos pos) {
		if (!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos)) {
			return false;
		}

		if (isReplaceable(world, pos) && isReplaceable(world, pos.up())) {
			return isNotChest(world, pos.north()) && isNotChest(world, pos.east()) &&
					isNotChest(world, pos.south()) && isNotChest(world, pos.west());
		}

		return false;
	}

	private static boolean isReplaceable(World world, BlockPos pos) {
		if (!VanillaDeathChest.CUBIC_CHUNKS_LOADED) {
			final int y = pos.getY();

			if (y < 1 || y > world.getActualHeight()) {
				return false;
			}
		}

		return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}

	private static boolean isNotChest(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() != Blocks.CHEST;
	}

	private static boolean isOnSolidBlocks(World world, BlockPos pos, boolean isDoubleChest) {
		if (!VDCConfig.Spawning.mustBeOnSolidBlocks) {
			return true;
		}

		final BlockPos down = pos.down();

		if (!world.isSideSolid(down, EnumFacing.UP)) {
			return false;
		}

		return !isDoubleChest || world.isSideSolid(down.east(), EnumFacing.UP);
	}
}
