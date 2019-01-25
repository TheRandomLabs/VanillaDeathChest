package com.therandomlabs.vanilladeathchest.mixin;

import java.util.Collections;
import java.util.List;
import com.therandomlabs.vanilladeathchest.api.listener.GetBlockDropListener;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Block.class)
public class MixinBlock {
	@Overwrite
	public static void spawnAsEntity(World world, BlockPos pos, ItemStack stack) {
		if(world.isRemote || stack.isEmpty() || !world.getGameRules().getBoolean("doTileDrops")) {
			return;
		}

		List<ItemStack> drops = Collections.singletonList(stack);

		for(GetBlockDropListener listener :
				RiftLoader.instance.getListeners(GetBlockDropListener.class)) {
			final List<ItemStack> newDrops = listener.getDrop(world, pos, stack);

			if(newDrops != null) {
				drops = newDrops;
				break;
			}
		}

		for(ItemStack drop : drops) {
			if(drop.isEmpty()) {
				continue;
			}

			final EntityItem item = new EntityItem(
					world,
					pos.getX() + world.rand.nextFloat() * 0.5 + 0.25,
					pos.getY() + world.rand.nextFloat() * 0.5 + 0.25,
					pos.getZ() + world.rand.nextFloat() * 0.5 + 0.25,
					drop
			);
			item.setDefaultPickupDelay();
			world.spawnEntity(item);
		}
	}
}
