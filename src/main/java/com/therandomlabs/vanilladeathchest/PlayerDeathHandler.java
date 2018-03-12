package com.therandomlabs.vanilladeathchest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

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

			coords.sort(SEARCH_COMPARATOR);

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

	private static class DeathChestCallable extends TickCallbackHandler.Callable {
		private final WeakReference<EntityPlayer> player;
		private final GameProfile profile;
		private final BlockPos position;
		private final List<EntityItem> loot;
		private final boolean useDouble;

		public DeathChestCallable(World world, EntityPlayer player, List<EntityItem> loot) {
			super(world);

			this.player = new WeakReference<>(player);
			profile = player.getGameProfile();
			position = player.getPosition();

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
			if(!(tile instanceof TileEntityChest) ||
					(useDouble && !(tile instanceof TileEntityChest))) {
				LOGGER.warn("Failed to place death chest at [" + pos + "] due to invalid " +
						"tile entity");
				return false;
			}

			final TileEntityChest chest = (TileEntityChest) tile;
			MiscEventHandler.addChest(world, chest.getPos());

			LOGGER.info("Death chest for " + profile.getName() + " spawned at [" + pos + "]");

			final EntityPlayerMP playerMP =
					world.getMinecraftServer().getPlayerList().getPlayerByUUID(profile.getId());
			final ITextComponent text = new TextComponentTranslation(
					"vanillaDeathChest.chestSpawnedAt", pos.getX(), pos.getY(), pos.getZ());
			playerMP.sendMessage(new TextComponentString(text.getFormattedText()));

			final int end = useDouble ? 27 : loot.size();
			for(int j = 0; j < end && !loot.isEmpty(); j++) {
				chest.setInventorySlotContents(j, loot.get(0).getItem());
				loot.remove(0);
			}

			if(useDouble) {
				final TileEntityChest chest2 = (TileEntityChest) tile2;
				MiscEventHandler.addChest(world, chest2.getPos());

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
			int y = position.getY();
			if(world.isOutsideBuildHeight(position)) {
				y = Math.min(Math.max(y, 1), 256);
			}

			final BlockPos searchPos = new BlockPos(position.getX(), y, position.getZ());
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
			if(useDouble) {
				return canPlace2(world, player, pos) && canPlace2(world, player, pos.east());
			}

			return canPlace2(world, player, pos);
		}

		private static boolean canPlace2(World world, EntityPlayer player, BlockPos pos) {
			if(!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos)) {
				return false;
			}

			final IBlockState state = world.getBlockState(pos);
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
			final EntityPlayer player = this.player.get();
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerDrops(PlayerDropsEvent event) {
		final EntityPlayer player = event.getEntityPlayer();
		final World world = event.getEntityPlayer().getEntityWorld();
		final List<EntityItem> drops = event.getDrops();

		if(world.isRemote || player instanceof FakePlayer || drops.isEmpty()) {
			return;
		}

		final GameRules gameRules = world.getGameRules();
		if(gameRules.getBoolean("keepInventory") || gameRules.getBoolean("dontSpawnDeathChests")) {
			return;
		}

		TickCallbackHandler.addCallback(new DeathChestCallable(world, player, drops));

		event.setCanceled(true);
	}
}
