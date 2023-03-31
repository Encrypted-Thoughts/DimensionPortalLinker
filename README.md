# Dimension Portal Linker

![License](https://img.shields.io/github/license/Encrypted-Thoughts/DimensionPortalLinker)
![Version](https://img.shields.io/github/v/tag/Encrypted-Thoughts/DimensionPortalLinker)
![Downloads](https://img.shields.io/github/downloads/Encrypted-Thoughts/DimensionPortalLinker/total)

By default nether and end portals only work in the regular overworld, nether, and end. 
This mod adds the ability to enable them to work in custom dimensions by mapping which portal should go to which dimension via a config file.

Mostly made to be used in concert with [my bingo mod](https://github.com/Encrypted-Thoughts/DidSomeoneSayBingo) so that it can have multiple different types of playable dimensions where the portals still work and link up correctly with nether and end dimensions that I want.

Each defined dimension has 6 potentials properties:
- Dimension - the actual name of the dimensions.
- Type - The kind of dimension that it is. Default avaialable types that come with minecraft are minecraft:overworld, minecraft:overworld_caves, minecraft:the_nether, minecraft:the_end
- IsNetherPortalEnabled - Set whether nether portals should be lightable and function in this dimension.
- NetherPortalDestinationDimension - If nether portals are enabled set the name of the dimension it should take you to.
- IsEndPortalEnabled - Set whether end portals should function in this dimension.
- EndportalDestinationDimension - If end portals are enabled set the name of the dimension it should take you to.

Example config for my Bingo server (portal_linker.json):
```json
{
  "Dimensions": [
    {
      "Dimension": "minecraft:overworld",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "minecraft:the_nether",
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "minecraft:the_end"
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
      "EndPortalDestinationDimension": "minecraft:overworld"
    },
    {
      "Dimension": "bingo:duplicate_overworld",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:duplicate_nether",
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:duplicate_end"
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
      "EndPortalDestinationDimension": "bingo:duplicate_overworld"
    },
    {
      "Dimension": "bingo:tiny_biomes_overworld",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:tiny_biomes_nether",
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:tiny_biomes_end"
    },
    {
      "Dimension": "bingo:tiny_biomes_nether",
      "Type": "minecraft:the_nether",
      "IsNetherPortalEnabled": true,
      "NetherPortalDestinationDimension": "bingo:tiny_biomes_overworld",
      "IsEndPortalEnabled": false
    },
    {
      "Dimension": "bingo:tiny_biomes_end",
      "Type": "minecraft:the_end",
      "IsNetherPortalEnabled": false,
      "IsEndPortalEnabled": true,
      "EndPortalDestinationDimension": "bingo:tiny_biomes_overworld"
    },
	
    {
      "Dimension": "dream:creative_superflat",
      "Type": "minecraft:overworld",
      "IsNetherPortalEnabled": false,
      "IsEndPortalEnabled": false
    }
  ]
}
```
