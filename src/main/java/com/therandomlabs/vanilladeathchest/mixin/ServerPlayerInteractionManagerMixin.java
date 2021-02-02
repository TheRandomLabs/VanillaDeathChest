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
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestInteractions;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public final class ServerPlayerInteractionManagerMixin {
	@Shadow
	public ServerWorld world;

	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
	private void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		if (!world.getBlockState(pos).getBlock().hasBlockEntity()) {
			return;
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof DeathChestBlockEntity) {
			final DeathChest deathChest = ((DeathChestBlockEntity) blockEntity).getDeathChest();

			if (deathChest != null &&
					!DeathChestInteractions.attemptBreak(pos, deathChest, player)) {
				info.setReturnValue(false);
			}
		}
	}
}
