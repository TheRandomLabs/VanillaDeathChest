package com.therandomlabs.vanilladeathchest.api.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface RightClickBlockListener {
	EnumActionResult onRightClickBlock(World world, EntityPlayer player,
			ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ);
}
