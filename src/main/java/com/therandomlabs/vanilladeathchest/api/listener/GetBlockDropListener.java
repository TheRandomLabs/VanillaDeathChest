package com.therandomlabs.vanilladeathchest.api.listener;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface GetBlockDropListener {
	List<ItemStack> getDrop(World world, BlockPos pos, ItemStack stack);
}
