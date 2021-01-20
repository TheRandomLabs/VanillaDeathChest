package com.therandomlabs.vanilladeathchest.deathchest;

import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/**
 * Handles death chest interactions.
 */
public final class DeathChestInteractions {
	private static DeathChest ignoreDeathChest;

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

			return attemptInteract(deathChest, (ServerPlayerEntity) player) ?
					ActionResult.PASS : ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	/**
	 * TODO
	 *
	 * @param deathChest
	 * @param player
	 * @return
	 */
	public static boolean attemptInteract(DeathChest deathChest, ServerPlayerEntity player) {
		//TODO
		return true;
	}

	/**
	 * TODO
	 *
	 * @param deathChest
	 * @param player
	 * @return
	 */
	public static boolean attemptBreak(DeathChest deathChest, ServerPlayerEntity player) {
		if (deathChest == ignoreDeathChest) {
			return true;
		}

		if (!attemptInteract(deathChest, player)) {
			return false;
		}

		if (deathChest.isDoubleChest()) {
			ignoreDeathChest = deathChest;
			player.interactionManager.tryBreakBlock(deathChest.getPos().east());
		}

		//TODO
		return true;
	}

	/*
	private static boolean canInteract(ServerPlayerEntity player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		final VDCConfig.Defense config = VanillaDeathChest.config().defense;

		if(config.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getStackInHand(player.getActiveHand());
		final int amount = config.unlockerConsumeAmount;

		if(stack.getItem() == config.unlockerItem) {
			if(amount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if(config.damageUnlockerInsteadOfConsume) {
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

		final String message = config.unlockFailedMessage;

		if(!message.isEmpty()) {
			final Text component = new LiteralText(String.format(
					message,
					amount,
					new TranslatableText(config.unlockerItem.getTranslationKey()).
							formatted().asString().trim()
			));

			player.sendMessage(component, config.unlockFailedStatusMessage);
		}

		return false;
	}
	 */
}
