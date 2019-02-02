package com.therandomlabs.vanilladeathchest.api.listener;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.minecraft.entity.EntityLivingBase;

public interface LivingEntityTickListener {
	void onLivingEntityTick(EntityLivingBase entity, DeathChestDefenseEntity defenseEntity);
}
