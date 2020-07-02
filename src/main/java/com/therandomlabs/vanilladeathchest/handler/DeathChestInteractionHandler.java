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

package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.api.event.block.BreakBlockCallback;
import com.therandomlabs.vanilladeathchest.api.event.block.ExplosionDetonationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DeathChestInteractionHandler implements
		BreakBlockCallback, UseBlockCallback, ExplosionDetonationCallback {
	@Nullable
	private static BlockPos harvesting;

	@SuppressWarnings("NullAway")
	@Override
	public boolean breakBlock(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
		if (pos.equals(harvesting)) {
			return true;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if (deathChest == null) {
			return true;
		}

		if (!canInteract(player, deathChest)) {
			return false;
		}

		final DeathChest chest = DeathChestManager.removeDeathChest(world, pos);

		if (chest.isDoubleChest()) {
			harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
			player.interactionManager.tryBreakBlock(harvesting);
		}

		return true;
	}

	//onRightClickBlock (UseBlockCallback)
	@Override
	public ActionResult interact(
			PlayerEntity player, World world, Hand hand, BlockHitResult result
	) {
		if(world.isClient) {
			return ActionResult.PASS;
		}

		final DeathChest deathChest =
				DeathChestManager.getDeathChest((ServerWorld) world, result.getBlockPos());

		if(deathChest == null) {
			return ActionResult.PASS;
		}

		return canInteract((ServerPlayerEntity) player, deathChest) ?
				ActionResult.PASS : ActionResult.SUCCESS;
	}

	@Override
	public void onExplosionDetonate(ServerWorld world, Explosion explosion) {
		explosion.getAffectedBlocks().removeIf(pos -> DeathChestManager.isLocked(world, pos));
	}

	private static boolean canInteract(ServerPlayerEntity player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.Defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getStackInHand(player.getActiveHand());
		final int amount = VDCConfig.Defense.unlockerConsumeAmount;

		if(stack.getItem() == VDCConfig.Defense.unlocker) {
			if(amount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if(VDCConfig.Defense.damageUnlockerInsteadOfConsume) {
				boolean unlocked = player.abilities.creativeMode;

				if(!unlocked && stack.isDamageable() &&
						stack.getDamage() + amount < stack.getDamage()) {
					stack.damage(amount, player.getRandom(), player);
					unlocked = true;
				}

				if(unlocked) {
					deathChest.setUnlocked(true);
					return true;
				}
			} else if(stack.getCount() >= amount) {
				if(!player.abilities.creativeMode) {
					stack.decrement(amount);
				}

				deathChest.setUnlocked(true);
				return true;
			}
		}

		final String message = VDCConfig.Defense.unlockFailedMessage;

		if(!message.isEmpty()) {
			final Text component = new LiteralText(String.format(
					message,
					amount,
					new TranslatableText(
							VDCConfig.Defense.unlocker.getTranslationKey()
					).formatted().asString().trim()
			));

			player.sendMessage(component, VDCConfig.Defense.unlockFailedStatusMessage);
		}

		return false;
	}
}
