import mods.VanillaDeathChest;

//If a property is not set, the value set in the configuration is used.
//Stages should be defined in order. Calling any of the following functions defines a stage.

//The registry name of the unlocker item.
VanillaDeathChest.setUnlockerRegistryName("example_stage", "minecraft:diamond_axe");

//32767 is the wildcard value.
VanillaDeathChest.setUnlockerMeta("example_stage", 32767);

//If this is true, the unlocker item is damaged instead of consumed.
VanillaDeathChest.setDamageUnlockerInsteadOfConsume("example_stage", true);

//The amount of the unlocker item that is consumed (or damaged).
VanillaDeathChest.setUnlockerConsumeAmount("example_stage", 1000);

VanillaDeathChest.setDefenseEntityRegistryName("example_stage", "minecraft:zombie_pigman");

VanillaDeathChest.setDefenseEntityNBT("example_stage", "{HandItems:[{Count:1,id:\"minecraft:diamond_sword\"}]}");

VanillaDeathChest.setDefenseEntitySpawnCount("example_stage", 500);
