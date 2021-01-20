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

package com.therandomlabs.vanilladeathchest.world;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestIdentifier;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Persistent death chests state.
 */
public final class DeathChestsState extends PersistentState {
	private final ServerWorld world;
	private final Map<DeathChestIdentifier, DeathChest> deathChests = new HashMap<>();
	private final Map<BlockPos, DeathChest> deathChestsByPos = new HashMap<>();
	private final Queue<DeathChest> queuedDeathChests = new ArrayDeque<>();

	private DeathChestsState(String name, ServerWorld world) {
		super(name);
		this.world = world;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromTag(CompoundTag tag) {
		deathChests.clear();
		tag.getList("DeathChests", NbtType.COMPOUND).stream().
				map(deathChestTag -> DeathChest.fromTag(world, (CompoundTag) deathChestTag)).
				forEach(deathChest -> deathChests.put(deathChest.getIdentifier(), deathChest));

		for (DeathChest deathChest : deathChests.values()) {
			final DeathChest oldDeathChest = deathChestsByPos.get(deathChest.getPos());

			if (oldDeathChest == null ||
					deathChest.getCreationTime() > oldDeathChest.getCreationTime()) {
				deathChestsByPos.put(deathChest.getPos(), deathChest);
			}
		}

		queuedDeathChests.clear();
		tag.getList("QueuedDeathChests", NbtType.COMPOUND).stream().
				map(deathChestTag -> DeathChest.fromTag(world, (CompoundTag) deathChestTag)).
				forEach(queuedDeathChests::add);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		final ListTag deathChestsList = new ListTag();
		deathChests.values().stream().
				map(deathChest -> deathChest.toTag(new CompoundTag())).
				forEach(deathChestsList::add);
		tag.put("DeathChests", deathChestsList);

		final ListTag queuedDeathChestsList = new ListTag();
		queuedDeathChests.stream().
				map(deathChest -> deathChest.toTag(new CompoundTag())).
				forEach(queuedDeathChestsList::add);
		tag.put("QueuedDeathChests", queuedDeathChestsList);

		return tag;
	}

	/**
	 * Returns all placed death chests.
	 *
	 * @return a {@link Collection} of all placed death chests.
	 */
	public Collection<DeathChest> getDeathChests() {
		return deathChests.values();
	}

	/**
	 * Returns the death chest with the specified identifier.
	 *
	 * @param identifier an identifier.
	 * @return the {@link DeathChest} with the specified identifier.
	 */
	public DeathChest getDeathChest(DeathChestIdentifier identifier) {
		return deathChests.get(identifier);
	}

	/**
	 * Returns the most recently placed death chest at the specified position.
	 *
	 * @param pos a position.
	 * @return the {@link DeathChest} at the specified {@link BlockPos}.
	 */
	@Nullable
	public DeathChest getDeathChest(BlockPos pos) {
		final DeathChest deathChest = deathChestsByPos.get(pos);
		return deathChest == null ? deathChestsByPos.get(pos.west()) : deathChest;
	}

	/**
	 * Adds a death chest.
	 *
	 * @param deathChest a {@link DeathChest}.
	 */
	public void addDeathChest(DeathChest deathChest) {
		deathChests.put(deathChest.getIdentifier(), deathChest);
		deathChestsByPos.put(deathChest.getPos(), deathChest);
	}

	/**
	 * Returns the death chest at the specified position if it exists in the world.
	 *
	 * @param pos a position.
	 * @return the {@link DeathChest} at the specified {@link BlockPos}.
	 */
	@Nullable
	public DeathChest getExistingDeathChest(BlockPos pos) {
		final DeathChest deathChest = getDeathChest(pos);

		if (deathChest == null || !world.getBlockState(pos).getBlock().hasBlockEntity()) {
			return null;
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity instanceof DeathChestBlockEntity &&
				((DeathChestBlockEntity) blockEntity).getDeathChest().equals(deathChest) ?
				deathChest : null;
	}

	/**
	 * Returns a queue of all unplaced death chests.
	 *
	 * @return a queue of all unplaced death chests.
	 */
	public Queue<DeathChest> getQueuedDeathChests() {
		return queuedDeathChests;
	}

	/**
	 * Returns the {@link DeathChestsState} instance for the specified world.
	 *
	 * @param world a {@link ServerWorld}.
	 * @return the {@link DeathChestsState} instance for the specified world.
	 */
	public static DeathChestsState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(
				() -> new DeathChestsState("deathchests", world), "deathchests"
		);
	}
}
