package com.therandomlabs.vanilladeathchest.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.therandomlabs.vanilladeathchest.api.event.player.PlayerDropAllItemsCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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
	@Final
	public PlayerEntity player;

	@Shadow
	@Final
	private List<DefaultedList<ItemStack>> combinedInventory;

	@Overwrite
	public void dropAll() {
		final ServerWorld world = (ServerWorld) player.getEntityWorld();
		final List<ItemEntity> drops = new ArrayList<>();

		for(List<ItemStack> stacks : combinedInventory) {
			for(int i = 0; i < stacks.size(); i++) {
				final ItemStack stack = stacks.get(i);

				if(!stack.isEmpty()) {
					final ItemEntity item = dropItem(world, player, stack);
					drops.add(item);
					stacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		if(!PlayerDropAllItemsCallback.EVENT.invoker().onPlayerDropAllItems(world, player, drops)) {
			return;
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
				player.y - 0.30000001192092896 + player.getStandingEyeHeight(),
				player.z,
				stack
		);

		item.setPickupDelay(40);

		final Random random = player.getRand();

		final float value1 = random.nextFloat() * 0.5F;
		final float value2 = random.nextFloat() * 6.2831855F;

		item.setVelocity(
				-MathHelper.sin(value2) * value1,
				0.20000000298023224,
				MathHelper.cos(value2) * value1
		);

		return item;
	}
}
