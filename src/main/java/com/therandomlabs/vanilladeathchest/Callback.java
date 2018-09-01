package com.therandomlabs.vanilladeathchest;

import java.lang.ref.WeakReference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class Callback {
	private final WeakReference<World> world;
	private final WeakReference<EntityPlayer> player;

	private boolean alreadyCalled;

	public Callback(World world, EntityPlayer player) {
		this.world = new WeakReference<>(world);
		this.player = new WeakReference<>(player);
	}

	public final boolean run() {
		//Delay by a tick to avoid conflicts with other mods that place blocks upon death
		if(!alreadyCalled) {
			alreadyCalled = true;
			return false;
		}

		final World world = this.world.get();

		if(world == null) {
			return true;
		}

		final EntityPlayer player = this.player.get();

		if(player == null) {
			return true;
		}

		run(world, player);

		return true;
	}

	public abstract void run(World world, EntityPlayer player);
}
