package com.therandomlabs.vanilladeathchest;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import jline.internal.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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
			// first order by Manhattan distance
			int diff = coordSum(a) - coordSum(b);
			if (diff != 0) return diff;

			// then by distance from axis
			return coordMax(b) - coordMax(a);
		}
	};

	private static class SearchOrder implements Iterable<BlockPos> {
		public final int size;

		private final List<BlockPos> coords;

		public SearchOrder(int size) {
			this.size = size;

			List<BlockPos> coords = Lists.newArrayList();

			for (int x = -size; x <= size; x++)
				for (int y = -size; y <= size; y++)
					for (int z = -size; z <= size; z++)
						coords.add(new BlockPos(x, y, z));

			Collections.sort(coords, SEARCH_COMPARATOR);

			this.coords = ImmutableList.copyOf(coords);
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return coords.iterator();
		}
	}

	private static SearchOrder searchOrder;

	static Iterable<BlockPos> getSearchOrder(int size) {
		if (searchOrder == null || searchOrder.size != size) searchOrder = new SearchOrder(size);
		return searchOrder;
	}

	private abstract static class GravePlacementChecker {
		public GravePlacementChecker() {}

		public boolean canPlace(World world, EntityPlayer player, BlockPos pos) {
			if (!world.isBlockLoaded(pos)) return false;
			if (!world.isBlockModifiable(player, pos)) return false;

			IBlockState block = world.getBlockState(pos);
			return checkBlock(world, pos, block);
		}

		public abstract boolean checkBlock(World world, BlockPos pos, IBlockState state);
	}

	static final GravePlacementChecker POLITE = new GravePlacementChecker() {
		@Override
		public boolean checkBlock(World world, BlockPos pos, IBlockState state) {
			return checkBlock2(world, pos, state) && checkBlock2(world, pos.east(), state);
		}

		private boolean checkBlock2(World world, BlockPos pos, IBlockState state) {
			final Block block = state.getBlock();
			return (block.isAir(state, world, pos) || block.isReplaceable(world, pos));
		}
	};

	private static class GraveCallable implements Runnable {

		private final GameProfile stiffId;

		private final BlockPos playerPos;

		private final List<EntityItem> loot;

		private final WeakReference<World> world;

		private final WeakReference<EntityPlayer> exPlayer;

		public GraveCallable(World world, EntityPlayer exPlayer, List<EntityItem> loot) {
			this.playerPos = exPlayer.getPosition();

			this.world = new WeakReference<>(world);

			this.exPlayer = new WeakReference<>(exPlayer);
			this.stiffId = exPlayer.getGameProfile();
			this.loot = ImmutableList.copyOf(loot);
		}

		private boolean tryPlaceGrave(World world, final BlockPos gravePos) {
			world.setBlockState(gravePos, Blocks.CHEST.getDefaultState());
			world.setBlockState(gravePos.east(), Blocks.CHEST.getDefaultState());
			TileEntity tile = world.getTileEntity(gravePos);
			if (tile == null || !(tile instanceof TileEntityChest)) {
				Log.warn("Failed to place death chest @ %s: invalid tile entity: %s(%s)", gravePos, tile, tile != null? tile.getClass() : "?");
				return false;
			}

			TileEntityChest chest = (TileEntityChest)tile;

			IInventory loot = getLoot();

			Log.info("Death chest for (%s,%s) was spawned at (%s) (player died at (%s))", stiffId.getId(), stiffId.getName(), gravePos, playerPos);

			for(int i = 0, j = 0; i < loot.getSizeInventory(); i++) {
				if(!loot.getStackInSlot(i).isEmpty()) {
					chest.setInventorySlotContents(j++, loot.getStackInSlot(i));
				}
			}

			return true;
		}

		protected IInventory getLoot() {
			final GenericInventory loot = new GenericInventory("tmpplayer", false, this.loot.size());
			final IItemHandler handler = loot.getHandler();
			for (EntityItem entityItem : this.loot) {
				ItemStack stack = entityItem.getItem();
				if (!stack.isEmpty()) ItemHandlerHelper.insertItemStacked(handler, stack, false);
			}
			return loot;
		}

		private boolean trySpawnGrave(EntityPlayer player, World world) {
			final BlockPos location = findLocation(world, player);

			Log.debug("Death chest for %s will be spawned at (%s)", stiffId, location);

			return tryPlaceGrave(world, location);
		}

		private BlockPos findLocation(World world, EntityPlayer player, GravePlacementChecker checker) {
			final int limitedPosY = Math.min(Math.max(playerPos.getY(), 1), 256);
			BlockPos searchPos = new BlockPos(playerPos.getX(), limitedPosY, playerPos.getZ());
			final int searchSize = 5;

			for (BlockPos c : getSearchOrder(searchSize)) {
				final BlockPos tryPos = searchPos.add(c);
				if (checker.canPlace(world, player, tryPos)) return tryPos;
			}

			return null;
		}

		private BlockPos findLocation(World world, EntityPlayer player) {
			BlockPos location = findLocation(world, player, POLITE);
			if (location != null) return location;
			return null;
		}


		@Override
		public void run() {
			EntityPlayer player = exPlayer.get();
			if (player == null) {
				Log.warn("Lost player while placing player %s death chest", stiffId);
				return;
			}

			World world = this.world.get();
			if (world == null) {
				Log.warn("Lost world while placing player %s death chest", stiffId);
				return;
			}

			if (!trySpawnGrave(player, world)) {
				for (EntityItem drop : loot)
					world.spawnEntity(drop);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public static void onPlayerDrops(PlayerDropsEvent event) {
		World world = event.getEntityPlayer().world;
		if (world.isRemote) return;

		final EntityPlayer player = event.getEntityPlayer();

		if (player instanceof FakePlayer) {
			Log.debug("'%s' (%s) is a fake player, death chest will not be spawned", player, player.getClass());
			return;
		}

		if (event.isCanceled()) {
			Log.warn("Event for player '%s' cancelled, death chest will not be spawned", player);
			return;
		}

		final List<EntityItem> drops = event.getDrops();
		if (drops.isEmpty()) {
			Log.debug("No drops from player '%s', death chest will not be spawned'", player);
			return;
		}

		final GameRules gameRules = world.getGameRules();
		if (gameRules.getBoolean("keepInventory") ||
				gameRules.getBoolean("dontSpawnDeathChests")) {
			Log.debug("Graves disabled by gamerule (player '%s')", player);
			return;
		}

		Log.debug("Scheduling death chest placement for player '%s':'%s' with %d item(s) stored",
				player, player.getGameProfile(), drops.size());

		DelayedActionTickHandler.addTickCallback(world, new GraveCallable(world, player, drops));

		event.setCanceled(true);
	}
}
