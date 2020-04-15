package com.therandomlabs.vanilladeathchest.handler;

import java.util.Map;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.world.storage.VDCSavedData;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestContentsChecker {
	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if (!VDCConfig.Misc.deathChestsDisappearWhenEmptied) {
			return;
		}

		final ServerWorld world = (ServerWorld) event.world;

		final VDCSavedData savedData = VDCSavedData.get(world);
		final ServerChunkProvider provider = world.getChunkProvider();

		for (Map.Entry<BlockPos, DeathChest> entry : savedData.getDeathChests().entrySet()) {
			final BlockPos pos = entry.getKey();

			//Make sure we don't unnecessarily load any chunks
			final Chunk chunk = provider.func_225313_a(pos.getX() >> 4, pos.getZ() >> 4);

			if (chunk == null || chunk.isEmpty()) {
				continue;
			}

			final TileEntity tileEntity = event.world.getTileEntity(pos);

			if (!(tileEntity instanceof LockableLootTileEntity)) {
				continue;
			}

			final IItemHandler itemHandler = tileEntity.getCapability(
					CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH
			).orElse(null);
			boolean empty = true;

			for (int i = 0; i < itemHandler.getSlots(); i++) {
				if (!itemHandler.getStackInSlot(i).isEmpty()) {
					empty = false;
					break;
				}
			}

			if (empty && DeathChestManager.removeDeathChest(world, pos) != null) {
				event.world.setBlockState(entry.getValue().getPos(), Blocks.AIR.getDefaultState());
			}
		}
	}
}
