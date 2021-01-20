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

import java.util.Queue;

import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.server.world.ServerWorld;

/**
 * Handles death chest placement.
 */
public final class DeathChestPlacer {
	private DeathChestPlacer() {}

	/**
	 * Places all queued death chests that are ready to be placed in the specified world.
	 * This is called at the end of every world tick.
	 *
	 * @param world a {@link ServerWorld}.
	 */
	public static void placeQueued(ServerWorld world) {
		final DeathChestsState state = DeathChestsState.get(world);
		final Queue<DeathChest> queue = state.getQueuedDeathChests();

		//We wait two ticks to prevent conflicts with other mods that place things after death.
		if (queue.isEmpty() || queue.peek().getCreationTime() - world.getTime() < 2L) {
			return;
		}

		while (!queue.isEmpty() && queue.peek().getCreationTime() - world.getTime() >= 2L) {
			place(queue.poll());
		}

		state.markDirty();
	}

	private static void place(DeathChest deathChest) {
		//TODO
	}

	/*
	@SuppressWarnings({"Duplicates", "NullAway"})
	private void place(ServerWorld world, PlayerEntity player) {
		final VDCConfig.Spawning config = VanillaDeathChest.config().spawning;

		final DeathChestType type = config.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = new BlockPos(player.getPos());

		final Pattern pattern = Pattern.compile(config.registryNameRegex);
		final List<ItemEntity> filtered = drops.stream().
				filter(item -> pattern.matcher(
						Registry.ITEM.getId(item.getStack().getItem()).toString()
				).matches()).
				collect(Collectors.toList());

		boolean useDoubleChest = (type == DeathChestType.SINGLE_OR_DOUBLE ||
				type == DeathChestType.DOUBLE_SHULKER_BOX) && filtered.size() > 27;

		if (config.useContainerInInventory) {
			final List<ItemEntity> empty = new ArrayList<>();

			boolean foundOne = false;
			boolean foundAll = false;

			for (ItemEntity item : drops) {
				final ItemStack stack = item.getStack();

				if (type == DeathChestType.SINGLE_ONLY || type == DeathChestType.SINGLE_OR_DOUBLE) {
					if (stack.getItem() != Item.BLOCK_ITEMS.get(Blocks.CHEST)) {
						continue;
					}
				} else {
					if (!(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) {
						continue;
					}

					final CompoundTag tag = stack.getTag();

					if (tag != null) {
						final DefaultedList<ItemStack> inventory =
								DefaultedList.ofSize(27, ItemStack.EMPTY);
						Inventories.fromTag(tag.getCompound("BlockEntityTag"), inventory);

						//Shulker box must be empty.
						if (inventory.stream().anyMatch(itemStack -> !itemStack.isEmpty())) {
							continue;
						}
					}
				}

				if (!useDoubleChest) {
					stack.decrement(1);

					if (stack.isEmpty()) {
						empty.add(item);
					}

					foundAll = true;
					break;
				}

				if (stack.getCount() > 1) {
					stack.decrement(2);

					if (stack.isEmpty()) {
						empty.add(item);
					}

					foundAll = true;
					break;
				}

				stack.decrement(1);

				if (stack.isEmpty()) {
					empty.add(item);
				}

				if (foundOne) {
					foundAll = true;
					break;
				}

				foundOne = true;
			}

			if (useDoubleChest) {
				if (!foundAll) {
					if (!foundOne) {
						return;
					}

					useDoubleChest = false;
				}
			} else if (!foundAll) {
				return;
			}

			drops.removeAll(empty);
			filtered.removeAll(empty);
		}

		final AtomicBoolean doubleChest = new AtomicBoolean(useDoubleChest);

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, doubleChest);

		useDoubleChest = doubleChest.get();

		if (pos == null) {
			VanillaDeathChest.logger.warn(
					"No death chest location found for player at [{}]", playerPos
			);
			return;
		}

		final Block block;

		if (type == DeathChestType.SHULKER_BOX || type == DeathChestType.DOUBLE_SHULKER_BOX) {
			block = ShulkerBoxBlock.get(config.shulkerBoxColor.get());
		} else {
			block = Blocks.CHEST;
		}

		final BlockState state = block.getDefaultState();
		final BlockPos east = pos.east();

		if (useDoubleChest) {
			if (block == Blocks.CHEST) {
				world.setBlockState(pos, state.with(ChestBlock.CHEST_TYPE, ChestType.LEFT));
				world.setBlockState(east, state.with(ChestBlock.CHEST_TYPE, ChestType.RIGHT));
			} else {
				world.setBlockState(pos, state);
				world.setBlockState(east, state);
			}
		} else {
			world.setBlockState(pos, state);
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);
		final BlockEntity blockEntity2 = useDoubleChest ? world.getBlockEntity(east) : null;

		if (!(blockEntity instanceof LootableContainerBlockEntity) ||
				(useDoubleChest && !(blockEntity2 instanceof LootableContainerBlockEntity))) {
			VanillaDeathChest.logger.warn(
					"Failed to place death chest at [{}] due to invalid block entity", pos
			);
			return;
		}

		LootableContainerBlockEntity chest =
				(LootableContainerBlockEntity) (useDoubleChest ? blockEntity2 : blockEntity);

		for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
			final ItemEntity item = filtered.get(0);
			chest.setStack(i, item.getStack());
			filtered.remove(0);
			drops.remove(item);
		}

		if (!config.containerDisplayName.isEmpty()) {
			chest.setCustomName(new LiteralText(config.containerDisplayName));
		}

		if (useDoubleChest) {
			chest = (LootableContainerBlockEntity) blockEntity;

			for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
				final ItemEntity item = filtered.get(0);
				chest.setStack(i, item.getStack());
				filtered.remove(0);
				drops.remove(item);
			}

			//If this is a shulker box, this has to be set separately.
			if (!config.containerDisplayName.isEmpty()) {
				chest.setCustomName(new LiteralText(config.containerDisplayName));
			}
		}

		final VDCConfig.Defense defense = VanillaDeathChest.config().defense;

		if (defense.defenseEntity != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for (int i = 0; i < defense.defenseEntitySpawnCount; i++) {
				CompoundTag compound = null;

				try {
					compound = StringNbtReader.parse(defense.defenseEntityNBT);
				} catch (CommandSyntaxException ignored) {}

				compound.putString("id", defense.defenseEntityRegistryName);

				final Entity entity = EntityType.loadEntityWithPassengers(
						compound, world, spawnedEntity -> {
							spawnedEntity.setPos(x, y, z);
							return !world.tryLoadEntity(spawnedEntity) ? null : spawnedEntity;
						}
				);

				if (entity instanceof MobEntity) {
					final MobEntity living = (MobEntity) entity;

					living.setPersistent();
					living.initialize(
							world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null
					);
					living.setAttacker(player);

					final DeathChestDefenseEntity defenseEntity = (DeathChestDefenseEntity) living;

					defenseEntity.setDeathChestPlayer(player.getUuid());
					defenseEntity.setDeathChestPos(pos);
				}
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.logger.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		player.sendMessage(new LiteralText(String.format(
				config.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)), false);
	}
	 */
}
