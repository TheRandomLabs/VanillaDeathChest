package com.therandomlabs.vanilladeathchest.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.event.DeathChestRemoveEvent;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		final BlockPos pos = event.getPos();

		if (!justRemoved.containsKey(pos)) {
			return;
		}

		justRemoved.remove(pos);

		final List<ItemStack> drops = event.getDrops();

		if (!drops.isEmpty()) {
			drops.remove(0);
		}
	}

	//Because HarvestDropsEvent doesn't capture shulker boxes and I don't want to write a coremod
	//just for this
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		final World world = event.getWorld();

		if (world.isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		if (!(entity instanceof ItemEntity)) {
			return;
		}

		final ItemStack stack = ((ItemEntity) entity).getItem();

		if (stack.getCount() != 1) {
			return;
		}

		final Block block = Block.getBlockFromItem(stack.getItem());

		if (!(block instanceof ShulkerBoxBlock)) {
			return;
		}

		final Vec3d pos = event.getEntity().getPositionVector();

		for (Map.Entry<BlockPos, Block> entry : justRemoved.entrySet()) {
			if (block != entry.getValue()) {
				continue;
			}

			final BlockPos chestPos = entry.getKey();

			//Drops spawn a maximum of 0.75 blocks per axis away from the block position
			//3 * 0.75^2 = 1.6875
			if (pos.squareDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) <= 1.6875) {
				final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
				ItemStackHelper.loadAllItems(
						stack.getTag().getCompound("BlockEntityTag"), inventory
				);

				for (ItemStack drop : inventory) {
					if (!drop.isEmpty()) {
						Block.spawnAsEntity(world, chestPos, drop);
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
