package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.deathchest.DeathChestInteractions;
import com.therandomlabs.vanilladeathchest.util.DeathChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public final class ServerPlayerInteractionManagerMixin {
	@Shadow
	public ServerWorld world;

	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
	private void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		if (!world.getBlockState(pos).getBlock().hasBlockEntity()) {
			return;
		}

		final BlockEntity blockEntity = world.getBlockEntity(pos);

		if (blockEntity instanceof DeathChestBlockEntity) {
			final DeathChest deathChest = ((DeathChestBlockEntity) blockEntity).getDeathChest();

			if (!DeathChestInteractions.attemptBreak(pos, deathChest, player)) {
				info.setReturnValue(false);
			}
		}
	}
}
