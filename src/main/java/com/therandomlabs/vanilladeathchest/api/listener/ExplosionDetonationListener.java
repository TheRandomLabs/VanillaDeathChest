package com.therandomlabs.vanilladeathchest.api.listener;

import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public interface ExplosionDetonationListener {
	void onExplosionDetonate(World world, Explosion explosion);
}
