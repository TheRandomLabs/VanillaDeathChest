package com.therandomlabs.vanilladeathchest.world.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.class_37;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.PersistedState;
import net.minecraft.nbt.Tag;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.ArrayUtils;

public class VDCSavedData extends PersistedState {
	public static final int TAG_COMPOUND = ArrayUtils.indexOf(Tag.TYPES, "COMPOUND");

	public static final String TAG_KEY = "DeathChests";
	public static final String UUID_KEY = "UUID";
	public static final String CREATION_TIME_KEY = "CreationTime";
	public static final String POS_KEY = "Pos";
	public static final String IS_DOUBLE_CHEST_KEY = "IsDoubleChest";

	private Map<BlockPos, DeathChest> deathChests = new ConcurrentHashMap<>();

	public VDCSavedData() {
		super(VanillaDeathChest.MOD_ID);
	}

	public VDCSavedData(String name) {
		super(name);
	}

	@Override
	public void deserialize(CompoundTag nbt) {
		deathChests.clear();

		final ListTag list = nbt.getList(TAG_KEY, TAG_COMPOUND);

		for(Tag tag : list) {
			final CompoundTag compound = (CompoundTag) tag;

			final UUID playerID = TagHelper.deserializeUuid(compound.getCompound(UUID_KEY));
			final long creationTime = compound.getLong(CREATION_TIME_KEY);
			final BlockPos pos = TagHelper.deserializeBlockPos(compound.getCompound(POS_KEY));
			final boolean isDoubleChest = compound.getBoolean(IS_DOUBLE_CHEST_KEY);

			deathChests.put(pos, new DeathChest(playerID, creationTime, pos, isDoubleChest));
		}
	}

	@Override
	public CompoundTag serialize(CompoundTag nbt) {
		final ListTag tagList = new ListTag();

		for(Map.Entry<BlockPos, DeathChest> entry : deathChests.entrySet()) {
			final DeathChest deathChest = entry.getValue();
			final CompoundTag compound = new CompoundTag();

			compound.put(UUID_KEY, TagHelper.serializeUuid(deathChest.getPlayerID()));
			compound.putLong(CREATION_TIME_KEY, deathChest.getCreationTime());
			compound.put(POS_KEY, TagHelper.serializeBlockPos(entry.getKey()));
			compound.putBoolean(IS_DOUBLE_CHEST_KEY, deathChest.isDoubleChest());

			tagList.add(compound);
		}

		nbt.put(TAG_KEY, tagList);
		return nbt;
	}

	public Map<BlockPos, DeathChest> getDeathChests() {
		return deathChests;
	}

	public static VDCSavedData get(World world) {
		final class_37 storage = world.method_8646();
		final DimensionType dimensionType = world.getDimension().getType();

		VDCSavedData instance =
				storage.method_268(dimensionType, VDCSavedData::new, VanillaDeathChest.MOD_ID);

		if(instance == null) {
			instance = new VDCSavedData();
			storage.method_267(dimensionType, VanillaDeathChest.MOD_ID, instance);
		}

		return instance;
	}
}
