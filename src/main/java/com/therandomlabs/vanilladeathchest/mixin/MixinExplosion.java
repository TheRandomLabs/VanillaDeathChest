package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.event.block.ExplosionDetonationCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class MixinExplosion {
	@Shadow
	@Final
	private World world;

	@Inject(method = "collectBlocksAndDamageEntities", at = @At(
			value = "NEW",
			target = "net/minecraft/util/math/Vec3d"
	))
	public void collectBlocksAndDamageEntities(CallbackInfo callback) {
		ExplosionDetonationCallback.EVENT.invoker().onExplosionDetonate(
				(ServerWorld) world, (Explosion) (Object) this
		);
	}
}
