package dev.bingley.testrun

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import java.util.UUID

object ScenarioManager {
    private val scenarios = mutableMapOf<UUID, MutableMap<String, TestScenario>>()
    
    private val armourSlots = listOf(
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD
    )
    
    fun save(player: ServerPlayer, name: String) {
        
        val extraNbt = CompoundTag().apply {
            putInt("Air", player.airSupply)
            putInt("Fire", player.remainingFireTicks)
            putInt("FrozenTicks", player.ticksFrozen)
            putDouble("fallDistance", player.fallDistance)
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
        )
        
        // Save scenario
        scenarios.getOrPut(player.uuid) {
            mutableMapOf()
        }[name] = scenario
    }
    
    fun get(player: ServerPlayer, name: String): TestScenario? {
        return scenarios[player.uuid]?.get(name)
    }
    
    fun list(player: ServerPlayer): List<String> {
        return scenarios[player.uuid]?.keys?.sorted() ?: emptyList()
    }
    
    fun delete(player: ServerPlayer, name: String): Boolean {
        return scenarios[player.uuid]?.remove(name) != null
    }
    
    fun restore(player: ServerPlayer, name: String): Boolean {
        val scenario = get(player, name) ?: return false
        
        if (player.level().dimension() != scenario.dimension) {
            return false
        }
        
        return player.teleportTo(
            player.level(),
            scenario.x,
            scenario.y,
            scenario.z,
            emptySet(),
            scenario.yaw,
            scenario.pitch,
            true
        )
    }
}