package dev.bingley.testrun

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier
import java.util.UUID

object ScenarioManager {
    private val scenarios = mutableMapOf<UUID, MutableMap<String, TestScenario>>()
    
    private val armourSlots = listOf(
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD
    )
    
    private val lastScenario = mutableMapOf<UUID, String>()
    
    fun save(player: ServerPlayer, name: String) {
        
        val extraNbt = CompoundTag().apply {
            putInt("Air", player.airSupply)
            putInt("Fire", player.remainingFireTicks)
            putInt("FrozenTicks", player.ticksFrozen)
            putDouble("FallDistance", player.fallDistance)
            putBoolean("NoGravity", player.isNoGravity)
        }
        val scenario = TestScenario(
            name = name,
            dimension = player.level().dimension(),
                
            x = player.x,
            y = player.y,
            z = player.z,
                
            yaw = player.yRot,
            pitch = player.xRot,
                
            health = player.health,
            hunger = player.foodData.foodLevel,
            saturation = player.foodData.saturationLevel,
            
            totalExperience = player.totalExperience,
            experienceLevel = player.experienceLevel,
            experienceProgress = player.experienceProgress,
                
            gamemode = player.gameMode.gameModeForPlayer,
            
            inventory = player.inventory
                .nonEquipmentItems
                .map { it.copy() },
            
            armour = armourSlots.map { slot ->
                player.getItemBySlot(slot).copy()
            },
                
            offhand = player.getItemBySlot(
                EquipmentSlot.OFFHAND
            ).copy(),
                
            selectedSlot = player.inventory.selectedSlot,
                
            tags = player.entityTags().toSet(),
            team = player.team?.name,
            extraNbt = extraNbt,
            
            resetFunction = get(player, name)?.resetFunction 
        )
        
        // Save scenario
        scenarios.getOrPut(player.uuid) {
            mutableMapOf()
        }[name] = scenario
        
        lastScenario[player.uuid] = name
    }
    
    fun get(player: ServerPlayer, name: String): TestScenario? {
        return scenarios[player.uuid]?.get(name)
    }
    
    fun list(player: ServerPlayer): List<String> {
        return scenarios[player.uuid]?.keys?.sorted() ?: emptyList()
    }
    
    fun delete(player: ServerPlayer, name: String): Boolean {
        val removed = scenarios[player.uuid]?.remove(name) != null
        
        if (removed && lastScenario[player.uuid] == name) {
            lastScenario.remove(player.uuid)
        }
        
        return removed
    }
    
    fun restore(player: ServerPlayer, name: String): Boolean {
        val scenario = get(player, name) ?: return false
        
        val server = player.level().server
        
        val level = server.getLevel(scenario.dimension)
            ?: return false
            
        val resetFunction = scenario.resetFunction?.let {
            functionName ->
            val id = Identifier.tryParse(functionName) ?: return false
            
            server.functions.get(id).orElse(null)
                ?: return false
        
        }
        
        val teleport = player.teleportTo(
            level,
            scenario.x,
            scenario.y,
            scenario.z,
            emptySet(),
            scenario.yaw,
            scenario.pitch,
            true
        )
        
        if (!teleport) return false // check if teleport is valid
        
        player.setGameMode(scenario.gamemode)
        
        player.health = scenario.health
        player.foodData.setFoodLevel(scenario.hunger)
        player.foodData.setSaturation(scenario.saturation)
        
        player.totalExperience = scenario.totalExperience
        player.experienceLevel = scenario.experienceLevel
        player.experienceProgress = scenario.experienceProgress
        
        val inventory = player.inventory
        
        for (i in inventory.nonEquipmentItems.indices) {
            inventory.setItem(
                i,
                    scenario.inventory.getOrElse(i) {
                        ItemStack.EMPTY
                    }.copy()
            )
        }
        
        for ((index, slot) in armourSlots.withIndex()) {
            player.setItemSlot(
                slot,
                scenario.armour[index].copy()
            )
        }
        
        player.setItemSlot(
            EquipmentSlot.OFFHAND,
            scenario.offhand.copy()
        )
        
        inventory.setSelectedSlot(scenario.selectedSlot)
        
        inventory.setChanged()
        player.inventoryMenu.broadcastChanges()
        
        val currentTags = player.entityTags().toSet()
        
        for (tag in currentTags) {
            player.removeTag(tag)
        }
        
        for (tag in scenario.tags) {
            player.addTag(tag)
        }
        
        val scoreboard = server.scoreboard
        val scoreboardName = player.scoreboardName
        
        scoreboard.removePlayerFromTeam(scoreboardName)
        
        // Create team if it doesn't exist, and assign player
        scenario.team?.let {
            teamName ->
            val team = scoreboard.getPlayerTeam(teamName)
                ?: scoreboard.addPlayerTeam(teamName)
        
            scoreboard.addPlayerToTeam(scoreboardName, team)
        }
        
        val nbt = scenario.extraNbt
        
        player.airSupply = nbt.getIntOr("Air", 300)
        player.remainingFireTicks = nbt.getIntOr("Fire", 0)
        player.ticksFrozen = nbt.getIntOr("FrozenTicks", 0)
        player.fallDistance = nbt.getDoubleOr("FallDistance", 0.0)
        
        player.setNoGravity(
            nbt.getBooleanOr("NoGravity", false)
        )
        
        resetFunction?.let { function ->
            server.functions.execute(
                function,
                player.createCommandSourceStack()
            )
        }
        
        lastScenario[player.uuid] = name
        return true
    }
    
    fun runAgain(player: ServerPlayer): Boolean {
        val name = lastScenario[player.uuid] ?: return false
        return restore(player, name)
    }
    
    fun setResetFunction(player: ServerPlayer, name: String, functionName: String): Boolean {
        val scenario = get(player, name) ?: return false
        
        val id = Identifier.tryParse(functionName) ?: return false
        
        val server = player.level().server
        if (server.functions.get(id).isEmpty()) {
            return false
        }
        
        val updated = scenario.copy(resetFunction = id.toString())
        
        scenarios[player.uuid]?.set(name, updated)
        
        return true
    }
}