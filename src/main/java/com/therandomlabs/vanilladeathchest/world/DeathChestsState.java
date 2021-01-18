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
import net.fabricmc.fabric.api.util.NbtType;
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
	private final Map<BlockPos, DeathChest> deathChests = new HashMap<>();
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
				forEach(deathChest -> deathChests.put(deathChest.getPos(), deathChest));

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
	 * Returns a mutable map of all placed death chests.
	 *
	 * @return a mutable map of all placed death chests.
	 */
	public Map<BlockPos, DeathChest> getDeathChests() {
		return deathChests;
	}

	/**
	 * Returns the death chest at the specified position.
	 *
	 * @param pos a position.
	 * @return the {@link DeathChest} at the specified {@link BlockPos}.
	 */
	@Nullable
	public DeathChest getDeathChest(BlockPos pos) {
		final DeathChest deathChest = deathChests.get(pos);
		return deathChest == null ? deathChests.get(pos.east()) : deathChest;
	}

	/**
	 * Returns a queue of all unplaced death chests.
	 *
	 * @return a queue of all unplaced death chests.
	 */
	public Queue<DeathChest> getQueuedDeathChests() {
		return queuedDeathChests;
	}

	private ListTag toTag(Collection<DeathChest> deathChests) {
		final ListTag list = new ListTag();
		deathChests.stream().
				map(deathChest -> deathChest.toTag(new CompoundTag())).
				forEach(list::add);
		return list;
	}

	private void fromTag(ListTag list, Collection<DeathChest> deathChests) {
		list.stream().
				map(tag -> DeathChest.fromTag(world, (CompoundTag) tag)).
				forEach(deathChests::add);
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
