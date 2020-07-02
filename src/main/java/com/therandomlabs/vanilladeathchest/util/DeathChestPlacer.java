package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.utils.forge.BooleanWrapper;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

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
		if (!alreadyCalled) {
			alreadyCalled = true;
			return false;
		}

		final ServerWorld world = this.world.get();

		if (world == null) {
			return true;
		}

		final PlayerEntity player = this.player.get();

		if (player == null) {
			return true;
		}

		place(world, player);

		//Drop any remaining items
		for (ItemEntity drop : drops) {
			world.addEntity(new ItemEntity(
					world, drop.getPosX(), drop.getPosY(), drop.getPosZ(), drop.getItem()
			));
		}

		return true;
	}

	@SuppressWarnings("Duplicates")
	private void place(ServerWorld world, PlayerEntity player) {
		final DeathChestType type = VDCConfig.Spawning.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = new BlockPos(player.getPositionVec());

		final Pattern pattern = Pattern.compile(VDCConfig.Spawning.registryNameRegex);
		final List<ItemEntity> filtered = drops.stream().
				filter(item -> pattern.matcher(
						item.getItem().getItem().getRegistryName().toString()
				).matches()).
				collect(Collectors.toList());

		boolean useDoubleChest = (type == DeathChestType.SINGLE_OR_DOUBLE ||
				type == DeathChestType.DOUBLE_SHULKER_BOX) && filtered.size() > 27;

		if (VDCConfig.Spawning.useContainerInInventory) {
			final List<ItemEntity> empty = new ArrayList<>();

			boolean foundOne = false;
			boolean foundAll = false;

			for (ItemEntity item : drops) {
				final ItemStack stack = item.getItem();

				if (type == DeathChestType.SINGLE_ONLY || type == DeathChestType.SINGLE_OR_DOUBLE) {
					if (stack.getItem() != Item.BLOCK_TO_ITEM.get(Blocks.CHEST)) {
						continue;
					}
				} else {
					if (!(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) {
						continue;
					}

					final CompoundNBT tag = stack.getTag();

					if (tag != null) {
						final NonNullList<ItemStack> inventory =
								NonNullList.withSize(27, ItemStack.EMPTY);
						ItemStackHelper.loadAllItems(tag.getCompound("BlockEntityTag"), inventory);

						//Shulker box must be empty.
						if (inventory.stream().anyMatch(itemStack -> !itemStack.isEmpty())) {
							continue;
						}
					}
				}

				if (!useDoubleChest) {
					stack.shrink(1);

					if (stack.isEmpty()) {
						empty.add(item);
					}

					foundAll = true;
					break;
				}

				if (stack.getCount() > 1) {
					stack.shrink(2);

					if (stack.isEmpty()) {
						empty.add(item);
					}

					foundAll = true;
					break;
				}

				stack.shrink(1);

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

		final BooleanWrapper doubleChest = new BooleanWrapper(useDoubleChest);

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, doubleChest);

		useDoubleChest = doubleChest.get();

		if (pos == null) {
			VanillaDeathChest.LOGGER.warn(
					"No death chest location found for player at [{}]", playerPos
			);
			return;
		}

		final Block block;

		if (type == DeathChestType.SHULKER_BOX || type == DeathChestType.DOUBLE_SHULKER_BOX) {
			block = ShulkerBoxBlock.getBlockByColor(VDCConfig.Spawning.shulkerBoxColor.get());
		} else {
			block = Blocks.CHEST;
		}

		final BlockState state = block.getDefaultState();
		final BlockPos east = pos.east();

		if (useDoubleChest) {
			if (block == Blocks.CHEST) {
				world.setBlockState(pos, state.with(ChestBlock.TYPE, ChestType.LEFT));
				world.setBlockState(east, state.with(ChestBlock.TYPE, ChestType.RIGHT));
			} else {
				world.setBlockState(pos, state);
				world.setBlockState(east, state);
			}
		} else {
			world.setBlockState(pos, state);
		}

		final TileEntity blockEntity = world.getTileEntity(pos);
		final TileEntity blockEntity2 = useDoubleChest ? world.getTileEntity(east) : null;

		if (!(blockEntity instanceof LockableLootTileEntity) ||
				(useDoubleChest && !(blockEntity2 instanceof LockableLootTileEntity))) {
			VanillaDeathChest.LOGGER.warn(
					"Failed to place death chest at [{}] due to invalid tile entity", pos
			);
			return;
		}

		LockableLootTileEntity chest =
				(LockableLootTileEntity) (useDoubleChest ? blockEntity2 : blockEntity);

		for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
			final ItemEntity item = filtered.get(0);
			chest.setInventorySlotContents(i, item.getItem());
			filtered.remove(0);
			drops.remove(item);
		}

		if (!VDCConfig.Spawning.containerDisplayName.isEmpty()) {
			chest.setCustomName(new StringTextComponent(VDCConfig.Spawning.containerDisplayName));
		}

		if (useDoubleChest) {
			chest = (LockableLootTileEntity) blockEntity;

			for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
				final ItemEntity item = filtered.get(0);
				chest.setInventorySlotContents(i, item.getItem());
				filtered.remove(0);
				drops.remove(item);
			}

			//If this is a shulker box, this has to be set separately.
			if (!VDCConfig.Spawning.containerDisplayName.isEmpty()) {
				chest.setCustomName(new StringTextComponent(VDCConfig.Spawning.containerDisplayName));
			}
		}

		if (VDCConfig.Defense.defenseEntityRegistryName != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for (int i = 0; i < VDCConfig.Defense.defenseEntitySpawnCount; i++) {
				CompoundNBT compound = null;

				try {
					compound = JsonToNBT.getTagFromJson(VDCConfig.Defense.defenseEntityNBT);
				} catch (CommandSyntaxException ignored) {}

				compound.putString(
						"id",
						VDCConfig.Defense.defenseEntityRegistryName.getRegistryName().toString()
				);

				final Entity entity = EntityType.func_220335_a(
						compound, world, spawnedEntity -> {
							spawnedEntity.setPosition(x, y, z);
							return !world.summonEntity(spawnedEntity) ? null : spawnedEntity;
						}
				);

				if (entity instanceof MobEntity) {
					final MobEntity living = (MobEntity) entity;

					living.enablePersistence();
					living.onInitialSpawn(
							world, world.getDifficultyForLocation(pos), SpawnReason.EVENT, null,
							null
					);
					living.setLastAttackedEntity(player);

					final CompoundNBT data = living.getPersistentData();

					data.put("DeathChestPlayer", NBTUtil.func_240626_a_(player.getUniqueID()));
					data.put("DeathChestPos", NBTUtil.writeBlockPos(pos));
				}
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		player.sendStatusMessage(new StringTextComponent(String.format(
				VDCConfig.Spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)), false);
	}
}
