package com.therandomlabs.vanilladeathchest.api.event.player;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface PlayerDropAllItemsCallback {
	Event<PlayerDropAllItemsCallback> EVENT = EventFactory.createArrayBacked(
			PlayerDropAllItemsCallback.class,
			listeners -> (world, player, drops) -> {
				for(PlayerDropAllItemsCallback event : listeners) {
					event.onPlayerDropAllItems(world, player, drops);
				}
			}
	);

	void onPlayerDropAllItems(ServerWorld world, PlayerEntity player, List<ItemEntity> drops);
}
