package com.therandomlabs.vanilladeathchest.world.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.WorldSavedDataStorage;
import org.apache.commons.lang3.ArrayUtils;

public class VDCSavedData extends WorldSavedData {
	public static final int TAG_COMPOUND = ArrayUtils.indexOf(INBTBase.NBT_TYPES, "COMPOUND");

	public static final String ID = VanillaDeathChest.MOD_ID;

	public static final String DEATH_CHESTS_KEY = "DeathChests";

	public static final String UUID_KEY = "UUID";
	public static final String CREATION_TIME_KEY = "CreationTime";
	public static final String POS_KEY = "Pos";
	public static final String IS_DOUBLE_CHEST_KEY = "IsDoubleChest";
	public static final String UNLOCKED_KEY = "Unlocked";

	private static World currentWorld;

	private final World world;
	private Map<BlockPos, DeathChest> deathChests = new ConcurrentHashMap<>();

	public VDCSavedData() {
		this(ID);
	}

	public VDCSavedData(String name) {
		super(name);
		world = currentWorld;
	}

	@Override
	public void read(NBTTagCompound nbt) {
		deathChests.clear();

		final NBTTagList list = nbt.getList(DEATH_CHESTS_KEY, TAG_COMPOUND);

		for(INBTBase tag : list) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final UUID playerID = NBTUtil.readUniqueId(compound.getCompound(UUID_KEY));
			final long creationTime = compound.getLong(CREATION_TIME_KEY);
			final BlockPos pos = NBTUtil.readBlockPos(compound.getCompound(POS_KEY));
			final boolean isDoubleChest = compound.getBoolean(IS_DOUBLE_CHEST_KEY);
			final boolean unlocked = compound.getBoolean(UNLOCKED_KEY);

			deathChests.put(pos, new DeathChest(
					world, playerID, creationTime, pos, isDoubleChest, unlocked
			));
		}
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbt) {
		final NBTTagList tagList = new NBTTagList();

		for(Map.Entry<BlockPos, DeathChest> entry : deathChests.entrySet()) {
			final DeathChest deathChest = entry.getValue();
			final NBTTagCompound compound = new NBTTagCompound();

			compound.put(UUID_KEY, NBTUtil.writeUniqueId(deathChest.getPlayerID()));
			compound.putLong(CREATION_TIME_KEY, deathChest.getCreationTime());
			compound.put(POS_KEY, NBTUtil.writeBlockPos(entry.getKey()));
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

	public static VDCSavedData get(World world) {
		currentWorld = world;

		final WorldSavedDataStorage storage = world.getSavedDataStorage();
		final DimensionType dimensionType = world.getDimension().getType();

		VDCSavedData instance =
				storage.get(dimensionType, VDCSavedData::new, VanillaDeathChest.MOD_ID);

		if(instance == null) {
			instance = new VDCSavedData();
			storage.set(dimensionType, ID, instance);
		}

		currentWorld = null;

		return instance;
	}
}
