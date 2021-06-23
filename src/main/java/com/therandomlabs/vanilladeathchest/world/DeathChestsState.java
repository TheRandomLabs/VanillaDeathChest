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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.mixin.ChestBlockEntityAccessor;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Persistent death chests state.
 */
public final class DeathChestsState extends PersistentState {
	private final ServerWorld world;
	private final Map<UUID, DeathChest> deathChests = new HashMap<>();
	private final Map<BlockPos, DeathChest> existingDeathChests = new HashMap<>();
	private final Queue<DeathChest> queuedDeathChests =
			new PriorityQueue<>(Comparator.comparing(DeathChest::getCreationTime));

	private DeathChestsState(String name, ServerWorld world) {
		super();
		this.world = world;
	}

	/**
	 *
	 */
	public DeathChestsState readNbt(NbtCompound tag) {
		deathChests.clear();
		tag.getList("DeathChests", NbtType.COMPOUND).stream().
				map(deathChestTag -> DeathChest.readNbt(world, (NbtCompound) deathChestTag)).
				forEach(deathChest -> deathChests.put(deathChest.getIdentifier(), deathChest));

		existingDeathChests.clear();
		tag.getList("ExistingDeathChests", NbtType.INT_ARRAY).stream().
				map(NbtHelper::toUuid).
				map(deathChests::get).
				forEach(deathChest -> existingDeathChests.put(deathChest.getPos(), deathChest));

		queuedDeathChests.clear();
		tag.getList("QueuedDeathChests", NbtType.COMPOUND).stream().
				map(deathChestTag -> DeathChest.readNbt(world, (NbtCompound) deathChestTag)).
				forEach(queuedDeathChests::add);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		final NbtList deathChestsList = new NbtList();
		deathChests.values().stream().
				map(deathChest -> deathChest.writeNbt(new NbtCompound())).
				forEach(deathChestsList::add);
		tag.put("DeathChests", deathChestsList);

		final NbtList existingDeathChestsList = new NbtList();
		existingDeathChests.values().stream().
				map(DeathChest::getIdentifier).
				map(NbtHelper::fromUuid).
				forEach(existingDeathChestsList::add);
		tag.put("ExistingDeathChests", existingDeathChestsList);

		final NbtList queuedDeathChestsList = new NbtList();
		queuedDeathChests.stream().
				map(deathChest -> deathChest.writeNbt(new NbtCompound())).
				forEach(queuedDeathChestsList::add);
		tag.put("QueuedDeathChests", queuedDeathChestsList);

		return tag;
	}

	/**
	 * Returns the identifiers of all placed death chests.
	 *
	 * @return a {@link Set} of {@link UUID}s.
	 */
	public Set<UUID> getDeathChestIdentifiers() {
		return deathChests.keySet();
	}

	/**
	 * Returns the identifier strings of all placed death chests.
	 *
	 * @return a {@link Set} of strings.
	 */
	public Set<String> getDeathChestIdentifierStrings() {
		return getDeathChestIdentifiers().stream().map(UUID::toString).collect(Collectors.toSet());
	}

	/**
	 * Returns all placed death chests.
	 *
	 * @return a {@link Collection} of all placed death chests.
	 */
	public Collection<DeathChest> getDeathChests() {
		return new HashSet<>(deathChests.values());
	}

	/**
	 * Returns the death chest with the specified identifier.
	 *
	 * @param identifier an identifier.
	 * @return the {@link DeathChest} with the specified identifier.
	 */
	@Nullable
	public DeathChest getDeathChest(UUID identifier) {
		return deathChests.get(identifier);
	}

	/**
	 * Returns all existing death chests.
	 *
	 * @return a {@link Collection} of all existing death chests.
	 */
	public Collection<DeathChest> getExistingDeathChests() {
		return new HashSet<>(existingDeathChests.values());
	}

	/**
	 * Returns the existing death chest at the specified position.
	 *
	 * @param pos a position.
	 * @return the existing {@link DeathChest} at the specified {@link BlockPos},
	 * or {@code null} if it does not exist.
	 */
	@Nullable
	public DeathChest getExistingDeathChest(BlockPos pos) {
		final DeathChest deathChest = existingDeathChests.get(pos);
		return deathChest == null ? existingDeathChests.get(pos.west()) : deathChest;
	}

	/**
	 * Adds an existing death chest.
	 *
	 * @param deathChest a {@link DeathChest}.
	 */
	public void addDeathChest(DeathChest deathChest) {
		deathChests.put(deathChest.getIdentifier(), deathChest);
		existingDeathChests.put(deathChest.getPos(), deathChest);
	}

	/**
	 * Returns a queue of all unplaced death chests. Changes to this queue are kept.
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
				(data) -> DeathChestsState.stateFromNbt(world, data),
				() -> DeathChestsState.createState(world), "deathchests"
		);
	}

	/**
	 * Called when a block entity is unloaded.
	 *
	 * @param blockEntity a {@link BlockEntity}.
	 * @param world a {@link ServerWorld}.
	 */
	public static void onBlockEntityUnload(BlockEntity blockEntity, ServerWorld world) {
		//Fabric API invokes this event in three different locations, but only two of them are
		//when a block entity is removed. The other one is just when a chunk is unloaded.
		//We can differentiate between these by checking whether the block entity is in
		//World#unloadedBlockEntities.

		// Chunk removal in ClientWorld#unloadBlockEntities no longer relies
		// on World#unloadedBlockEntities.
		/*if (((WorldAccessor) world).getUnloadedBlockEntities().contains(blockEntity)) {
			return;
		}*/

		// Using a heuristic for chunk unload: If the chest was destroyed (and not chunk unloaded)
		// the chest inventory should be empty.

		if (blockEntity instanceof DeathChestBlockEntity) {
			final DeathChest deathChest = ((DeathChestBlockEntity) blockEntity).getDeathChest();
			final DeathChestsState state = get(world);
			final boolean isEmpty;

			if (blockEntity instanceof ChestBlockEntityAccessor) {
				isEmpty = ((ChestBlockEntityAccessor) blockEntity).getInventory().
						stream().
						allMatch(ItemStack::isEmpty);
			} else {
				//not sure if this case can happen, but we keep the chest around
				isEmpty = false;
			}

			if (deathChest != null && isEmpty) {
				Block bl = blockEntity.getCachedState().getBlock();
				state.existingDeathChests.values().remove(deathChest);
			}
		}
	}

	/**
	 *
	 */
	public static DeathChestsState createState(ServerWorld world) {
		//Is name really unused?
		DeathChestsState deathChestsState = new DeathChestsState("deathchests", world);
		Objects.requireNonNull(deathChestsState);
		return deathChestsState;
	}

	/**
	 *
	 */
	public static DeathChestsState stateFromNbt(ServerWorld world, NbtCompound nbt) {
		return createState(world).readNbt(nbt);
	}
}
