package com.therandomlabs.vanilladeathchest.deathchest;

import com.google.common.base.Objects;
import net.minecraft.util.math.BlockPos;

public final class DeathChestIdentifier {
	private final long creationTime;
	private final BlockPos pos;

	public DeathChestIdentifier(long creationTime, BlockPos pos) {
		this.creationTime = creationTime;
		this.pos = pos;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DeathChestIdentifier)) {
			return false;
		}

		final DeathChestIdentifier identifier = (DeathChestIdentifier) object;
		return creationTime == identifier.creationTime && pos.equals(identifier.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(creationTime, pos);
	}
}
