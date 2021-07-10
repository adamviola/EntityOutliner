# Entity Outliner
Entity Outliner is a clientside mod that allows you to select entity types to outline, making them visible through obstructions at any distance.

## Why Use It?
This mod will help with:
<details>
  <summary>Finding passive mobs</summary>
  
  by outlining them.
  
  ![Image of outlined bees](https://i.imgur.com/jqhVLSX.png "It's hard to find bees!")
</details>

<details>
  <summary>Finding unlit caves</summary>
  
  by outlining zombies, creepers, skeletons, and spiders.
  
  ![Gif showing how outlining monsters can reveal unlit caves](https://i.imgur.com/owNj5BE.gif "Great for when you've reached a dead end in your cave!")
  
  
</details>

<details>
  <summary>Fighting other players</summary>
  
  by outlining players.
  
  ![Image of outlined players](https://i.imgur.com/TiEldyM.png "Even works while they're sneaking!")
  
</details>

<details>
  <summary>Finding your death location</summary>
  
  by outlining items and experience orbs.
  
  
  ![Image of outlined items/xp orbs of death location](https://i.imgur.com/sOzk89i.png "Tombstone mods are cool too!")
  
  
</details>

<details>
  <summary>Wither skeleton skull hunting</summary>
  
  by outlining wither skeletons
  
  ![Image of outlined wither skeletons](https://i.imgur.com/cc4rhaY.png "I actually like the grind for wither skeleton skulls!")
  
</details>

<details>
  <summary>Finding mineshafts</summary>
  
  by outlining cave spiders and minecarts with chests.
  
  
  ![Image of outlined chest minecarts](https://i.imgur.com/36rMnDc.png "I hate cave spiders!")
  
</details>

And many more!

## Features
**Entity Selector**

![GIF demonstrating use of the entity selector screen](https://i.imgur.com/XozyBa4.gif "It's a prefix search!")

This screen allows outlining of any entity in the game. There's a search bar for narrowing down entities and buttons to organize the results by entity category, deselect all entities, and toggle on/off the outlines. Entities added by other mods **do** appear in the results.

For the technically inclined, the search works using a precomputed hashtable that maps a string prefix to a corresponding list of results. The lists of results are computed for all prefixes that correspond at least one entity type.

**Controls for toggling the outlines and opening the selector**

![Image of the keybind selector for toggling the outlines and selector screen](https://i.imgur.com/au39Ov1.png "Hopefully o and p aren't taken!")

Custom keybinds are provided to open the entity selector and toggle the outline. The outline can also be toggled via a button inside the entity selector.


## Installation
1. Install [Fabric](https://fabricmc.net/use/)
2. Drop the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) jar into the mods folder
3. Drop the Entity Outliner jar into the mods folder

## Compatibility
Works with MobZ. Let me know if you find any compatibility issues.

## License
MIT. Feel free to use this mod in any modpack.
