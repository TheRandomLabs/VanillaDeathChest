package com.therandomlabs.vanilladeathchest.mixin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {
	public static final Field ALL_INVENTORIES =
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
					final EntityItem item = inventory.player.dropItem(stack, true, false);

					world.removeEntity(item);
					item.isDead = false;

					drops.add(item);

					stacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		if(!drops.isEmpty()) {
			VanillaDeathChest.onDeath(inventory.player, drops);
		}
	}

	/*
	public static void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
		final World world = event.getWorld();

		if(world.isRemote || DeathChestHandler.getDeathChest(world, event.getPos()) == null ||
				VDCConfig.misc.dropDeathChests) {
			return;
		}

		final List<ItemStack> drops = event.getDrops();

		for(ItemStack stack : drops) {
			if(stack.getItem() == CHEST && stack.getCount() == 1 && stack.getMetadata() == 0) {
				drops.remove(stack);
				break;
			}
		}
	}*/
}
