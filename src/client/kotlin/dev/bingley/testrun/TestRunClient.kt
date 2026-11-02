package dev.bingley.testrun

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import com.mojang.blaze3d.platform.InputConstants

object TestRunClient : ClientModInitializer {
    override fun onInitializeClient() {
        val category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("testrun", "controls")
        )
        
        val restartKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.testrun.again",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K,
                category
            )
        )
        
        val quickSaveKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.testrun.quicksave",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_F6,
                category
            )
        )
        
        val quickLoadKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.testrun.quickload",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_F7,
                category
            )
        )
        
        val finishAttemptKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.testrun.finishattempt",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_M,
                category
            )
        )
        
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (restartKey.consumeClick()) {
                client.connection?.sendCommand("testrun again")
            }
            
            while (quickSaveKey.consumeClick()) {
                client.connection?.sendCommand("testrun save quick")
            }
            
            while (quickLoadKey.consumeClick()) {
                client.connection?.sendCommand("testrun run quick")
            }
            
            while (finishAttemptKey.consumeClick()) {
                client.connection?.sendCommand("testrun finish")
            }
        }
    }
}