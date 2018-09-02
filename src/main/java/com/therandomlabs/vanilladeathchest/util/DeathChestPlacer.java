package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import static com.therandomlabs.vanilladeathchest.VanillaDeathChest.LOGGER;

public final class DeathChestPlacer {
	private final WeakReference<World> world;
	private final WeakReference<EntityPlayer> player;
	private final List<EntityItem> drops;

	private boolean alreadyCalled;

	public DeathChestPlacer(World world, EntityPlayer player, List<EntityItem> drops) {
		this.world = new WeakReference<>(world);
		this.player = new WeakReference<>(player);
		this.drops = drops;
	}

	public final boolean run() {
		//Delay by a tick to avoid conflicts with other mods that place blocks upon death
		if(!alreadyCalled) {
			alreadyCalled = true;
			return false;
		}

		final World world = this.world.get();

		if(world == null) {
			return true;
		}

		final EntityPlayer player = this.player.get();

		if(player == null) {
			return true;
		}

		place(world, player);

		//Drop any remaining items
		for(EntityItem drop : drops) {
			world.spawnEntity(drop);
		}

		return true;
	}

	private void place(World world, EntityPlayer player) {
		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = player.getPosition();
		final boolean useDoubleChest = VDCConfig.spawning.useDoubleChests && drops.size() > 27;

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, useDoubleChest);
		final BlockPos east = pos.east();

		if(useDoubleChest) {
			world.setBlockState(pos, Blocks.CHEST.getDefaultState().withProperty(
					BlockChest.TYPE,
					ChestType.LEFT
			));
			world.setBlockState(east, Blocks.CHEST.getDefaultState().withProperty(
					BlockChest.TYPE,
					ChestType.RIGHT
			));
		} else {
			world.setBlockState(pos, Blocks.CHEST.getDefaultState());
		}

		final TileEntity tile = world.getTileEntity(pos);
		final TileEntity tile2 = useDoubleChest ? world.getTileEntity(east) : null;

		if(!(tile instanceof TileEntityChest) ||
				(useDoubleChest && !(tile2 instanceof TileEntityChest))) {
			LOGGER.warn("Failed to place death chest at [" + pos + "] due to invalid " +
					"tile entity");
			return;
		}

		final UUID playerID = player.getUniqueID();
		final long creationTime = world.getTotalWorldTime();

		DeathChestManager.addDeathChest(world, playerID, creationTime, pos, useDoubleChest);

		LOGGER.info("Death chest for " + profile.getName() + " spawned at [" + pos + "]");

		player.sendMessage(new TextComponentString(String.format(
				VDCConfig.spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)));

		TileEntityChest chest = (TileEntityChest) (useDoubleChest ? tile2 : tile);

		for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
			chest.setInventorySlotContents(i, drops.get(0).getItem());
			drops.remove(0);
		}

		if(useDoubleChest) {
			chest = (TileEntityChest) tile;

			for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
				chest.setInventorySlotContents(i, drops.get(0).getItem());
				drops.remove(0);
			}
		}

		//Drop any remaining items
		for(EntityItem drop : drops) {
			world.spawnEntity(drop);
		}
	}
}
