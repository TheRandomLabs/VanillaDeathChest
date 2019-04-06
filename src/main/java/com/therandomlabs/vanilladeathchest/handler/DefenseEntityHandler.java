package com.therandomlabs.vanilladeathchest.handler;

import java.util.UUID;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public final class DefenseEntityHandler {
	@SubscribeEvent
	public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		final EntityLivingBase entity = event.getEntityLiving();
		final World world = entity.getEntityWorld();

		if(world.isRemote) {
			return;
		}

		final NBTTagCompound data = entity.getEntityData();

		if(!data.hasKey("DeathChestPlayer")) {
			return;
		}

		final UUID playerUUID = NBTUtil.getUUIDFromTag(data.getCompoundTag("DeathChestPlayer"));
		final EntityPlayer player = world.getPlayerEntityByUUID(playerUUID);

		if(player != null) {
			entity.setRevengeTarget(player);
		}

		if(VDCConfig.Defense.defenseEntityMaxDistanceSquared == 0.0) {
			return;
		}

		final BlockPos pos = NBTUtil.getPosFromTag(data.getCompoundTag("DeathChestPos"));
		final BlockPos entityPos = entity.getPosition();

		final double distanceSq = entityPos.distanceSq(pos);
		final double distanceSqFromPlayer;

		if(player == null) {
			distanceSqFromPlayer = Double.MAX_VALUE;
		} else {
			distanceSqFromPlayer = entityPos.distanceSq(player.getPosition());
		}

		if(distanceSq > VDCConfig.Defense.defenseEntityMaxDistanceSquared) {
			final double maxDistanceSqFromPlayer =
					VDCConfig.Defense.defenseEntityMaxDistanceSquaredFromPlayer;

			if(maxDistanceSqFromPlayer == 0.0 || distanceSqFromPlayer > maxDistanceSqFromPlayer) {
				entity.setPosition(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingDrops(LivingDropsEvent event) {
		if(!VDCConfig.Defense.defenseEntityDropsItems &&
				event.getEntity().getEntityData().hasKey("DeathChestPlayer")) {
			event.getDrops().clear();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
		if(!VDCConfig.Defense.defenseEntityDropsExperience &&
				event.getEntity().getEntityData().hasKey("DeathChestPlayer")) {
			event.setCanceled(true);
		}
	}
}
