# VanillaDeathChest (Fabric)

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
![Build](https://github.com/TheRandomLabs/VanillaDeathChest/workflows/Build/badge.svg?branch=1.16-fabric)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/TheRandomLabs/VanillaDeathChest.svg)](http://isitmaintained.com/project/TheRandomLabs/VanillaDeathChest "Average time to resolve an issue")

[![Downloads](http://cf.way2muchnoise.eu/full_vanilladeathchest_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/vanilladeathchest-fabric)
[![Files](https://curse.nikky.moe/api/img/393000/files?logo)](https://www.curseforge.com/minecraft/mc-mods/vanilladeathchest-fabric/files)
[![Download](https://curse.nikky.moe/api/img/393000?logo)](https://curse.nikky.moe/api/url/285612)

Places chests (or shulker boxes) where players die that contain all of their items.

**When reporting issues or suggesting enhancements, please use the**
**[GitHub issue tracker](https://github.com/TheRandomLabs/VanillaDeathChest/issues)—it's easier**
**to keep track of things this way. Avoid commenting them on the CurseForge project page or**
**sending them to me in a direct message. Thank you!**

## Sponsor

I've partnered with Apex Hosting! In my experience, their servers are lag-free, easy to manage,
and of high quality. Check them out here:

<a href="https://billing.apexminecrafthosting.com/aff.php?aff=3907">
	<img src="https://cdn.apexminecrafthosting.com/img/theme/apex-hosting-mobile.png" width="594" height="100" border="0">
</a>

## Aims

VanillaDeathChest aims to provide a lightweight, configurable, vanilla-compatible and entirely
server-sided death chest solution. When players die, containers (either chests or shulker boxes)
are placed containing their items.

As it is a server-sided mod, clients without the mod installed can connect to servers with it and
have access to its full functionality. Of course, VanillaDeathChest can also be used in
singleplayer worlds.

## Configuration

The VanillaDeathChest configuration can be found at `config/vanilladeathchest.toml`.

* All properties and categories should be well-commented such that there is little need for further
explanation.
* All configuration values should be automatically validated and reset if they are invalid.
* A configuration GUI can be accessed from
[Mod Menu](https://www.curseforge.com/minecraft/mc-mods/modmenu).
* The configuration can be reloaded from disk in-game using the command `/vdc reloadconfig`.

These are some of the options that are configurable:

### Container type

Four container types can be chosen:
* Only single chests.
* Single or double chests.
* Single shulker boxes.
* Single or double shulker boxes.
The shulker box color can also be configured.

### Spawn dimensions

A whitelist or blacklist of death chest spawning dimensions can be configured.

### Item filter

A reguluar expression can be configured to determine the items that can be placed in death
chests.

### Use container in inventory

If this is enabled, players must have a suitable container in their inventory that can be
consumed for a death chest to be placed when they die.

### Key item

If a key item is configured, it is required to unlock death chests.

### Defense entities

If defense entities are configured, they spawn every time a death chest is placed, and if they
are hostile, they target the player.

### Protection

By default, death chests are protected for five in-game days, which means that only the owner of
a death chest can unlock it.

### Game rule

The name of the game rule that controls whther death chests should be spawned is
`spawnDeathChests` by default, and can be configured.

## Game Stages support

I'll get around to re-implementing this.
