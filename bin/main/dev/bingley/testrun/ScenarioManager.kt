package dev.bingley.testrun

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.clock.WorldClocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier

object ScenarioManager {
    private val armourSlots = listOf(
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD
    )
    
    fun save(player: ServerPlayer, name: String, part: String = "all"): Boolean {
        
        val previous = get(player, name)
        if (part != "all" && previous == null) {
            return false // Partial saves require an exisiting scenario
        }
        
        val extraNbt = CompoundTag().apply {
            putInt("Air", player.airSupply)
            putInt("Fire", player.remainingFireTicks)
            putInt("FrozenTicks", player.ticksFrozen)
            putDouble("FallDistance", player.fallDistance)
            putBoolean("NoGravity", player.isNoGravity)
            putBoolean("Sprinting", player.isSprinting)
            putBoolean("Sneaking", player.isShiftKeyDown)
            putDouble("VelocityX", player.deltaMovement.x)
            putDouble("VelocityY", player.deltaMovement.y)
            putDouble("VelocityZ", player.deltaMovement.z)
            
            val clock = player.level().registryAccess()
                .get(WorldClocks.OVERWORLD)
                .orElse(null)
                
            if (clock != null) {
                val time = player.level().clockManager()
                    .getInstance(clock)
                    .totalTicks()
                    
                putLong("WorldTime", time)
            }
            
            putBoolean("Raining", player.level().isRaining)
            putBoolean("Thundering", player.level().isThundering)
        }
        val current = TestScenario(
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
        
        val old = previous ?: current
        val updatedNbt = old.extraNbt.copy()
        
        if (part == "player" || part == "world") {
            val changes = current.extraNbt.copy()
            
            if (part == "player") {
                changes.remove("WorldTime")
                changes.remove("Raining")
                changes.remove("Thundering")
            } else {
                val playerKeys = listOf("Air", "Fire", "FrozenTicks", "FallDistance", "NoGravity", "Sprinting", "Sneaking", "VelocityX", "VelocityY", "VelocityZ")
                
                for (key in playerKeys) {
                    changes.remove(key)
                }
            }
            
            updatedNbt.merge(changes)
        }

        
        val updated = when (part) {
            "all" -> current
            
            "position" -> old.copy(
                dimension = current.dimension,
                x = current.x,
                y = current.y,
                z = current.z,
                yaw = current.yaw,
                pitch = current.pitch
            )
            
            "inventory" -> old.copy(
                inventory = current.inventory,
                armour = current.armour,
                offhand = current.offhand,
                selectedSlot = current.selectedSlot
            )
            
            "player" -> old.copy(
                health = current.health,
                hunger = current.hunger,
                saturation = current.saturation,
                totalExperience = current.totalExperience,
                experienceLevel = current.experienceLevel,
                experienceProgress = current.experienceProgress,
                gamemode = current.gamemode,
                tags = current.tags,
                team = current.team,
                extraNbt = updatedNbt
            )
            
            "world" -> old.copy(
                extraNbt = updatedNbt
            )
            
            else -> return false
        }
        
        val data = TestRunSavedData.forPlayer(player)
        
        if (part == "all") {
            data.save(player.uuid, updated)
        } else {
            data.update(player.uuid, updated)
        }
        
        return true
    }
    
    fun get(player: ServerPlayer, name: String): TestScenario? {
        return TestRunSavedData.forPlayer(player).get(
            player.uuid,
            name
        )
    }
    
    fun list(player: ServerPlayer): List<String> {
        return TestRunSavedData.forPlayer(player).names(
            player.uuid
        )
    }
    
    fun delete(player: ServerPlayer, name: String): Boolean {
        return TestRunSavedData.forPlayer(player).delete(
            player.uuid,
            name
        )
    }
    
    fun restore(player: ServerPlayer, name: String, part: String = "all"): Boolean {
        return restoreAdv(player, player, name, part)
    }
    
    fun restoreAdv(owner: ServerPlayer, player: ServerPlayer, name: String, part: String = "all"): Boolean {
        val scenario = get(owner, name) ?: return false
        
        val server = player.level().server
        
        val level = server.getLevel(scenario.dimension)
            ?: return false
            
        val resetFunction = scenario.resetFunction
            ?.let { Identifier.tryParse(it) }
            ?.let {server.functions.get(it).orElse(null)}
        
        if (part == "all" || part == "position") {
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
            
            if (!teleport) return false
        }
        
        if (part == "all" || part == "player") {
            player.setGameMode(scenario.gamemode)
            
            player.health = scenario.health
            player.foodData.foodLevel = scenario.hunger
            player.foodData.setSaturation(scenario.saturation)
            
            player.totalExperience = scenario.totalExperience
            player.experienceLevel = scenario.experienceLevel
            player.experienceProgress = scenario.experienceProgress
        }
        
        if (part == "all" || part == "inventory") {
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

            inventory.selectedSlot = scenario.selectedSlot
            
            inventory.setChanged()
            player.inventoryMenu.broadcastChanges()
        }
        
        val nbt = scenario.extraNbt
        
        if (part == "all" || part == "player") {
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
            val teamName = scenario.team
            if (teamName != null) {
                var team = scoreboard.getPlayerTeam(teamName)
                
                if (team == null) {
                    team = scoreboard.addPlayerTeam(teamName)
                }
                
                scoreboard.addPlayerToTeam(scoreboardName, team)
            }
            
            player.airSupply = nbt.getIntOr("Air", 300)
            player.remainingFireTicks = nbt.getIntOr("Fire", 0)
            player.ticksFrozen = nbt.getIntOr("FrozenTicks", 0)
            player.fallDistance = nbt.getDoubleOr("FallDistance", 0.0)
            player.isNoGravity = nbt.getBooleanOr("NoGravity", false)
            player.isSprinting = nbt.getBooleanOr("Sprinting", false)
            player.isShiftKeyDown = nbt.getBooleanOr("Sneaking", false)
            
            player.setDeltaMovement(
                nbt.getDoubleOr("VelocityX", 0.0),
                nbt.getDoubleOr("VelocityY", 0.0),
                nbt.getDoubleOr("VelocityZ", 0.0),
            )
        }
        
        if (part == "all" || part == "world") {
            if (nbt.contains("WorldTime")) {
                val clock = level.registryAccess()
                    .get(WorldClocks.OVERWORLD)
                    .orElse(null)
                    
                if (clock != null) {
                    level.clockManager().setTotalTicks(
                        clock,
                        nbt.getLongOr("WorldTime", 0L)
                    )
                }
            }
            
            if (nbt.contains("Raining")) {
                val raining = nbt.getBooleanOr("Raining", false)
                val thundering = nbt.getBooleanOr("Thundering", false)
                
                if (raining) {
                    server.setWeatherParameters(0, 12000, true, thundering)
                } else {
                    server.setWeatherParameters(12000, 0, false, false)
                }
            }
        }
        
        if (part == "all") {
            if (resetFunction != null) {
                server.functions.execute(
                    resetFunction,
                    player.createCommandSourceStack()
                )
            }
        }
        
        if (owner.uuid == player.uuid) {
            TestRunSavedData.forPlayer(player).remember(
                player.uuid,
                name
            )
        }
        
        val attempt = PracticeManager.start(player, name)
        
        return true
    }
    
    fun runAgain(player: ServerPlayer): Boolean {
        val name = TestRunSavedData.forPlayer(player).lastUsed(player.uuid) ?: return false
        
        return restore(player, name)
    }
    
    fun setResetFunction(player: ServerPlayer, name: String, functionName: String): Boolean {
        val scenario = get(player, name) ?: return false
        
        val id = Identifier.tryParse(functionName) ?: return false
        
        val server = player.level().server
        if (server.functions.get(id).isEmpty) {
            return false
        }
        
        val updated = scenario.copy(resetFunction = id.toString())
        
        TestRunSavedData.forPlayer(player).update(
            player.uuid,
            updated
        )
        
        return true
    }
    
    fun copy(player: ServerPlayer, old: String, new: String): Boolean {
        val original = get(player, old) ?: return false
        
        val copied = original.copy(name = new)
            
        TestRunSavedData.forPlayer(player).save(
            player.uuid,
            copied
        )
        
        return true
    }
    
    fun clearResetFunction(player: ServerPlayer, name: String): Boolean {
        val scenario = get(player, name) ?: return false
        
        if (scenario.resetFunction == null) {
            return false
        }
        
        val updated = scenario.copy(resetFunction = null)
        
        TestRunSavedData.forPlayer(player).update(player.uuid, updated)
        
        return true
    }
    
}