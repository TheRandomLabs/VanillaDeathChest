package com.therandomlabs.vanilladeathchest.api.event;

import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockEvent {
	@FunctionalInterface
	public interface GetDrop {
		ItemStack getDrop(World world, BlockPos pos, ItemStack drop);
	}

	public static final HandlerArray<GetDrop> GET_DROP = new HandlerArray<>(GetDrop.class);
}
