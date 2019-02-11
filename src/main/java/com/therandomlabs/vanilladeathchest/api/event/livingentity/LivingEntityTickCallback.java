package com.therandomlabs.vanilladeathchest.api.event.livingentity;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.mob.MobEntity;

public interface LivingEntityTickCallback {
	Event<LivingEntityTickCallback> EVENT = EventFactory.createArrayBacked(
			LivingEntityTickCallback.class,
			listeners -> (entity, defenseEntity) -> {
				for(LivingEntityTickCallback event : listeners) {
					event.onLivingEntityTick(entity, defenseEntity);
				}
			}
	);

	void onLivingEntityTick(MobEntity entity, DeathChestDefenseEntity defenseEntity);
}
