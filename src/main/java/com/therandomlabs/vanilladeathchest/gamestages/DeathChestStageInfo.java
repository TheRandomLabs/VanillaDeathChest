package com.therandomlabs.vanilladeathchest.gamestages;

import java.util.LinkedHashMap;
import java.util.Map;
import com.therandomlabs.vanilladeathchest.VDCConfig;
import com.therandomlabs.vanilladeathchest.VanillaDeathChest;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

public class DeathChestStageInfo {
	public static final Map<String, DeathChestStageInfo> STAGES = new LinkedHashMap<>();

	private static DeathChestStageInfo defaultInfo;

	private boolean damageUnlockerInsteadOfConsume =
			VDCConfig.defense.damageUnlockerInsteadOfConsume;
	private String defenseEntityNBT = VDCConfig.defense.defenseEntityNBT;
	private String defenseEntityRegistryName = VDCConfig.defense.defenseEntityRegistryName;
	private int defenseEntitySpawnCount = VDCConfig.defense.defenseEntitySpawnCount;
	private int unlockerConsumeAmount = VDCConfig.defense.unlockerConsumeAmount;
	private int unlockerMeta = VDCConfig.defense.unlockerMeta;
	private String unlockerRegistryName = VDCConfig.defense.unlockerRegistryName;

	private ResourceLocation defenseEntity = VDCConfig.defense.defenseEntity;
	private Item unlocker = VDCConfig.defense.unlocker;

	public boolean damageUnlockerInsteadOfConsume() {
		return damageUnlockerInsteadOfConsume;
	}

	public void setDamageUnlockerInsteadOfConsume(boolean flag) {
		damageUnlockerInsteadOfConsume = flag;
	}

	public String getDefenseEntityNBT() {
		return defenseEntityNBT;
	}

	public void setDefenseEntityNBT(String nbt) {
		try {
			JsonToNBT.getTagFromJson(nbt);
			defenseEntityNBT = nbt;
		} catch(NBTException ex) {
			defenseEntityNBT = "{}";
		}
	}

	public String getDefenseEntityRegistryName() {
		return defenseEntityRegistryName;
	}

	@SuppressWarnings("Duplicates")
	public void setDefenseEntityRegistryName(String registryName) {
		final ResourceLocation[] entityNames =
				EntityList.getEntityNameList().toArray(new ResourceLocation[0]);
		final int index = ArrayUtils.indexOf(
				entityNames, new ResourceLocation(registryName)
		);

		if(index == ArrayUtils.INDEX_NOT_FOUND) {
			defenseEntity = null;
			defenseEntityRegistryName = "";
		} else {
			defenseEntity = entityNames[index];
			defenseEntityRegistryName = defenseEntity.toString();
		}
	}

	public int getDefenseEntitySpawnCount() {
		return defenseEntitySpawnCount;
	}

	public void setDefenseEntitySpawnCount(int count) {
		defenseEntitySpawnCount = count < 1 ? 1 : count;
	}

	public ResourceLocation getDefenseEntity() {
		return defenseEntity;
	}

	public int getUnlockerConsumeAmount() {
		return unlockerConsumeAmount;
	}

	public void setUnlockerConsumeAmount(short amount) {
		unlockerConsumeAmount = amount < 0 ? 0 : amount;
	}

	public int getUnlockerMeta() {
		return unlockerMeta;
	}

	public void setUnlockerMeta(short meta) {
		unlockerMeta = meta < 0 ? 0 : meta;
	}

	public String getUnlockerRegistryName() {
		return unlockerRegistryName;
	}

	@SuppressWarnings("Duplicates")
	public void setUnlockerRegistryName(String registryName) {
		unlocker = VanillaDeathChest.ITEM_REGISTRY.getValue(
				new ResourceLocation(registryName)
		);

		if(unlocker != null) {
			if(unlocker == Items.AIR) {
				unlocker = null;
				unlockerRegistryName = "";
			} else {
				unlockerRegistryName = unlocker.getRegistryName().toString();
			}
		}
	}

	public Item getUnlocker() {
		return unlocker;
	}

	public static DeathChestStageInfo get(EntityPlayer player) {
		if(!VanillaDeathChest.GAME_STAGES_LOADED) {
			return defaultInfo;
		}

		DeathChestStageInfo info = defaultInfo;

		for(Map.Entry<String, DeathChestStageInfo> entry : STAGES.entrySet()) {
			if(GameStageHelper.hasStage(player, entry.getKey())) {
				info = entry.getValue();
			}
		}

		return info;
	}

	public static void reloadDefault() {
		defaultInfo = new DeathChestStageInfo();
	}
}
