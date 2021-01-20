package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShulkerBoxBlock.class)
public final class ShulkerBoxBlockMixin {
	@SuppressWarnings("ConstantConditions")
	@Redirect(
			method = "onBreak",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;" +
							"spawnEntity(Lnet/minecraft/entity/Entity;)Z"
			)
	)
	private boolean spawnEntity(World world, Entity entity) {
		if (VanillaDeathChest.config().misc.dropDeathChests) {
			return world.spawnEntity(entity);
		}

		final BlockPos pos = entity.getBlockPos();
		final BlockEntity blockEntity = world.getBlockEntity(pos);

		if (!(blockEntity instanceof DeathChestBlockEntity) ||
				((DeathChestBlockEntity) blockEntity).getDeathChest() == null) {
			return world.spawnEntity(entity);
		}

		final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
		Inventories.fromTag(
				((ItemEntity) entity).getStack().getTag().getCompound("BlockEntityTag"), inventory
		);

		boolean dropped = false;

		for (ItemStack drop : inventory) {
			if (!drop.isEmpty()) {
				final ItemEntity item = new ItemEntity(
						world,
						pos.getX() + world.random.nextFloat() * 0.5F + 0.25,
						pos.getY() + world.random.nextFloat() * 0.5F + 0.25,
						pos.getZ() + world.random.nextFloat() * 0.5F + 0.25,
						drop
				);
				item.setToDefaultPickupDelay();
				world.spawnEntity(item);
				dropped = true;
			}
		}

		return dropped;
	}
}
