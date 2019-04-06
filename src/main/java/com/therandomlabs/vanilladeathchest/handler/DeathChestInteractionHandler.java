package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
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

	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
		final World world = event.getWorld();
		event.getAffectedBlocks().removeIf(pos -> DeathChestManager.isLocked(world, pos));
	}

	private static boolean canInteract(EntityPlayer player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.Defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getHeldItem(player.getActiveHand());

		if(stack.getItem() == VDCConfig.Defense.unlocker) {
			if(VDCConfig.Defense.unlockerConsumeAmount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if(VDCConfig.Defense.damageUnlockerInsteadOfConsume) {
				final int newDamage =
						stack.getItemDamage() + VDCConfig.Defense.unlockerConsumeAmount;

				if(stack.isItemStackDamageable() && newDamage < stack.getMaxDamage()) {
					if(!player.capabilities.isCreativeMode) {
						stack.damageItem(VDCConfig.Defense.unlockerConsumeAmount, player);
					}

					deathChest.setUnlocked(true);
					return true;
				}
			} else if(stack.stackSize >= VDCConfig.Defense.unlockerConsumeAmount) {
				if(!player.capabilities.isCreativeMode) {
					stack.stackSize -= VDCConfig.Defense.unlockerConsumeAmount;
				}

				deathChest.setUnlocked(true);
				return true;
			}
		}

		if(!VDCConfig.Defense.unlockFailedMessage.isEmpty()) {
			final TextComponentString component = new TextComponentString(String.format(
					VDCConfig.Defense.unlockFailedMessage,
					VDCConfig.Defense.unlockerConsumeAmount,
					new TextComponentTranslation(
							VDCConfig.Defense.unlocker.getUnlocalizedName() + ".name"
					).getFormattedText().trim()
			));

			if(VDCConfig.Defense.unlockFailedStatusMessage) {
				player.sendStatusMessage(component);
			} else {
				player.sendMessage(component);
			}
		}

		return false;
	}
}
