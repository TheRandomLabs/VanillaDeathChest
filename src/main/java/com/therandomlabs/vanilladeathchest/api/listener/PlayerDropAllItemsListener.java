package com.therandomlabs.vanilladeathchest.api.listener;

import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface PlayerDropAllItemsListener {
	boolean onPlayerDropAllItems(World world, EntityPlayer player, List<EntityItem> drops);
}
