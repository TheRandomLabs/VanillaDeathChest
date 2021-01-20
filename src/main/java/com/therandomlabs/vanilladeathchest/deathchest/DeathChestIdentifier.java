/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TheRandomLabs
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
