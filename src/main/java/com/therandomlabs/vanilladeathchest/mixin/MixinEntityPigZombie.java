package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.IAngerable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPigZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPigZombie.class)
public class MixinEntityPigZombie implements IAngerable {
	@Override
	public void makeAngryAt(Entity entity) {
		becomeAngryAt(entity);
	}

	@Shadow
	private void becomeAngryAt(Entity entity) {}
}
