package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.common.DeathChestHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInteractionManager.class)
public abstract class MixinPlayerInteractionManager {
	@Inject(method = "processRightClickBlock", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/player/EntityPlayer;getHeldItemMainhand()" +
					"Lnet/minecraft/item/ItemStack",
			shift = At.Shift.BY,
			by = -1
	))
	public void onRightClickBlock(CallbackInfo info, EntityPlayer player, World world,
			ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ) {
		VanillaDeathChest.onBlockInteract(info, player, world, pos);
	}

	@Inject(method = "tryHarvestBlock", at = @At(
			value = "HEAD"
	))
	public void onHarvestBlock(CallbackInfo info, BlockPos pos) {
		final PlayerInteractionManager manager = (PlayerInteractionManager) (Object) this;

		if(VanillaDeathChest.onBlockInteract(info, manager.player, manager.world, pos)) {
			DeathChestHandler.removeDeathChest(manager.world, pos);
		}
	}
}
