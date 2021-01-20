package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LockableContainerBlockEntity.class)
public final class LockableContainerBlockEntityMixin implements DeathChestBlockEntity {
	@Unique
	private DeathChest deathChest;

	@Override
	public DeathChest getDeathChest() {
		return deathChest;
	}

	@Override
	public void setDeathChest(DeathChest deathChest) {
		this.deathChest = deathChest;
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "fromTag", at = @At("TAIL"))
	private void fromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
		if (tag.getBoolean("IsDeathChest")) {
			final BlockEntity blockEntity = (BlockEntity) (Object) this;
			deathChest = DeathChestsState.get((ServerWorld) blockEntity.getWorld()).
					getDeathChest(blockEntity.getPos());
		}
	}

	@Inject(method = "toTag", at = @At("TAIL"))
	private void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
		if (deathChest != null) {
			tag.putBoolean("IsDeathChest", true);
		}
	}
}
