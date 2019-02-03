import mods.vanilladeathchest.DeathChestDefense;

//If a property is not set, the value set in the configuration is used.
//Stages should be defined in order. Calling any of the following functions defines a stage.

//The registry name of the unlocker item.
DeathChestDefense.setUnlockerRegistryName("example_stage", "minecraft:diamond_axe");

//32767 is the wildcard value.
DeathChestDefense.setUnlockerMeta("example_stage", 32767);

//If this is true, the unlocker item is damaged instead of consumed.
DeathChestDefense.setDamageUnlockerInsteadOfConsume("example_stage", true);

//The amount of the unlocker item that is consumed (or damaged).
DeathChestDefense.setUnlockerConsumeAmount("example_stage", 1000);

DeathChestDefense.setDefenseEntityRegistryName("example_stage", "minecraft:zombie_pigman");

DeathChestDefense.setDefenseEntityNBT("example_stage", "{HandItems:[{Count:1,id:\"minecraft:diamond_sword\"}]}");

DeathChestDefense.setDefenseEntitySpawnCount("example_stage", 500);
