package com.therandomlabs.vanilladeathchest.world.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.commons.lang3.ArrayUtils;

public class VDCSavedData extends WorldSavedData {
	public static final int TAG_COMPOUND = ArrayUtils.indexOf(NBTBase.NBT_TYPES, "COMPOUND");

	public static final String TAG_KEY = "DeathChests";
	public static final String UUID_KEY = "UUID";
	public static final String CREATION_TIME_KEY = "CreationTime";
	public static final String POS_KEY = "Pos";
	public static final String IS_DOUBLE_CHEST_KEY = "IsDoubleChest";

	private Map<BlockPos, DeathChest> deathChests = new ConcurrentHashMap<>();

	public VDCSavedData() {
		super(VanillaDeathChest.MODID);
	}

	public VDCSavedData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		deathChests.clear();

		final NBTTagList list = nbt.getTagList(TAG_KEY, TAG_COMPOUND);

		for(NBTBase tag : list) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final UUID playerID = NBTUtil.getUUIDFromTag(compound.getCompoundTag(UUID_KEY));
			final long creationTime = compound.getLong(CREATION_TIME_KEY);
			final BlockPos pos = NBTUtil.getPosFromTag(compound.getCompoundTag(POS_KEY));
			final boolean isDoubleChest = compound.getBoolean(IS_DOUBLE_CHEST_KEY);

			deathChests.put(pos, new DeathChest(playerID, creationTime, pos, isDoubleChest));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		final NBTTagList tagList = new NBTTagList();

		for(Map.Entry<BlockPos, DeathChest> entry : deathChests.entrySet()) {
			final DeathChest deathChest = entry.getValue();
			final NBTTagCompound compound = new NBTTagCompound();

			compound.setTag(UUID_KEY, NBTUtil.createUUIDTag(deathChest.getPlayerID()));
			compound.setLong(CREATION_TIME_KEY, deathChest.getCreationTime());
			compound.setTag(POS_KEY, NBTUtil.createPosTag(entry.getKey()));
			compound.setBoolean(IS_DOUBLE_CHEST_KEY, deathChest.isDoubleChest());

			tagList.appendTag(compound);
		}

		nbt.setTag(TAG_KEY, tagList);
		return nbt;
	}

	public Map<BlockPos, DeathChest> getDeathChests() {
		return deathChests;
	}

	public static VDCSavedData get(World world) {
		final MapStorage storage = world.getMapStorage();
		VDCSavedData instance =
				(VDCSavedData) storage.getOrLoadData(VDCSavedData.class, VanillaDeathChest.MODID);

		if(instance == null) {
			instance = new VDCSavedData();
			storage.setData(VanillaDeathChest.MODID, instance);
		}

		return instance;
	}
}
