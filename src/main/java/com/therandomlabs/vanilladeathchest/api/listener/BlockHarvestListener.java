package com.therandomlabs.vanilladeathchest.api.listener;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockHarvestListener {
	EnumActionResult onBlockHarvest(World world, EntityPlayerMP player, BlockPos pos);
}
