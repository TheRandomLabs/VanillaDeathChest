package com.therandomlabs.vanilladeathchest.api.event;

import java.util.List;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class PlayerEvent {
	@FunctionalInterface
	public interface DropAllItems {
		boolean onPlayerDropAllItems(ServerWorld world, PlayerEntity player,
				List<ItemEntity> drops);
	}

	public static final HandlerArray<DropAllItems> DROP_ALL_ITEMS =
			new HandlerArray<>(DropAllItems.class);
}
