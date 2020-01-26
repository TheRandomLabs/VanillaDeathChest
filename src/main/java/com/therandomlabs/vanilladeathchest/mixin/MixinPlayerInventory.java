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

import java.util.ArrayList;
import java.util.List;

import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("NullAway")
@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
	@Shadow
	@Final
	public PlayerEntity player;

	private final ArrayList<ItemEntity> drops = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Inject(method = "dropAll", at = @At("TAIL"))
	public void dropAll(CallbackInfo callback) {
		if (!PlayerDropAllItemsCallback.EVENT.invoker().onPlayerDropAllItems(
				(ServerWorld) player.getEntityWorld(), player, (List<ItemEntity>) drops.clone()
		)) {
			drops.forEach(Entity::remove);
		}

		drops.clear();
	}

	@Redirect(method = "dropAll", at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/player/PlayerEntity.dropItem(" +
					"Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"
	))
	public ItemEntity dropItem(
			PlayerEntity player, ItemStack stack, boolean flag1, boolean flag2
	) {
		final ItemEntity item = player.dropItem(stack, flag1, flag2);
		drops.add(item);
		return item;
	}
}
