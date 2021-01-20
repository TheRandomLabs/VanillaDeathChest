package com.therandomlabs.vanilladeathchest.deathchest;

import com.google.common.base.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
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

	/**
	 * Returns the death chest creation time.
	 *
	 * @return the death chest creation time.
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns the death chest position.
	 *
	 * @return the death chest position.
	 */
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * Serializes this {@link DeathChestIdentifier} to a {@link CompoundTag}.
	 *
	 * @param tag a {@link CompoundTag}.
	 * @return the {@link CompoundTag}.
	 */
	public CompoundTag toTag(CompoundTag tag) {
		tag.putLong("CreationTime", creationTime);
		tag.put("Pos", NbtHelper.fromBlockPos(pos));
		return tag;
	}

	/**
	 * Deserializes a {@link DeathChestIdentifier} from a {@link CompoundTag}.
	 *
	 * @param tag a {@link CompoundTag}.
	 * @return the deserialized {@link DeathChestIdentifier}.
	 */
	public static DeathChestIdentifier fromTag(CompoundTag tag) {
		return new DeathChestIdentifier(
				tag.getLong("CreationTime"), NbtHelper.toBlockPos(tag.getCompound("Pos"))
		);
	}
}
