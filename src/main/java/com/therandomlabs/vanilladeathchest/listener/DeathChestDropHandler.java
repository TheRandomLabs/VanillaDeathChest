package com.therandomlabs.vanilladeathchest.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.listener.DeathChestRemoveListener;
import com.therandomlabs.vanilladeathchest.api.listener.GetBlockDropListener;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dimdev.rift.listener.ServerTickable;

public class DeathChestDropHandler implements
		DeathChestRemoveListener, GetBlockDropListener, ServerTickable {
	private static final Set<BlockPos> justRemoved = new HashSet<>();

	@Override
	public void onDeathChestRemove(DeathChest chest, BlockPos west, BlockPos east) {
		if(VDCConfig.misc.dropDeathChests) {
			return;
		}

		if(west != null) {
			justRemoved.add(west);
		}

		if(east != null) {
			justRemoved.add(east);
		}
	}

	@Override
	public List<ItemStack> getDrop(World world, BlockPos pos, ItemStack drop) {
		if(!justRemoved.contains(pos)) {
			return null;
		}

		justRemoved.remove(pos);

		if(Block.getBlockFromItem(drop.getItem()) instanceof BlockShulkerBox) {
			final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(
					drop.getTagCompound().getCompoundTag("BlockEntityTag"), inventory
			);
			return inventory;
		}

		return Collections.emptyList();
	}

	@Override
	public void serverTick(MinecraftServer server) {
		justRemoved.clear();
	}
}
