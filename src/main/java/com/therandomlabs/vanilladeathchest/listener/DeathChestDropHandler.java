package com.therandomlabs.vanilladeathchest.listener;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.listener.DeathChestRemoveListener;
import com.therandomlabs.vanilladeathchest.api.listener.GetBlockDropListener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dimdev.rift.listener.ServerTickable;

public class DeathChestDropHandler implements
		DeathChestRemoveListener, GetBlockDropListener, ServerTickable {
	private static final List<BlockPos> justRemoved = new ArrayList<>();

	@Override
	public void onDeathChestRemove(DeathChest chest, BlockPos west, @Nullable BlockPos east) {
		justRemoved.add(west);

		if(east != null) {
			justRemoved.add(east);
		}
	}

	@Override
	public IItemProvider getBlockDrop(IBlockState state, World world, BlockPos pos, int fortune) {
		return justRemoved.contains(pos) ? Items.AIR : DEFAULT;
	}

	@Override
	public void serverTick(MinecraftServer server) {
		justRemoved.clear();
	}
}
