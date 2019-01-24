package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestInteractionHandler {
	private static BlockPos harvesting;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
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

		if(!deathChest.canInteract(player)) {
			return EnumActionResult.PASS;
		}

		if(VDCConfig.defense.unlocker == null || deathChest.isUnlocked()) {
			return EnumActionResult.SUCCESS;
		}

		final ItemStack stack = player.getHeldItem(player.getActiveHand());

		if(stack.getItem() != VDCConfig.defense.unlocker ||
				(VDCConfig.defense.unlockerMeta != OreDictionary.WILDCARD_VALUE &&
						stack.getMetadata() != VDCConfig.defense.unlockerMeta)) {
			return EnumActionResult.PASS;
		}

		final int amount = VDCConfig.defense.unlockerConsumeAmount;

		if(amount != 0 && !player.capabilities.isCreativeMode) {
			if(VDCConfig.defense.damageUnlockerInsteadOfConsume) {
				if(stack.isItemStackDamageable()) {
					if(stack.getItemDamage() + amount >= stack.getMaxDamage()) {
						return EnumActionResult.PASS;
					}

					stack.damageItem(amount, player);
				}
			} else {
				if(stack.getCount() < amount) {
					return EnumActionResult.PASS;
				}

				stack.shrink(amount);
			}
		}

		deathChest.setUnlocked(true);
		return EnumActionResult.SUCCESS;
	}
}
