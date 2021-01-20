package com.therandomlabs.vanilladeathchest.deathchest;

import com.google.common.base.Objects;
import net.minecraft.util.math.BlockPos;

/**
 * A unique death chest identifier.
 */
public final class DeathChestIdentifier {
	private final long creationTime;
	private final BlockPos pos;

	/**
	 * Constructs a {@link DeathChestIdentifier}.
	 *
	 * @param creationTime the creation time.
	 * @param pos the position.
	 */
	public DeathChestIdentifier(long creationTime, BlockPos pos) {
		this.creationTime = creationTime;
		this.pos = pos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(creationTime, pos);
	}

	/**
	 * {@inheritDoc}
	 */
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
}
