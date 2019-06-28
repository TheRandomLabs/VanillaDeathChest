package com.therandomlabs.vanilladeathchest.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.therandomlabs.vanilladeathchest.api.event.block.GetBlockDropCallback;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class MixinBlock {
	private static final List<BlockPos> currentlyDropping = new ArrayList<>();

	@Inject(
			method = "dropStack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;" +
							"spawnEntity(Lnet/minecraft/entity/Entity;)Z"
			),
			cancellable = true
	)
	private static void dropStack(
			World world, BlockPos pos, ItemStack stack, CallbackInfo callbackInfo
	) {
		if(currentlyDropping.contains(pos)) {
			return;
		}

		List<ItemStack> drops = GetBlockDropCallback.EVENT.invoker().getDrop(world, pos, stack);

		if(drops == null) {
			drops = Collections.singletonList(stack);
		}

		currentlyDropping.add(pos);

		for(ItemStack drop : drops) {
			if(!drop.isEmpty()) {
				Block.dropStack(world, pos, drop);
			}
		}

		currentlyDropping.remove(pos);
		callbackInfo.cancel();
	}
}
