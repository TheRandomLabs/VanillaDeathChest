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

package com.therandomlabs.vanilladeathchest.deathchest;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handles death chest interactions.
 */
public final class DeathChestInteractions {
	@Nullable
	private static DeathChest ignoreDeathChest;

	private DeathChestInteractions() {}

	/**
	 * Called when a block is right-clicked.
	 *
	 * @param player the player.
	 * @param world the world.
	 * @param hand the hand.
	 * @param blockHitResult the {@link BlockHitResult}.
	 * @return {@link ActionResult#PASS} if the interaction can occur,
	 * or {@link ActionResult#SUCCESS} if it cannot.
	 */
	public static ActionResult interact(
			PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult
	) {
		if (!(world instanceof ServerWorld)) {
			return ActionResult.PASS;
		}

		if (!world.getBlockState(blockHitResult.getBlockPos()).getBlock().hasBlockEntity()) {
			return ActionResult.PASS;
		}

		final BlockEntity blockEntity = world.getBlockEntity(blockHitResult.getBlockPos());

		if (blockEntity instanceof DeathChestBlockEntity) {
			final DeathChest deathChest = ((DeathChestBlockEntity) blockEntity).getDeathChest();
			return deathChest == null || attemptInteract(deathChest, (ServerPlayerEntity) player) ?
					ActionResult.PASS : ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	/**
	 * If the specified death chest is locked, an unlock attempt is performed.
	 * The method then returns whether the specified player can unlock the death chest.
	 *
	 * @param deathChest a death chest.
	 * @param player a player.
	 * @return {@code true} if the player can interact with the death chest,
	 * or otherwise {@code false}.
	 */
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public static boolean attemptInteract(DeathChest deathChest, ServerPlayerEntity player) {
		if (deathChest.isProtectedFrom(player)) {
			return false;
		}

		final VDCConfig.KeyItem config = VanillaDeathChest.config().keyItem;

		if (config.item == null) {
			deathChest.setLocked(false);
			return true;
		}

		if (!deathChest.isLocked()) {
			return true;
		}

		final ItemStack stack = player.getStackInHand(player.getActiveHand());
		final int amount = config.amountToConsume;

		if (stack.getItem() == config.item) {
			if (amount == 0 || player.abilities.creativeMode) {
				deathChest.setLocked(false);
				return true;
			}

			if (config.consumptionBehavior == VDCConfig.KeyConsumptionBehavior.DAMAGE) {
				if (stack.isDamageable() && stack.getDamage() + amount <= stack.getMaxDamage()) {
					stack.damage(
							amount, player,
							breaker -> breaker.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
					);
					deathChest.setLocked(false);
					return true;
				}
			} else if (stack.getCount() >= amount) {
				stack.decrement(amount);
				deathChest.setLocked(false);
				return true;
			}
		}

		final String message = config.unlockFailureMessage;

		if (!message.isEmpty()) {
			final Text component = new LiteralText(String.format(
					message,
					amount, new TranslatableText(config.item.getTranslationKey()).getString()
			));

			player.sendMessage(component, config.unlockFailureStatusMessage);
		}

		return false;
	}

	/**
	 * Called when a player attempts to break a death chest.
	 * Returns whether the death chest should be broken.
	 * If this is {@code true} and the death chest is a double chest, the other death chest
	 * block is automatically destroyed.
	 *
	 * @param pos a position.
	 * @param deathChest a death chest.
	 * @param player a player.
	 * @return {@code true} if the death chest should be broken, or otherwise {@code false}.
	 */
	public static boolean attemptBreak(
			BlockPos pos, DeathChest deathChest, ServerPlayerEntity player
	) {
		if (deathChest.equals(ignoreDeathChest)) {
			return true;
		}

		if (!attemptInteract(deathChest, player)) {
			return false;
		}

		if (deathChest.isDoubleChest()) {
			ignoreDeathChest = deathChest;
			final BlockPos west = deathChest.getPos();
			player.interactionManager.tryBreakBlock(pos.equals(west) ? west.east() : west);
		}

		return true;
	}
}
