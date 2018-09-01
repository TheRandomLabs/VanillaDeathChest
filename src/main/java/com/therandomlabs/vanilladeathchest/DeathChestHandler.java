package com.therandomlabs.vanilladeathchest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.therandomlabs.vanilladeathchest.VanillaDeathChest.LOGGER;

public final class DeathChestHandler {
	public static final Comparator<BlockPos> SEARCH_COMPARATOR = (pos1, pos2) -> {
		final int difference = sumCoordinates(pos1) - sumCoordinates(pos2);
		return difference != 0 ? difference : highestCoordinate(pos2) - highestCoordinate(pos1);
	};

	public static class SearchOrder implements Iterable<BlockPos> {
		public final int size;
		private final List<BlockPos> coordinates;

		public SearchOrder(int size) {
			this.size = size;

			final List<BlockPos> coordinates = new ArrayList<>();

			for(int x = -size; x <= size; x++) {
				for(int y = -size; y <= size; y++) {
					for(int z = -size; z <= size; z++) {
						coordinates.add(new BlockPos(x, y, z));
					}
				}
			}

			this.coordinates = ImmutableList.copyOf(coordinates);
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return coordinates.iterator();
		}
	}

	public static class DCCallback extends Callback {
		private final GameProfile profile;
		private final BlockPos pos;
		private final List<EntityItem> drops;
		private final boolean useDoubleChest;

		public DCCallback(World world, EntityPlayer player, List<EntityItem> drops) {
			super(world, player);

			profile = player.getGameProfile();
			pos = player.getPosition();

			this.drops = new ArrayList<>();

			for(EntityItem entityItem : drops) {
				if(!entityItem.getItem().isEmpty()) {
					this.drops.add(entityItem);
				}
			}

			useDoubleChest = VDCConfig.spawning.useDoubleChests && drops.size() > 27;
		}

		@Override
		public void run(World world, EntityPlayer player) {
			placeDeathChest(world, player);

			//Drop any remaining items
			for(EntityItem drop : drops) {
				world.spawnEntity(drop);
			}
		}

		private void placeDeathChest(World world, EntityPlayer player) {
			final BlockPos pos = findLocation(world, player);
			final BlockPos east = pos.east();

			world.setBlockState(pos, Blocks.CHEST.getDefaultState());

			if(useDoubleChest) {
				world.setBlockState(east, Blocks.CHEST.getDefaultState());
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

			if(useDoubleChest) {
				addDeathChest(world, playerID, creationTime, pos, east);
			} else {
				addDeathChest(world, playerID, creationTime, pos);
			}

			LOGGER.info("Death chest for " + profile.getName() + " spawned at [" + pos + "]");

			player.sendMessage(new TextComponentString(String.format(
					VDCConfig.spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
			)));

			TileEntityChest chest = (TileEntityChest) tile;

			for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
				chest.setInventorySlotContents(i, drops.get(0).getItem());
				drops.remove(0);
			}

			if(useDoubleChest) {
				chest = (TileEntityChest) tile2;

				for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
					chest.setInventorySlotContents(i, drops.get(0).getItem());
					drops.remove(0);
				}
			}
		}

		private BlockPos findLocation(World world, EntityPlayer player) {
			int y = pos.getY();

			if(y < 1) {
				y = 1;
			}

			if(y > 256) {
				y = 256;
			}

			final BlockPos searchPos = new BlockPos(pos.getX(), y, pos.getZ());

			for(BlockPos c : getSearchOrder(VDCConfig.spawning.locationSearchRadius)) {
				final BlockPos potentialPos = searchPos.add(c);

				if(canPlace(world, player, potentialPos)) {
					return potentialPos;
				}
			}

			return null;
		}

		private boolean canPlace(World world, EntityPlayer player, BlockPos pos) {
			if(useDoubleChest) {
				return canPlaceSingle(world, player, pos) &&
						canPlaceSingle(world, player, pos.east());
			}

			return canPlaceSingle(world, player, pos);
		}
	}

	private static SearchOrder searchOrder;

	private DeathChestHandler() {}

	public static Iterable<BlockPos> getSearchOrder(int size) {
		if(searchOrder == null || searchOrder.size != size) {
			searchOrder = new SearchOrder(size);
		}

		return searchOrder;
	}

	public static boolean canPlaceSingle(World world, EntityPlayer player, BlockPos pos) {
		if(!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos)) {
			return false;
		}

		final IBlockState state = world.getBlockState(pos);
		final IBlockState state2 = world.getBlockState(pos.up());

		final BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(
				player,
				ItemStack.EMPTY,
				pos,
				EnumFacing.DOWN,
				0.0F,
				0.0F,
				0.0F
		));

		if((state.isAir() || state.isReplaceable(context)) &&
				(state2.isAir() || state2.isReplaceable(context))) {
			return isNotChest(world, pos.north()) && isNotChest(world, pos.east()) &&
					isNotChest(world, pos.south()) && isNotChest(world, pos.west());
		}

		return false;
	}

	public static void addDeathChest(World world, UUID playerID, long creationTime,
			BlockPos... positions) {
		final VDCSavedData data = VDCSavedData.get(world);
		final Map<BlockPos, DeathChest> deathChests = data.getDeathChests();
		final DeathChest deathChest = new DeathChest(playerID, creationTime);

		for(BlockPos pos : positions) {
			deathChests.put(pos, deathChest);
		}

		data.markDirty();
	}

	public static DeathChest getDeathChest(World world, BlockPos pos) {
		if(world.getBlockState(pos).getBlock() != Blocks.CHEST) {
			return null;
		}

		final Map<BlockPos, DeathChest> deathChests = VDCSavedData.get(world).getDeathChests();
		DeathChest deathChest = deathChests.get(pos);

		if(deathChest == null) {
			return null;
		}

		return deathChest;
	}

	public static void removeDeathChest(World world, BlockPos pos) {
		final Map<BlockPos, DeathChest> deathChests = VDCSavedData.get(world).getDeathChests();
		deathChests.remove(pos);
		deathChests.remove(pos.east());
	}

	public static void onPlayerDeath(World world, EntityPlayer player, List<EntityItem> drops) {
		final GameRules gameRules = world.getGameRules();

		if(gameRules.getBoolean("keepInventory")) {
			return;
		}

		if(!(VDCConfig.misc.gameruleName.isEmpty() ||
				gameRules.getBoolean(VDCConfig.misc.gameruleName))) {
			return;
		}

		final Queue<Callback> callbacks = CallbackHandler.getCallbacks(world);
		callbacks.add(new DeathChestHandler.DCCallback(world, player, drops));
	}

	public static boolean onBlockInteract(World world, EntityPlayer player, BlockPos pos,
			CallbackInfoReturnable<?> info) {
		final DeathChest deathChest = DeathChestHandler.getDeathChest(world, pos);

		if(deathChest == null) {
			return false;
		}

		if(deathChest.canInteract(player)) {
			return true;
		}

		info.cancel();

		return false;
	}

	private static int sumCoordinates(BlockPos pos) {
		return Math.abs(pos.getX()) + Math.abs(pos.getY()) + Math.abs(pos.getZ());
	}

	private static int highestCoordinate(BlockPos pos) {
		return Math.max(Math.max(Math.abs(pos.getX()), Math.abs(pos.getY())), Math.abs(pos.getZ()));
	}

	private static boolean isNotChest(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() != Blocks.CHEST;
	}
}
