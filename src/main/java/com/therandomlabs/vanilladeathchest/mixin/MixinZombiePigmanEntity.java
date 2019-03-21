package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.IAngerable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombiePigmanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ZombiePigmanEntity.class)
public class MixinZombiePigmanEntity implements IAngerable {
	@Override
	public void makeAngryAt(Entity entity) {
		copyEntityData(entity);
	}

	//I think the mapping is wrong - it should be something like becomeAngryAt
	@Shadow
	private void copyEntityData(Entity entity) {}
}
