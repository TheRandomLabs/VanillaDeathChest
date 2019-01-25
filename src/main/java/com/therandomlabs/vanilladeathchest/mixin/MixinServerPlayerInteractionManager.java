package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.event.BlockEvent;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {
	@Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
	public void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
		final ServerPlayerInteractionManager manager =
				(ServerPlayerInteractionManager) (Object) this;

		for(BlockEvent.Break event : BlockEvent.BREAK.getBackingArray()) {
			if(!event.breakBlock((ServerWorld) manager.world, manager.player, pos)) {
				callback.setReturnValue(false);
				callback.cancel();
				return;
			}
		}
	}
}
