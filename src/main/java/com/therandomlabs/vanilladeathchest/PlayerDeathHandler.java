package com.therandomlabs.vanilladeathchest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.Logger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = VanillaDeathChest.MODID)
public class PlayerDeathHandler {
	static final Comparator<BlockPos> SEARCH_COMPARATOR = new Comparator<BlockPos>() {
		private int coordSum(BlockPos c) {
			return Math.abs(c.getX()) + Math.abs(c.getY()) + Math.abs(c.getZ());
		}

		private int coordMax(BlockPos c) {
			return Math.max(Math.max(Math.abs(c.getX()), Math.abs(c.getY())), Math.abs(c.getZ()));
		}

		@Override
		public int compare(BlockPos a, BlockPos b) {
			final int diff = coordSum(a) - coordSum(b);
			return diff != 0 ? diff : coordMax(b) - coordMax(a);
		}
	};

	private static class SearchOrder implements Iterable<BlockPos> {
		public final int size;

		private final List<BlockPos> coords;

		public SearchOrder(int size) {
			this.size = size;

			final List<BlockPos> coords = Lists.newArrayList();

			for(int x = -size; x <= size; x++) {
				for(int y = -size; y <= size; y++) {
					for(int z = -size; z <= size; z++) {
						coords.add(new BlockPos(x, y, z));
					}
				}
			}

			Collections.sort(coords, SEARCH_COMPARATOR);

			this.coords = ImmutableList.copyOf(coords);
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return coords.iterator();
		}
	}

	static final Logger LOGGER = VanillaDeathChest.LOGGER;
	private static SearchOrder searchOrder;

	static Iterable<BlockPos> getSearchOrder(int size) {
		if(searchOrder == null || searchOrder.size != size) {
			searchOrder = new SearchOrder(size);
		}
		return searchOrder;
	}

	private static class DeathChestCallable implements Runnable {
		private final GameProfile stiffId;
		private final BlockPos playerPos;
		private final List<EntityItem> loot;
		private final WeakReference<World> world;
		private final WeakReference<EntityPlayer> exPlayer;
		private final boolean useDouble;

		public DeathChestCallable(World world, EntityPlayer exPlayer, List<EntityItem> loot) {
			playerPos = exPlayer.getPosition();

			this.world = new WeakReference<>(world);

			this.exPlayer = new WeakReference<>(exPlayer);
			stiffId = exPlayer.getGameProfile();

			this.loot = new ArrayList<>();
			for(EntityItem entityItem : loot) {
				if(!entityItem.getItem().isEmpty()) {
					this.loot.add(entityItem);
				}
			}
			useDouble = loot.size() > 27;
		}

		private boolean tryPlaceDeathChest(World world, BlockPos pos) {
			world.setBlockState(pos, Blocks.CHEST.getDefaultState());
			if(useDouble) {
				world.setBlockState(pos.east(), Blocks.CHEST.getDefaultState());
			}

			final TileEntity tile = world.getTileEntity(pos);
			final TileEntity tile2 = useDouble ? world.getTileEntity(pos.east()) : null;
			if((tile == null || !(tile instanceof TileEntityChest)) ||
					useDouble && (tile2 == null || !(tile instanceof TileEntityChest))) {
				LOGGER.warn("Failed to place death chest at [" + pos + "] due to invalid " +
						"tile entity");
				return false;
			}

			final TileEntityChest chest = (TileEntityChest) tile;

			LOGGER.info("Death chest for " + stiffId.getName() + " spawned at [" + pos + "]");

			final int end = useDouble ? 27 : loot.size();
			for(int j = 0; j < end && !loot.isEmpty(); j++) {
				chest.setInventorySlotContents(j, loot.get(0).getItem());
				loot.remove(0);
			}

			if(useDouble) {
				final TileEntityChest chest2 = (TileEntityChest) tile2;

				for(int j = 0; j < 27 && !loot.isEmpty(); j++) {
					chest2.setInventorySlotContents(j, loot.get(0).getItem());
					loot.remove(0);
				}
			}

			return true;
		}

		private boolean trySpawnDeathChest(EntityPlayer player, World world) {
			return tryPlaceDeathChest(world, findLocation(world, player));
		}

		private BlockPos findLocation(World world, EntityPlayer player) {
			final int limitedPosY = Math.min(Math.max(playerPos.getY(), 1), 256);
			final BlockPos searchPos =
					new BlockPos(playerPos.getX(), limitedPosY, playerPos.getZ());
			final int searchSize = 7;

			for(BlockPos c : getSearchOrder(searchSize)) {
				final BlockPos tryPos = searchPos.add(c);
				if(canPlace(world, player, tryPos)) {
					return tryPos;
				}
			}

			return null;
		}

		private boolean canPlace(World world, EntityPlayer player, BlockPos pos) {
			if(!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos)) {
				return false;
			}

			return checkBlock(world, pos, world.getBlockState(pos));
		}

		private boolean checkBlock(World world, BlockPos pos, IBlockState state) {
			if(useDouble) {
				return checkBlock2(world, pos, state) && checkBlock2(world, pos.east(), state);
			}
			return checkBlock2(world, pos, state);
		}

		private static boolean checkBlock2(World world, BlockPos pos, IBlockState state) {
			final Block block = state.getBlock();
			if(block.isAir(state, world, pos) || block.isReplaceable(world, pos)) {
				return notChest(world, pos.north()) && notChest(world, pos.east()) &&
						notChest(world, pos.south()) && notChest(world, pos.west());
			}
			return false;
		}

		private static boolean notChest(World world, BlockPos pos) {
			return world.getBlockState(pos).getBlock() != Blocks.CHEST;
		}

		@Override
		public void run() {
			final EntityPlayer player = exPlayer.get();
			if(player == null) {
				return;
			}

			final World world = this.world.get();
			if(world == null) {
				return;
			}

			trySpawnDeathChest(player, world);

			//Drop any remaining loot
			for(EntityItem drop : loot) {
				world.spawnEntity(drop);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerDrops(PlayerDropsEvent event) {
		final EntityPlayer player = event.getEntityPlayer();
		final World world = event.getEntityPlayer().getEntityWorld();
		final List<EntityItem> drops = event.getDrops();

		if(world.isRemote || player instanceof FakePlayer || drops.isEmpty()) {
			return;
		}

		final GameRules gameRules = world.getGameRules();
		if(gameRules.getBoolean("keepInventory") || !gameRules.getBoolean("spawnDeathChests")) {
			return;
		}

		MiscEventHandler.addTickCallback(world, new DeathChestCallable(world, player, drops));

		event.setCanceled(true);
	}
}
