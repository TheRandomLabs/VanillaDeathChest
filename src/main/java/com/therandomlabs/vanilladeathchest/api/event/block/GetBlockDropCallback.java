package com.therandomlabs.vanilladeathchest.api.event.block;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface GetBlockDropCallback {
	Event<GetBlockDropCallback> EVENT = EventFactory.createArrayBacked(
			GetBlockDropCallback.class,
			listeners -> (world, pos, drop) -> {
				for(GetBlockDropCallback event : listeners) {
					final List<ItemStack> drops = event.getDrop(world, pos, drop);

					if(drops != null) {
						return drops;
					}
				}

				return null;
			}
	);

	List<ItemStack> getDrop(World world, BlockPos pos, ItemStack drop);
}
