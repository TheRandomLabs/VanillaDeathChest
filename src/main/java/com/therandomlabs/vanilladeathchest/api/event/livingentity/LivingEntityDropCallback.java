package com.therandomlabs.vanilladeathchest.api.event.livingentity;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;

public interface LivingEntityDropCallback {
	Event<LivingEntityDropCallback> EVENT = EventFactory.createArrayBacked(
			LivingEntityDropCallback.class,
			listeners -> (entity, defenseEntity, recentlyHit, lootingModifier, source) -> {
				for(LivingEntityDropCallback event : listeners) {
					if(!event.onLivingEntityDrop(
							entity, defenseEntity, recentlyHit, lootingModifier, source
					)) {
						return false;
					}
				}

				return true;
			}
	);

	boolean onLivingEntityDrop(MobEntity entity, DeathChestDefenseEntity defenseEntity,
			boolean recentlyHit, int lootingModifier, DamageSource source);
}
