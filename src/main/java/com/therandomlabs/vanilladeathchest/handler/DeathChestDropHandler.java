package com.therandomlabs.vanilladeathchest.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.event.DeathChestRemoveEvent;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
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
	private static final Set<BlockPos> justRemoved = new HashSet<>();

	@SubscribeEvent
	public static void onDeathChestRemove(DeathChestRemoveEvent event) {
		if(VDCConfig.Misc.dropDeathChests) {
			return;
		}

		final BlockPos west = event.getWest();
		final BlockPos east = event.getEast();

		if(west != null) {
			justRemoved.add(west);
		}

		if(east != null) {
			justRemoved.add(east);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		final BlockPos pos = event.getPos();

		if(!justRemoved.contains(pos)) {
			return;
		}

		justRemoved.remove(pos);

		final List<ItemStack> drops = event.getDrops();

		if(!drops.isEmpty()) {
			drops.remove(0);
		}
	}

	//Because HarvestDropsEvent doesn't capture shulker boxes and I don't want to write a coremod
	//just for this
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		if(!(entity instanceof EntityItem)) {
			return;
		}

		final ItemStack stack = ((EntityItem) entity).getItem();

		if(stack.getCount() != 1 ||
				!(Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox)) {
			return;
		}

		final Vec3d pos = event.getEntity().getPositionVector();

		for(BlockPos chestPos : justRemoved) {
			//Drops spawn a maximum of 0.75 blocks per axis away from the block position
			//3 * 0.75^2 = 1.6875
			if(pos.squareDistanceTo(chestPos.getX(), chestPos.getY(), chestPos.getZ()) <= 1.6875) {
				final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
				ItemStackHelper.loadAllItems(
						stack.getTagCompound().getCompoundTag("BlockEntityTag"), inventory
				);

				for(ItemStack drop : inventory) {
					if(!drop.isEmpty()) {
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
		if(event.phase == TickEvent.Phase.END) {
			justRemoved.clear();
		}
	}
}
