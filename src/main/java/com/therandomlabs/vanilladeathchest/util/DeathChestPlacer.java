package com.therandomlabs.vanilladeathchest.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.api.IAngerable;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestManager;
import com.therandomlabs.vanilladeathchest.config.VDCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public final class DeathChestPlacer {
	public enum DeathChestType {
		SINGLE_ONLY,
		SINGLE_OR_DOUBLE,
		SHULKER_BOX,
		RANDOM_SHULKER_BOX_COLOR
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
			VanillaDeathChest.LOGGER.warn(
					"No death chest location found for player at [{}]", playerPos
			);
			return;
		}

		final Block block;

		if(type == DeathChestType.SHULKER_BOX) {
			block = BlockShulkerBox.getBlockByColor(VDCConfig.spawning.shulkerBoxColor);
		} else if(type == DeathChestType.RANDOM_SHULKER_BOX_COLOR) {
			block = BlockShulkerBox.getBlockByColor(EnumDyeColor.byId(random.nextInt(16)));
		} else {
			block = Blocks.CHEST;
		}

		final IBlockState state = block.getDefaultState();
		final BlockPos east = pos.east();

		if(useDoubleChest) {
			world.setBlockState(pos, state.withProperty(BlockChest.TYPE, ChestType.LEFT));
			world.setBlockState(east, state.withProperty(BlockChest.TYPE, ChestType.RIGHT));
		} else {
			world.setBlockState(pos, state);
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

		TileEntityLockableLoot chest = (TileEntityLockableLoot) (useDoubleChest ? tile2 : tile);

		for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
			chest.setInventorySlotContents(i, drops.get(0).getItem());
			drops.remove(0);
		}

		if(useDoubleChest) {
			chest = (TileEntityLockableLoot) tile;

			for(int i = 0; i < 27 && !drops.isEmpty(); i++) {
				chest.setInventorySlotContents(i, drops.get(0).getItem());
				drops.remove(0);
			}
		}

		if(VDCConfig.defense.defenseEntity != null) {
			final double x = pos.getX() + 0.5;
			final double y = pos.getY() + 1.0;
			final double z = pos.getZ() + 0.5;

			for(int i = 0; i < VDCConfig.defense.defenseEntitySpawnCount; i++) {
				NBTTagCompound compound = null;

				try {
					compound = JsonToNBT.getTagFromJson(VDCConfig.defense.defenseEntityNBT);
				} catch(CommandSyntaxException ignored) {}

				compound.setString("id", VDCConfig.defense.defenseEntityRegistryName);

				final Entity entity =
						AnvilChunkLoader.readWorldEntityPos(compound, world, x, y, z, true);

				if(entity instanceof EntityLiving) {
					final EntityLiving living = (EntityLiving) entity;

					living.enablePersistence();
					living.onInitialSpawn(world.getDifficultyForLocation(pos), null, null);

					if(living instanceof EntityPigZombie) {
						((IAngerable) living).makeAngryAt(player);
					}

					final DeathChestDefenseEntity defenseEntity = (DeathChestDefenseEntity) living;

					defenseEntity.setDeathChestPlayer(player.getUniqueID());
					defenseEntity.setDeathChestPos(pos);
				}

				world.spawnEntity(entity);
			}
		}

		DeathChestManager.addDeathChest(world, player, pos, useDoubleChest);

		VanillaDeathChest.LOGGER.info("Death chest for {} spawned at [{}]", profile.getName(), pos);

		player.sendMessage(new TextComponentString(String.format(
				VDCConfig.spawning.chatMessage, pos.getX(), pos.getY(), pos.getZ()
		)));
	}
}
