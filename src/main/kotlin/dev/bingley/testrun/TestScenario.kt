package dev.bingley.testrun

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.GameType
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

data class TestScenario(
    val name: String,
    val dimension: ResourceKey<Level>,
    
    val x: Double,
    val y: Double,
    val z: Double,
    
    val yaw: Float,
    val pitch: Float,
    
    
    val health: Float,
    val hunger: Int,
    val saturation: Float,
    
    val totalExperience: Int,
    val experienceLevel: Int,
    val experienceProgress: Float,
    
    val gamemode: GameType,
    
    val inventory: List<ItemStack>,
    val armour: List<ItemStack>,
    val offhand: ItemStack,
    val selectedSlot: Int,
    
    val tags: Set<String>,
    val team: String?,
    
    val extraNbt: CompoundTag,
    
    val resetFunction: String? = null
)