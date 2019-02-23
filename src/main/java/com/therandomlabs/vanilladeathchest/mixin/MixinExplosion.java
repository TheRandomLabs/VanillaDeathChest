package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.listener.ExplosionDetonationListener;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.dimdev.riftloader.RiftLoader;
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

	@Inject(method = "doExplosionA", at = @At(
			value = "NEW",
			target = "net/minecraft/util/math/Vec3d"
	))
	public void doExplosionA(CallbackInfo callback) {
		for(ExplosionDetonationListener listener :
				RiftLoader.instance.getListeners(ExplosionDetonationListener.class)) {
			listener.onExplosionDetonate(world, (Explosion) (Object) this);
		}
	}
}
