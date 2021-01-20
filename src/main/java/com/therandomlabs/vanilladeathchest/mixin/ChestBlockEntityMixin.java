package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.util.ViewerCount;
import net.minecraft.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChestBlockEntity.class)
public final class ChestBlockEntityMixin implements ViewerCount {
	@Shadow
	protected int viewerCount;

	@Override
	public int getViewerCount() {
		return viewerCount;
	}
}
