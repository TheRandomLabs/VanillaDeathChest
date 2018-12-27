package com.therandomlabs.vanilladeathchest.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.therandomlabs.vanilladeathchest.api.event.PlayerEvent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
	@Shadow
	public PlayerEntity player;

	@Shadow
	@Final
	private List<DefaultedList<ItemStack>> field_7543;

	@Overwrite
	public void dropAll() {
		final World world = player.getEntityWorld();
		final List<ItemEntity> drops = new ArrayList<>();

		for(List<ItemStack> stacks : field_7543) {
			for(int i = 0; i < stacks.size(); i++) {
				final ItemStack stack = stacks.get(i);

				if(!stack.isEmpty()) {
					final ItemEntity item = dropItem(world, player, stack);
					drops.add(item);
					stacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		for(PlayerEvent.DropAllItems event :
				PlayerEvent.DROP_ALL_ITEMS.getBackingArray()) {
			final ActionResult result = event.onPlayerDropAllItems(world, player, drops);

			if(result == ActionResult.PASS) {
				return;
			}
		}

		if(!drops.isEmpty()) {
			for(ItemEntity drop : drops) {
				world.spawnEntity(drop);
			}
		}
	}

	private static ItemEntity dropItem(World world, PlayerEntity player, ItemStack stack) {
		final ItemEntity item = new ItemEntity(
				world,
				player.x,
				player.y - 0.30000001192092896 + player.getEyeHeight(),
				player.z,
				stack
		);

		item.setPickupDelay(40);

		final Random random = player.getRand();

		final float value1 = random.nextFloat() * 0.5F;
		final float value2 = random.nextFloat() * 6.2831855F;

		item.velocityX = -MathHelper.sin(value2) * value1;
		item.velocityY = 0.20000000298023224;
		item.velocityZ = MathHelper.cos(value2) * value1;

		return item;
	}
}
