package com.therandomlabs.vanilladeathchest.api.listener;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface GetBlockDropListener {
	IItemProvider DEFAULT = null;

	IItemProvider getBlockDrop(IBlockState state, World world, BlockPos pos, int fortune);
}
