package com.therandomlabs.vanilladeathchest.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.event.BlockEvent;
import com.therandomlabs.vanilladeathchest.api.event.DeathChestEvent;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathChestDropHandler implements
		DeathChestEvent.Remove, BlockEvent.GetDrop, Consumer<MinecraftServer> {
	private static final List<BlockPos> justRemoved = new ArrayList<>();

	@Override
	public void onRemove(DeathChest chest, BlockPos west, BlockPos east) {
		if(west != null) {
			justRemoved.add(west);
		}

		if(east != null) {
			justRemoved.add(east);
		}
	}

	@Override
	public ItemStack getDrop(World world, BlockPos pos, ItemStack drop) {
		return !VDCConfig.misc.dropDeathChests && justRemoved.contains(pos) ?
				ItemStack.EMPTY : null;
	}

	@Override
	public void accept(MinecraftServer server) {
		justRemoved.clear();
	}
}
