package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import com.mojang.authlib.GameProfile;
import com.therandomlabs.randomlib.BooleanWrapper;
import com.therandomlabs.randomlib.TRLUtils;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public final class DeathChestPlacer {
	public enum DeathChestType {
		SINGLE_ONLY("singleOnly"),
		SINGLE_OR_DOUBLE("singleOrDouble");

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
		final DeathChestType type = VDCConfig.Spawning.chestType;

		final GameProfile profile = player.getGameProfile();
		final BlockPos playerPos = player.getPosition();

		boolean useDoubleChest =
				type == DeathChestType.SINGLE_OR_DOUBLE && drops.size() > 27;

		final BooleanWrapper doubleChest = new BooleanWrapper(useDoubleChest);

		final BlockPos pos =
				DeathChestLocationFinder.findLocation(world, player, playerPos, doubleChest);

		useDoubleChest = doubleChest.get();

		if(pos == null) {
			VanillaDeathChest.LOGGER.warn(
					"No death chest location found for player at [{}]", playerPos
			);
			return;
		}

		final IBlockState state = Blocks.CHEST.getDefaultState();
		final BlockPos east = pos.east();

		world.setBlockState(pos, state);

		if(useDoubleChest) {
			world.setBlockState(east, state);
		}

		final TileEntity tile = world.getTileEntity(pos);
		final TileEntity tile2 = useDoubleChest ? world.getTileEntity(east) : null;

		if(!(tile instanceof TileEntityChest) ||
				(useDoubleChest && !(tile2 instanceof TileEntityChest))) {
			VanillaDeathChest.LOGGER.warn(
					"Failed to place death chest at [{}] due to invalid tile entity", pos
			);
			return;
		}

		TileEntityChest chest = (TileEntityChest) tile;

		for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
			chest.setInventorySlotContents(i, drops.get(0).getEntityItem());
			drops.remove(0);
		}

		if(useDoubleChest) {
			chest = (TileEntityChest) tile2;

			for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
				chest.setInventorySlotContents(i, drops.get(0).getEntityItem());
				drops.remove(0);
			}
		}

		if(!VDCConfig.Spawning.containerDisplayName.isEmpty()) {
			chest.setCustomName(VDCConfig.Spawning.containerDisplayName);
		}

		if(!VDCConfig.Defense.defenseEntityRegistryName.isEmpty()) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for(int i = 0; i < VDCConfig.Defense.defenseEntitySpawnCount; i++) {
				NBTTagCompound compound = null;

				try {
					compound = JsonToNBT.getTagFromJson(VDCConfig.Defense.defenseEntityNBT);
				} catch(NBTException ignored) {}

				compound.setString("id", VDCConfig.Defense.defenseEntityRegistryName);

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

					data.setTag("DeathChestPlayer", NBTUtil.createUUIDTag(player.getUniqueID()));
					data.setTag("DeathChestPos", NBTUtil.createPosTag(pos));
				}
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		if(!VDCConfig.Spawning.chatMessage.isEmpty()) {
			player.sendMessage(new TextComponentString(String.format(
					VDCConfig.Spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
			)));
		}
	}
}
