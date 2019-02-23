package com.therandomlabs.vanilladeathchest.api.event.block;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.explosion.Explosion;

public interface ExplosionDetonationCallback {
	Event<ExplosionDetonationCallback> EVENT = EventFactory.createArrayBacked(
			ExplosionDetonationCallback.class,
			listeners -> (world, explosion) -> {
				for(ExplosionDetonationCallback event : listeners) {
					event.onExplosionDetonate(world, explosion);
				}
			}
	);

	void onExplosionDetonate(ServerWorld world, Explosion explosion);
}
