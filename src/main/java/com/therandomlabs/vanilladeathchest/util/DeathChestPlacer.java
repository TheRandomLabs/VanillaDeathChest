package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import static com.therandomlabs.vanilladeathchest.VanillaDeathChest.LOGGER;

public final class DeathChestPlacer {
	public enum DeathChestType {
		SINGLE_ONLY("singleOnly"),
		SINGLE_OR_DOUBLE("singleOrDouble"),
		SHULKER_BOX("shulkerBox"),
		RANDOM_SHULKER_BOX_COLOR("randomShulkerBoxColor");

		private final String translationKey;

		DeathChestType(String translationKey) {
			this.translationKey = "vanilladeathchest.config.spawning.chestType." + translationKey;
		}

		@Override
		public String toString() {
			return translationKey;
		}
	}

	private static final Random random = new Random();

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
		final DeathChestType type = VDCConfig.spawning.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = player.getPosition();

		boolean useDoubleChest =
				type == DeathChestType.SINGLE_OR_DOUBLE && drops.size() > 27;

		final BooleanWrapper doubleChest = new BooleanWrapper(useDoubleChest);

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, doubleChest);

		useDoubleChest = doubleChest.get();

		if(pos == null) {
			LOGGER.warn("No death chest location found for player at [%s]", pos);
			return;
		}

		final Block block;

		if(type == DeathChestType.SHULKER_BOX) {
			block = BlockShulkerBox.getBlockByColor(VDCConfig.spawning.shulkerBoxColor.get());
		} else if(type == DeathChestType.RANDOM_SHULKER_BOX_COLOR) {
			block = BlockShulkerBox.getBlockByColor(EnumDyeColor.byMetadata(random.nextInt(16)));
		} else {
			block = Blocks.CHEST;
		}

		final IBlockState state = block.getDefaultState();
		final BlockPos east = pos.east();

		world.setBlockState(pos, state);

		if(useDoubleChest) {
			world.setBlockState(east, state);
		}

		final TileEntity tile = world.getTileEntity(pos);
		final TileEntity tile2 = useDoubleChest ? world.getTileEntity(east) : null;

		if(!(tile instanceof TileEntityLockableLoot) ||
				(useDoubleChest && !(tile2 instanceof TileEntityLockableLoot))) {
			LOGGER.warn("Failed to place death chest at [%s] due to invalid tile entity", pos);
			return;
		}

		final UUID playerID = player.getUniqueID();
		final long creationTime = world.getTotalWorldTime();

		DeathChestManager.addDeathChest(world, playerID, creationTime, pos, useDoubleChest);

		LOGGER.info("Death chest for %s spawned at [%s]", profile.getName(), pos);

		player.sendMessage(new TextComponentString(String.format(
				VDCConfig.spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)));

		TileEntityLockableLoot chest = (TileEntityLockableLoot) tile;

		for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
			chest.setInventorySlotContents(i, drops.get(0).getItem());
			drops.remove(0);
		}

		if(useDoubleChest) {
			chest = (TileEntityLockableLoot) tile2;

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
