package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.IAngerable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PigZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PigZombieEntity.class)
public class MixinPigZombieEntity implements IAngerable {
	@Override
	public void makeAngryAt(Entity entity) {
		copyEntityData(entity);
	}

	//I think the mapping is wrong - it should be something like becomeAngryAt
	@Shadow
	private void copyEntityData(Entity entity) {}
}
