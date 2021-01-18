package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.util.DropsList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerEntity.class, priority = Integer.MAX_VALUE)
public final class PlayerEntityMixin {
	//We don't redirect PlayerEntity#dropItem to prevent conflicts with other mods that do the same.
	@SuppressWarnings("ConstantConditions")
	@Redirect(
			method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
			at = @At(
					value = "INVOKE",
					target = "net/minecraft/entity/ItemEntity.setPickupDelay(I)V"
			)
	)
	public void setPickupDelay(ItemEntity entity, int pickupDelay) {
		entity.setPickupDelay(pickupDelay);
		((DropsList) (Object) this).getDrops().add(entity);
	}
}
