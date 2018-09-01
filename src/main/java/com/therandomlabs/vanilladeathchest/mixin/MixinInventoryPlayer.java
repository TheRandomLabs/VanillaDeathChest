package com.therandomlabs.vanilladeathchest.mixin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.therandomlabs.vanilladeathchest.DeathChestHandler;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
	private static final Field ALL_INVENTORIES =
			VanillaDeathChest.findField(InventoryPlayer.class, "allInventories", "f");

	@SuppressWarnings("unchecked")
	@Overwrite
	public void dropAllItems() {
		final List<List<ItemStack>> allInventories;

		try {
			allInventories = (List<List<ItemStack>>) ALL_INVENTORIES.get(this);
		} catch(Exception ex) {
			throw new ReportedException(new CrashReport("Failed to get allInventories", ex));
		}

		final InventoryPlayer inventory = (InventoryPlayer) (Object) this;
		final World world = inventory.player.getEntityWorld();
		final List<EntityItem> drops = new ArrayList<>();

		for(List<ItemStack> stacks : allInventories) {
			for(int i = 0; i < stacks.size(); i++) {
				final ItemStack stack = stacks.get(i);

				if(!stack.isEmpty()) {
					final EntityItem item = dropItem(world, inventory.player, stack);
					drops.add(item);
					stacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		if(!drops.isEmpty()) {
			DeathChestHandler.onPlayerDeath(world, inventory.player, drops);
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
