/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 TheRandomLabs
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

package com.therandomlabs.vanilladeathchest.handler;

import java.util.UUID;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DefenseEntityHandler implements
		LivingEntityTickCallback, LivingEntityDropCallback, LivingEntityDropExperienceCallback {
	@Override
	public void onLivingEntityTick(MobEntity entity, DeathChestDefenseEntity defenseEntity) {
		final World world = entity.getEntityWorld();

		if(world.isClient) {
			return;
		}

		final UUID playerUUID = defenseEntity.getDeathChestPlayer();

		if(playerUUID == null) {
			return;
		}

		final PlayerEntity player = world.getPlayerByUuid(playerUUID);

		if(player != null) {
			entity.setTarget(player);
		}

		final VDCConfig.Defense config = VanillaDeathChest.config().defense;

		if(config.defenseEntityMaxDistanceSquared == 0.0) {
			return;
		}

		final BlockPos pos = defenseEntity.getDeathChestPos();
		final Vec3d entityPos = entity.getPos();

		final double distanceSq = entityPos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
		final double distanceSqFromPlayer;

		if(player == null) {
			distanceSqFromPlayer = Double.MAX_VALUE;
		} else {
			distanceSqFromPlayer = entityPos.squaredDistanceTo(player.getPos());
		}

		if(distanceSq > config.defenseEntityMaxDistanceSquared) {
			final double maxDistanceSqFromPlayer = config.defenseEntityMaxDistanceSquaredFromPlayer;

			if(maxDistanceSqFromPlayer == 0.0 || distanceSqFromPlayer > maxDistanceSqFromPlayer) {
				entity.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
			}
		}
	}

	@Override
	public boolean onLivingEntityDrop(
			MobEntity entity, DeathChestDefenseEntity defenseEntity, boolean recentlyHit,
			int lootingModifier, DamageSource source
	) {
		return VanillaDeathChest.config().defense.defenseEntityDropsItems ||
				defenseEntity.getDeathChestPlayer() == null;
	}

	@Override
	public int onLivingEntityDropExperience(
			MobEntity entity, DeathChestDefenseEntity defenseEntity, int experience
	) {
		if(!VanillaDeathChest.config().defense.defenseEntityDropsExperience &&
				defenseEntity.getDeathChestPlayer() != null) {
			return 0;
		}

		return experience;
	}
}
