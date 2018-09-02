package com.therandomlabs.vanilladeathchest.api.deathchest;

import java.util.UUID;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.math.BlockPos;

public class DeathChest {
	private final UUID playerID;
	private final long creationTime;
	private final BlockPos pos;
	private final boolean isDoubleChest;

	public DeathChest(UUID playerID, long creationTime, BlockPos pos, boolean isDoubleChest) {
		this.playerID = playerID;
		this.creationTime = creationTime;
		this.pos = pos;
		this.isDoubleChest = isDoubleChest;
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

	public boolean canInteract(EntityPlayer player) {
		if(player == null) {
			return false;
		}

		if(!VDCConfig.protection.enabled ||
				playerID.equals(player.getUniqueID()) ||
				(VDCConfig.protection.bypassIfCreative && player.capabilities.isCreativeMode)) {
			return true;
		}

		final UserListOpsEntry entry = player.getServer().getPlayerList().getOppedPlayers().
				getEntry(player.getGameProfile());

		if(entry == null ||
				entry.getPermissionLevel() < VDCConfig.protection.bypassPermissionLevel) {
			if(VDCConfig.protection.period == 0) {
				return false;
			}

			final long timeElapsed = player.getEntityWorld().getTotalWorldTime() - creationTime;
			return timeElapsed > VDCConfig.protection.period;
		}

		return true;
	}
}
