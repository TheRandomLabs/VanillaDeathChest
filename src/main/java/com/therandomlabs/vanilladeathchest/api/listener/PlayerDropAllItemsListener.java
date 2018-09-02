package com.therandomlabs.vanilladeathchest.api.listener;

import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;

public interface PlayerDropAllItemsListener {
	EnumActionResult onPlayerDropAllItems(World world, EntityPlayer player, List<EntityItem> drops);
}
