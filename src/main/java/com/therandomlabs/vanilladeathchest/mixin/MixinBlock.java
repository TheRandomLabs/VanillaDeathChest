package com.therandomlabs.vanilladeathchest.mixin;

import java.util.Collections;
import java.util.List;
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

		List<ItemStack> drops = Collections.singletonList(stack);

		for(BlockEvent.GetDrop event : BlockEvent.GET_DROP.getBackingArray()) {
			final List<ItemStack> newDrops = event.getDrop(world, pos, stack);

			if(newDrops != null) {
				drops = newDrops;
				break;
			}
		}

		for(ItemStack drop : drops) {
			if(drop.isEmpty()) {
				continue;
			}

			final ItemEntity item = new ItemEntity(
					world,
					pos.getX() + world.random.nextFloat() * 0.5 + 0.25,
					pos.getY() + world.random.nextFloat() * 0.5 + 0.25,
					pos.getZ() + world.random.nextFloat() * 0.5 + 0.25,
					drop
			);
			item.setToDefaultPickupDelay();
			world.spawnEntity(item);
		}
	}
}
