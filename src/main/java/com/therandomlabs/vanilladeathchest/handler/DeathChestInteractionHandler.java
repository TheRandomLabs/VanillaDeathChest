package com.therandomlabs.vanilladeathchest.handler;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestInteractionHandler {
	private static BlockPos harvesting;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		final IWorld world = event.getWorld();

		if(world.isRemote()) {
			return;
		}

		final BlockPos pos = event.getPos();

		if(harvesting == pos) {
			return;
		}

		final ServerWorld serverWorld = (ServerWorld) world;

		final DeathChest deathChest = DeathChestManager.getDeathChest(serverWorld, pos);

		if(deathChest == null) {
			return;
		}

		final ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

		if(!canInteract(player, deathChest)) {
			event.setCanceled(true);
			return;
		}

		final DeathChest chest = DeathChestManager.removeDeathChest(serverWorld, pos);

		if(chest.isDoubleChest()) {
			harvesting = chest.getPos().equals(pos) ? pos.east() : pos.west();
			player.interactionManager.tryHarvestBlock(harvesting);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		final IWorld world = event.getWorld();

		if(world.isRemote()) {
			return;
		}

		final DeathChest deathChest =
				DeathChestManager.getDeathChest((ServerWorld) world, event.getPos());

		if(deathChest == null) {
			return;
		}

		if(!canInteract((ServerPlayerEntity) event.getPlayer(), deathChest)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
		event.getAffectedBlocks().removeIf(
				pos -> DeathChestManager.isLocked((ServerWorld) event.getWorld(), pos)
		);
	}

	private static boolean canInteract(ServerPlayerEntity player, DeathChest deathChest) {
		if(!deathChest.canInteract(player)) {
			return false;
		}

		if(VDCConfig.Defense.unlocker == null || deathChest.isUnlocked()) {
			return true;
		}

		final ItemStack stack = player.getHeldItem(player.getActiveHand());
		final int amount = VDCConfig.Defense.unlockerConsumeAmount;

		if(stack.getItem() == VDCConfig.Defense.unlocker) {
			if(amount == 0) {
				deathChest.setUnlocked(true);
				return true;
			}

			if(VDCConfig.Defense.damageUnlockerInsteadOfConsume) {
				if(stack.isDamageable() && stack.getDamage() + amount < stack.getMaxDamage()) {
					if(!player.abilities.isCreativeMode) {
						stack.damageItem(amount, player, player2 -> {});
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

		final String message = VDCConfig.Defense.unlockFailedMessage;

		if(!message.isEmpty()) {
			final StringTextComponent component = new StringTextComponent(String.format(
					message,
					amount,
					new TranslationTextComponent(
							VDCConfig.Defense.unlocker.getTranslationKey() + ".name"
					).getFormattedText().trim()
			));

			if(VDCConfig.Defense.unlockFailedStatusMessage) {
				player.sendStatusMessage(component, true);
			} else {
				player.sendMessage(component);
			}
		}

		return false;
	}
}
