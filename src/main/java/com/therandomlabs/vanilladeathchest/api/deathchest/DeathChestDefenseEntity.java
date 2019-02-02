package com.therandomlabs.vanilladeathchest.api.deathchest;

import java.util.UUID;
import net.minecraft.util.math.BlockPos;

public interface DeathChestDefenseEntity {
	UUID getDeathChestPlayer();

	void setDeathChestPlayer(UUID uuid);

	BlockPos getDeathChestPos();

	void setDeathChestPos(BlockPos pos);
}
