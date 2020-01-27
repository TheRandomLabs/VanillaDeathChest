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

package com.therandomlabs.vanilladeathchest.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableList;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DeathChestLocationFinder {
	private static class SearchOrder implements Iterable<BlockPos> {
		public final int size;
		private List<BlockPos> translations;

		public SearchOrder(int size) {
			this.size = size;

			translations = new ArrayList<>();

			for(int x = 0; x <= size; x++) {
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
			for(int y = 0; y <= size; y++) {
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

	@Nullable
	private static SearchOrder searchOrder;

	public static Iterable<BlockPos> getSearchOrder(int size) {
		if (searchOrder == null || searchOrder.size != size) {
			searchOrder = new SearchOrder(size);
		}

		return searchOrder;
	}

	@Nullable
	public static BlockPos findLocation(
			World world, PlayerEntity player, BlockPos pos, AtomicBoolean doubleChest
	) {
		final boolean isDoubleChest = doubleChest.get();

		final BlockPos searchPos = new BlockPos(
				pos.getX(),
				Math.min(256, Math.max(1, pos.getY())),
				pos.getZ()
		);

		BlockPos singleChestPos = null;

		for(BlockPos translation : getSearchOrder(VDCConfig.Spawning.locationSearchRadius)) {
			final BlockPos potentialPos = searchPos.add(translation);

			if(canPlace(world, player, potentialPos)) {
				if ((!isDoubleChest || canPlace(world, player, potentialPos.east())) &&
						isOnSolidBlocks(world, player, potentialPos, isDoubleChest)) {
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
			World world, PlayerEntity player, BlockPos pos, boolean doubleChest
	) {
		if (doubleChest) {
			return canPlace(world, player, pos) && canPlace(world, player, pos.east());
		}

		return canPlace(world, player, pos);
	}

	@SuppressWarnings("deprecation")
	public static boolean canPlace(World world, PlayerEntity player, BlockPos pos) {
		if (!world.isBlockLoaded(pos) || !world.canPlayerModifyAt(player, pos)) {
			return false;
		}

		final ItemPlacementContext context = new ItemPlacementContext(new ItemUsageContext(
				player,
				Hand.MAIN_HAND,
				new BlockHitResult(
						new Vec3d(0.0, 0.0, 0.0),
						Direction.DOWN,
						pos,
						false
				)
		));

		if(isReplaceable(world, pos, context) && isReplaceable(world, pos.up(), context)) {
			return isNotChest(world, pos.north()) && isNotChest(world, pos.east()) &&
					isNotChest(world, pos.south()) && isNotChest(world, pos.west());
		}

		return false;
	}

	private static boolean isReplaceable(World world, BlockPos pos, ItemPlacementContext context) {
		if(pos.getY() < 1 || pos.getY() > world.getEffectiveHeight()) {
			return false;
		}

		final BlockState state = world.getBlockState(pos);
		return state.isAir() || state.canReplace(context);
	}

	private static boolean isNotChest(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() != Blocks.CHEST;
	}

	private static boolean isOnSolidBlocks(World world, PlayerEntity player, BlockPos pos,
			boolean isDoubleChest) {
		if(!VDCConfig.Spawning.mustBeOnSolidBlocks) {
			return true;
		}

		final BlockPos down = pos.down();

		if(!world.doesBlockHaveSolidTopSurface(down, player)) {
			return false;
		}

		return !isDoubleChest || world.doesBlockHaveSolidTopSurface(down.east(), player);
	}
}
