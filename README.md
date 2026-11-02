# Test Run

> Note: This is an early alpha project, and has no available build as of yet. Check back tomorrow!

Test Run is a Minecraft Fabric mod designed for developers and map creators. It allows users to create a state of the current world and the player's dimension, position, hunger, health, tags, and other information. This can then be reloaded multiple times for testing purposes.

## Instructions

Download the mod file from the releases tab from the side, or from Modrinth: [link soon]

- When in a world, use `/testrun save [name]` to save a scenario.
- You can load scenarios with with `/testrun run [name]`
- You can list all scenarios with `/testrun list`.
- You can load another player's scenario with `/testrun runfrom [player] [name]`
- If you want to make a player load their own scenario, try using `/execute as [player] run testrun run [name]` to run it as that specific player.
- To rerun the last used scenario, use `/testrun again`.

## Features

- Save, load, and delete scenarios
- List all scenarios
- Scenarios contain position, rotation, hunger, saturation, gamemode, tags, teams, inventory, and some other nbt data
- Rerun the last scenario used with `/testrun again`, or use a hotkey (default: K)
- Run a reset function when loading a scenario with `/testrun setup [scenario_name] [function_name]`
- Scenarios are persistent between sessions
- Command autocomplete for scenarios and datapack functions
- Quick save and load keys
- Ability for other players to load another player's scenario
- Selective restoration

Possible features that may come out (maybe):

- Duplicating scenarios
- Manually editing certain scenario features
- Exporting scenarios

## Notes

This is an active WIP, and is a small project. It may not receive future updates once complete (but it might!), and I am considering making a more advanced creator toolpack that would contain a similar feature to twhat mod does/will do.

This is also one of my first Minecraft mods. I've worked on multiple maps and datapacks in the past, but never really mods, so here it is.

Built as part of Hack Club's Terra!
