package com.therandomlabs.vanilladeathchest.api.event;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.fabricmc.fabric.util.HandlerList;
import net.minecraft.util.math.BlockPos;

public final class DeathChestEvent {
	@FunctionalInterface
	public interface Remove {
		void onRemove(DeathChest chest, BlockPos west, BlockPos east);
	}

	public static final HandlerList<Remove> REMOVE = new HandlerList<>(Remove.class);
}
