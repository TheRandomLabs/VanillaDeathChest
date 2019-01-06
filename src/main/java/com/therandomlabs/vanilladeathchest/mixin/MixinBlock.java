package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.event.BlockEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Block.class)
public class MixinBlock {
	@Overwrite
	public static void dropStack(World world, BlockPos pos, ItemStack stack) {
		if(world.isClient || stack.isEmpty() || !world.getGameRules().getBoolean("doTileDrops")) {
			return;
		}

		for(BlockEvent.GetDrop event : BlockEvent.GET_DROP.getBackingArray()) {
			final ItemStack newStack = event.getDrop(world, pos, stack);

			if(newStack != null) {
				stack = newStack;
				break;
			}
		}

		final ItemEntity item = new ItemEntity(
				world,
				pos.getX() + world.random.nextFloat() * 0.5 + 0.25,
				pos.getY() + world.random.nextFloat() * 0.5 + 0.25,
				pos.getZ() + world.random.nextFloat() * 0.5 + 0.25,
				stack
		);
		item.setPickupDelayDefault();
		world.spawnEntity(item);
	}
}
