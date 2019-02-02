package com.therandomlabs.vanilladeathchest.api.listener;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public interface LivingEntityDropListener {
	boolean onLivingEntityDrop(EntityLivingBase entity, DeathChestDefenseEntity defenseEntity,
			boolean recentlyHit, int lootingModifier, DamageSource source);
}
