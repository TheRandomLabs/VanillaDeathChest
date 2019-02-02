package com.therandomlabs.vanilladeathchest.listener;

import java.util.UUID;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.listener.LivingEntityDropListener;
import com.therandomlabs.vanilladeathchest.api.listener.LivingEntityExperienceDropListener;
import com.therandomlabs.vanilladeathchest.api.listener.LivingEntityTickListener;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DefenseEntityHandler implements
		LivingEntityTickListener, LivingEntityDropListener, LivingEntityExperienceDropListener {
	@Override
	public void onLivingEntityTick(EntityLivingBase entity, DeathChestDefenseEntity defenseEntity) {
		final World world = entity.getEntityWorld();

		if(world.isRemote) {
			return;
		}

		final UUID playerUUID = defenseEntity.getDeathChestPlayer();

		if(playerUUID == null) {
			return;
		}

		final EntityPlayer player = world.getPlayerEntityByUUID(playerUUID);

		if(player != null) {
			entity.setRevengeTarget(player);
		}

		if(VDCConfig.defense.defenseEntityMaxDistanceSquared == 0.0) {
			return;
		}

		final BlockPos pos = defenseEntity.getDeathChestPos();
		final BlockPos entityPos = entity.getPosition();

		final double distanceSq = entityPos.distanceSq(pos);
		final double distanceSqFromPlayer;

		if(player == null) {
			distanceSqFromPlayer = Double.MAX_VALUE;
		} else {
			distanceSqFromPlayer = entityPos.distanceSq(player.getPosition());
		}

		if(distanceSq > VDCConfig.defense.defenseEntityMaxDistanceSquared) {
			final double maxDistanceSqFromPlayer =
					VDCConfig.defense.defenseEntityMaxDistanceSquaredFromPlayer;

			if(maxDistanceSqFromPlayer == 0.0 || distanceSqFromPlayer > maxDistanceSqFromPlayer) {
				entity.setPosition(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
			}
		}
	}

	@Override
	public boolean onLivingEntityDrop(EntityLivingBase entity,
			DeathChestDefenseEntity defenseEntity, boolean recentlyHit, int lootingModifier,
			DamageSource source) {
		return VDCConfig.defense.defenseEntityDropsItems ||
				defenseEntity.getDeathChestPlayer() == null;
	}

	@Override
	public int onLivingEntityDropExperience(EntityLivingBase entity,
			DeathChestDefenseEntity defenseEntity, int experience) {
		if(!VDCConfig.defense.defenseEntityDropsExperience &&
				defenseEntity.getDeathChestPlayer() != null) {
			return 0;
		}

		return experience;
	}
}
