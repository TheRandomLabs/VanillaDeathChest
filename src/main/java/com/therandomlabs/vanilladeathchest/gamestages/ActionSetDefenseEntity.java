package com.therandomlabs.vanilladeathchest.gamestages;

import crafttweaker.api.entity.IEntityDefinition;

public class ActionSetDefenseEntity extends VDCAction {
	public ActionSetDefenseEntity(String stage, IEntityDefinition defenseEntity) {
		super(
				stage,
				info -> info.setDefenseEntityRegistryName(defenseEntity.getId()),
				"Setting defense entity to " + defenseEntity
		);
	}
}
