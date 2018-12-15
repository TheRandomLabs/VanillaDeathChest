package com.therandomlabs.vanilladeathchest.api.event;

import net.fabricmc.fabric.util.HandlerList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockEvent {
	@FunctionalInterface
	public interface GetDrop {
		ItemStack getDrop(World world, BlockPos pos, ItemStack drop);
	}

	public static final HandlerList<GetDrop> GET_DROP = new HandlerList<>(GetDrop.class);
}
