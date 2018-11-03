package com.therandomlabs.vanilladeathchest.handler;

import java.util.ArrayList;
import java.util.List;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.event.DeathChestRemoveEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestDropHandler {
	private static final Item CHEST = Item.getItemFromBlock(Blocks.CHEST);
	private static final List<BlockPos> justRemoved = new ArrayList<>();

	@SubscribeEvent
	public static void onDeathChestRemove(DeathChestRemoveEvent event) {
		final BlockPos west = event.getWest();
		final BlockPos east = event.getEast();

		if(west != null) {
			justRemoved.add(west);
		}

		if(east != null) {
			justRemoved.add(east);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		if(VDCConfig.misc.dropDeathChests || !justRemoved.contains(event.getPos())) {
			return;
		}

		final List<ItemStack> drops = event.getDrops();

		for(ItemStack stack : drops) {
			if(stack.getItem() == CHEST && stack.getCount() == 1 && stack.getMetadata() == 0) {
				drops.remove(stack);
				break;
			}
		}
	}

	@SubscribeEvent
	public static void serverTick(TickEvent.ServerTickEvent event) {
		justRemoved.clear();
	}
}
