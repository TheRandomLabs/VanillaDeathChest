/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.vanilladeathchest.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.deathchest.DeathChestRemoveCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DeathChestDropHandler implements
		DeathChestRemoveCallback, GetBlockDropCallback, ServerTickEvents.EndTick {
	private static final Set<BlockPos> justRemoved = new HashSet<>();

	@Override
	public void onRemove(DeathChest chest, BlockPos west, @Nullable BlockPos east) {
		if (VanillaDeathChest.config().misc.dropDeathChests) {
			return;
		}

		if (west != null) {
			justRemoved.add(west);
		}

		if (east != null) {
			justRemoved.add(east);
		}
	}

	@Nullable
	@Override
	public List<ItemStack> getDrop(World world, BlockPos pos, ItemStack drop) {
		if (!justRemoved.contains(pos)) {
			return null;
		}

		justRemoved.remove(pos);

		if (Block.getBlockFromItem(drop.getItem()) instanceof ShulkerBoxBlock) {
			final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
			Inventories.fromTag(drop.getTag().getCompound("BlockEntityTag"), inventory);
			return inventory;
		}

		return Collections.emptyList();
	}

	@Override
	public void onEndTick(MinecraftServer server) {
		justRemoved.clear();
	}
}
