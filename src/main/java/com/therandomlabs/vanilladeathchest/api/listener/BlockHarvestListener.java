package com.therandomlabs.vanilladeathchest.api.listener;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockHarvestListener {
	boolean onBlockHarvest(World world, EntityPlayerMP player, BlockPos pos);
}
