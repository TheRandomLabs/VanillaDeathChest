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

			return attemptInteract(deathChest, (ServerPlayerEntity) player) ?
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
	public static boolean attemptInteract(DeathChest deathChest, ServerPlayerEntity player) {
		if (!deathChest.canInteract(player)) {
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

		final String message = config.unlockFailedMessage;

		if (!message.isEmpty()) {
			final Text component = new LiteralText(String.format(
					message, amount,
					new TranslatableText(config.item.getTranslationKey()).
							formatted().asString().trim()
			));

			player.sendMessage(component, config.unlockFailedStatusMessage);
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
		if (deathChest == ignoreDeathChest) {
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
