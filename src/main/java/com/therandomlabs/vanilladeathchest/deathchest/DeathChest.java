/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.vanilladeathchest.deathchest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a death chest.
 */
public final class DeathChest {
	private final UUID identifier;
	private final ServerWorld world;
	private final UUID playerUUID;
	@SuppressWarnings("PMD.LooseCoupling")
	private final ArrayList<ItemEntity> items;
	private final PlayerInventory inventory;
	private final long creationTime;
	private final BlockPos pos;
	private final boolean isDoubleChest;
	private boolean locked;

	/**
	 * Constructs a {@link DeathChest} with the specified properties.
	 *
	 * @param world a {@link ServerWorld}.
	 * @param playerUUID a player UUID.
	 * @param items the items.
	 * @param inventory the player inventory at the time of death.
	 * @param creationTime a creation time.
	 * @param pos a position. If this is a double chest, this is the position of the west block.
	 * @param isDoubleChest whether the chest is a double chest.
	 * @param locked whether the chest is locked.
	 */
	public DeathChest(
			UUID identifier, ServerWorld world, UUID playerUUID, List<ItemEntity> items,
			PlayerInventory inventory, long creationTime, BlockPos pos, boolean isDoubleChest,
			boolean locked
	) {
		this.identifier = identifier;
		this.world = world;
		this.playerUUID = playerUUID;
		this.items = new ArrayList<>(items);
		this.inventory = inventory;
		this.creationTime = creationTime;
		this.pos = pos;
		this.isDoubleChest = isDoubleChest;
		this.locked = locked;
	}

	/**
	 * Returns this death chest's identifier.
	 *
	 * @return a {@link UUID}.
	 */
	public UUID getIdentifier() {
		return identifier;
	}

	/**
	 * Returns this death chest's world.
	 *
	 * @return this death chest's world.
	 */
	public ServerWorld getWorld() {
		return world;
	}

	/**
	 * Returns this death chest's player UUID.
	 *
	 * @return this death chest's player UUID.
	 */
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	/**
	 * Returns a mutable list containing this death chest's items.
	 *
	 * @return a mutable list containing this death chest's items.
	 */
	public List<ItemEntity> getItems() {
		return items;
	}

	/**
	 * Returns a cloned mutable list containing this death chest's items.
	 *
	 * @return a cloned mutable list containing this death chest's items.
	 */
	@SuppressWarnings("unchecked")
	public List<ItemEntity> cloneItems() {
		return (List<ItemEntity>) items.clone();
	}

	/**
	 * Returns the player inventory at the time of death.
	 *
	 * @return the {@link PlayerInventory} at the time of death.
	 */
	public PlayerInventory getInventory() {
		return inventory;
	}

	/**
	 * Returns this death chest's creation time.
	 *
	 * @return this death chest's creation time.
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns this death chest's position.
	 * If this is a double chest, this is the position of the west block.
	 *
	 * @return this death chest's position.
	 */
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * Returns whether this death chest is a double chest.
	 * This may be {@code true} even if one half of the double chest has been destroyed.
	 *
	 * @return {@code true} if this death chest is a double chest, or otherwise {@code false}.
	 */
	public boolean isDoubleChest() {
		return isDoubleChest;
	}

	/**
	 * Returns whether this death chest is locked.
	 *
	 * @return {@code true} if this death chest is locked, or otherwise {@code false}.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Sets whether this death chest is locked.
	 *
	 * @param flag {@code true} if this death chest should be locked,
	 * or {@code false} if this death chest should be unlocked.
	 */
	public void setLocked(boolean flag) {
		locked = flag;
		DeathChestsState.get(world).markDirty();
	}

	/**
	 * Returns whether this death chest exists in the world.
	 *
	 * @return {@code true} if this death chest exists, or otherwise {@code false}.
	 */
	public boolean exists() {
		if (!world.getBlockState(pos).getBlock().hasBlockEntity()) {
			return false;
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity instanceof DeathChestBlockEntity &&
				equals(((DeathChestBlockEntity) blockEntity).getDeathChest());
	}

	/**
	 * Returns whether death chest protection prevents the specified player from opening this
	 * death chest.
	 *
	 * @param player a player.
	 * @return {@code true} if this death chest is protected from the specified player,
	 * or otherwise {@code false}.
	 */
	@SuppressWarnings("ConstantConditions")
	public boolean isProtectedFrom(PlayerEntity player) {
		final VDCConfig.Protection config = VanillaDeathChest.config().protection;

		if (!config.enable || playerUUID.equals(player.getUuid()) ||
				(config.bypassInCreativeMode && player.abilities.creativeMode)) {
			return false;
		}

		final OperatorEntry entry =
				player.getServer().getPlayerManager().getOpList().get(player.getGameProfile());

		if (entry != null && entry.getPermissionLevel() >= config.bypassPermissionLevel) {
			return false;
		}

		return config.period == 0 ||
				player.getEntityWorld().getTime() - creationTime <= config.period;
	}

	/**
	 * Serializes this death chest to a {@link CompoundTag}.
	 *
	 * @param tag a {@link CompoundTag}.
	 * @return the {@link CompoundTag}.
	 */
	public CompoundTag toTag(CompoundTag tag) {
		tag.put("PlayerUUID", NbtHelper.fromUuid(playerUUID));

		final ListTag itemsList = new ListTag();

		for (ItemEntity item : items) {
			final CompoundTag itemTag = item.toTag(new CompoundTag());
			item.writeCustomDataToTag(itemTag);
			itemsList.add(itemTag);
		}

		tag.put("Items", itemsList);

		final ListTag inventoryList = new ListTag();
		inventory.serialize(inventoryList);
		tag.put("Inventory", inventoryList);

		tag.putLong("CreationTime", creationTime);
		tag.put("Pos", NbtHelper.fromBlockPos(pos));
		tag.putBoolean("IsDoubleChest", isDoubleChest);
		tag.putBoolean("Locked", locked);
		return tag;
	}

	/**
	 * Deserializes a death chest from a {@link CompoundTag}.
	 *
	 * @param world a {@link ServerWorld}.
	 * @param tag a {@link CompoundTag}.
	 * @return the deserialized {@link DeathChest}.
	 */
	@SuppressWarnings("ConstantConditions")
	public static DeathChest fromTag(ServerWorld world, CompoundTag tag) {
		final List<ItemEntity> items = new ArrayList<>();

		for (Tag itemTag : tag.getList("Items", NbtType.COMPOUND)) {
			final ItemEntity item = new ItemEntity(EntityType.ITEM, world);
			item.fromTag((CompoundTag) itemTag);
			item.readCustomDataFromTag((CompoundTag) itemTag);
			items.add(item);
		}

		final PlayerInventory inventory = new PlayerInventory(null);
		inventory.deserialize(tag.getList("Inventory", NbtType.COMPOUND));

		return new DeathChest(
				NbtHelper.toUuid(tag.getCompound("Identifier")), world,
				NbtHelper.toUuid(tag.get("PlayerUUID")), items, inventory,
				tag.getLong("CreationTime"), NbtHelper.toBlockPos(tag.getCompound("Pos")),
				tag.getBoolean("IsDoubleChest"), tag.getBoolean("Locked")
		);
	}
}
