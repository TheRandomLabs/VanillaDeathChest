/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2019 TheRandomLabs
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

import java.util.Collections;
import java.util.List;

import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("UnusedMethod")
@Mixin(ShulkerBoxBlock.class)
public class MixinShulkerBoxBlock {
	@SuppressWarnings("Duplicates")
	@Redirect(
			method = "onBreak",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;" +
					"spawnEntity(Lnet/minecraft/entity/Entity;)Z")
	)
	public boolean onBreak(World world, Entity entity) {
		final BlockPos pos = entity.getBlockPos();
		final ItemStack stack = ((ItemEntity) entity).getStack();

		List<ItemStack> drops = GetBlockDropCallback.EVENT.invoker().getDrop(world, pos, stack);

		if (drops == null) {
			drops = Collections.singletonList(stack);
		}

		boolean dropped = false;

		for (ItemStack drop : drops) {
			if (!drop.isEmpty()) {
				final ItemEntity item = new ItemEntity(
						world,
						pos.getX() + world.random.nextFloat() * 0.5F + 0.25,
						pos.getY() + world.random.nextFloat() * 0.5F + 0.25,
						pos.getZ() + world.random.nextFloat() * 0.5F + 0.25,
						drop
				);
				item.setToDefaultPickupDelay();
				world.spawnEntity(item);
				dropped = true;
			}
		}

		return dropped;
	}
}
