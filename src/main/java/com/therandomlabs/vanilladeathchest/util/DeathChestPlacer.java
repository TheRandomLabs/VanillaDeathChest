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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public final class DeathChestPlacer {
	public enum DeathChestType {
		SINGLE_ONLY,
		SINGLE_OR_DOUBLE,
		SHULKER_BOX,
		DOUBLE_SHULKER_BOX
	}

	private final WeakReference<ServerWorld> world;
	private final WeakReference<PlayerEntity> player;
	private final List<ItemEntity> drops;

	private boolean alreadyCalled;

	public DeathChestPlacer(ServerWorld world, PlayerEntity player, List<ItemEntity> drops) {
		this.world = new WeakReference<>(world);
		this.player = new WeakReference<>(player);
		this.drops = drops;
	}

	public final boolean run() {
		//Delay by a tick to avoid conflicts with other mods that place blocks upon death
		if(!alreadyCalled) {
			alreadyCalled = true;
			return false;
		}

		final ServerWorld world = this.world.get();

		if(world == null) {
			return true;
		}

		final PlayerEntity player = this.player.get();

		if (player == null) {
			return true;
		}

		place(world, player);

		//Drop any remaining items
		for (ItemEntity drop : drops) {
			world.spawnEntity(
					new ItemEntity(world, drop.getX(), drop.getY(), drop.getZ(), drop.getStack()));
		}

		return true;
	}

	@SuppressWarnings({"Duplicates", "NullAway"})
	private void place(ServerWorld world, PlayerEntity player) {
		final DeathChestType type = VDCConfig.Spawning.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = new BlockPos(player.getPos());

		final Pattern pattern = Pattern.compile(VDCConfig.Spawning.registryNameRegex);
		final List<ItemEntity> filtered = drops.stream().
				filter(item -> pattern.matcher(
						Registry.ITEM.getId(item.getStack().getItem()).toString()
				).matches()).
				collect(Collectors.toList());

		boolean useDoubleChest = (type == DeathChestType.SINGLE_OR_DOUBLE ||
				type == DeathChestType.DOUBLE_SHULKER_BOX) && filtered.size() > 27;

		if (VDCConfig.Spawning.useContainerInInventory) {
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
			block = ShulkerBoxBlock.get(VDCConfig.Spawning.shulkerBoxColor.get());
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
			chest.setInvStack(i, item.getStack());
			filtered.remove(0);
			drops.remove(item);
		}

		if (!VDCConfig.Spawning.containerDisplayName.isEmpty()) {
			chest.setCustomName(new LiteralText(VDCConfig.Spawning.containerDisplayName));
		}

		if (useDoubleChest) {
			chest = (LootableContainerBlockEntity) blockEntity;

			for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
				final ItemEntity item = filtered.get(0);
				chest.setInvStack(i, item.getStack());
				filtered.remove(0);
				drops.remove(item);
			}

			//If this is a shulker box, this has to be set separately.
			if (!VDCConfig.Spawning.containerDisplayName.isEmpty()) {
				chest.setCustomName(new LiteralText(VDCConfig.Spawning.containerDisplayName));
			}
		}

		if (VDCConfig.Defense.defenseEntityRegistryName != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for (int i = 0; i < VDCConfig.Defense.defenseEntitySpawnCount; i++) {
				CompoundTag compound = null;

				try {
					compound = StringNbtReader.parse(VDCConfig.Defense.defenseEntityNBT);
				} catch (CommandSyntaxException ignored) {}

				compound.putString("id", VDCConfig.Defense.defenseEntityRegistryName.toString());

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
							world, world.getLocalDifficulty(pos), SpawnType.EVENT, null, null
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

		player.addChatMessage(new LiteralText(String.format(
				VDCConfig.Spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)), false);
	}
}
