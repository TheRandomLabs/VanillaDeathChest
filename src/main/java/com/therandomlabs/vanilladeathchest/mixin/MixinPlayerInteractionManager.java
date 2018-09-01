package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.DeathChestHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = PlayerInteractionManager.class)
public class MixinPlayerInteractionManager {
	@Inject(method = "processRightClickBlock", at = @At("HEAD"), cancellable = true)
	private void processRightClickBlock(EntityPlayer player, World world,
			ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, CallbackInfoReturnable<EnumActionResult> ci) {
		DeathChestHandler.onBlockInteract(world, player, pos, ci);

		if(ci.isCancelled()) {
			ci.setReturnValue(EnumActionResult.PASS);
		}
	}

	@Inject(method = "tryHarvestBlock", at = @At("HEAD"))
	private void tryHarvestBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		final PlayerInteractionManager manager = (PlayerInteractionManager) (Object) this;

		if(DeathChestHandler.onBlockInteract(manager.world, manager.player, pos, ci)) {
			DeathChestHandler.removeDeathChest(manager.world, pos);
		}

		if(ci.isCancelled()) {
			ci.setReturnValue(false);
		}
	}
}
