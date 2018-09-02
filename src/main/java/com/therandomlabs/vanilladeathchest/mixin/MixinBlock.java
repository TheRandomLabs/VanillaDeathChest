package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.listener.GetBlockDropListener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
	@Inject(method = "getItemDropped", at = @At("RETURN"), cancellable = true)
	public void getItemDropped(IBlockState state, World world, BlockPos pos,
			int fortune, CallbackInfoReturnable<IItemProvider> callback) {
		for(GetBlockDropListener listener :
				RiftLoader.instance.getListeners(GetBlockDropListener.class)) {
			final IItemProvider provider = listener.getBlockDrop(state, world, pos, fortune);

			if(provider != GetBlockDropListener.DEFAULT) {
				callback.setReturnValue(provider);
				callback.cancel();
				return;
			}
		}
	}
}
