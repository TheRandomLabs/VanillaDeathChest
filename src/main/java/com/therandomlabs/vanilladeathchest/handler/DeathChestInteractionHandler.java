package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MODID)
public final class DeathChestInteractionHandler {
	private static BlockPos harvesting;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final EnumActionResult result =
				onBlockInteract(world, event.getEntityPlayer(), event.getPos());

		if(result == EnumActionResult.PASS) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final BlockPos pos = event.getPos();

		if(harvesting == pos) {
			return;
		}

		final EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
		final EnumActionResult result = onBlockInteract(world, player, event.getPos());

		if(result == EnumActionResult.SUCCESS) {
			final DeathChest chest = DeathChestManager.removeDeathChest(world, pos);

			if(chest.isDoubleChest()) {
				harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
				player.interactionManager.tryHarvestBlock(harvesting);
			}
		} else if(result == EnumActionResult.PASS) {
			event.setCanceled(true);
		}
	}

	public static EnumActionResult onBlockInteract(World world, EntityPlayer player, BlockPos pos) {
		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if(deathChest == null) {
			return EnumActionResult.FAIL;
		}

		if(deathChest.canInteract(player)) {
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
