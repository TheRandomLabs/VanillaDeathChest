package com.therandomlabs.vanilladeathchest.api.event;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.util.math.BlockPos;

public final class DeathChestEvent {
	@FunctionalInterface
	public interface Remove {
		void onRemove(DeathChest chest, BlockPos west, BlockPos east);
	}

	public static final HandlerArray<Remove> REMOVE = new HandlerArray<>(Remove.class);
}
