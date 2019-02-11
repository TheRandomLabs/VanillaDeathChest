package com.therandomlabs.vanilladeathchest.mixin;

import java.util.Random;
import java.util.UUID;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements DeathChestDefenseEntity {
	@Shadow
	protected PlayerEntity field_6258;
	@Shadow
	protected int playerHitTimer;

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

	@Inject(method = "update", at = @At("HEAD"))
	public void update(CallbackInfo callback) {
		if((Object) this instanceof MobEntity) {
			LivingEntityTickCallback.EVENT.invoker().onLivingEntityTick(
					(MobEntity) (Object) this, this
			);
		}
	}

	@Inject(method = "writeCustomDataToTag", at = @At("HEAD"))
	public void writeCustomDataToTag(CompoundTag tag, CallbackInfo callback) {
		if((Object) this instanceof MobEntity && deathChestPlayer != null) {
			tag.put("DeathChestPlayer", TagHelper.serializeUuid(deathChestPlayer));
			tag.put("DeathChestPos", TagHelper.serializeBlockPos(deathChestPos));
		}
	}

	@Inject(method = "readCustomDataFromTag", at = @At("HEAD"))
	public void readCustomDataFromTag(CompoundTag tag, CallbackInfo callback) {
		if(((Object) this instanceof MobEntity && tag.containsKey("DeathChestPlayer"))) {
			deathChestPlayer = TagHelper.deserializeUuid(tag.getCompound("DeathChestPlayer"));
			deathChestPos = TagHelper.deserializeBlockPos(tag.getCompound("DeathChestPos"));
		}
	}

	@Inject(method = "method_16077", at = @At("HEAD"), cancellable = true)
	public void method_16077(DamageSource source, boolean recentlyHit, CallbackInfo callback) {
		final Object object = this;

		if(!(object instanceof MobEntity)) {
			return;
		}

		if(!LivingEntityDropCallback.EVENT.invoker().onLivingEntityDrop(
				(MobEntity) object, this, recentlyHit, 0, source
		)) {
			callback.cancel();
		}
	}

	@Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
	public void dropEquipment(DamageSource source, int lootingModifier, boolean recentlyHit,
			CallbackInfo callback) {
		final Object object = this;

		if(!(object instanceof MobEntity)) {
			return;
		}

		if(!LivingEntityDropCallback.EVENT.invoker().onLivingEntityDrop(
				(MobEntity) object, this, recentlyHit, lootingModifier, source
		)) {
			callback.cancel();
		}
	}

	@Overwrite
	public void updatePostDeath() {
		final LivingEntity entity = (LivingEntity) (Object) this;

		entity.deathCounter++;

		if(entity.deathCounter != 20) {
			return;
		}

		if(!entity.world.isClient && (method_6071() || playerHitTimer > 0 && canDropLootAndXp() &&
				entity.world.getGameRules().getBoolean("doMobLoot"))) {
			int experience = getCurrentExperience(field_6258);

			if((Object) this instanceof MobEntity) {
				experience = LivingEntityDropExperienceCallback.EVENT.invoker().
						onLivingEntityDropExperience((MobEntity) (Object) this, this, experience);
			}

			while(experience > 0) {
				final int split = ExperienceOrbEntity.roundToOrbSize(experience);
				experience -= split;
				entity.world.spawnEntity(new ExperienceOrbEntity(
						entity.world, entity.x, entity.y, entity.z, split
				));
			}
		}

		entity.invalidate();

		final Random random = entity.getRand();
		final float width = entity.getWidth();
		final float height = entity.getHeight();

		for(int i = 0; i < 20; i++) {
			entity.world.addParticle(
					ParticleTypes.POOF,
					entity.x + random.nextFloat() * width * 2.0 - width,
					entity.y + random.nextFloat() * height,
					entity.z + random.nextFloat() * width * 2.0 - width,
					random.nextGaussian() * 0.02,
					random.nextGaussian() * 0.02,
					random.nextGaussian() * 0.02
			);
		}
	}

	@Shadow
	protected boolean method_6071() {
		return false;
	}

	@Shadow
	protected boolean canDropLootAndXp() {
		return true;
	}

	@Shadow
	protected int getCurrentExperience(PlayerEntity player) {
		return 0;
	}
}
