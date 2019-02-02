package com.therandomlabs.vanilladeathchest.api.listener;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.minecraft.entity.EntityLivingBase;

public interface LivingEntityExperienceDropListener {
	int onLivingEntityDropExperience(EntityLivingBase entity, DeathChestDefenseEntity defenseEntity,
			int experience);
}
