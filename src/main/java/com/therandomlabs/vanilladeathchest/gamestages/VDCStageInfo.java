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
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;

public class VDCStageInfo {
	public static final Map<String, VDCStageInfo> STAGES = new LinkedHashMap<>();

	private boolean damageUnlockerSet;
	private boolean damageUnlockerInsteadOfConsume;
	private String defenseEntityNBT;
	private String defenseEntityRegistryName;
	private int defenseEntitySpawnCount;
	private int unlockerConsumeAmount = Integer.MAX_VALUE;
	private int unlockerMeta = -1;
	private String unlockerRegistryName;
	private String unlockFailedMessage;

	private ResourceLocation defenseEntity;
	private Item unlocker;

	private String chatMessage;

	public boolean damageUnlockerInsteadOfConsume() {
		return damageUnlockerSet ?
				damageUnlockerInsteadOfConsume : VDCConfig.defense.damageUnlockerInsteadOfConsume;
	}

	public void setDamageUnlockerInsteadOfConsume(boolean flag) {
		damageUnlockerSet = true;
		damageUnlockerInsteadOfConsume = flag;
	}

	public String getDefenseEntityNBT() {
		return defenseEntityNBT == null ? VDCConfig.defense.defenseEntityNBT : defenseEntityNBT;
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
		return defenseEntityRegistryName == null ?
				VDCConfig.defense.defenseEntityRegistryName : defenseEntityRegistryName;
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
		return defenseEntitySpawnCount == 0 ?
				VDCConfig.defense.defenseEntitySpawnCount : defenseEntitySpawnCount;
	}

	public void setDefenseEntitySpawnCount(int count) {
		defenseEntitySpawnCount = count < 1 ? 1 : count;
	}

	public ResourceLocation getDefenseEntity() {
		return defenseEntity == null ? VDCConfig.defense.defenseEntity : defenseEntity;
	}

	public int getUnlockerConsumeAmount() {
		return unlockerConsumeAmount == Integer.MAX_VALUE ?
				VDCConfig.defense.unlockerConsumeAmount : unlockerConsumeAmount;
	}

	public void setUnlockerConsumeAmount(int amount) {
		unlockerConsumeAmount = MathHelper.clamp(amount, 0, Short.MAX_VALUE);
	}

	public int getUnlockerMeta() {
		return unlockerMeta == -1 ? VDCConfig.defense.unlockerMeta : unlockerMeta;
	}

	public void setUnlockerMeta(int meta) {
		unlockerMeta = MathHelper.clamp(meta, 0, Short.MAX_VALUE);
	}

	public String getUnlockerRegistryName() {
		return unlockerRegistryName == null ?
				VDCConfig.defense.unlockerRegistryName : unlockerRegistryName;
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
		return unlocker == null ? VDCConfig.defense.unlocker : unlocker;
	}

	public String getChatMessage() {
		return chatMessage == null ? VDCConfig.spawning.chatMessage : chatMessage;
	}

	public void setChatMessage(String message) {
		chatMessage = message;
	}

	public String getUnlockFailedMessage() {
		return unlockFailedMessage == null ?
				VDCConfig.defense.unlockFailedMessage : unlockFailedMessage;
	}

	public void setUnlockFailedMessage(String message) {
		unlockFailedMessage = message;
	}

	public static VDCStageInfo get(EntityPlayer player) {
		if(!VanillaDeathChest.GAME_STAGES_LOADED) {
			return new VDCStageInfo();
		}

		VDCStageInfo info = null;

		for(Map.Entry<String, VDCStageInfo> entry : STAGES.entrySet()) {
			if(GameStageHelper.hasStage(player, entry.getKey())) {
				info = entry.getValue();
			}
		}

		return info == null ? new VDCStageInfo() : info;
	}
}
