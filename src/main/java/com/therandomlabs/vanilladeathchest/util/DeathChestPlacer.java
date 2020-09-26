package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;
import com.therandomlabs.randomlib.BooleanWrapper;
import com.therandomlabs.randomlib.TRLUtils;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import com.therandomlabs.vanilladeathchest.gamestages.VDCStageInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public final class DeathChestPlacer {
	public enum DeathChestType {
		SINGLE_ONLY("singleOnly"),
		SINGLE_OR_DOUBLE("singleOrDouble"),
		SHULKER_BOX("shulkerBox"),
		DOUBLE_SHULKER_BOX("doubleShulkerBox");

		private final String translationKey;

		DeathChestType(String translationKey) {
			this.translationKey = "vanilladeathchest.config.spawning.chestType." + translationKey;
		}

		@Override
		public String toString() {
			return translationKey;
		}
	}

	private static final Method BECOME_ANGRY_AT = TRLUtils.findMethod(
			EntityPigZombie.class, "becomeAngryAt", "func_70835_c", Entity.class
	);

	private final WeakReference<World> world;
	private final WeakReference<EntityPlayer> player;
	private final List<EntityItem> drops;

	private boolean alreadyCalled;

	public DeathChestPlacer(World world, EntityPlayer player, List<EntityItem> drops) {
		this.world = new WeakReference<>(world);
		this.player = new WeakReference<>(player);
		//Create a new ArrayList since CatServer seems to empty the drops List.
		//Plus, it's probably good practice.
		this.drops = new ArrayList<>(drops);
	}

	public final boolean run() {
		//Delay by a tick to avoid conflicts with other mods that place blocks upon death
		if (!alreadyCalled) {
			alreadyCalled = true;
			return false;
		}

		final World world = this.world.get();

		if (world == null) {
			return true;
		}

		final EntityPlayer player = this.player.get();

		if (player == null) {
			return true;
		}

		place(world, player);

		//Drop any remaining items
		for (EntityItem drop : drops) {
			world.spawnEntity(drop);
		}

		return true;
	}

	@SuppressWarnings("Duplicates")
	private void place(World world, EntityPlayer player) {
		final DeathChestType type = VDCConfig.Spawning.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = player.getPosition();

		final VDCStageInfo info = VDCStageInfo.get(player);

		final Pattern pattern = Pattern.compile(info.getRegistryNameRegex());
		final List<EntityItem> filtered = drops.stream().
				filter(item -> pattern.matcher(
						item.getItem().getItem().getRegistryName().toString()
				).matches()).
				collect(Collectors.toList());

		boolean useDoubleChest = (type == DeathChestType.SINGLE_OR_DOUBLE ||
				type == DeathChestType.DOUBLE_SHULKER_BOX) && filtered.size() > 27;

		if (info.useContainerInInventory()) {
			final List<EntityItem> empty = new ArrayList<>();

			boolean foundOne = false;
			boolean foundAll = false;

			for (EntityItem item : drops) {
				final ItemStack stack = item.getItem();

				if (type == DeathChestType.SINGLE_ONLY || type == DeathChestType.SINGLE_OR_DOUBLE) {
					if (stack.getItem() != Item.getItemFromBlock(Blocks.CHEST)) {
						continue;
					}
				} else {
					if (!(Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox)) {
						continue;
					}

					final NBTTagCompound compound = stack.getTagCompound();

					if (compound != null) {
						final NonNullList<ItemStack> inventory =
								NonNullList.withSize(27, ItemStack.EMPTY);
						ItemStackHelper.loadAllItems(
								compound.getCompoundTag("BlockEntityTag"), inventory
						);

						//Shulker box must be empty.
						if (inventory.stream().anyMatch(itemStack -> !itemStack.isEmpty())) {
							continue;
						}
					}
				}

				if (!useDoubleChest) {
					stack.shrink(1);

					if (stack.isEmpty()) {
						empty.add(item);
					}

					foundAll = true;
					break;
				}

				if (stack.getCount() > 1) {
					stack.shrink(2);

					if (stack.isEmpty()) {
						empty.add(item);
					}

					foundAll = true;
					break;
				}

				stack.shrink(1);

				if (stack.isEmpty()) {
					empty.add(item);
				}

				if (foundOne) {
					foundAll = true;
					break;
				}

				foundOne = true;
			}

			if (useDoubleChest) {
				if (!foundAll) {
					if (!foundOne) {
						return;
					}

					useDoubleChest = false;
				}
			} else if (!foundAll) {
				return;
			}

			drops.removeAll(empty);
			filtered.removeAll(empty);
		}

		final BooleanWrapper doubleChest = new BooleanWrapper(useDoubleChest);

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, doubleChest);

		useDoubleChest = doubleChest.get();

		if (pos == null) {
			VanillaDeathChest.LOGGER.warn(
					"No death chest location found for player at [{}]", playerPos
			);
			return;
		}

		final Block block;

		if (type == DeathChestType.SHULKER_BOX || type == DeathChestType.DOUBLE_SHULKER_BOX) {
			block = BlockShulkerBox.getBlockByColor(VDCConfig.Spawning.shulkerBoxColor.get());
		} else {
			block = Blocks.CHEST;
		}

		final IBlockState state = block.getDefaultState();
		final BlockPos east = pos.east();

		world.setBlockState(pos, state);

		if (useDoubleChest) {
			world.setBlockState(east, state);
		}

		final TileEntity tile = world.getTileEntity(pos);
		final TileEntity tile2 = useDoubleChest ? world.getTileEntity(east) : null;

		if (!(tile instanceof TileEntityLockableLoot) ||
				(useDoubleChest && !(tile2 instanceof TileEntityLockableLoot))) {
			VanillaDeathChest.LOGGER.warn(
					"Failed to place death chest at [{}] due to invalid tile entity", pos
			);
			return;
		}

		TileEntityLockableLoot chest = (TileEntityLockableLoot) tile;

		for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
			final EntityItem item = filtered.get(0);
			chest.setInventorySlotContents(i, item.getItem());
			filtered.remove(0);
			drops.remove(item);
		}

		final String displayName = info.getContainerDisplayName();

		if (!displayName.isEmpty()) {
			chest.setCustomName(displayName);
		}

		if (useDoubleChest) {
			chest = (TileEntityLockableLoot) tile2;

			for (int i = 0; i < 27 && !filtered.isEmpty(); i++) {
				final EntityItem item = filtered.get(0);
				chest.setInventorySlotContents(i, item.getItem());
				filtered.remove(0);
				drops.remove(item);
			}

			//If this is a shulker box, this has to be set separately.
			if (!displayName.isEmpty()) {
				chest.setCustomName(displayName);
			}
		}

		if (info.getDefenseEntity() != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			final int count = info.getDefenseEntitySpawnCount();
			final String nbt = info.getDefenseEntityNBT();
			final String registryName = info.getDefenseEntityRegistryName();

			for (int i = 0; i < count; i++) {
				NBTTagCompound compound = null;

				try {
					compound = JsonToNBT.getTagFromJson(nbt);
				} catch (NBTException ignored) {}

				compound.setString("id", registryName);

				final Entity entity =
						AnvilChunkLoader.readWorldEntityPos(compound, world, x, y, z, true);

				if (entity instanceof EntityLiving) {
					final EntityLiving living = (EntityLiving) entity;

					living.enablePersistence();
					living.onInitialSpawn(world.getDifficultyForLocation(pos), null);

					if (living instanceof EntityPigZombie) {
						try {
							BECOME_ANGRY_AT.invoke(living, player);
						} catch (IllegalAccessException | InvocationTargetException ex) {
							VanillaDeathChest.LOGGER.error(
									"Failed to make zombie pigman angry", ex
							);
						}
					}

					final NBTTagCompound data = living.getEntityData();

					data.setTag("DeathChestPlayer", NBTUtil.createUUIDTag(player.getUniqueID()));
					data.setTag("DeathChestPos", NBTUtil.createPosTag(pos));
				}
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		final String chatMessage = info.getChatMessage();

		if (!chatMessage.isEmpty()) {
			player.sendMessage(new TextComponentString(String.format(
					chatMessage, pos.getX(), pos.getY(), pos.getZ()
			)));
		}
	}
}
