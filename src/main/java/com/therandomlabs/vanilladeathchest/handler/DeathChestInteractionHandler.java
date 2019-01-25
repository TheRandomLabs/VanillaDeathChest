package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestInteractionHandler {
	private static BlockPos harvesting;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final BlockPos pos = event.getPos();

		if(harvesting == pos) {
			return;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if(deathChest == null) {
			return;
		}

		final EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();

		if(!canInteract(player, deathChest)) {
			event.setCanceled(true);
			return;
		}

		final DeathChest chest = DeathChestManager.removeDeathChest(world, pos);

		if(chest.isDoubleChest()) {
			harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
			player.interactionManager.tryHarvestBlock(harvesting);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest(world, event.getPos());

		if(deathChest == null) {
			return;
		}

		if(!canInteract(event.getEntityPlayer(), deathChest)) {
			event.setCanceled(true);
		}
	}

	public static boolean canInteract(EntityPlayer player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getHeldItem(player.getActiveHand());

		if(stack.getItem() != VDCConfig.defense.unlocker) {
			return false;
		}

		final int amount = VDCConfig.defense.unlockerConsumeAmount;

		if(amount != 0 && !player.capabilities.isCreativeMode) {
			if(VDCConfig.defense.damageUnlockerInsteadOfConsume) {
				if(stack.isItemStackDamageable()) {
					if(stack.getItemDamage() + amount >= stack.getMaxDamage()) {
						return false;
					}

					stack.damageItem(amount, player);
				}
			} else {
				if(stack.getCount() < amount) {
					return false;
				}

				stack.shrink(amount);
			}
		}

		deathChest.setUnlocked(true);
		return true;
	}
}
