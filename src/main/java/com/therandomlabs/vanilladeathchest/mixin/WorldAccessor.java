package com.therandomlabs.vanilladeathchest.mixin;

import java.util.List;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccessor {
	@Accessor
	List<BlockEntity> getUnloadedBlockEntities();
}
