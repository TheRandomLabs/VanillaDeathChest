package com.therandomlabs.vanilladeathchest.api.deathchest;

import java.util.UUID;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.world.storage.VDCSavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.OpEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class DeathChest {
	private final ServerWorld world;
	private final UUID playerID;
	private final long creationTime;
	private final BlockPos pos;
	private final boolean isDoubleChest;
	private boolean unlocked;

	public DeathChest(
			ServerWorld world, UUID playerID, long creationTime, BlockPos pos,
			boolean isDoubleChest, boolean unlocked
	) {
		this.world = world;
		this.playerID = playerID;
		this.creationTime = creationTime;
		this.pos = pos;
		this.isDoubleChest = isDoubleChest;
		this.unlocked = unlocked;
	}

	public ServerWorld getWorld() {
		return world;
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public BlockPos getPos() {
		return pos;
	}

	public boolean isDoubleChest() {
		return isDoubleChest;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean flag) {
		unlocked = flag;
		VDCSavedData.get(world).markDirty();
	}

	public boolean canInteract(PlayerEntity player) {
		if(player == null) {
			return false;
		}

		if(!VDCConfig.Protection.enabled ||
				playerID.equals(player.getUniqueID()) ||
				(VDCConfig.Protection.bypassIfCreative && player.abilities.isCreativeMode)) {
			return true;
		}

		final OpEntry entry = player.getServer().getPlayerList().getOppedPlayers().
				getEntry(player.getGameProfile());

		if(entry == null ||
				entry.getPermissionLevel() < VDCConfig.Protection.bypassPermissionLevel) {
			if(VDCConfig.Protection.period == 0) {
				return false;
			}

			final long timeElapsed = player.getEntityWorld().getGameTime() - creationTime;
			return timeElapsed > VDCConfig.Protection.period;
		}

		return true;
	}
}
