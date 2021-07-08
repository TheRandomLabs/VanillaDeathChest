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

package com.therandomlabs.vanilladeathchest.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.config.DefenseEntities;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.util.DropsList;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(value = LivingEntity.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityMixin implements DropsList, DeathChestDefenseEntity {
	@Unique
	private final List<ItemEntity> drops = new ArrayList<>();

	@Unique
	private PlayerInventory inventory;

	@Unique
	private DeathChest deathChest;

	@Unique
	private UUID deathChestPlayerUUID;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ItemEntity> getDrops() {
		return drops;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDeathChest(DeathChest deathChest) {
		this.deathChest = deathChest;
		deathChestPlayerUUID = deathChest.getPlayerUUID();
	}

	@Inject(method = "drop", at = @At("HEAD"))
	public void dropHead(CallbackInfo info) {
		if ((Object) this instanceof PlayerEntity) {
			drops.clear();
			//We can't pass in null here because Campanion mixins into setStack and needs the
			//player.
			inventory = new PlayerInventory((PlayerEntity) (Object) this);
			final PlayerInventory oldInventory = ((PlayerEntity) (Object) this).getInventory();

			for (int i = 0; i < oldInventory.size(); i++) {
				inventory.setStack(i, oldInventory.getStack(i).copy());
			}
		}
	}

	@Inject(method = "drop", at = @At("TAIL"))
	public void dropTail(CallbackInfo info) {
		if (drops.isEmpty()) {
			return;
		}

		final LivingEntity entity = (LivingEntity) (Object) this;
		final ServerWorld world = (ServerWorld) entity.getEntityWorld();

		if ((VanillaDeathChest.SPAWN_DEATH_CHESTS != null &&
				!world.getGameRules().getBoolean(VanillaDeathChest.SPAWN_DEATH_CHESTS)) ||
				!VanillaDeathChest.getConfig().spawning.isDimensionEnabled(world)) {
			return;
		}

		drops.forEach(drop -> drop.remove(Entity.RemovalReason.DISCARDED));
		final DeathChestsState deathChestsState = DeathChestsState.get(world);
		final BlockPos pos = entity.getBlockPos();
		final DeathChest deathChest = new DeathChest(
				UUID.randomUUID(), world, entity.getUuid(), drops, inventory, world.getTime(), pos,
				false, true
		);
		deathChestsState.getQueuedDeathChests().add(deathChest);
		deathChestsState.markDirty();

		VanillaDeathChest.logger.info(
				"Death chest for {} queued at [{}, {}, {}] with identifier {}",
				((PlayerEntity) (Object) this).getGameProfile().getName(),
				pos.getX(), pos.getY(), pos.getZ(), deathChest.getIdentifier()
		);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo info) {
		if (deathChestPlayerUUID == null) {
			return;
		}

		final LivingEntity entity = (LivingEntity) (Object) this;
		final PlayerEntity player = entity.getEntityWorld().getPlayerByUuid(deathChestPlayerUUID);

		if ((Object) this instanceof MobEntity) {
			final MobEntity mobEntity = (MobEntity) (Object) this;
			mobEntity.setPersistent();

			if (player != null) {
				mobEntity.setAttacker(player);
				mobEntity.setTarget(player);
			}
		}

		if (player != null && this instanceof Angerable) {
			final Angerable angerable = (Angerable) this;
			angerable.setTarget(player);
			angerable.setAngerTime(Integer.MAX_VALUE);
		}

		final DefenseEntities config = VanillaDeathChest.getConfig().defenseEntities;

		if (config.maxSquaredDistanceFromChest == 0.0) {
			return;
		}

		if (deathChest != null && !deathChest.exists()) {
			deathChest = null;
		}

		if (deathChest == null) {
			return;
		}

		final BlockPos pos = deathChest.getPos();
		final double squaredDistanceFromChest = pos.getSquaredDistance(entity.getPos(), true);

		if (squaredDistanceFromChest > config.maxSquaredDistanceFromChest) {
			final double squaredDistanceFromPlayer = player == null ?
					Double.MAX_VALUE : entity.getPos().squaredDistanceTo(player.getPos());

			if (config.maxSquaredDistanceFromPlayer == 0.0 ||
					squaredDistanceFromPlayer > config.maxSquaredDistanceFromPlayer) {
				entity.refreshPositionAndAngles(
						pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
						entity.getYaw(), entity.getPitch()
				);
			}
		}
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
	public void writeCustomDataToNbt(NbtCompound tag, CallbackInfo info) {
		if (deathChestPlayerUUID != null) {
			if (deathChest != null) {
				tag.put(
						"DeathChestIdentifier", NbtHelper.fromUuid(deathChest.getIdentifier())
				);
			}

			tag.put("DeathChestPlayer", NbtHelper.fromUuid(deathChestPlayerUUID));
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
	public void readCustomDataFromNbt(NbtCompound tag, CallbackInfo info) {
		if (tag.contains("DeathChestPlayer")) {
			deathChestPlayerUUID = NbtHelper.toUuid(tag.get("DeathChestPlayer"));

			if (tag.contains("DeathChestIdentifier")) {
				final DeathChestsState deathChestsState = DeathChestsState.get(
						(ServerWorld) ((LivingEntity) (Object) this).getEntityWorld()
				);
				deathChest = deathChestsState.getDeathChest(
						NbtHelper.toUuid(tag.getCompound("DeathChestIdentifier"))
				);
			}
		}
	}

	@Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
	public void dropLoot(DamageSource source, boolean recentlyHit, CallbackInfo info) {
		if (deathChestPlayerUUID != null &&
				!VanillaDeathChest.getConfig().defenseEntities.dropItems) {
			info.cancel();
		}
	}

	@Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
	public void dropEquipment(
			DamageSource source, int lootingModifier, boolean recentlyHit, CallbackInfo info
	) {
		if (deathChestPlayerUUID != null &&
				!VanillaDeathChest.getConfig().defenseEntities.dropItems) {
			info.cancel();
		}
	}

	@Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
	public void dropXp(CallbackInfo info) {
		if (deathChestPlayerUUID != null &&
				!VanillaDeathChest.getConfig().defenseEntities.dropExperience) {
			info.cancel();
		}
	}
}
