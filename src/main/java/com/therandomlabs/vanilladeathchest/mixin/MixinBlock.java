package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.DeathChestHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(Block.class)
public class MixinBlock {
	@Inject(method = "getItemDropped", at = @At("HEAD"), cancellable = true)
	private void getItemDropped(IBlockState state, World world, BlockPos pos,
			int fortune, CallbackInfoReturnable<IItemProvider> ci) {
		if(DeathChestHandler.getDeathChest(world, pos) != null) {
			ci.cancel();
			ci.setReturnValue(null);
		}
	}
}
