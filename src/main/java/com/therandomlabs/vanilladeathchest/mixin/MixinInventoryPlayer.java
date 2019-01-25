package com.therandomlabs.vanilladeathchest.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.therandomlabs.vanilladeathchest.api.listener.PlayerDropAllItemsListener;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
	@Shadow
	public EntityPlayer player;
	@Shadow
	@Final
	private List<NonNullList<ItemStack>> allInventories;

	@Overwrite
	public void dropAllItems() {
		final World world = player.getEntityWorld();
		final List<EntityItem> drops = new ArrayList<>();

		for(List<ItemStack> stacks : allInventories) {
			for(int i = 0; i < stacks.size(); i++) {
				final ItemStack stack = stacks.get(i);

				if(!stack.isEmpty()) {
					final EntityItem item = dropItem(world, player, stack);
					drops.add(item);
					stacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		for(PlayerDropAllItemsListener listener :
				RiftLoader.instance.getListeners(PlayerDropAllItemsListener.class)) {
			if(!listener.onPlayerDropAllItems(world, player, drops)) {
				return;
			}
		}

		if(!drops.isEmpty()) {
			for(EntityItem drop : drops) {
				world.spawnEntity(drop);
			}
		}
	}

	private static EntityItem dropItem(World world, EntityPlayer player, ItemStack stack) {
		final double y = player.posY - 0.30000001192092896 + player.getEyeHeight();
		final EntityItem item = new EntityItem(world, player.posX, y, player.posZ, stack);

		item.setPickupDelay(40);

		final Random rng = player.getRNG();

		final float value1 = rng.nextFloat() * 0.5F;
		final float value2 = rng.nextFloat() * 6.2831855F;

		item.motionX = -MathHelper.sin(value2) * value1;
		item.motionY = 0.20000000298023224;
		item.motionZ = MathHelper.cos(value2) * value1;

		return item;
	}
}
