# BlockOverride
***
BlockOverride is a block editing mod for Minecraft. It aims to allow the player to change certain block properties like hardness and blast resistance, even when ingame with no restart required.
***
## **How BlockOverride works?**
It reads the changes from its own config and executes them at forge Pre Init. This makes it so almost any block properties (vanilla or modded) can be changed.
It also remembers the default state of the block in memory, so if you decide to remove a block from the config it will default back to its original setting.
Using the command /blockoverride_reload you can reload the config ingame and changes takes place immediately.
***

> Created and coded by Kratdavaham