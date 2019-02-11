package com.therandomlabs.vanilladeathchest.api.event.deathchest;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.math.BlockPos;

public interface DeathChestRemoveCallback {
	Event<DeathChestRemoveCallback> EVENT = EventFactory.createArrayBacked(
			DeathChestRemoveCallback.class,
			listeners -> (chest, west, east) -> {
				for(DeathChestRemoveCallback event : listeners) {
					event.onRemove(chest, west, east);
				}
			}
	);

	void onRemove(DeathChest chest, BlockPos west, BlockPos east);
}
