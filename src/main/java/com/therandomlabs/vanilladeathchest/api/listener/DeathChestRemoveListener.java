package com.therandomlabs.vanilladeathchest.api.listener;

import javax.annotation.Nullable;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.util.math.BlockPos;

public interface DeathChestRemoveListener {
	void onDeathChestRemove(DeathChest chest, BlockPos west, @Nullable BlockPos east);
}
