package com.therandomlabs.vanilladeathchest.api.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockHarvestListener {
	EnumActionResult onBlockHarvest(World world, EntityPlayer player, BlockPos pos);
}
