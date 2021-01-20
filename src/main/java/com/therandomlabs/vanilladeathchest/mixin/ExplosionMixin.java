package com.therandomlabs.vanilladeathchest.mixin;

import java.util.List;

import com.therandomlabs.vanilladeathchest.deathchest.DeathChest;
import com.therandomlabs.vanilladeathchest.world.DeathChestsState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
	@Shadow
	@Final
	private World world;

	@Shadow
	public abstract List<BlockPos> getAffectedBlocks();

	@Inject(method = "collectBlocksAndDamageEntities", at = @At("TAIL"))
	private void collectBlocksAndDamageEntities(CallbackInfo info) {
		final DeathChestsState deathChestsState = DeathChestsState.get((ServerWorld) world);
		getAffectedBlocks().removeIf(pos -> {
			final DeathChest deathChest = deathChestsState.getExistingDeathChest(pos);
			return deathChest != null && deathChest.isLocked();
		});
	}
}
