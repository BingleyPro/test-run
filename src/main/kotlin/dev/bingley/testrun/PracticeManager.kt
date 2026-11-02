package dev.bingley.testrun

import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object PracticeManager {
    private val attempts = mutableMapOf<Pair<UUID, String>, Int>()
    private val startTimes = mutableMapOf<UUID, Long>()
    
    fun start(player: ServerPlayer, name: String): Int {
        // Identify scenario
        val key = Pair(player.uuid, name)

        val previous = attempts[key] ?: 0
        val total = previous + 1
        attempts[key] = total

        startTimes[player.uuid] = System.nanoTime()
        
        return total
    }
    
    fun finish(player: ServerPlayer): Double? {
        val startTime = startTimes[player.uuid] ?: return null

        val endTime = System.nanoTime()
        val elapsed = endTime - startTime
        
        startTimes.remove(player.uuid)
        
        // Convert nanoseconds to seconds
        return elapsed / 1_000_000_000.0
    }
}