package com.therandomlabs.vanilladeathchest.world.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionalPersistentStateManager;
import org.apache.commons.lang3.ArrayUtils;

public class VDCSavedData extends PersistentState {
	public static final int TAG_COMPOUND = ArrayUtils.indexOf(Tag.TYPES, "COMPOUND");

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
	public void fromTag(CompoundTag nbt) {
		deathChests.clear();

		final ListTag list = nbt.getList(DEATH_CHESTS_KEY, TAG_COMPOUND);

		for(Tag tag : list) {
			final CompoundTag compound = (CompoundTag) tag;

			final UUID playerID = TagHelper.deserializeUuid(compound.getCompound(UUID_KEY));
			final long creationTime = compound.getLong(CREATION_TIME_KEY);
			final BlockPos pos = TagHelper.deserializeBlockPos(compound.getCompound(POS_KEY));
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

			compound.put(UUID_KEY, TagHelper.serializeUuid(deathChest.getPlayerID()));
			compound.putLong(CREATION_TIME_KEY, deathChest.getCreationTime());
			compound.put(POS_KEY, TagHelper.serializeBlockPos(entry.getKey()));
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

		final DimensionalPersistentStateManager stateManager =
				world.getDimensionalPersistentStateManager();
		VDCSavedData instance = stateManager.get(VDCSavedData::new, ID);

		if(instance == null) {
			instance = new VDCSavedData();
			stateManager.set(instance);
		}

		currentWorld = null;

		return instance;
	}
}
