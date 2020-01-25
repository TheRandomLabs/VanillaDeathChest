package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityDropExperienceCallback;
import com.therandomlabs.vanilladeathchest.api.event.livingentity.LivingEntityTickCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements DeathChestDefenseEntity {
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

	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo callback) {
		if((Object) this instanceof MobEntity) {
			LivingEntityTickCallback.EVENT.invoker().onLivingEntityTick(
					(MobEntity) (Object) this, this
			);
		}
	}

	@Inject(method = "writeCustomDataToTag", at = @At("HEAD"))
	public void writeCustomDataToTag(CompoundTag tag, CallbackInfo callback) {
		if((Object) this instanceof MobEntity && deathChestPlayer != null) {
			tag.put("DeathChestPlayer", NbtHelper.fromUuid(deathChestPlayer));
			tag.put("DeathChestPos", NbtHelper.fromBlockPos(deathChestPos));
		}
	}

	@Inject(method = "readCustomDataFromTag", at = @At("HEAD"))
	public void readCustomDataFromTag(CompoundTag tag, CallbackInfo callback) {
		//noinspection ConstantConditions
		if((Object) this instanceof MobEntity && tag.contains("DeathChestPlayer")) {
			deathChestPlayer = NbtHelper.toUuid(tag.getCompound("DeathChestPlayer"));
			deathChestPos = NbtHelper.toBlockPos(tag.getCompound("DeathChestPos"));
		}
	}

	@Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
	public void dropLoot(DamageSource source, boolean recentlyHit, CallbackInfo callback) {
		final Object object = this;

		//noinspection ConstantConditions
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

		//noinspection ConstantConditions
		if(!(object instanceof MobEntity)) {
			return;
		}

		if(!LivingEntityDropCallback.EVENT.invoker().onLivingEntityDrop(
				(MobEntity) object, this, recentlyHit, lootingModifier, source
		)) {
			callback.cancel();
		}
	}

	@Redirect(method = "dropXp", at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/LivingEntity.getCurrentExperience(" +
					"Lnet/minecraft/entity/player/PlayerEntity;)I"
	))
	public int getExperience(LivingEntity entity, PlayerEntity player) {
		final int experience = getCurrentExperience(player);

		//noinspection ConstantConditions
		if((Object) this instanceof MobEntity) {
			return LivingEntityDropExperienceCallback.EVENT.invoker().
					onLivingEntityDropExperience((MobEntity) (Object) this, this, experience);
		}

		return experience;
	}

	@Shadow
	protected int getCurrentExperience(PlayerEntity player) {
		return 0;
	}
}
