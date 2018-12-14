package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DeathChestInteractionHandler implements PlayerInteractionEvent.Block,
		PlayerInteractionEvent.BlockPositioned {
	private static BlockPos harvesting;

	//onBreakBlock
	@Override
	public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos,
			Direction direction) {
		if(harvesting == pos) {
			return ActionResult.SUCCESS;
		}

		final ActionResult result = onBlockInteract(world, player, pos);

		if(result == ActionResult.SUCCESS) {
			final DeathChest chest = DeathChestManager.removeDeathChest(world, pos);

			if(chest.isDoubleChest()) {
				harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
				((ServerPlayerEntity) player).interactionManager.tryBreakBlock(harvesting);
			}
		}

		return result;
	}

	//onRightClickBlock
	@Override
	public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos,
			Direction direction, float hitX, float hitY, float hitZ) {
		return onBlockInteract(world, player, pos);
	}

	public static ActionResult onBlockInteract(World world, PlayerEntity player,
			BlockPos pos) {
		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if(deathChest == null) {
			return ActionResult.FAILURE;
		}

		if(deathChest.canInteract(player)) {
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
