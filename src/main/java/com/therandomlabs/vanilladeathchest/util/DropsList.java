package com.therandomlabs.vanilladeathchest.util;

import java.util.List;

import net.minecraft.entity.ItemEntity;

/**
 * Allows player {@link ItemEntity} drops to be accessed.
 */
public interface DropsList {
	/**
	 * Returns the player drops.
	 *
	 * @return the player {@link ItemEntity} drops.
	 */
	List<ItemEntity> getDrops();
}
