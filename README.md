# Dimension Portal Linker

![License](https://img.shields.io/github/license/Encrypted-Thoughts/DimensionPortalLinker)
![Version](https://img.shields.io/github/v/tag/Encrypted-Thoughts/DimensionPortalLinker)
![Downloads](https://img.shields.io/github/downloads/Encrypted-Thoughts/DimensionPortalLinker/total)

By default nether and end portals only work in the regular overworld, nether, and end. 
This mod adds the ability to enable them to work in custom dimensions by mapping which portal should go to which dimension via a config file.

Mostly made to be used in concert with [my bingo mod](https://github.com/Encrypted-Thoughts/DidSomeoneSayBingo) so that it can have multiple different types of playable dimensions where the portals still work and link up correctly with nether and end dimensions that I want.

Each defined dimension has 6 potentials properties:
- Dimension - the actual name of the dimensions.
- Type - The kind of dimension that it is. Default available types that come with minecraft are minecraft:overworld, minecraft:overworld_caves, minecraft:the_nether, minecraft:the_end
- IsNetherPortalEnabled - Set whether nether portals should be lightable and function in this dimension.
- NetherPortalDestinationDimension - If nether portals are enabled set the name of the dimension it should take you to.
- IsEndPortalEnabled - Set whether end portals should function in this dimension.
- EndportalDestinationDimension - If end portals are enabled set the name of the dimension it should take you to.
- OverrideWorldSpawn - Set whether the world spawn should be overridden to a different dimension and spawn point. This applies when the player dies with no spawn point set or when going from the end portal in the end to the overworld. Also applies to entities going through the end portal in the end.
- OverridePlayerSpawn - Set whether the player's set spawn point should be overridden. This applies when the player has set their spawn at a bed or respawn anchor.
- SpawnDimension - If world spawn or player spawn has been overridden this sets the new dimension where spawn should be.
- SpawnPoint - If world spawn or player spawn has been overridden this sets the location in the overridden spawn dimension where spawn should be.

> Currently the dragon fight doesn't work in custom end dimensions so you'll need to manually place a portal to return back to the overworld as it won't spawn naturally.

## Commands

### `/portalLinker`

Display the current settings for all dimensions.

### `/portalLinker save`

Save the current defined settings to the config so they'll apply on a server restart.

### `/portalLinker [dimension]`

- dimension: `dimension` specify the dimension to define portal linking properties for.

Display the current settings for the selected dimension.

### `/portalLinker [dimension] netherPortal [enabled] [destinationDimension]`

- dimension: `dimension` specify the dimension to define portal linking properties for.
- enabled: `bool` set whether nether portals are enabled in the selected dimension.
- destinationDimension: `dimension` specify the dimension that should be traveled to when going through a nether portal in the selected dimension.

Configure whether nether portals are enabled and where the portal takes you for the selected dimension.

### `/portalLinker [dimension] endPortal [enabled] [destinationDimension]`

- dimension: `dimension` specify the dimension to define portal linking properties for.
- enabled: `bool` set whether end portals are enabled in the selected dimension.
- destinationDimension: `dimension` specify the dimension that should be traveled to when going through an end portal in the selected dimension.

Configure whether end portals are enabled and where the portal takes you for the selected dimension.

### `/portalLinker [dimension] spawn [overrideWorldSpawn] [overridePlayerSpawn] [spawnDimension] [spawnPoint]`

- dimension: `dimension` specify the dimension to define portal linking properties for.
- overrideWorldSpawn: `bool` set whether the world spawn location should be overriden while in the selected dimension.
- overridePlayerSpawn: `bool` set whether the player spawn location should be overriden while in the selected dimension.
- spawnDimension: `dimension` specify the dimension that the spawn location should be while in the selected dimension.
- spawnPoint: `coordinates` specify the coordinates that spawn should be at in the defined spawn dimension.

Configure whether world spawn and/or player spawns should be overriden while in the specified dimension. Determines where end portals in an end dimension take entities along with where players go if they die.

## Example config for my Bingo server (portal_linker.json)
```json
{
  "Dimensions": [
    {
      "Dimension": "minecraft:overworld",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "minecraft:the_nether",
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "minecraft:the_end",
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "minecraft:the_nether",
      "Type": "minecraft:the_nether",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "minecraft:overworld",
      "IsEndPortalEnabled": false
    },
    {
      "Dimension": "minecraft:the_end",
      "Type": "minecraft:the_end",
      "IsNetherPortalEnabled": false,
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "minecraft:overworld",
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "bingo:duplicate_overworld",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:duplicate_nether",
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:duplicate_end",
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "bingo:duplicate_nether",
      "Type": "minecraft:the_nether",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:duplicate_overworld",
      "IsEndPortalEnabled": false
    },
    {
      "Dimension": "bingo:duplicate_end",
      "Type": "minecraft:the_end",
      "IsNetherPortalEnabled": false,
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:duplicate_overworld",
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "bingo:tiny_biomes_overworld",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:tiny_biomes_nether",
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:tiny_biomes_end",
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "bingo:tiny_biomes_nether",
      "Type": "minecraft:the_nether",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:tiny_biomes_overworld",
      "IsEndPortalEnabled": false,
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "bingo:tiny_biomes_end",
      "Type": "minecraft:the_end",
      "IsNetherPortalEnabled": false,
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:tiny_biomes_overworld",
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false
    },
    {
      "Dimension": "dream:creative_superflat",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": false,
      "IsEndPortalEnabled": false,
      "OverrideWorldSpawn": false,
      "OverridePlayerSpawn": false,
      "SpawnDimension": "dream:creative_superflat",
      "SpawnPoint": "0, 100, 0"
    }
  ]
}
```
