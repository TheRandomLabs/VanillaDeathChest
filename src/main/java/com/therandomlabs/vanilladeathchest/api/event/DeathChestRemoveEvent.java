package com.therandomlabs.vanilladeathchest.api.event;

import javax.annotation.Nullable;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

public class DeathChestRemoveEvent extends Event {
	private final DeathChest chest;
	private final BlockPos west;
	private final BlockPos east;

	public DeathChestRemoveEvent(DeathChest chest, BlockPos west, BlockPos east) {
		this.chest = chest;
		this.west = west;
		this.east = east;
	}

	public DeathChest getChest() {
		return chest;
	}

	public BlockPos getWest() {
		return west;
	}

	@Nullable
	public BlockPos getEast() {
		return east;
	}
}
