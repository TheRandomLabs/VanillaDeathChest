package com.therandomlabs.vanilladeathchest.world.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.commons.lang3.ArrayUtils;

public class VDCSavedData extends WorldSavedData {
	public static final int TAG_COMPOUND = ArrayUtils.indexOf(INBT.NBT_TYPES, "COMPOUND");

	public static final String ID = VanillaDeathChest.MOD_ID;

	public static final String DEATH_CHESTS_KEY = "DeathChests";

	public static final String UUID_KEY = "UUID";
	public static final String CREATION_TIME_KEY = "CreationTime";
	public static final String POS_KEY = "Pos";
	public static final String IS_DOUBLE_CHEST_KEY = "IsDoubleChest";
	public static final String UNLOCKED_KEY = "Unlocked";

	private static ServerWorld currentWorld;

	private final ServerWorld world;
	private Map<BlockPos, DeathChest> deathChests = new ConcurrentHashMap<>();

	public VDCSavedData() {
		this(ID);
	}

	public VDCSavedData(String name) {
		super(name);
		world = currentWorld;
	}

	@Override
	public void read(CompoundNBT nbt) {
		deathChests.clear();

		final ListNBT list = nbt.getList(DEATH_CHESTS_KEY, TAG_COMPOUND);

		for(INBT tag : list) {
			final CompoundNBT compound = (CompoundNBT) tag;

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
	public CompoundNBT write(CompoundNBT nbt) {
		final ListNBT tagList = new ListNBT();

		for(Map.Entry<BlockPos, DeathChest> entry : deathChests.entrySet()) {
			final DeathChest deathChest = entry.getValue();
			final CompoundNBT compound = new CompoundNBT();

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

	public static VDCSavedData get(ServerWorld world) {
		currentWorld = world;
		final VDCSavedData instance = world.getSavedData().getOrCreate(VDCSavedData::new, ID);
		currentWorld = null;
		return instance;
	}
}
