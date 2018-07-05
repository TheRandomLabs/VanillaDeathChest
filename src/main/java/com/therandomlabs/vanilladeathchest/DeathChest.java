package com.therandomlabs.vanilladeathchest;

import java.util.UUID;
import com.therandomlabs.vanilladeathchest.base.VDCConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.UserListOpsEntry;

public class DeathChest {
	private final UUID playerID;
	private final long creationTime;

	public DeathChest(UUID playerID, long creationTime) {
		this.playerID = playerID;
		this.creationTime = creationTime;
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public long getCreationTime() {
		return creationTime;
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
