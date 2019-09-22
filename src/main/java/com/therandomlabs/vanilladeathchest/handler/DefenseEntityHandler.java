package com.therandomlabs.vanilladeathchest.handler;

import java.util.UUID;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DefenseEntityHandler {
	@SubscribeEvent
	public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		final LivingEntity entity = event.getEntityLiving();
		final World world = entity.getEntityWorld();

		if(world.isRemote()) {
			return;
		}

		final CompoundNBT data = entity.getPersistentData();

		if(!data.contains("DeathChestPlayer")) {
			return;
		}

		final UUID playerUUID = NBTUtil.readUniqueId(data.getCompound("DeathChestPlayer"));
		final PlayerEntity player = world.getPlayerByUuid(playerUUID);

		if(player != null) {
			entity.setRevengeTarget(player);
		}

		if(VDCConfig.Defense.defenseEntityMaxDistanceSquared == 0.0) {
			return;
		}

		final BlockPos pos = NBTUtil.readBlockPos(data.getCompound("DeathChestPos"));
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
				event.getEntity().getPersistentData().contains("DeathChestPlayer")) {
			event.getDrops().clear();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
		if(!VDCConfig.Defense.defenseEntityDropsExperience &&
				event.getEntity().getPersistentData().contains("DeathChestPlayer")) {
			event.setCanceled(true);
		}
	}
}
