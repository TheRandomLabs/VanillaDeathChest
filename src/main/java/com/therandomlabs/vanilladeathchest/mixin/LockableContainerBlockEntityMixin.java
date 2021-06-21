/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LockableContainerBlockEntity.class)
public final class LockableContainerBlockEntityMixin implements DeathChestBlockEntity {
	@Unique
	private boolean isDeathChest;

	@Unique
	private DeathChest deathChest;

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public DeathChest getDeathChest() {
		if (!isDeathChest) {
			return null;
		}

		if (deathChest != null) {
			return deathChest;
		}

		final BlockEntity blockEntity = (BlockEntity) (Object) this;
		final ServerWorld world = (ServerWorld) blockEntity.getWorld();

		if (world == null) {
			return null;
		}

		deathChest = DeathChestsState.get(world).getExistingDeathChest(blockEntity.getPos());
		return deathChest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markAsDeathChest() {
		isDeathChest = true;
	}

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeNbt(
			NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir
	) {
		isDeathChest = nbt.getBoolean("IsDeathChest");
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void readNbt(
			NbtCompound nbt, CallbackInfo ci
	) {
		if (isDeathChest) {
			nbt.putBoolean("IsDeathChest", true);
		}
	}
}
