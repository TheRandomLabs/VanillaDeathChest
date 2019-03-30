package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.IAngerable;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
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
import net.minecraft.entity.mob.ZombiePigmanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sortme.JsonLikeTagParser;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class DeathChestPlacer {
	public enum DeathChestType {
		SINGLE_ONLY,
		SINGLE_OR_DOUBLE,
		SHULKER_BOX,
		RANDOM_SHULKER_BOX_COLOR
	}

	private static final Random random = new Random();

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

		if(player == null) {
			return true;
		}

		place(world, player);

		//Drop any remaining items
		for(ItemEntity drop : drops) {
			world.spawnEntity(drop);
		}

		return true;
	}

	private void place(ServerWorld world, PlayerEntity player) {
		final DeathChestType type = VDCConfig.spawning.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = new BlockPos(player.getPos());

		boolean useDoubleChest =
				type == DeathChestType.SINGLE_OR_DOUBLE && drops.size() > 27;

		final BooleanWrapper doubleChest = new BooleanWrapper(useDoubleChest);

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, doubleChest);

		useDoubleChest = doubleChest.get();

		if(pos == null) {
			VanillaDeathChest.LOGGER.warn(
					"No death chest location found for player at [{}]", playerPos
			);
			return;
		}

		final Block block;

		if(type == DeathChestType.SHULKER_BOX) {
			block = ShulkerBoxBlock.get(VDCConfig.spawning.shulkerBoxColor);
		} else if(type == DeathChestType.RANDOM_SHULKER_BOX_COLOR) {
			block = ShulkerBoxBlock.get(DyeColor.byId(random.nextInt(16)));
		} else {
			block = Blocks.CHEST;
		}

		final BlockState state = block.getDefaultState();
		final BlockPos east = pos.east();

		if(useDoubleChest) {
			world.setBlockState(pos, state.with(ChestBlock.CHEST_TYPE, ChestType.LEFT));
			world.setBlockState(east, state.with(ChestBlock.CHEST_TYPE, ChestType.RIGHT));
		} else {
			world.setBlockState(pos, state);
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);
		final BlockEntity blockEntity2 = useDoubleChest ? world.getBlockEntity(east) : null;

		if(!(blockEntity instanceof LootableContainerBlockEntity) ||
				(useDoubleChest && !(blockEntity2 instanceof LootableContainerBlockEntity))) {
			VanillaDeathChest.LOGGER.warn(
					"Failed to place death chest at [{}] due to invalid block entity", pos
			);
			return;
		}

		LootableContainerBlockEntity chest =
				(LootableContainerBlockEntity) (useDoubleChest ? blockEntity2 : blockEntity);

		for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
			chest.setInvStack(i, drops.get(0).getStack());
			drops.remove(0);
		}

		if(useDoubleChest) {
			chest = (LootableContainerBlockEntity) blockEntity;

			for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
				chest.setInvStack(i, drops.get(0).getStack());
				drops.remove(0);
			}
		}

		if(VDCConfig.defense.defenseEntity != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for(int i = 0; i < VDCConfig.defense.defenseEntitySpawnCount; i++) {
				CompoundTag compound = null;

				try {
					compound = new JsonLikeTagParser(
							new StringReader(VDCConfig.defense.defenseEntityNBT)
					).parseCompoundTag();
				} catch(CommandSyntaxException ignored) {}

				compound.putString("id", VDCConfig.defense.defenseEntityRegistryName);

				final Entity entity = EntityType.loadEntityWithPassengers(
						compound, world, spawnedEntity -> {
							spawnedEntity.setPosition(x, y, z);
							return !world.method_18768(spawnedEntity) ? null : spawnedEntity;
						}
				);

				if(entity instanceof MobEntity) {
					final MobEntity living = (MobEntity) entity;

					living.setPersistent();
					living.prepareEntityData(
							world, world.getLocalDifficulty(pos), SpawnType.EVENT, null, null
					);

					if(living instanceof ZombiePigmanEntity) {
						((IAngerable) living).makeAngryAt(player);
					}

					final DeathChestDefenseEntity defenseEntity = (DeathChestDefenseEntity) living;

					defenseEntity.setDeathChestPlayer(player.getUuid());
					defenseEntity.setDeathChestPos(pos);
				}
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		player.addChatMessage(new StringTextComponent(String.format(
				VDCConfig.spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)), false);
	}
}
