package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.listener.BlockHarvestListener;
import com.therandomlabs.vanilladeathchest.api.listener.RightClickBlockListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerInteractionManager.class)
public class MixinPlayerInteractionManager {
	@Shadow
	public World world;

	@Shadow
	public EntityPlayerMP player;

	@Inject(method = "processRightClickBlock", at = @At("HEAD"), cancellable = true)
	public void processRightClickBlock(EntityPlayer player, World world,
			ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX,
			float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> callback) {
		for(RightClickBlockListener listener :
				RiftLoader.instance.getListeners(RightClickBlockListener.class)) {
			if(!listener.onRightClickBlock(
					world, (EntityPlayerMP) player, stack, hand, pos, facing, hitX, hitY, hitZ
			)) {
				callback.setReturnValue(EnumActionResult.PASS);
				callback.cancel();
				return;
			}
		}
	}

	@Inject(method = "tryHarvestBlock", at = @At("HEAD"), cancellable = true)
	public void tryHarvestBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
		for(BlockHarvestListener listener :
				RiftLoader.instance.getListeners(BlockHarvestListener.class)) {
			if(!listener.onBlockHarvest(world, player, pos)) {
				callback.setReturnValue(false);
				return;
			}
		}
	}
}
