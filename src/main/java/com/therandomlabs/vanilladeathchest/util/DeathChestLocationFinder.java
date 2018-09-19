package com.therandomlabs.vanilladeathchest.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathChestLocationFinder {
	private static class SearchOrder implements Iterable<BlockPos> {
		public final int size;
		private List<BlockPos> coordinates;

		public SearchOrder(int size) {
			this.size = size;

			coordinates = new ArrayList<>();

			for(int x = 0; x <= size; x++) {
				add(x);
				add(-x);
			}

			coordinates = ImmutableList.copyOf(coordinates);
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return coordinates.iterator();
		}

		private void add(int x) {
			for(int y = 0; y <= size; y++) {
				add(x, y);
				add(x, -y);
			}
		}

		private void add(int x, int y) {
			for(int z = 0; z <= size; z++) {
				coordinates.add(new BlockPos(x, y, z));
				coordinates.add(new BlockPos(x, y, -z));
			}
		}
	}

	private static SearchOrder searchOrder;

	public static Iterable<BlockPos> getSearchOrder(int size) {
		if(searchOrder == null || searchOrder.size != size) {
			searchOrder = new SearchOrder(size);
		}

		return searchOrder;
	}

	public static BlockPos findLocation(World world, EntityPlayer player, BlockPos pos,
			boolean doubleChest) {
		int y = pos.getY();

		if(y < 1) {
			y = 1;
		}

		if(y > 256) {
			y = 256;
		}

		final BlockPos searchPos = new BlockPos(pos.getX(), y, pos.getZ());

		for(BlockPos c : getSearchOrder(VDCConfig.spawning.locationSearchRadius)) {
			final BlockPos potentialPos = searchPos.add(c);

			if(canPlace(world, player, potentialPos, doubleChest)) {
				return potentialPos;
			}
		}

		return null;
	}

	public static boolean canPlace(World world, EntityPlayer player, BlockPos pos,
			boolean doubleChest) {
		if(doubleChest) {
			return canPlaceSingle(world, player, pos) &&
					canPlaceSingle(world, player, pos.east());
		}

		return canPlaceSingle(world, player, pos);
	}

	public static boolean canPlaceSingle(World world, EntityPlayer player, BlockPos pos) {
		if(!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos)) {
			return false;
		}

		final IBlockState state = world.getBlockState(pos);
		final Block block = state.getBlock();

		final BlockPos pos2 = pos.up();
		final IBlockState state2 = world.getBlockState(pos2);
		final Block block2 = state2.getBlock();

		if((block.isAir(state, world, pos) || block.isReplaceable(world, pos)) &&
				(block2.isAir(state2, world, pos2) || block2.isReplaceable(world, pos2))) {
			return isNotChest(world, pos.north()) && isNotChest(world, pos.east()) &&
					isNotChest(world, pos.south()) && isNotChest(world, pos.west());
		}

		return false;
	}

	private static boolean isNotChest(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() != Blocks.CHEST;
	}
}
