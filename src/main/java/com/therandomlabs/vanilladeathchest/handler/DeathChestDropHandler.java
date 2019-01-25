package com.therandomlabs.vanilladeathchest.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.event.BlockEvent;
import com.therandomlabs.vanilladeathchest.api.event.DeathChestEvent;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.InventoryUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathChestDropHandler
		implements DeathChestEvent.Remove, BlockEvent.GetDrop, Consumer<MinecraftServer> {
	private static final Set<BlockPos> justRemoved = new HashSet<>();

	@Override
	public void onRemove(DeathChest chest, BlockPos west, BlockPos east) {
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

		if(Block.getBlockFromItem(drop.getItem()) instanceof ShulkerBoxBlock) {
			final DefaultedList<ItemStack> inventory = DefaultedList.create(27, ItemStack.EMPTY);
			InventoryUtil.deserialize(drop.getTag().getCompound("BlockEntityTag"), inventory);
			return inventory;
		}

		return Collections.emptyList();
	}

	@Override
	public void accept(MinecraftServer server) {
		justRemoved.clear();
	}
}
