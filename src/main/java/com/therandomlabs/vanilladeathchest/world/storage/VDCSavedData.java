/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2019 TheRandomLabs
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

package com.therandomlabs.vanilladeathchest.world.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.checkerframework.checker.nullness.qual.Nullable;

public class VDCSavedData extends PersistentState {
	public static final int TAG_COMPOUND = new CompoundTag().getType();

	public static final String ID = VanillaDeathChest.MOD_ID;

	public static final String DEATH_CHESTS_KEY = "DeathChests";

	public static final String UUID_KEY = "UUID";
	public static final String CREATION_TIME_KEY = "CreationTime";
	public static final String POS_KEY = "Pos";
	public static final String IS_DOUBLE_CHEST_KEY = "IsDoubleChest";
	public static final String UNLOCKED_KEY = "Unlocked";

	@Nullable
	private static ServerWorld currentWorld;

	private final ServerWorld world;
	private final Map<BlockPos, DeathChest> deathChests = new ConcurrentHashMap<>();

	public VDCSavedData() {
		this(ID);
	}

	public VDCSavedData(String name) {
		super(name);

		if (currentWorld == null) {
			throw new NullPointerException("currentWorld should not be null");
		}

		world = currentWorld;
	}

	@Override
	public void fromTag(CompoundTag nbt) {
		deathChests.clear();

		final ListTag list = nbt.getList(DEATH_CHESTS_KEY, TAG_COMPOUND);

		for(Tag tag : list) {
			final CompoundTag compound = (CompoundTag) tag;

			final UUID playerID = NbtHelper.toUuid(compound.getCompound(UUID_KEY));
			final long creationTime = compound.getLong(CREATION_TIME_KEY);
			final BlockPos pos = NbtHelper.toBlockPos(compound.getCompound(POS_KEY));
			final boolean isDoubleChest = compound.getBoolean(IS_DOUBLE_CHEST_KEY);
			final boolean unlocked = compound.getBoolean(UNLOCKED_KEY);

			deathChests.put(pos, new DeathChest(
					world, playerID, creationTime, pos, isDoubleChest, unlocked
			));
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag nbt) {
		final ListTag tagList = new ListTag();

		for(Map.Entry<BlockPos, DeathChest> entry : deathChests.entrySet()) {
			final DeathChest deathChest = entry.getValue();
			final CompoundTag compound = new CompoundTag();

			compound.put(UUID_KEY, NbtHelper.fromUuid(deathChest.getPlayerID()));
			compound.putLong(CREATION_TIME_KEY, deathChest.getCreationTime());
			compound.put(POS_KEY, NbtHelper.fromBlockPos(entry.getKey()));
			compound.putBoolean(IS_DOUBLE_CHEST_KEY, deathChest.isDoubleChest());
			compound.putBoolean(UNLOCKED_KEY, deathChest.isUnlocked());

			tagList.add(compound);
		}

		nbt.put(DEATH_CHESTS_KEY, tagList);
		return nbt;
	}

	public Map<BlockPos, DeathChest> getDeathChests() {
		return deathChests;
	}

	public static VDCSavedData get(ServerWorld world) {
		currentWorld = world;

		final VDCSavedData instance =
				world.getPersistentStateManager().getOrCreate(VDCSavedData::new, ID);

		currentWorld = null;

		return instance;
	}
}
