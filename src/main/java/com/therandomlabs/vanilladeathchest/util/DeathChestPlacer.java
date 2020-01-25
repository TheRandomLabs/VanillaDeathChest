package com.therandomlabs.vanilladeathchest.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.utils.fabric.BooleanWrapper;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

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
			world.spawnEntity(new ItemEntity(world, drop.getX(), drop.getY(), drop.getZ(), drop.getStack()));
		}

		return true;
	}

	private void place(ServerWorld world, PlayerEntity player) {
		final DeathChestType type = VDCConfig.Spawning.chestType;

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
			//TODO ColorConfig.get()
			block = ShulkerBoxBlock.get(DyeColor.valueOf(
					VDCConfig.Spawning.shulkerBoxColor.name()
			));
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

		if(!VDCConfig.Spawning.containerDisplayName.isEmpty()) {
			chest.setCustomName(new LiteralText(VDCConfig.Spawning.containerDisplayName));
		}

		if(VDCConfig.Defense.defenseEntity != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for(int i = 0; i < VDCConfig.Defense.defenseEntitySpawnCount; i++) {
				CompoundTag compound = null;

				try {
					compound = StringNbtReader.parse(VDCConfig.Defense.defenseEntityNBT);
				} catch(CommandSyntaxException ignored) {}

				compound.putString("id", VDCConfig.Defense.defenseEntityRegistryName);

				final Entity entity = EntityType.loadEntityWithPassengers(
						compound, world, spawnedEntity -> {
							spawnedEntity.setPos(x, y, z);
							return !world.tryLoadEntity(spawnedEntity) ? null : spawnedEntity;
						}
				);

				if(entity instanceof MobEntity) {
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

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		player.addChatMessage(new LiteralText(String.format(
				VDCConfig.Spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)), false);
	}
}
