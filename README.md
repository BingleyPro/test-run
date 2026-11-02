# Test Run

Test Run is a Minecraft Fabric 26.3 mod designed for map makers, developers, testers and creators who need to repeatedly test a specific situation.

It allows operators to save their current player state (including position, inventory, health, experience, gamemode and other attributes), and then restore it whenever you need to retest something.

## Instructions

Download TestRun from GitHub Releases on the side, or from [Modrinth](https://modrinth.com/project/test-run-bingley/).

If you manually install it, you will need Fabric API and Fabric Language Kotlin. Place the mod and dependencies into your mods folder.

Launch Minecraft using the Fabric Loader.

## Commands

| Command | Description |
|---------|-------------|
|`/testrun save <name>` | Save a scenario |
|`/testrun run <name>` | Restore a scenario |
|`/testrun save <name> <part>` | Selectively updated a saved scenario |
|`/testrun run <name> <part>` | Restore only part of a saved scenario |
|`/testrun again` | Repeat the last scenario |
|`/testrun info <name>` | Show scenario information |
|`/testrun copy <old> <new>` | Duplicate a scenario |
|`/testrun delete <name>` | Delete a scenario |
|`/testrun runfrom <player> <name>` | Load another player's scenario |
|`/testrun setup <name> <function>` | Attach a datapack reset function |
|`/testrun list` | List all scenarios |
|`/testrun finish` | Finish an scenario attempt, gives you the time it took between starting and ending |

### Selective saving and restoration

When using `save` or `run`, you can optionally specify which parts of the scenario to save or restore

| Part | Description |
|---------|-------------|
|`all` | Everything (default) |
|`position` | Position, dimension and rotation |
|`inventory` | Inventory, armour and offhand |
|`player` | Health, hunger, experience, gamemode and other player attributes |
|`world` | World time and weather |

Note that selective saving requires an exisiting scenario.

Tips:

- When in a world, use `/testrun save [name]` to save a scenario.
- You can load scenarios with with `/testrun run [name]`
- You can list all scenarios with `/testrun list`.
- You can load another player's scenario with `/testrun runfrom [player] [name]`
- If you want to make a player load their own scenario, try using `/execute as [player] run testrun run [name]` to run it as that specific player.
- To rerun the last used scenario, use `/testrun again`.

## Features

- Save, restore, inspect, copy and delete scenarios
- Persistent scenario storage between sessions
- Save position, rotation, dimension, inventory, equipment, health, hunger, experience, gamemode, tags, teams, world time, weather and some other nbt data
- Selectively save or restore specific attributes
- Restore scenarios created by other players
- Rerun the last scenario used with `/testrun again`, or use a hotkey (default: K)
- Run a reset function when loading a scenario with `/testrun setup [scenario_name] [function_name]`
- Full command autocomplete
- Quick save and load keys (default: F6 and F7)
- Supports Pirate Speak, Shakespearean and LOLCAT languages

Features that aren't included:

- Manually editing certain scenario features
- Exporting scenarios

## Notes

This is an active WIP, and is a small project. It may not receive future updates and I am considering making a more advanced creator toolpack that would contain a similar feature to what mod does/will do (but at this stage this is unlikely)

