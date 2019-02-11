package com.therandomlabs.vanilladeathchest.api.event.block;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface BreakBlockCallback {
	Event<BreakBlockCallback> EVENT = EventFactory.createArrayBacked(
			BreakBlockCallback.class,
			listeners -> (world, player, pos) -> {
				for(BreakBlockCallback event : listeners) {
					if(!event.breakBlock(world, player, pos)) {
						return false;
					}
				}

				return true;
			}
	);

	boolean breakBlock(ServerWorld world, ServerPlayerEntity player, BlockPos pos);
}
