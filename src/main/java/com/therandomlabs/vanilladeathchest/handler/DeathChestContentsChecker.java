package com.therandomlabs.vanilladeathchest.handler;

import java.util.Map;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import com.therandomlabs.vanilladeathchest.world.storage.VDCSavedData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestContentsChecker {
	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if (!VDCConfig.Misc.deathChestsDisappearWhenEmptied) {
			return;
		}

		final VDCSavedData savedData = VDCSavedData.get(event.world);
		final IChunkProvider provider = event.world.getChunkProvider();

		for (Map.Entry<BlockPos, DeathChest> entry : savedData.getDeathChests().entrySet()) {
			final BlockPos pos = entry.getKey();

			//Make sure we don't unnecessarily load any chunks
			final Chunk chunk = provider.getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);

			if (chunk == null || chunk.isEmpty()) {
				continue;
			}

			final TileEntity tileEntity = event.world.getTileEntity(pos);

			if (!(tileEntity instanceof TileEntityLockableLoot)) {
				continue;
			}

			final TileEntityLockableLoot lockableLoot = (TileEntityLockableLoot) tileEntity;
			boolean empty = true;

			for (int i = 0; i < lockableLoot.getSizeInventory(); i++) {
				if (!lockableLoot.getStackInSlot(i).isEmpty()) {
					empty = false;
					break;
				}
			}

			if (empty) {
				DeathChestManager.removeDeathChest(event.world, pos);

				event.world.setBlockToAir(pos);

				if (entry.getValue().isDoubleChest()) {
					event.world.setBlockToAir(pos.east());
				}
			}
		}
	}
}
