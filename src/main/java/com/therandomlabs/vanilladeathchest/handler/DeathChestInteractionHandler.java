package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.api.event.BlockEvent;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sortme.ChatMessageType;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DeathChestInteractionHandler
		implements BlockEvent.Break, PlayerInteractionEvent.BlockPositioned {
	private static BlockPos harvesting;

	@Override
	public boolean breakBlock(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
		if(harvesting == pos) {
			return true;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if(deathChest == null) {
			return true;
		}

		if(!canInteract(player, deathChest)) {
			return false;
		}

		final DeathChest chest = DeathChestManager.removeDeathChest(world, pos);

		if(chest.isDoubleChest()) {
			harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
			player.interactionManager.tryBreakBlock(harvesting);
		}

		return true;
	}

	//onRightClickBlock (PlayerInteractionEvent.BlockPositioned)
	@Override
	public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos,
			Direction direction, float hitX, float hitY, float hitZ) {
		if(world.isClient) {
			return ActionResult.PASS;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest((ServerWorld) world, pos);

		if(deathChest == null) {
			return ActionResult.PASS;
		}

		return canInteract((ServerPlayerEntity) player, deathChest) ?
				ActionResult.PASS : ActionResult.SUCCESS;
	}

	private static boolean canInteract(ServerPlayerEntity player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getStackInHand(player.getActiveHand());
		final int amount = VDCConfig.defense.unlockerConsumeAmount;

		if(stack.getItem() == VDCConfig.defense.unlocker) {
			if(amount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if(VDCConfig.defense.damageUnlockerInsteadOfConsume) {
				if(stack.hasDurability() && stack.getDamage() + amount < stack.getDurability()) {
					if(!player.abilities.creativeMode) {
						stack.applyDamage(amount, player);
					}

					deathChest.setUnlocked(true);
					return true;
				}
			} else if(stack.getAmount() >= amount) {
				if(!player.abilities.creativeMode) {
					stack.subtractAmount(amount);
				}

				deathChest.setUnlocked(true);
				return true;
			}
		}

		final String message = VDCConfig.defense.unlockFailedMessage;

		if(!message.isEmpty()) {
			final StringTextComponent component = new StringTextComponent(String.format(
					message,
					amount,
					new TranslatableTextComponent(
							VDCConfig.defense.unlocker.getTranslationKey()
					).getFormattedText().trim()
			));

			if(VDCConfig.defense.unlockFailedStatusMessage) {
				player.addChatMessage(component, true);
			} else {
				player.sendChatMessage(component, ChatMessageType.CHAT);
			}
		}

		return false;
	}
}
