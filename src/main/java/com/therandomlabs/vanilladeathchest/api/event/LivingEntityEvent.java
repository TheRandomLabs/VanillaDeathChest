package com.therandomlabs.vanilladeathchest.api.event;

import com.therandomlabs.vanilladeathchest.api.deathchest.DeathChestDefenseEntity;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;

public class LivingEntityEvent {
	@FunctionalInterface
	public interface Drop {
		boolean onLivingEntityDrop(MobEntity entity, DeathChestDefenseEntity defenseEntity,
				boolean recentlyHit, int lootingModifier, DamageSource source);
	}

	@FunctionalInterface
	public interface DropExperience {
		int onLivingEntityDropExperience(MobEntity entity, DeathChestDefenseEntity defenseEntity,
				int experience);
	}

	@FunctionalInterface
	public interface Tick {
		void onLivingEntityTick(MobEntity entity, DeathChestDefenseEntity defenseEntity);
	}

	public static final HandlerArray<Drop> DROP = new HandlerArray<>(Drop.class);
	public static final HandlerArray<DropExperience> DROP_EXPERIENCE =
			new HandlerArray<>(DropExperience.class);
	public static final HandlerArray<Tick> TICK = new HandlerArray<>(Tick.class);
}
