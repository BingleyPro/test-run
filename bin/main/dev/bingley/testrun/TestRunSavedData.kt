package dev.bingley.testrun

import com.mojang.serialization.Codec
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import net.minecraft.util.datafix.DataFixTypes
import java.util.UUID

class TestRunSavedData : SavedData() {
    private val scenarios = mutableMapOf<UUID, MutableMap<String, TestScenario>>()
    
    private val lastUsed = mutableMapOf<UUID, String>()
    
    fun get(playerId: UUID, name: String): TestScenario? {
        // Get a scenario for a given player and scenario name
        return scenarios[playerId]?.get(name)
    }
    
    fun lastUsed(playerId: UUID): String? {
        // Return the last used scenario for a player
        return lastUsed[playerId]
    }

    fun save(playerId: UUID, scenario: TestScenario) {
        // Save a scenario to the disk
        scenarios.getOrPut(playerId) {
            mutableMapOf()
        }[scenario.name] = scenario
        
        lastUsed[playerId] = scenario.name
        
        setDirty() // Means saved data needs to written to disk!
    }
    
    fun update(playerId: UUID, scenario: TestScenario) {
        // Update a scenario
        save(playerId, scenario)
    }
    
    fun remember(playerId: UUID, name: String) {
        // Remember the last scenario used
        lastUsed[playerId] = name
        setDirty()
    }
    
    fun delete(playerId: UUID, name: String): Boolean {
        // Remove a scenario
        val removed = scenarios[playerId]?.remove(name) ?: return false

        if (lastUsed[playerId] == name) {
            // If it was last used, clear
            lastUsed.remove(playerId)
        }
            
        setDirty()
        return true
    }

    private fun toTag(registries: HolderLookup.Provider): CompoundTag {
        // Convert all saved scenarios to NBT
        val root = CompoundTag()

        val entries = ListTag()

        // Iterate through each player, and their scenarios
        for ((playerId, playerScenarios) in scenarios) {
            for (scenario in playerScenarios.values) {
                val tag = writeScenario(scenario, registries)
                
                tag.putString("Owner", playerId.toString())
                entries.add(tag)
            }
        }
        
        root.put("Scenarios", entries)

        return root
    }
    
    fun names(playerId: UUID): List<String> {
        // Return all the names of scenarios for a player
        return scenarios[playerId]?.keys?.sorted() ?: emptyList()
    }
    
    private fun writeScenario(scenario: TestScenario, registries: HolderLookup.Provider): CompoundTag {
        // Write scenario to be stored
        val tag = CompoundTag()
        
        val ops = RegistryOps.create(
            NbtOps.INSTANCE,
                registries
        )
        
        tag.putString("Name", scenario.name)
        
        tag.store(
            "Dimension",
            Level.RESOURCE_KEY_CODEC,
            scenario.dimension
        )
        
        tag.putDouble("X", scenario.x)
        tag.putDouble("Y", scenario.y)
        tag.putDouble("Z", scenario.z)
        
        
        tag.putFloat("Yaw", scenario.yaw)
        tag.putFloat("Pitch", scenario.pitch)
        
        tag.putFloat("Health", scenario.health)
        tag.putInt("Hunger", scenario.hunger)
        tag.putFloat("Saturation", scenario.saturation)
        
        tag.putInt("TotalExperience", scenario.totalExperience)
        tag.putInt("ExperienceLevel", scenario.experienceLevel)
        tag.putFloat("ExperienceProgress", scenario.experienceProgress)
        
        tag.store(
            "Gamemode",
            GameType.CODEC,
            scenario.gamemode
        )
        
        tag.store(
            "Inventory",
            ItemStack.OPTIONAL_CODEC.listOf(),
            ops,
            scenario.inventory
        )
        
        tag.store(
            "Armour",
            ItemStack.OPTIONAL_CODEC.listOf(),
            ops,
            scenario.armour
        )
        
        tag.store(
            "Offhand",
            ItemStack.OPTIONAL_CODEC,
            ops,
            scenario.offhand
        )
        
        tag.putInt("SelectedSlot", scenario.selectedSlot)
        
        tag.store(
            "Tags",
            Codec.STRING.listOf(),
            scenario.tags.sorted()
        )
        
        scenario.team?.let {
            tag.putString("Team", it)
        }
        
        tag.put("ExtraNbt", scenario.extraNbt.copy())
        
        scenario.resetFunction?.let {
            tag.putString("ResetFunction", it)
        }
        
        return tag
    }
    
    private fun readScenario(tag: CompoundTag, registries: HolderLookup.Provider): TestScenario? {
        // Read a scenario
        val ops = RegistryOps.create(
            NbtOps.INSTANCE,
            registries
        )
        
        // Required properties
        val name = tag.getString("Name").orElse(null)
            ?: return null

        val dimension = tag.read(
            "Dimension",
            Level.RESOURCE_KEY_CODEC
        ).orElse(null) ?: return null
        
        val gamemode = tag.read(
            "Gamemode",
            GameType.CODEC
        ).orElse(null) ?: return null
        
        val inventory = tag.read(
            "Inventory",
            ItemStack.OPTIONAL_CODEC.listOf(),
            ops
        ).orElse(null) ?: return null
        
        val armour = tag.read(
            "Armour",
            ItemStack.OPTIONAL_CODEC.listOf(),
            ops
        ).orElse(null) ?: return null
        
        val offhand = tag.read(
            "Offhand",
            ItemStack.OPTIONAL_CODEC,
            ops
        ).orElse(null) ?: return null
        
        return TestScenario(
            name = name,
            dimension = dimension,
                
            x = tag.getDoubleOr("X", 0.0),
            y = tag.getDoubleOr("Y", 64.0),
            z = tag.getDoubleOr("Z", 0.0),
                
            yaw = tag.getFloatOr("Yaw", 0f),
            pitch = tag.getFloatOr("Pitch", 0f),
                
            health = tag.getFloatOr("Health", 20f),
            hunger = tag.getIntOr("Hunger", 20),
            saturation = tag.getFloatOr("Saturation", 5f),
                
            totalExperience = tag.getIntOr("TotalExperience", 0),
            experienceLevel = tag.getIntOr("ExperienceLevel", 0),
            experienceProgress = tag.getFloatOr("ExperienceProgress", 0f),
                
            gamemode = gamemode,
            
            inventory = inventory.map { it.copy() },
            armour = armour.map { it.copy() },
            offhand = offhand.copy(),
            
            selectedSlot = tag.getIntOr("SelectedSlot", 0),

            tags = tag.read(
                "Tags",
                Codec.STRING.listOf()
            ).orElse(emptyList())?.toSet() ?: emptySet(),
            
            team = tag.getString("Team").orElse(null),
            
            extraNbt = tag.getCompoundOrEmpty(
                "ExtraNbt"
            ).copy(),
                
            resetFunction = tag.getString(
                "ResetFunction"
            ).orElse(null)
        )
    }
    
    companion object {
        // This should tell Minecraft how to create/save/load
        
        private val types = java.util.WeakHashMap<ServerLevel, SavedDataType<TestRunSavedData>>()
        
        
        fun forPlayer(player: ServerPlayer): TestRunSavedData {
            // Keep all scenarios in the same save (same dimension)
            val overworld = requireNotNull(
                player.level().server.getLevel(Level.OVERWORLD)
            )
            
            // Check if there is a save for this world already
            val type = types.getOrPut(overworld) {
                val registries = overworld.registryAccess()
                
                val codec: Codec<TestRunSavedData> =
                    CompoundTag.CODEC.xmap(
                        { tag: CompoundTag ->
                            fromTag(tag, registries)
                        },
                        { data: TestRunSavedData ->
                            data.toTag(registries)
                        }
                    )
                    
                SavedDataType<TestRunSavedData>(
                    Identifier.fromNamespaceAndPath("testrun", "scenarios"),
                   { TestRunSavedData( )},
                    codec,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
                )
            }
            
            // Loads exisiting data or creates new
            return overworld.dataStorage.computeIfAbsent(type)
        }
        
        private fun fromTag(root: CompoundTag, registries: HolderLookup.Provider): TestRunSavedData {
            val data = TestRunSavedData()
            
            // Load each scenario
            for (element in root.getListOrEmpty("Scenarios")) {
                val entry = element as? CompoundTag ?: continue
                
                val owner: UUID
                
                try {
                    owner = UUID.fromString(
                        entry.getStringOr("Owner", "")
                    )
                } catch (e: IllegalArgumentException) {
                    continue
                }
                
                val scenario = data.readScenario(entry, registries)
                    ?: continue
                    
                data.scenarios.getOrPut(owner) {
                    mutableMapOf()
                }[scenario.name] = scenario
            }
            
             for (element in root.getListOrEmpty("LastUsed")) {
                val entry = element as? CompoundTag ?: continue
                
                val owner = runCatching {
                   UUID.fromString(entry.getStringOr("Owner", ""))
                }.getOrNull() ?: continue
                
                val name = entry.getStringOr("Name", "")
                
                if (data.scenarios[owner]?.containsKey(name) == true) {
                   data.lastUsed[owner] = name
                }
            }
            
            return data
        }      
    }
}