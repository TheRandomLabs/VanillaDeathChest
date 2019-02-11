package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.event.block.BreakBlockCallback;
import net.minecraft.server.network.ServerPlayerInteractionManager;
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

		if(!BreakBlockCallback.EVENT.invoker().breakBlock(manager.world, manager.player, pos)) {
			callback.setReturnValue(false);
			callback.cancel();
		}
	}
}
