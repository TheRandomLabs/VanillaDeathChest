package com.therandomlabs.vanilladeathchest.api.event;

import java.util.List;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockEvent {
	@FunctionalInterface
	public interface Break {
		boolean breakBlock(ServerWorld world, ServerPlayerEntity player, BlockPos pos);
	}

	@FunctionalInterface
	public interface GetDrop {
		List<ItemStack> getDrop(World world, BlockPos pos, ItemStack drop);
	}

	public static final HandlerArray<Break> BREAK = new HandlerArray<>(Break.class);
	public static final HandlerArray<GetDrop> GET_DROP = new HandlerArray<>(GetDrop.class);
}
