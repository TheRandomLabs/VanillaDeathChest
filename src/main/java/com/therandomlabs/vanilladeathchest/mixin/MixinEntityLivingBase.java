package com.therandomlabs.vanilladeathchest.mixin;

import java.util.Random;
import java.util.UUID;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.listener.LivingEntityDropListener;
import com.therandomlabs.vanilladeathchest.api.listener.LivingEntityExperienceDropListener;
import com.therandomlabs.vanilladeathchest.api.listener.LivingEntityTickListener;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase implements DeathChestDefenseEntity {
	@Shadow
	protected EntityPlayer attackingPlayer;
	@Shadow
	protected int recentlyHit;

	private UUID deathChestPlayer;
	private BlockPos deathChestPos;

	@Override
	public UUID getDeathChestPlayer() {
		return deathChestPlayer;
	}

	@Override
	public void setDeathChestPlayer(UUID uuid) {
		deathChestPlayer = uuid;
	}

	@Override
	public BlockPos getDeathChestPos() {
		return deathChestPos;
	}

	@Override
	public void setDeathChestPos(BlockPos pos) {
		deathChestPos = pos;
	}

	@Inject(method = "onLivingUpdate", at = @At("HEAD"))
	public void onLivingUpdate(CallbackInfo callback) {
		for(LivingEntityTickListener listener :
				RiftLoader.instance.getListeners(LivingEntityTickListener.class)) {
			listener.onLivingEntityTick((EntityLivingBase) (Object) this, this);
		}
	}

	@Inject(method = "writeEntityToNBT", at = @At("HEAD"))
	public void writeEntityToNBT(NBTTagCompound compound, CallbackInfo callback) {
		if(deathChestPlayer != null) {
			compound.setTag("DeathChestPlayer", NBTUtil.createUUIDTag(deathChestPlayer));
			compound.setTag("DeathChestPos", NBTUtil.createPosTag(deathChestPos));
		}
	}

	@Inject(method = "readEntityFromNBT", at = @At("HEAD"))
	public void readEntityFromNBT(NBTTagCompound compound, CallbackInfo callback) {
		if(compound.hasKey("DeathChestPlayer")) {
			deathChestPlayer = NBTUtil.getUUIDFromTag(compound.getCompoundTag("DeathChestPlayer"));
			deathChestPos = NBTUtil.getPosFromTag(compound.getCompoundTag("DeathChestPos"));
		}
	}

	@Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
	public void dropLoot(boolean recentlyHit, int lootingModifier, DamageSource source,
			CallbackInfo callback) {
		for(LivingEntityDropListener listener :
				RiftLoader.instance.getListeners(LivingEntityDropListener.class)) {
			if(!listener.onLivingEntityDrop(
					(EntityLivingBase) (Object) this, this, recentlyHit, lootingModifier, source
			)) {
				callback.cancel();
			}
		}
	}

	@Overwrite
	public void onDeathUpdate() {
		final EntityLivingBase entity = (EntityLivingBase) (Object) this;

		entity.deathTime++;

		if(entity.deathTime != 20) {
			return;
		}

		if(!entity.world.isRemote && (isPlayer() || recentlyHit > 0 && canDropLoot() &&
				entity.world.getGameRules().getBoolean("doMobLoot"))) {
			int experience = getExperiencePoints(attackingPlayer);

			for(LivingEntityExperienceDropListener listener :
					RiftLoader.instance.getListeners(LivingEntityExperienceDropListener.class)) {
				experience = listener.onLivingEntityDropExperience(
						(EntityLivingBase) (Object) this, this, experience
				);
			}

			while(experience > 0) {
				final int split = EntityXPOrb.getXPSplit(experience);
				experience -= split;
				entity.world.spawnEntity(new EntityXPOrb(
						entity.world, entity.posX, entity.posY, entity.posZ, split
				));
			}
		}

		entity.setDead();

		final Random random = entity.getRNG();

		for(int i = 0; i < 20; i++) {
			entity.world.spawnParticle(
					Particles.POOF,
					entity.posX + random.nextFloat() * entity.width * 2.0 - entity.width,
					entity.posY + random.nextFloat() * entity.height,
					entity.posZ + random.nextFloat() * entity.width * 2.0 - entity.width,
					random.nextGaussian() * 0.02,
					random.nextGaussian() * 0.02,
					random.nextGaussian() * 0.02
			);
		}
	}

	@Shadow
	protected boolean isPlayer() {
		return false;
	}

	@Shadow
	protected boolean canDropLoot() {
		return true;
	}

	@Shadow
	protected int getExperiencePoints(EntityPlayer player) {
		return 0;
	}
}
