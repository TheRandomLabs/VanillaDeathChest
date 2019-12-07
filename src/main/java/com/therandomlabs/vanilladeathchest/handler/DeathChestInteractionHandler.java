package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import com.therandomlabs.vanilladeathchest.gamestages.VDCStageInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
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

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestInteractionHandler {
	private static BlockPos harvesting;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		final World world = event.getWorld();

		if (world.isRemote) {
			return;
		}

		final BlockPos pos = event.getPos();

		if (harvesting == pos) {
			return;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if (deathChest == null) {
			return;
		}

		final EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();

		if (!canInteract(player, deathChest)) {
			event.setCanceled(true);
			return;
		}

		final DeathChest chest = DeathChestManager.removeDeathChest(world, pos);

		if (chest.isDoubleChest()) {
			harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
			player.interactionManager.tryHarvestBlock(harvesting);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();

		if (world.isRemote) {
			return;
		}

		final DeathChest deathChest = DeathChestManager.getDeathChest(world, event.getPos());

		if (deathChest == null) {
			return;
		}

		if (!canInteract((EntityPlayerMP) event.getEntityPlayer(), deathChest)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
		final World world = event.getWorld();
		event.getAffectedBlocks().removeIf(pos -> DeathChestManager.isLocked(world, pos));
	}

	private static boolean canInteract(EntityPlayerMP player, DeathChest deathChest) {
		if (!deathChest.canInteract(player)) {
			return false;
		}

		final VDCStageInfo info = VDCStageInfo.get(player);
		final Item unlocker = info.getUnlocker();

		if (unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getHeldItem(player.getActiveHand());
		final int amount = info.getUnlockerConsumeAmount();

		if (stack.getItem() == unlocker) {
			if (amount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if (info.damageUnlockerInsteadOfConsume()) {
				boolean unlocked = player.capabilities.isCreativeMode;

				if (!unlocked && stack.isItemStackDamageable() &&
						stack.getItemDamage() + amount < stack.getMaxDamage()) {
					stack.attemptDamageItem(amount, player.getRNG(), player);
					unlocked = true;
				}

				if (unlocked) {
					deathChest.setUnlocked(true);
					return true;
				}
			} else if (stack.getCount() >= amount) {
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(amount);
				}

				deathChest.setUnlocked(true);
				return true;
			}
		}

		final String message = info.getUnlockFailedMessage();

		if (!message.isEmpty()) {
			final TextComponentString component = new TextComponentString(String.format(
					message,
					amount,
					new TextComponentTranslation(
							unlocker.getTranslationKey() + ".name"
					).getFormattedText().trim()
			));

			if (VDCConfig.Defense.unlockFailedStatusMessage) {
				player.sendStatusMessage(component, true);
			} else {
				player.sendMessage(component);
			}
		}

		return false;
	}
}
