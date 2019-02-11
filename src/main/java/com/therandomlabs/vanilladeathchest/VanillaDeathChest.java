package com.therandomlabs.vanilladeathchest;

import java.lang.reflect.Method;
import java.util.Arrays;
import com.therandomlabs.vanilladeathchest.command.CommandVDCReload;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
		modid = VanillaDeathChest.MOD_ID, version = VanillaDeathChest.VERSION,
		acceptedMinecraftVersions = VanillaDeathChest.ACCEPTED_MINECRAFT_VERSIONS,
		acceptableRemoteVersions = VanillaDeathChest.ACCEPTABLE_REMOTE_VERSIONS,
		dependencies = VanillaDeathChest.DEPENDENCIES, updateJSON = VanillaDeathChest.UPDATE_JSON,
		certificateFingerprint = VanillaDeathChest.CERTIFICATE_FINGERPRINT
)
public final class VanillaDeathChest {
	public static final String MOD_ID = "vanilladeathchest";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12,1.13)";
	public static final String DEPENDENCIES = "after:gamestages@[2.0.89,)";
	public static final String ACCEPTABLE_REMOTE_VERSIONS = "*";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/VanillaDeathChest/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final IForgeRegistry<Item> ITEM_REGISTRY = GameRegistry.findRegistry(Item.class);

	public static final boolean CUBIC_CHUNKS_LOADED = Loader.isModLoaded("cubicchunks");
	public static final boolean GAME_STAGES_LOADED = Loader.isModLoaded("gamestages");

	@SidedProxy(clientSide = "com.therandomlabs.vanilladeathchest.ClientProxy",
			serverSide = "com.therandomlabs.vanilladeathchest.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public static void construct(FMLConstructionEvent event) {
		proxy.construct();
	}

	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}

	@Mod.EventHandler
	public static void init(FMLInitializationEvent event) {
		proxy.init();
	}

	@Mod.EventHandler
	public static void serverStarting(FMLServerStartingEvent event) {
		if(VDCConfig.misc.vdcreload) {
			event.registerServerCommand(new CommandVDCReload(Side.SERVER));
		}
	}

	public static Method findMethod(Class<?> clazz, String name, String obfName,
			Class<?>... parameterTypes) {
		for(Method method : clazz.getDeclaredMethods()) {
			final String methodName = method.getName();

			if((name.equals(methodName) || obfName.equals(methodName)) &&
					Arrays.equals(method.getParameterTypes(), parameterTypes)) {
				method.setAccessible(true);
				return method;
			}
		}

		return null;
	}
}
