# Miner Overview
A simple hud that shows coordinates, biome, fps, lightlevel and more. Shows durability and stack size of items in the inventory and armor. Highlights blocks where to place torches and other light sources.

## Features
### Info Overview
Shows information about the world on the top left side of the screen. The visibility of these can be changed in the mod menu settings or in the config file.
<br/><br/>
![](https://i.imgur.com/UuQxQGx.png)

### Item Overview
Shows information about items in the inventory. This includes the mainhand, offhand, armor and custom inventory slots. The slots can by added by clicking on it + holding the toggle slot keybind.
<br/><br/>
![](https://i.imgur.com/ZjOH6NB.png)
![](https://i.imgur.com/3ltzEwV.png)

### Light Levels
The light levels is shown in the info texts. You can also enable Light Level Distance. When this is enabled it will show highlights on blocks and in the info text where to place the next light source. This is very handy while strip mining to make sure no mobs spawn. The minimum light level and height of the light source can be changed in the settings.
<br/><br/>
![](https://i.imgur.com/08HJIDM.png)

## Key Bindings

| Name                  |      Default Key       | Description                                                                               |
|-----------------------|:----------------------:|-------------------------------------------------------------------------------------------|
| Toggle Overview        |          `M`           | Toggles the visibility of the overview hud.                                                                 |
| Toggle Slot For Item Overview   | `LEFT ALT` | The key to hold when adding/removing a slot for the item overview.                                                            |

You can change every key binding in the Minecraft key binding settings.

## TODO
- [ ] Make light algorithm more efficient
- [ ] Make light data structure more efficient