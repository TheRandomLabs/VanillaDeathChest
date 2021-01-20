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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Finds suitable locations for the placement of death chests.
 */
public final class DeathChestLocationFinder {
	/**
	 * A death chest location.
	 */
	public static final class Location {
		private final BlockPos pos;
		private final boolean isDoubleChest;

		private Location(BlockPos pos, boolean isDoubleChest) {
			this.pos = pos;
			this.isDoubleChest = isDoubleChest;
		}

		/**
		 * Returns the {@link BlockPos} of this location.
		 * If this is a double chest, this is the position of the west block.
		 *
		 * @return the {@link BlockPos} of this location.
		 */
		public BlockPos getPos() {
			return pos;
		}

		/**
		 * Returns whether a double chest should be placed at this location.
		 *
		 * @return {@code true} if a double chest should be placed at this location,
		 * or otherwise {@code false}.
		 */
		public boolean isDoubleChest() {
			return isDoubleChest;
		}
	}

	private static final class SearchOrder implements Iterable<BlockPos> {
		private final int size;
		private List<BlockPos> translations;

		private SearchOrder(int size) {
			this.size = size;

			translations = new ArrayList<>();

			for (int x = 0; x <= size; x++) {
				addTranslations(x);
				addTranslations(-x);
			}

			translations = ImmutableList.copyOf(translations);
		}

		@NonNull
		@Override
		public Iterator<BlockPos> iterator() {
			return translations.iterator();
		}

		private void addTranslations(int x) {
			for (int y = 0; y <= size; y++) {
				addTranslations(x, y);
				addTranslations(x, -y);
			}
		}

		private void addTranslations(int x, int y) {
			for (int z = 0; z <= size; z++) {
				translations.add(new BlockPos(x, y, z));
				translations.add(new BlockPos(x, y, -z));
			}
		}
	}

	@Nullable
	private static SearchOrder searchOrder;

	private DeathChestLocationFinder() {}

	/**
	 * Finds the most suitable location to place a queued death chest.
	 *
	 * @param deathChest a queued {@link DeathChest}.
	 * @return a {@link Location} that describes the most suitable location to place the specified
	 * death chest.
	 */
	@Nullable
	public static Location find(DeathChest deathChest) {
		final World world = deathChest.getWorld();
		final PlayerEntity player = world.getPlayerByUuid(deathChest.getPlayerUUID());
		final BlockPos pos = deathChest.getPos();
		final boolean isDoubleChest = deathChest.isDoubleChest();

		final VDCConfig.Spawning config = VanillaDeathChest.config().spawning;

		final BlockPos searchPos = new BlockPos(
				pos.getX(), Math.min(256, Math.max(1, pos.getY())), pos.getZ()
		);

		BlockPos singleChestPos = null;

		for (BlockPos translation : getSearchOrder(config.locationSearchRadius)) {
			final BlockPos potentialPos = searchPos.add(translation);

			if (!canPlace(world, player, potentialPos)) {
				continue;
			}

			if (!isDoubleChest || canPlace(world, player, potentialPos.east())) {
				return new Location(potentialPos, isDoubleChest);
			}

			if (singleChestPos == null) {
				singleChestPos = potentialPos;
			}
		}

		if (singleChestPos != null) {
			return new Location(singleChestPos, false);
		}

		return config.forcePlacementIfNoSuitableLocation ? new Location(pos, isDoubleChest) : null;
	}

	private static Iterable<BlockPos> getSearchOrder(int size) {
		if (searchOrder == null || searchOrder.size != size) {
			searchOrder = new SearchOrder(size);
		}

		return searchOrder;
	}

	private static boolean canPlace(
			World world, PlayerEntity player, BlockPos pos, boolean doubleChest
	) {
		if (doubleChest) {
			return canPlace(world, player, pos) && canPlace(world, player, pos.east());
		}

		return canPlace(world, player, pos);
	}

	private static boolean canPlace(World world, PlayerEntity player, BlockPos pos) {
		if (!world.canPlayerModifyAt(player, pos) ||
				(VanillaDeathChest.config().spawning.requirePlacementOnSolidBlocks &&
						!world.isTopSolid(pos.down(), player))) {
			return false;
		}

		final ItemPlacementContext context = new ItemPlacementContext(new ItemUsageContext(
				player, Hand.MAIN_HAND,
				new BlockHitResult(new Vec3d(0.0, 0.0, 0.0), Direction.DOWN, pos, false)
		));

		if (isReplaceable(world, pos, context) && isReplaceable(world, pos.up(), context)) {
			return isNotChest(world, pos.north()) && isNotChest(world, pos.east()) &&
					isNotChest(world, pos.south()) && isNotChest(world, pos.west());
		}

		return false;
	}

	private static boolean isReplaceable(World world, BlockPos pos, ItemPlacementContext context) {
		if (pos.getY() < 1 || pos.getY() > world.getHeight()) {
			return false;
		}

		final BlockState state = world.getBlockState(pos);
		return state.isAir() || state.canReplace(context);
	}

	private static boolean isNotChest(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() != Blocks.CHEST;
	}
}
