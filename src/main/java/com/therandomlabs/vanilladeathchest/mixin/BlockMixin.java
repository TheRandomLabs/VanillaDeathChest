package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public final class BlockMixin {
	@Unique
	private static BlockPos brokenDeathChest;

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "onBreak", at = @At("HEAD"))
	private void onBreak(
			World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info
	) {
		if ((Object) this instanceof ShulkerBoxBlock || !state.getBlock().hasBlockEntity()) {
			return;
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof DeathChestBlockEntity &&
				((DeathChestBlockEntity) blockEntity).getDeathChest() != null) {
			brokenDeathChest = pos;
		}
	}

	@Inject(
			method = "dropStack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;" +
							"spawnEntity(Lnet/minecraft/entity/Entity;)Z"
			),
			cancellable = true
	)
	private static void dropStack(
			World world, BlockPos pos, ItemStack stack, CallbackInfo callback
	) {
		if (pos.equals(brokenDeathChest) && !VanillaDeathChest.config().misc.dropDeathChests) {
			brokenDeathChest = null;
			callback.cancel();
		}
	}
}
