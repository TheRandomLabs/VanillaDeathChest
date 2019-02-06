package com.therandomlabs.vanilladeathchest.listener;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.api.listener.BlockHarvestListener;
import com.therandomlabs.vanilladeathchest.api.listener.RightClickBlockListener;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class DeathChestInteractionHandler implements BlockHarvestListener, RightClickBlockListener {
	private static BlockPos harvesting;

	@Override
	public boolean onBlockHarvest(World world, EntityPlayerMP player, BlockPos pos) {
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
			player.interactionManager.tryHarvestBlock(harvesting);
		}

		return true;
	}

	@Override
	public boolean onRightClickBlock(World world, EntityPlayerMP player, ItemStack stack,
			EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
		final DeathChest deathChest = DeathChestManager.getDeathChest(world, pos);

		if(deathChest == null) {
			return true;
		}

		return canInteract(player, deathChest);
	}

	public static boolean canInteract(EntityPlayer player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getHeldItem(player.getActiveHand());
		final int amount = VDCConfig.defense.unlockerConsumeAmount;

		if(stack.getItem() == VDCConfig.defense.unlocker) {
			if(amount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if(VDCConfig.defense.damageUnlockerInsteadOfConsume) {
				if(stack.isDamageable() && stack.getDamage() + amount < stack.getMaxDamage()) {
					if(!player.abilities.isCreativeMode) {
						stack.damageItem(amount, player);
					}

					deathChest.setUnlocked(true);
					return true;
				}
			} else if(stack.getCount() >= amount) {
				if(!player.abilities.isCreativeMode) {
					stack.shrink(amount);
				}

				deathChest.setUnlocked(true);
				return true;
			}
		}

		final String message = VDCConfig.defense.unlockFailedMessage;

		if(!message.isEmpty()) {
			final TextComponentString component = new TextComponentString(String.format(
					message,
					amount,
					new TextComponentTranslation(
							VDCConfig.defense.unlocker.getTranslationKey()
					).getFormattedText().trim()
			));

			if(VDCConfig.defense.unlockFailedStatusMessage) {
				player.sendStatusMessage(component, true);
			} else {
				player.sendMessage(component);
			}
		}

		return false;
	}
}
