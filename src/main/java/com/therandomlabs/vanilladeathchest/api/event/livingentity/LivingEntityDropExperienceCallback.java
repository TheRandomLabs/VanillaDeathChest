package com.therandomlabs.vanilladeathchest.api.event.livingentity;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.mob.MobEntity;

public interface LivingEntityDropExperienceCallback {
	Event<LivingEntityDropExperienceCallback> EVENT = EventFactory.createArrayBacked(
			LivingEntityDropExperienceCallback.class,
			listeners -> (entity, defenseEntity, experience) -> {
				for(LivingEntityDropExperienceCallback event : listeners) {
					experience = event.onLivingEntityDropExperience(
							entity, defenseEntity, experience
					);
				}

				return experience;
			}
	);

	int onLivingEntityDropExperience(MobEntity entity, DeathChestDefenseEntity defenseEntity,
			int experience);
}
