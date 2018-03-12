package com.therandomlabs.vanilladeathchest;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = VanillaDeathChest.MODID)
public class MiscEventHandler {
	@SubscribeEvent
	public static void onCreateSpawn(WorldEvent.CreateSpawnPosition event) {
		final World world = event.getWorld();
		if(!world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD) {
			world.getGameRules().setOrCreateGameRule("dontSpawnDeathChests", "false");
		}
	}

	@SubscribeEvent
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		if(!event.getWorld().isRemote && !VanillaDeathChest.dropDeathChest &&
				isDeathChest(event.getWorld(), event.getPos())) {
			for(ItemStack stack : event.getDrops()) {
				if(stack.getItem() == Item.getItemFromBlock(Blocks.CHEST) &&
						stack.getCount() == 1 && stack.getMetadata() == 0) {
					event.getDrops().remove(stack);
					break;
				}
			}
		}
	}

	public static void addChest(World world, BlockPos pos) {
		final VDCSavedData data = VDCSavedData.get(world);
		data.getDeathChests().add(pos);
		data.markDirty();
	}

	public static boolean isDeathChest(World world, BlockPos pos) {
		return VDCSavedData.get(world).getDeathChests().contains(pos);
	}
}
