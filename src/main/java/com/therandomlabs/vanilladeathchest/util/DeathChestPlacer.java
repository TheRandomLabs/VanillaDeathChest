package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import com.mojang.authlib.GameProfile;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.gamestages.DeathChestStageInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

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

	private static final Method BECOME_ANGRY_AT = VanillaDeathChest.findMethod(
			EntityPigZombie.class, "becomeAngryAt", "func_70835_c", Entity.class
	);

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
			VanillaDeathChest.LOGGER.warn("No death chest location found for player at [{}]", pos);
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
			VanillaDeathChest.LOGGER.warn(
					"Failed to place death chest at [{}] due to invalid tile entity", pos
			);
			return;
		}

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

		final DeathChestStageInfo info = DeathChestStageInfo.get(player);

		if(info.getDefenseEntity() != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			final int count = info.getDefenseEntitySpawnCount();
			final String nbt = info.getDefenseEntityNBT();
			final String registryName = info.getDefenseEntityRegistryName();

			for(int i = 0; i < count; i++) {
				NBTTagCompound compound = null;

				try {
					compound = JsonToNBT.getTagFromJson(nbt);
				} catch(NBTException ignored) {}

				compound.setString("id", registryName);

				final Entity entity =
						AnvilChunkLoader.readWorldEntityPos(compound, world, x, y, z, true);

				if(entity instanceof EntityLiving) {
					final EntityLiving living = (EntityLiving) entity;

					living.enablePersistence();
					living.onInitialSpawn(world.getDifficultyForLocation(pos), null);

					if(living instanceof EntityPigZombie) {
						try {
							BECOME_ANGRY_AT.invoke(living, player);
						} catch(IllegalAccessException | InvocationTargetException ex) {
							VanillaDeathChest.LOGGER.error(
									"Failed to make zombie pigman angry", ex
							);
						}
					}

					final NBTTagCompound data = living.getEntityData();

					data.setTag("DeathChestPos", NBTUtil.createPosTag(pos));
					data.setTag("DeathChestPlayer", NBTUtil.createUUIDTag(player.getUniqueID()));
				}
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		player.sendMessage(new TextComponentString(String.format(
				VDCConfig.spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)));
	}
}
