# BlockOverride
***
BlockOverride is a block editing mod for Minecraft. It aims to allow the player to change certain block properties like hardness and blast resistance, even when ingame with no restart required.
***
## **How BlockOverride works?**
It reads the changes from its own config and executes them at forge Pre Init. This makes it so almost any block properties (vanilla or modded) can be changed.
It also remembers the default state of the block in memory, so if you decide to remove a block from the config it will default back to its original setting.
***
## **Commands:**
	/blockoverride help "Shows all commands and what they do."
	/blockoverride reload "Reloads the block override config. (Useful for more manual control, you dont need to reload the config after using addor remove)"
	/blockoverride add "Adds the block in your hand to the config."
	/blockoverride remove "Removes the block in your hand from the config."
	/blockoverride get "Gets the Hardness and Resistance value from the block in your hand."
	/blockoverride list "Lists all blocks that are currently overridden."
***

Mod download can be found on CurseForge [here](https://www.curseforge.com/minecraft/mc-mods/blockoverride).

> Created and coded by Kratdavaham