package com.therandomlabs.vanilladeathchest.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.Queues;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import com.therandomlabs.vanilladeathchest.util.DeathChestPlacer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VanillaDeathChest.MOD_ID)
public final class DeathChestPlaceHandler {
	private static final Map<DimensionType, Queue<DeathChestPlacer>> PLACERS = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerDrops(LivingDropsEvent event) {
		final Entity entity = event.getEntity();

		if (!(entity instanceof PlayerEntity)) {
			return;
		}

		final List<ItemEntity> drops = (List<ItemEntity>) event.getDrops();

		if (drops.isEmpty()) {
			return;
		}

		final PlayerEntity player = (PlayerEntity) entity;

		if (player instanceof FakePlayer) {
			return;
		}

		final ServerWorld world = (ServerWorld) player.getEntityWorld();
		final GameRules gameRules = world.getGameRules();

		if (gameRules.getBoolean(GameRules.KEEP_INVENTORY)) {
			return;
		}

		final GameRules.RuleKey<GameRules.BooleanValue> key =
				VanillaDeathChest.getDisableDeathChestsKey();

		if (key != null && gameRules.getBoolean(key)) {
			return;
		}

		final Queue<DeathChestPlacer> placers = getPlacers(world);
		placers.add(new DeathChestPlacer(world, player, drops));

		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void worldTick(TickEvent.WorldTickEvent event) {
		final Queue<DeathChestPlacer> placers = getPlacers(event.world);
		final List<DeathChestPlacer> toReadd = new ArrayList<>();
		DeathChestPlacer placer;

		while ((placer = placers.poll()) != null) {
			if (!placer.run()) {
				toReadd.add(placer);
			}
		}

		placers.addAll(toReadd);
	}

	public static Queue<DeathChestPlacer> getPlacers(World world) {
		synchronized (PLACERS) {
			return PLACERS.computeIfAbsent(
					world.getDimension().getType(),
					key -> Queues.newConcurrentLinkedQueue()
			);
		}
	}
}
