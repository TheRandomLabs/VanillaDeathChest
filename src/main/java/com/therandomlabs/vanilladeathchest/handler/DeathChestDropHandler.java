package com.therandomlabs.vanilladeathchest.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.event.DeathChestRemoveEvent;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestDropHandler {
	private static final Map<BlockPos, Block> justRemoved = new HashMap<>();

	@SubscribeEvent
	public static void onDeathChestRemove(DeathChestRemoveEvent event) {
		if (VDCConfig.Misc.dropDeathChests) {
			return;
		}

		final BlockPos west = event.getWest();
		final BlockPos east = event.getEast();

		if (west != null) {
			justRemoved.put(west, event.getChest().getWorld().getBlockState(west).getBlock());
		}

		if (east != null) {
			justRemoved.put(east, event.getChest().getWorld().getBlockState(east).getBlock());
		}
	}

	//Because HarvestDropsEvent doesn't capture named containers or shulker boxes, and
	//I don't want to write a coremod just for this.
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		final World world = event.getWorld();

		if (world.isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		if (!(entity instanceof EntityItem)) {
			return;
		}

		final ItemStack stack = ((EntityItem) entity).getItem();

		if (stack.getCount() != 1) {
			return;
		}

		final Block block = Block.getBlockFromItem(stack.getItem());
		final boolean isChest = block == Blocks.CHEST;

		if (!isChest && !(block instanceof BlockShulkerBox)) {
			return;
		}

		final Vec3d pos = event.getEntity().getPositionVector();

		for (Map.Entry<BlockPos, Block> entry : justRemoved.entrySet()) {
			if (block != entry.getValue()) {
				continue;
			}

			final BlockPos chestPos = entry.getKey();

			//Drops spawn a maximum of 0.75 blocks per axis away from the block position.
			//3 * 0.75^2 = 1.6875
			if (pos.squareDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) <= 1.6875) {
				if (!isChest) {
					final NonNullList<ItemStack> inventory =
							NonNullList.withSize(27, ItemStack.EMPTY);
					ItemStackHelper.loadAllItems(
							stack.getTagCompound().getCompoundTag("BlockEntityTag"), inventory
					);

					for (ItemStack drop : inventory) {
						if (!drop.isEmpty()) {
							Block.spawnAsEntity(world, chestPos, drop);
						}
					}
				}

				event.setCanceled(true);
				justRemoved.remove(chestPos);

				break;
			}
		}
	}

	@SubscribeEvent
	public static void serverTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			justRemoved.clear();
		}
	}
}
