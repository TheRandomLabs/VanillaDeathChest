package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DeathChestInteractionHandler
		implements PlayerInteractionEvent.Block, PlayerInteractionEvent.BlockPositioned {
	private static BlockPos harvesting;

	//onBreakBlock (PlayerInteractionEvent.Block)
	@Override
	public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos,
			Direction direction) {
		if(world.isClient || harvesting == pos) {
			return ActionResult.PASS;
		}

		final ServerWorld serverWorld = (ServerWorld) world;
		final DeathChest deathChest = DeathChestManager.getDeathChest(serverWorld, pos);

		if(deathChest == null) {
			return ActionResult.PASS;
		}

		if(!canInteract(player, deathChest)) {
			return ActionResult.SUCCESS;
		}

		final DeathChest chest = DeathChestManager.removeDeathChest(serverWorld, pos);

		if(chest.isDoubleChest()) {
			harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
			((ServerPlayerEntity) player).interactionManager.tryBreakBlock(harvesting);
		}

		return ActionResult.PASS;
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

		return canInteract(player, deathChest) ? ActionResult.PASS : ActionResult.SUCCESS;
	}

	public static boolean canInteract(PlayerEntity player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getStackInHand(player.getActiveHand());

		if(stack.getItem() != VDCConfig.defense.unlocker) {
			return false;
		}

		final int amount = VDCConfig.defense.unlockerConsumeAmount;

		if(amount != 0 && !player.abilities.creativeMode) {
			if(VDCConfig.defense.damageUnlockerInsteadOfConsume) {
				if(stack.hasDurability()) {
					if(stack.getDamage() + amount >= stack.getDurability()) {
						return false;
					}

					stack.applyDamage(amount, player);
				}
			} else {
				if(stack.getAmount() < amount) {
					return false;
				}

				stack.subtractAmount(amount);
			}
		}

		deathChest.setUnlocked(true);
		return true;
	}
}
