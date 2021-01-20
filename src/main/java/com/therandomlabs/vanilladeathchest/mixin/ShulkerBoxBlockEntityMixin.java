package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.util.ViewerCount;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShulkerBoxBlockEntity.class)
public final class ShulkerBoxBlockEntityMixin implements ViewerCount {
	@Shadow
	private int viewerCount;

	@Override
	public int getViewerCount() {
		return viewerCount;
	}
}
