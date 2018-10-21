package com.therandomlabs.vanilladeathchest.mixin;

import java.util.TreeMap;
import com.therandomlabs.vanilladeathchest.api.listener.GameRuleDefinitionModifier;
import net.minecraft.world.GameRules;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.class)
public class MixinGameRules {
	@Shadow
	@Final
	private static TreeMap<String, GameRules.ValueDefinition> DEFINITIONS;

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void init(CallbackInfo callback) {
		for(GameRuleDefinitionModifier listener :
				RiftLoader.instance.getListeners(GameRuleDefinitionModifier.class)) {
			listener.modify(DEFINITIONS);
		}
	}
}
