package com.therandomlabs.vanilladeathchest.mixin;

import com.therandomlabs.vanilladeathchest.api.DropsList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("NullAway")
@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
	@Shadow
	@Final
	public PlayerInventory inventory;

	//We don't directly redirect PlayerEntity#dropItem because other mods do this and cause
	//conflicts.
	@Redirect(
			method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
			at = @At(
					value = "INVOKE",
					target = "net/minecraft/entity/ItemEntity.setPickupDelay(I)V"
			)
	)
	public void setPickupDelay(ItemEntity entity, int pickupDelay) {
		entity.setPickupDelay(pickupDelay);
		((DropsList) inventory).addDrop(entity);
	}
}
